package com.moneyhere;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
import java.math.*;
@RestController @RequestMapping("/api") public class ApiController {
 private final AuthService auth;private final Goals goals;private final Entries entries;private final Favorites favorites;
 ApiController(AuthService a,Goals g,Entries e,Favorites f){auth=a;goals=g;entries=e;favorites=f;}
 private User user(Jwt jwt){return auth.me(jwt);}
 @PostMapping("/auth/register") public Map<String,Object> register(@Valid @RequestBody Dto.Register b){return auth.register(b);}
 @PostMapping("/auth/login") public Map<String,Object> login(@Valid @RequestBody Dto.Login b,
  jakarta.servlet.http.HttpServletRequest request){return auth.login(b,isLoopback(request));}
 @PostMapping("/auth/verify") public Map<String,String> verify(@RequestBody Map<String,String> b){auth.verify(b.getOrDefault("token",""));return Map.of("message","E-mail verificado");}
 @PostMapping("/auth/forgot") public Map<String,String> forgot(@Valid @RequestBody Dto.Email b){auth.forgot(b.email());return Map.of("message","Se o e-mail existir, você receberá instruções de recuperação.");}
 @PostMapping("/auth/reset") public Map<String,String> reset(@Valid @RequestBody Dto.Reset b){auth.reset(b.token(),b.password());return Map.of("message","Senha redefinida");}
 @GetMapping("/me") public Map<String,Object> me(@AuthenticationPrincipal Jwt jwt){return auth.view(user(jwt));}
 @PutMapping("/me") public Map<String,Object> updateMe(@AuthenticationPrincipal Jwt jwt,@RequestBody Dto.Profile b){return auth.update(user(jwt),b);}
 @PutMapping("/me/password") public Map<String,String> password(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody Dto.Password b,
  jakarta.servlet.http.HttpServletRequest request){auth.changePassword(user(jwt),b,isLoopback(request));return Map.of("message","Senha atualizada");}
 private boolean isLoopback(jakarta.servlet.http.HttpServletRequest request){
  return List.of("127.0.0.1","::1","0:0:0:0:0:0:0:1").contains(request.getRemoteAddr());
 }
 @DeleteMapping("/me") public ResponseEntity<Void> deleteMe(@AuthenticationPrincipal Jwt jwt){auth.delete(user(jwt));return ResponseEntity.noContent().build();}
 private Map<String,Object> goal(Goal g){Map<String,Object> m=new LinkedHashMap<>();m.put("id",g.id);m.put("title",g.title);m.put("targetAmount",g.targetAmount);m.put("currentAmount",g.currentAmount);m.put("deadline",g.deadline);return m;}
 @GetMapping("/goals") public List<Map<String,Object>> goals(@AuthenticationPrincipal Jwt jwt){return goals.findByUserIdOrderByIdDesc(user(jwt).id).stream().map(this::goal).toList();}
 // Busca uma meta pelo id, restrita ao usuario autenticado (404 se nao existir ou for de outro usuario)
 @GetMapping("/goals/{id}") public Map<String,Object> getGoal(@AuthenticationPrincipal Jwt jwt,@PathVariable Long id){return goal(findGoal(jwt,id));}
 private Goal findGoal(Jwt jwt,Long id){return goals.findByIdAndUserId(id,user(jwt).id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 @PostMapping("/goals") public Map<String,Object> addGoal(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody Dto.GoalBody b){Goal g=new Goal();g.user=user(jwt);apply(g,b);return goal(goals.save(g));}
 @PutMapping("/goals/{id}") public Map<String,Object> editGoal(@AuthenticationPrincipal Jwt jwt,@PathVariable Long id,@Valid @RequestBody Dto.GoalBody b){Goal g=findGoal(jwt,id);apply(g,b);return goal(goals.save(g));}
 private void apply(Goal g,Dto.GoalBody b){g.title=b.title().trim();g.targetAmount=b.targetAmount();g.currentAmount=b.currentAmount();g.deadline=b.deadline();}
 @DeleteMapping("/goals/{id}") public ResponseEntity<Void> delGoal(@AuthenticationPrincipal Jwt jwt,@PathVariable Long id){Goal g=findGoal(jwt,id);goals.delete(g);return ResponseEntity.noContent().build();}
 private Map<String,Object> entry(Entry e){return Map.of("id",e.id,"title",e.title,"category",e.category,"kind",e.kind,"amount",e.amount,"entryDate",e.entryDate);}
 @GetMapping("/budget") public List<Map<String,Object>> budget(@AuthenticationPrincipal Jwt jwt){return entries.findByUserIdOrderByEntryDateDescIdDesc(user(jwt).id).stream().map(this::entry).toList();}
 // Busca um lancamento do orcamento pelo id, restrito ao usuario autenticado
 @GetMapping("/budget/{id}") public Map<String,Object> getEntry(@AuthenticationPrincipal Jwt jwt,@PathVariable Long id){return entry(findEntry(jwt,id));}
 private Entry findEntry(Jwt jwt,Long id){return entries.findByIdAndUserId(id,user(jwt).id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 @PostMapping("/budget") public Map<String,Object> addEntry(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody Dto.EntryBody b){Entry e=new Entry();e.user=user(jwt);apply(e,b);return entry(entries.save(e));}
 @PutMapping("/budget/{id}") public Map<String,Object> editEntry(@AuthenticationPrincipal Jwt jwt,@PathVariable Long id,@Valid @RequestBody Dto.EntryBody b){Entry e=findEntry(jwt,id);apply(e,b);return entry(entries.save(e));}
 private void apply(Entry e,Dto.EntryBody b){e.title=b.title().trim();e.category=b.category().trim();e.kind=b.kind();e.amount=b.amount();e.entryDate=b.entryDate();}
 @DeleteMapping("/budget/{id}") public ResponseEntity<Void> delEntry(@AuthenticationPrincipal Jwt jwt,@PathVariable Long id){Entry e=findEntry(jwt,id);entries.delete(e);return ResponseEntity.noContent().build();}
 @GetMapping("/favorites") public List<String> favorite(@AuthenticationPrincipal Jwt jwt){return favorites.findByUserId(user(jwt).id).stream().map(f->f.contentKey).toList();}
 @PostMapping("/favorites") public Map<String,String> addFav(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody Dto.Key b){User u=user(jwt);if(favorites.findByUserIdAndContentKey(u.id,b.key()).isEmpty()){Favorite f=new Favorite();f.user=u;f.contentKey=b.key();favorites.save(f);}return Map.of("message","Salvo");}
 @DeleteMapping("/favorites/{key}") public ResponseEntity<Void> rmFav(@AuthenticationPrincipal Jwt jwt,@PathVariable String key){favorites.findByUserIdAndContentKey(user(jwt).id,key).ifPresent(favorites::delete);return ResponseEntity.noContent().build();}
}
