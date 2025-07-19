package com.chatapp.synk.controller;

import com.chatapp.synk.dto.AuthDTO;
import com.chatapp.synk.dto.UsersDTO;
import com.chatapp.synk.repository.UsersRepository;
import com.chatapp.synk.security.CustomUserDetailsService;
import com.chatapp.synk.service.UsersService;
import com.chatapp.synk.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.MediaType;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // disables Spring Security filters
@Import(AuthControllerTest.TestConfig.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthenticationManager authenticationManager;  // This is now injected from TestConfig

    @Autowired
    private JwtUtil jwtUtil;  // This is now injected from TestConfig
    @Autowired
    private UsersRepository usersRepository; // This is now injected from TestConfig
    @Autowired
    private CustomUserDetailsService userDetailsService;  // This is now injected from TestConfig

    @Autowired
    private UsersService usersService;  // This is now injected from TestConfig
    private UsersDTO sampleUser;
    @BeforeEach
    void setUp() {
        Mockito.reset(authenticationManager, jwtUtil, userDetailsService, usersService);
        sampleUser = new UsersDTO("8dc2c03d-b35a-4b9a-a212-b1d4a20dc56a_USER", "9999999999", "Abhinav", "https://example.com/pic.jpg", "Backend Dev");
    }

    @Test
    void testAuthenticate() throws Exception {
        String phoneNumber = "1234567890";
        AuthDTO request = new AuthDTO();
        request.setPhoneNumber(phoneNumber);

        UserDetails mockUserDetails = new User("testuser", "password", new ArrayList<>());

        when(userDetailsService.loadUserByUsername(phoneNumber)).thenReturn(mockUserDetails);
        when(jwtUtil.generateToken(mockUserDetails)).thenReturn("mockToken");

        mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\": \"" + phoneNumber + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("mockToken"));
    }

    @Test
    public void testCreateUser() throws Exception {
        when(usersService.createUser(any(UsersDTO.class))).thenReturn(sampleUser);

        String jsonInput = """
            {
              "phoneNumber": "9999999999",
              "name": "Abhinav",
              "profilePictureUrl": "https://example.com/pic.jpg",
              "about": "Backend Dev"
            }
            """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInput))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value("200"))
                .andExpect(jsonPath("$.message").value("Users created successfully"))
                .andExpect(jsonPath("$.data[0].name").value("Abhinav"));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean(name = "authenticationManager")
        @Primary
        public AuthenticationManager authenticationManager() {
            return mock(AuthenticationManager.class);
        }

        @Bean(name = "jwtUtil")
        @Primary
        public JwtUtil jwtUtil() {
            return mock(JwtUtil.class);
        }

        @Bean(name = "usersRepository")
        @Primary
        public UsersRepository userRepository() {
            return mock(UsersRepository.class);
        }

        @Bean(name = "userDetailsService")
        @Primary
        public CustomUserDetailsService userDetailsService() {
            return mock(CustomUserDetailsService.class);
        }

        @Bean(name = "usersService")
        @Primary
        public UsersService userService() {
            return mock(UsersService.class);
        }
    }
}
