package com.moneyhere;

import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {
    private final Users users;
    private final AuthService auth;
    private final Tokens tokens;
    private final PasswordEncoder passwords;
    private final JdbcTemplate jdbc;

    AdminController(Users users, AuthService auth, Tokens tokens, PasswordEncoder passwords, JdbcTemplate jdbc) {
        this.users = users;
        this.auth = auth;
        this.tokens = tokens;
        this.passwords = passwords;
        this.jdbc = jdbc;
    }

    @GetMapping
    public Page<Account> list(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "") String search) {
        requireAdmin(jwt);
        if (page < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Página inválida");
        String term = search.strip();
        var request = PageRequest.of(page, 25, Sort.by("id").descending());
        return (term.isEmpty() ? users.findAll(request) :
            users.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(term, term, request)).map(Account::from);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Account create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody Dto.Register body) {
        requireAdmin(jwt);
        return Account.from(auth.createAccount(body));
    }

    @PutMapping("/{id}/status")
    @Transactional
    public Account status(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                          @Valid @RequestBody Dto.AccountStatus body) {
        User account = target(jwt, id);
        if (account.enabled != body.enabled()) {
            account.enabled = body.enabled();
            account.sessionVersion++;
        }
        return Account.from(account);
    }

    @PutMapping("/{id}/role")
    @Transactional
    public Account role(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                        @Valid @RequestBody Dto.RoleChange body) {
        User account = target(jwt, id);
        if (body.role() == AccountRole.ADMIN && (!account.enabled || account.mustChangePassword))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ative a conta e conclua a troca de senha antes de promover");
        if (account.role != body.role()) {
            account.role = body.role();
            account.sessionVersion++;
            tokens.deleteByUserIdAndPurpose(id, "RESET");
        }
        return Account.from(account);
    }

    @PutMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void password(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                         @Valid @RequestBody Dto.AdminPassword body) {
        User account = target(jwt, id);
        if (body.password().getBytes(StandardCharsets.UTF_8).length > 72)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha deve ter no máximo 72 bytes");
        account.passwordHash = passwords.encode(body.password());
        account.mustChangePassword = true;
        account.sessionVersion++;
        tokens.deleteByUserIdAndPurpose(id, "RESET");
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        auth.deleteAccount(target(jwt, id));
    }

    private User target(Jwt jwt, Long id) {
        // A ordem serializa alterações administrativas, inclusive rebaixamentos cruzados.
        jdbc.query("select pg_advisory_xact_lock(246193825)", rows -> {});
        User actor = requireAdmin(jwt);
        if (actor.id.equals(id))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Use seu perfil para administrar sua própria senha");
        User account = users.findLockedById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada"));
        if (account.primaryAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A conta do administrador principal é protegida");
        }
        return account;
    }

    private User requireAdmin(Jwt jwt) {
        User user = auth.me(jwt);
        if (user.role != AccountRole.ADMIN || user.mustChangePassword
                || Boolean.TRUE.equals(jwt.getClaim("temporaryPassword"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso restrito a administradores");
        }
        return user;
    }

    public record Account(Long id, String name, String email, AccountRole role, boolean enabled, boolean primaryAdmin) {
        static Account from(User user) {
            return new Account(user.id, user.name, user.email, user.role, user.enabled, user.primaryAdmin);
        }
    }
}
