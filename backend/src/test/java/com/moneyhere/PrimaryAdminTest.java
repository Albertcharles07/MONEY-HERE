package com.moneyhere;

import java.sql.SQLException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PrimaryAdminTest {
    private final Users users = mock(Users.class);
    private final Tokens tokens = mock(Tokens.class);
    private final PasswordEncoder passwords = mock(PasswordEncoder.class);
    private final AuthService auth = new AuthService(users, tokens, passwords, mock(JwtEncoder.class),
        mock(JavaMailSender.class), "", "http://localhost");
    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
    private final AdminController admin = new AdminController(users, auth, tokens, passwords, mock(JdbcTemplate.class), events);

    @Test
    void editarEmailNormalizaEnderecoERevogaSessaoETokensAntigos() {
        User member = account(3L, "anterior@example.invalid", AccountRole.USER);
        member.verified = true;
        when(users.findLockedById(member.id)).thenReturn(Optional.of(member));

        var result = admin.edit(adminToken(), member.id, new Dto.AccountEdit(" Carlos ", "Carlos@example.invalid"));

        assertEquals("Carlos", result.name());
        assertEquals("carlos@example.invalid", result.email());
        assertFalse(member.verified);
        assertEquals(1, member.sessionVersion);
        verify(users).saveAndFlush(member);
        verify(tokens).deleteByUserId(member.id);
        verify(events).publishEvent(new AccountNotice("anterior@example.invalid", "O nome e o e-mail da sua conta foram alterados."));
    }

    @Test
    void rotaPutEditaSoNomeEPreservaSessaoETokens() throws Exception {
        User member = account(3L, "carlos@example.invalid", AccountRole.USER);
        member.verified = true;
        when(users.findLockedById(member.id)).thenReturn(Optional.of(member));

        var mvc = MockMvcBuilders.standaloneSetup(admin)
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(adminToken()));
        try {
            mvc.perform(put("/api/admin/users/{id}", member.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"name":"Carlos","email":"carlos@example.invalid"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Carlos"));
        } finally {
            SecurityContextHolder.clearContext();
        }

        assertTrue(member.verified);
        assertEquals(0, member.sessionVersion);
        verifyNoInteractions(tokens);
        verify(events).publishEvent(new AccountNotice(member.email, "O nome da sua conta foi alterado."));
    }

    @Test
    void edicaoSemAlteracoesNaoGeraAviso() {
        User member = account(3L, "carlos@example.invalid", AccountRole.USER);
        member.name = "Carlos";
        when(users.findLockedById(member.id)).thenReturn(Optional.of(member));

        admin.edit(adminToken(), member.id, new Dto.AccountEdit(" Carlos ", "Carlos@example.invalid"));

        verifyNoInteractions(tokens, events);
    }

    @Test
    void redefinirSenhaGeraAvisoSemIncluirSenha() {
        User member = account(3L, "carlos@example.invalid", AccountRole.USER);
        when(users.findLockedById(member.id)).thenReturn(Optional.of(member));

        admin.password(adminToken(), member.id, new Dto.AdminPassword("senha-temporaria"));

        assertTrue(member.mustChangePassword);
        verify(events).publishEvent(new AccountNotice(member.email, "A senha da sua conta foi redefinida."));
    }

    @Test
    void editarComEmailDuplicadoRetornaConflito() {
        User member = account(3L, "anterior@example.invalid", AccountRole.USER);
        when(users.findLockedById(member.id)).thenReturn(Optional.of(member));
        when(users.saveAndFlush(member)).thenThrow(new DataIntegrityViolationException("E-mail duplicado",
            new SQLException("Restrição de unicidade", "23505")));

        var error = assertThrows(ResponseStatusException.class,
            () -> admin.edit(adminToken(), member.id, new Dto.AccountEdit("Carlos", "usado@example.invalid")));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertEquals("E-mail já cadastrado", error.getReason());
        verifyNoInteractions(tokens, events);
    }

    @Test
    void outroAdministradorNaoPodeEditarContaPrincipal() {
        User primary = primaryAdmin();

        var error = assertThrows(ResponseStatusException.class,
            () -> admin.edit(adminToken(), primary.id, new Dto.AccountEdit("Carlos", "outro@example.invalid")));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        assertEquals("principal@example.invalid", primary.email);
    }

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
