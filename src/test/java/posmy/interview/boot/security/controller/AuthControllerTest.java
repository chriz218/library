package posmy.interview.boot.security.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import posmy.interview.boot.appuser.entity.AppUser;
import posmy.interview.boot.appuser.repository.AppUserRepository;
import posmy.interview.boot.security.jwt.config.JwtConfig;
import posmy.interview.boot.security.jwt.entity.Jwt;
import posmy.interview.boot.security.jwt.repository.JwtRepository;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtRepository jwtRepository;

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private SecretKey secretKey;

    @Test
    @WithMockUser(username = "rescue", roles = {"LIBRARIAN"})
    void itShouldLogout() throws Exception {
        Map<String, String> authority = new HashMap<>();
        authority.put("authority", "ROLE_LIBRARIAN");
        List<Map<String, String>> listOfAuthorities = new ArrayList<>();
        listOfAuthorities.add(authority);
        String jwt = Jwts.builder()
                .setSubject("rescue")
                .claim("authorities", listOfAuthorities)
                .setIssuedAt(new Date())
                .setExpiration(java.sql.Date.valueOf(LocalDate.now().plusDays(this.jwtConfig.getTokenExpirationAfterDays())))
                .signWith(this.secretKey)
                .compact();
        this.jwtRepository.save(new Jwt(jwt, "rescue"));
        String token = "Bearer " + jwt;
        ResultActions resultActions = this.mockMvc.perform(post("/api/v1/auth/logout")
            .header("Authorization", token));
        resultActions.andExpect(status().isOk()).andExpect(jsonPath("$").value("Logged out successfully"));
    }

}