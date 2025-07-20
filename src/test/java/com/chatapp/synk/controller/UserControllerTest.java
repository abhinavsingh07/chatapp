package com.chatapp.synk.controller;

import com.chatapp.synk.dto.UserDTO;
import com.chatapp.synk.repository.UserRepository;
import com.chatapp.synk.security.JwtAuthFilter;
import com.chatapp.synk.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class)// disables Spring Security filters
})
@AutoConfigureMockMvc(addFilters = false) // disables Spring Security filters
@Import(UserControllerTest.TestConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService; // This is now injected from TestConfig

    private UserDTO sampleUser;

    @BeforeEach
    public void setup() {
        Mockito.reset(userService); // Reset mock before each test
        sampleUser = new UserDTO("8dc2c03d-b35a-4b9a-a212-b1d4a20dc56a_USER", "9999999999", "Abhinav", "https://example.com/pic.jpg", "Backend Dev");
    }


    @Test
    public void testGetUserById_found() throws Exception {
        when(userService.getUserById("8dc2c03d-b35a-4b9a-a212-b1d4a20dc56a_USER")).thenReturn(sampleUser);

        mockMvc.perform(get("/api/users/8dc2c03d-b35a-4b9a-a212-b1d4a20dc56a_USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("8dc2c03d-b35a-4b9a-a212-b1d4a20dc56a_USER"));
    }

    @Test
    public void testGetUserById_notFound() throws Exception {
        when(userService.getUserById("8dc2c03d-b35a-4b9a-a212-b1d4a20dc56a_USER")).thenReturn(null);

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value("404"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    public void testDeleteUser() throws Exception {
        doNothing().when(userService).deleteUser("8dc2c03d-b35a-4b9a-a212-b1d4a20dc56a_USER");

        mockMvc.perform(delete("/api/users/8dc2c03d-b35a-4b9a-a212-b1d4a20dc56a_USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully"));
    }

    @TestConfiguration
    static class TestConfig {

        @Bean(name = "userRepository")
        @Primary
        public UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        @Bean(name = "userService")
        //giving name beacuse when test loads it picks actual bean to pick mock one giving name
        @Primary
        public UserService userService() {
            return mock(UserService.class);
        }

    }
}
