package com.chatapp.synk.controller;

import com.chatapp.synk.dto.UsersDTO;
import com.chatapp.synk.security.JwtAuthFilter;
import com.chatapp.synk.repository.UsersRepository;
import com.chatapp.synk.service.UsersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.mockito.Mockito.*;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class)// disables Spring Security filters
})
@AutoConfigureMockMvc(addFilters = false) // disables Spring Security filters
@Import(UserControllerTest.TestConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsersService usersService; // This is now injected from TestConfig

    private UsersDTO sampleUser;

    @BeforeEach
    public void setup() {
        Mockito.reset(usersService); // Reset mock before each test
        sampleUser = new UsersDTO("8dc2c03d-b35a-4b9a-a212-b1d4a20dc56a_USER", "9999999999", "Abhinav", "https://example.com/pic.jpg", "Backend Dev");
    }


    @Test
    public void testGetUserById_found() throws Exception {
        when(usersService.getUserById("8dc2c03d-b35a-4b9a-a212-b1d4a20dc56a_USER")).thenReturn(Optional.of(sampleUser));

        mockMvc.perform(get("/api/users/8dc2c03d-b35a-4b9a-a212-b1d4a20dc56a_USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("8dc2c03d-b35a-4b9a-a212-b1d4a20dc56a_USER"));
    }

    @Test
    public void testGetUserById_notFound() throws Exception {
        when(usersService.getUserById("8dc2c03d-b35a-4b9a-a212-b1d4a20dc56a_USER")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value("404"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    public void testDeleteUser() throws Exception {
        doNothing().when(usersService).deleteUser("8dc2c03d-b35a-4b9a-a212-b1d4a20dc56a_USER");

        mockMvc.perform(delete("/api/users/8dc2c03d-b35a-4b9a-a212-b1d4a20dc56a_USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Users deleted successfully"));
    }

    @TestConfiguration
    static class TestConfig {

        @Bean(name = "userRepository")
        @Primary
        public UsersRepository userRepository() {
            return mock(UsersRepository.class);
        }

        @Bean(name = "usersService")
//giving name beacuse when test loads it picks actual bean to pick mock one giving name
        @Primary
        public UsersService userService() {
            return mock(UsersService.class);
        }

    }
}
