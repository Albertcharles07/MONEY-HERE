package com.moneyhere;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContentController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.jwt-secret=local-test-secret-only-at-least-32-bytes")
class ContentAccessTest {
    @Autowired private MockMvc api;
    @MockitoBean private JdbcTemplate jdbc;
    @MockitoBean private AuthService auth;

    @Test
    void visitantePodeConsultarCatalogoMasNaoAsTrilhas() throws Exception {
        api.perform(get("/api/public/content")).andExpect(status().isOk());
        api.perform(get("/api/public/tracks")).andExpect(status().isUnauthorized());
        verifyNoInteractions(auth);
    }

    @Test
    void trilhasVerificamAContaAutenticada() throws Exception {
        api.perform(get("/api/public/tracks").with(jwt())).andExpect(status().isOk());
        verify(auth).me(any(Jwt.class));
    }

    @Test
    void senhaTemporariaNaoLiberaAsTrilhas() throws Exception {
        api.perform(get("/api/public/tracks").with(jwt().jwt(token -> token.claim("temporaryPassword", true))))
            .andExpect(status().isForbidden());
        verifyNoInteractions(auth, jdbc);
    }
}
