package com.moneyhere;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@EnabledIfEnvironmentVariable(named = "MONEY_HERE_NEON_TEST", matches = "true")
@SpringBootTest(properties = {
    "app.mail-from=integration@example.invalid",
    "management.health.mail.enabled=false",
    "debug=false"
})
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@Transactional
class NeonPersistenceTest {
    @Autowired MockMvc api;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManager entities;
    @Autowired PasswordEncoder passwords;
    @Autowired Users users;
    @Autowired AuthService auth;
    @Autowired jakarta.validation.Validator validator;
    @MockitoBean JavaMailSender mail;

    private final String email = UUID.randomUUID() + "@example.invalid";
    private final String password = UUID.randomUUID().toString();

    @Test
    void registraAutenticaSemConfirmacaoELeMetaDoPostgres() throws Exception {
        api.perform(get("/actuator/health")).andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
        api.perform(get("/api/public/content/orcamento-50-30-20")).andExpect(status().isOk());
        api.perform(get("/api/public/tracks")).andExpect(status().isUnauthorized());
        api.perform(get("/api/goals")).andExpect(status().isUnauthorized());
        api.perform(get("/api/me")).andExpect(status().isUnauthorized());
        api.perform(post("/api/auth/register").contentType(APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("name", "Teste Neon", "email", email,
                    "password", password, "consent", true))))
            .andExpect(status().isOk());

        entities.flush();
        entities.clear();
        String stored = jdbc.queryForObject(
            "select password_hash from app_users where email = ?", String.class, email);
        assertTrue(passwords.matches(password, stored));

