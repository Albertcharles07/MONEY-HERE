package com.moneyhere;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/progress")
class ProgressController {
    private final AuthService auth;
    private final Progresses progress;
    private final JdbcTemplate jdbc;

    ProgressController(AuthService auth, Progresses progress, JdbcTemplate jdbc) {
        this.auth = auth;
        this.progress = progress;
        this.jdbc = jdbc;
    }

    @GetMapping
    Map<String, Object> list(@AuthenticationPrincipal Jwt jwt) {
        Long id = auth.me(jwt).id;
        List<String> completed = progress.findByUserId(id).stream().map(item -> item.moduleKey).toList();
        List<String> opened = jdbc.queryForList(
            "select module_key from learning_module_visits where user_id = ?", String.class, id);
        return Map.of("completed", completed, "opened", opened, "xp", completed.size() * 25);
    }

    @PostMapping("/open")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void open(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody Dto.Key body) {
        Long id = auth.me(jwt).id;
        if (!Boolean.TRUE.equals(jdbc.queryForObject("""
            select exists(select 1 from educational_track_modules where content_key = ?)
            """, Boolean.class, body.key()))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Módulo não encontrado");
        }
        jdbc.update("""
            insert into learning_module_visits (user_id, module_key) values (?, ?)
            on conflict (user_id, module_key) do nothing
            """, id, body.key());
    }

    @PostMapping
    Map<String, String> complete(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody Dto.Key body) {
        Long id = auth.me(jwt).id;
        int inserted = jdbc.update("""
            insert into learning_progress (user_id, module_key)
            select user_id, module_key from learning_module_visits where user_id = ? and module_key = ?
            on conflict (user_id, module_key) do nothing
            """, id, body.key());
        if (inserted == 0 && !progress.existsByUserIdAndModuleKey(id, body.key())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Abra Estudar módulo antes de marcar como concluído");
        }
        return Map.of("message", "Progresso salvo");
    }
}
