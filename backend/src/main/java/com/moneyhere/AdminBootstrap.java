package com.moneyhere;

import jakarta.validation.Validator;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class AdminBootstrap implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    private final Users users;
    private final AuthService auth;
    private final Validator validator;
    private final PasswordEncoder passwords;
    private final String email;
    private final String password;
    private final boolean temporary;

    AdminBootstrap(JdbcTemplate jdbc, Users users, AuthService auth, Validator validator,
                   PasswordEncoder passwords, @Value("${MONEY_HERE_ADMIN_EMAIL:}") String email,
                   @Value("${MONEY_HERE_ADMIN_PASSWORD:}") String password,
                   @Value("${MONEY_HERE_ADMIN_TEMPORARY:false}") boolean temporary) {
        this.jdbc = jdbc;
        this.users = users;
        this.auth = auth;
        this.validator = validator;
        this.passwords = passwords;
        this.email = email;
        this.password = password;
        this.temporary = temporary;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() && password.isEmpty()) return;
        String normalizedEmail = auth.normalize(email);
        var account = new Dto.Register("Administrador", normalizedEmail, password, true);
        if (!validator.validate(account).isEmpty() || (!temporary && password.length() < 12)
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("Configure e-mail válido e senha de administração de 12 a 72 bytes");
        }
        jdbc.update("""
            insert into app_users(name, email, password_hash, role, must_change_password)
            values (?, ?, ?, 'ADMIN', ?) on conflict (email) do nothing
            """, account.name(), normalizedEmail, passwords.encode(password), temporary);
        if (users.findByEmail(normalizedEmail).filter(user -> user.role == AccountRole.ADMIN).isEmpty()) {
            throw new IllegalStateException("O e-mail de administração já pertence a uma conta comum");
        }
    }
}