        api.perform(post("/api/auth/login").contentType(APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("email", email, "password", "wrong-password"))))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Credenciais inválidas"));
        verifyNoInteractions(mail);

        var login = api.perform(post("/api/auth/login").contentType(APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("email", email, "password", password))))
            .andExpect(status().isOk()).andReturn();
        String authorization = "Bearer " + json.readTree(login.getResponse().getContentAsString())
            .path("token").asText();
        api.perform(get("/api/me").header("Authorization", authorization))
            .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(email));
        api.perform(get("/api/public/tracks").header("Authorization", authorization))
            .andExpect(status().isOk()).andExpect(jsonPath("$").isArray());

        api.perform(get("/api/admin/users").header("Authorization", authorization))
            .andExpect(status().isForbidden());
        String module = "{\"key\":\"orcamento-50-30-20\"}";
        api.perform(post("/api/progress").header("Authorization", authorization)
            .contentType(APPLICATION_JSON).content(module)).andExpect(status().isConflict());
        api.perform(post("/api/progress/open").header("Authorization", authorization)
            .contentType(APPLICATION_JSON).content(module)).andExpect(status().isNoContent());
        api.perform(post("/api/progress").header("Authorization", authorization)
            .contentType(APPLICATION_JSON).content(module)).andExpect(status().isOk());
        api.perform(post("/api/progress").header("Authorization", authorization)
            .contentType(APPLICATION_JSON).content(module)).andExpect(status().isOk());
        api.perform(get("/api/progress").header("Authorization", authorization))
            .andExpect(jsonPath("$.opened[0]").value("orcamento-50-30-20"))
            .andExpect(jsonPath("$.completed[0]").value("orcamento-50-30-20"))
            .andExpect(jsonPath("$.xp").value(25));

        var goal = api.perform(post("/api/goals").header("Authorization", authorization)
                .contentType(APPLICATION_JSON)
                .content("""
                    {"title":"Meta Neon","targetAmount":100,"currentAmount":1}
                    """))
            .andExpect(status().isOk()).andReturn();
        long goalId = json.readTree(goal.getResponse().getContentAsString()).path("id").asLong();
        entities.flush();
        entities.clear();
        assertEquals("Meta Neon", jdbc.queryForObject(
            "select title from financial_goals where id = ?", String.class, goalId));
        var goals = api.perform(get("/api/goals").header("Authorization", authorization))
            .andExpect(status().isOk()).andReturn();
        assertEquals(goalId, json.readTree(goals.getResponse().getContentAsString())
            .path(0).path("id").asLong());
    }

    @Test
    void administraContasComJwtERevogaAcessoAoDesativar() throws Exception {
        var bootstrap = new AdminBootstrap(jdbc, users, auth, validator, passwords, email, password, false);
        bootstrap.run(null);
        bootstrap.run(null);
        String adminToken = loginAs(email);
        String member = UUID.randomUUID() + "@empresa.com.br";
        String payload = json.writeValueAsString(Map.of(
            "name", "Usuário de teste", "email", member, "password", password,
            "consent", true, "role", "ADMIN"));
        var created = api.perform(post("/api/admin/users").header("Authorization", adminToken)
            .contentType(APPLICATION_JSON).content(payload))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.role").value("USER")).andReturn();
        long id = json.readTree(created.getResponse().getContentAsString()).path("id").asLong();
        api.perform(get("/api/admin/users").header("Authorization", adminToken))
            .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].email").value(member));

        String memberToken = loginAs(member);
        api.perform(put("/api/admin/users/{id}/status", id).header("Authorization", adminToken)
            .contentType(APPLICATION_JSON).content("{\"enabled\":false}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.enabled").value(false));
        entities.flush();
        entities.clear();
        api.perform(get("/api/me").header("Authorization", memberToken))
            .andExpect(status().isUnauthorized());
        api.perform(post("/api/auth/login").contentType(APPLICATION_JSON)
            .content(json.writeValueAsString(Map.of("email", member, "password", password))))
            .andExpect(status().isForbidden()).andExpect(jsonPath("$.message").value("Conta desativada"));
        api.perform(put("/api/admin/users/{id}/status", id).header("Authorization", adminToken)
            .contentType(APPLICATION_JSON).content("{\"enabled\":true}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.enabled").value(true));
        String restored = loginAs(member);
        api.perform(get("/api/admin/users").param("search", member.toUpperCase(java.util.Locale.ROOT))
            .header("Authorization", adminToken)).andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.content[0].email").value(member));
        api.perform(put("/api/admin/users/{id}/role", id).header("Authorization", adminToken)
            .contentType(APPLICATION_JSON).content("{\"role\":\"ADMIN\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("ADMIN"));
        api.perform(get("/api/admin/users").header("Authorization", restored)).andExpect(status().isUnauthorized());
        String promoted = loginAs(member);
        api.perform(get("/api/admin/users").header("Authorization", promoted)).andExpect(status().isOk());
        api.perform(delete("/api/admin/users/{id}", id).header("Authorization", promoted)).andExpect(status().isForbidden());
        api.perform(put("/api/admin/users/{id}/role", id).header("Authorization", adminToken)
            .contentType(APPLICATION_JSON).content("{\"role\":\"USER\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("USER"));
        String beforeReset = loginAs(member);
        String temporary = UUID.randomUUID().toString();
        api.perform(put("/api/admin/users/{id}/password", id).header("Authorization", adminToken)
            .contentType(APPLICATION_JSON).content(json.writeValueAsString(Map.of("password", temporary))))
            .andExpect(status().isNoContent());
        api.perform(get("/api/me").header("Authorization", beforeReset)).andExpect(status().isUnauthorized());
        api.perform(post("/api/auth/login").contentType(APPLICATION_JSON)
            .content(json.writeValueAsString(Map.of("email", member, "password", temporary)))
            .with(request -> { request.setRemoteAddr("192.0.2.1"); return request; }))
            .andExpect(status().isOk()).andExpect(jsonPath("$.user.mustChangePassword").value(true));
        api.perform(delete("/api/admin/users/{id}", id).header("Authorization", adminToken)).andExpect(status().isNoContent());
        entities.flush();
        assertEquals(0, jdbc.queryForObject("select count(*) from app_users where id = ?", Integer.class, id));
    }

    @Test
    void trocaSenhaTemporariaAntesDeLiberarAdministracao() throws Exception {
        String initial = "admin@123";
        new AdminBootstrap(jdbc, users, auth, validator, passwords, email, initial, true).run(null);
        String loginBody = json.writeValueAsString(Map.of("email", email, "password", initial));
        api.perform(post("/api/auth/login").contentType(APPLICATION_JSON).content(loginBody)
            .with(request -> { request.setRemoteAddr("192.0.2.1"); return request; }))
            .andExpect(status().isForbidden());
        var local = api.perform(post("/api/auth/login").contentType(APPLICATION_JSON).content(loginBody))
            .andExpect(status().isOk()).andExpect(jsonPath("$.user.mustChangePassword").value(true)).andReturn();
        String temporaryToken = "Bearer " + json.readTree(local.getResponse().getContentAsString()).path("token").asText();
        api.perform(get("/api/admin/users").header("Authorization", temporaryToken)).andExpect(status().isForbidden());
        api.perform(put("/api/me/password").header("Authorization", temporaryToken).contentType(APPLICATION_JSON)
            .content(json.writeValueAsString(Map.of("currentPassword", initial, "newPassword", password))))
            .andExpect(status().isOk());
        entities.flush();
        entities.clear();
        api.perform(post("/api/auth/login").contentType(APPLICATION_JSON).content(loginBody))
            .andExpect(status().isUnauthorized());
        api.perform(get("/api/admin/users").header("Authorization", temporaryToken)).andExpect(status().isForbidden());
        api.perform(get("/api/admin/users").header("Authorization", loginAs(email))).andExpect(status().isOk());
    }

    private String loginAs(String loginEmail) throws Exception {
        var response = api.perform(post("/api/auth/login").contentType(APPLICATION_JSON)
            .content(json.writeValueAsString(Map.of("email", loginEmail, "password", password))))
            .andExpect(status().isOk()).andReturn();
        return "Bearer " + json.readTree(response.getResponse().getContentAsString()).path("token").asText();
    }

    @AfterTransaction
    void contaDeTesteFoiRevertida() {
        assertEquals(0, jdbc.queryForObject(
            "select count(*) from app_users where email = ?", Integer.class, email));
    }
}
