package com.moneyhere;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;

/** Catálogo de artigos e trilhas persistido exclusivamente no PostgreSQL. */
@RestController
@RequestMapping("/api/public")
public class ContentController {
  private final JdbcTemplate jdbc;

  public ContentController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  @GetMapping("/content")
  public List<Map<String, Object>> all(@RequestParam(required = false) String category) {
    String base = "select content_key as key, title, category, level, content_type as type, minutes, body, url from educational_contents";
    if (category == null || category.isBlank()) {
      return jdbc.queryForList(base + " order by id");
    }
    return jdbc.queryForList(base + " where lower(category) = lower(?) order by id", category);
  }

  @GetMapping("/content/{key}")
  public Map<String, Object> one(@PathVariable String key) {
    List<Map<String, Object>> found = jdbc.queryForList(
      "select content_key as key, title, category, level, content_type as type, minutes, body, url " +
      "from educational_contents where content_key = ?", key);
    if (found.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conteúdo não encontrado");
    return found.get(0);
  }

  @GetMapping("/tracks")
  public List<Map<String, Object>> tracks() {
    List<Map<String, Object>> result = new java.util.ArrayList<>();
    for (Map<String, Object> track : jdbc.queryForList("select id, track_key as key, title from educational_tracks order by id")) {
      Long id = ((Number) track.get("id")).longValue();
      List<String> modules = jdbc.queryForList(
        "select content_key from educational_track_modules where track_id = ? order by position", String.class, id);
      result.add(Map.of("key", track.get("key"), "title", track.get("title"), "modules", modules));
    }
    return result;
  }
}
