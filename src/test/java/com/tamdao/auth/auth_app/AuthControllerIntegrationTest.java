package com.tamdao.auth.auth_app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tamdao.auth.auth_app.dto.LoginRequest;
import com.tamdao.auth.auth_app.dto.UserDTO;
import com.tamdao.auth.auth_app.repository.UserRepository;
import com.tamdao.auth.auth_app.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.http.MediaType;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.notNullValue;
import jakarta.servlet.http.Cookie;

@SpringBootTest
@ActiveProfiles("dev")
public class AuthControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void testFullAuthenticationFlow() throws Exception {
        // 1. Register a new user
        UserDTO registerDTO = UserDTO.builder()
                .email("testuser@example.com")
                .password("Password123!")
                .name("Test User")
                .enable(true)
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));

        // 2. Login with registered user
        LoginRequest loginRequest = new LoginRequest("testuser@example.com", "Password123!");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(notNullValue()))
                .andExpect(jsonPath("$.refreshToken").value(notNullValue()))
                .andReturn();

        // Extract Refresh Cookie
        Cookie refreshCookie = loginResult.getResponse().getCookie("refresh_token");

        // 3. Refresh Token
        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                .cookie(refreshCookie)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(notNullValue()))
                .andExpect(jsonPath("$.refreshToken").value(notNullValue()))
                .andReturn();

        Cookie newRefreshCookie = refreshResult.getResponse().getCookie("refresh_token");

        // 4. Logout
        mockMvc.perform(post("/api/v1/auth/logout")
                .cookie(newRefreshCookie))
                .andExpect(status().isNoContent());

        // 5. Try to refresh again using the logged out/revoked token (should fail)
        mockMvc.perform(post("/api/v1/auth/refresh")
                .cookie(newRefreshCookie)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}
