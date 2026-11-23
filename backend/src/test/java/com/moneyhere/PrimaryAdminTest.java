package com.moneyhere;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PrimaryAdminTest {
    private final Users users = mock(Users.class);
    private final Tokens tokens = mock(Tokens.class);
    private final PasswordEncoder passwords = mock(PasswordEncoder.class);
    private final AuthService auth = new AuthService(users, tokens, passwords, mock(JwtEncoder.class),
        mock(JavaMailSender.class), "", "http://localhost");
    private final AdminController admin = new AdminController(users, auth, tokens, passwords, mock(JdbcTemplate.class));

    @Test
    void outroAdministradorNaoPodeExcluirContaPrincipal() {
        User primary = primaryAdmin();

        var error = assertThrows(ResponseStatusException.class, () -> admin.delete(adminToken(), primary.id));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verify(users, never()).delete(primary);
    }

    @Test
    void outroAdministradorNaoPodeRebaixarContaPrincipal() {
        User primary = primaryAdmin();

        var error = assertThrows(ResponseStatusException.class,
            () -> admin.role(adminToken(), primary.id, new Dto.RoleChange(AccountRole.USER)));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        assertEquals(AccountRole.ADMIN, primary.role);
    }

    @Test
    void outroAdministradorNaoPodeAlterarStatusDaContaPrincipal() {
        User primary = primaryAdmin();

        var error = assertThrows(ResponseStatusException.class,
            () -> admin.status(adminToken(), primary.id, new Dto.AccountStatus(false)));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        assertTrue(primary.enabled);
        assertEquals(0, primary.sessionVersion);
    }

    @Test
    void outroAdministradorNaoPodeRedefinirSenhaDaContaPrincipal() {
        User primary = primaryAdmin();

        var error = assertThrows(ResponseStatusException.class,
            () -> admin.password(adminToken(), primary.id, new Dto.AdminPassword("senha-temporaria")));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        assertFalse(primary.mustChangePassword);
        assertEquals(0, primary.sessionVersion);
        verifyNoInteractions(passwords, tokens);
    }

    @Test
    void contaPrincipalNaoPodeSerExcluidaPeloProprioPerfil() {
        User primary = primaryAdmin();

        var error = assertThrows(ResponseStatusException.class, () -> auth.delete(primary));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verify(users, never()).delete(primary);
    }

    @Test
    void outroAdministradorAindaPodeExcluirAdministradorSecundario() {
        User secondary = account(3L, "secundario@example.invalid", AccountRole.ADMIN);
        when(users.findLockedById(secondary.id)).thenReturn(Optional.of(secondary));

        admin.delete(adminToken(), secondary.id);

        verify(users).delete(secondary);
    }

    @Test
    void outroAdministradorPodeDesativarEReativarAdministradorSecundario() {
        User secondary = account(3L, "secundario@example.invalid", AccountRole.ADMIN);
        when(users.findLockedById(secondary.id)).thenReturn(Optional.of(secondary));
        Jwt token = adminToken();

        assertFalse(admin.status(token, secondary.id, new Dto.AccountStatus(false)).enabled());
        assertTrue(admin.status(token, secondary.id, new Dto.AccountStatus(true)).enabled());

        assertEquals(AccountRole.ADMIN, secondary.role);
        assertEquals(2, secondary.sessionVersion);
    }

    private User primaryAdmin() {
        User primary = account(1L, "principal@example.invalid", AccountRole.ADMIN);
        primary.primaryAdmin = true;
        when(users.findLockedById(primary.id)).thenReturn(Optional.of(primary));
        return primary;
    }

    private Jwt adminToken() {
        User actor = account(2L, "admin@example.invalid", AccountRole.ADMIN);
        when(users.findByEmail(actor.email)).thenReturn(Optional.of(actor));
        return Jwt.withTokenValue("admin-token").header("alg", "HS256")
            .subject(actor.email).claim("uid", actor.id).build();
    }

    private User account(long id, String email, AccountRole role) {
        User user = new User();
        user.id = id;
        user.email = email;
        user.role = role;
        return user;
    }
}
