package com.chatapp.synk.controller;

import com.chatapp.synk.dto.UserDTO;
import com.chatapp.synk.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private UserDTO sampleUser;

    @BeforeEach
    public void setup() {
        sampleUser = new UserDTO("8dc2c03d-b35a-4b9a-a212-b1d4a20dc56a_USER", "9999999999", "Abhinav", "https://example.com/pic.jpg", "Backend Dev");
    }

    @Test
    public void testCreateUser() throws Exception {
        when(userService.createUser(any(UserDTO.class))).thenReturn(sampleUser);

        String jsonInput = """
            {
              "phoneNumber": "9999999999",
              "name": "Abhinav",
              "profilePictureUrl": "https://example.com/pic.jpg",
              "about": "Backend Dev"
            }
            """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInput))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value("200"))
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.data[0].name").value("Abhinav"));
    }

    @Test
    public void testGetUserById_found() throws Exception {
        when(userService.getUserById("8dc2c03d-b35a-4b9a-a212-b1d4a20dc56a_USER")).thenReturn(Optional.of(sampleUser));

        mockMvc.perform(get("/api/users/8dc2c03d-b35a-4b9a-a212-b1d4a20dc56a_USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("8dc2c03d-b35a-4b9a-a212-b1d4a20dc56a_USER"));
    }

    @Test
    public void testGetUserById_notFound() throws Exception {
        when(userService.getUserById("8dc2c03d-b35a-4b9a-a212-b1d4a20dc56a_USER")).thenReturn(Optional.empty());

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
}
