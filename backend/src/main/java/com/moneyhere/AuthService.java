package com.moneyhere;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
import java.security.*;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
@Service public class AuthService {
 private final Users users;private final Tokens tokens;private final PasswordEncoder enc;private final JwtEncoder jwt;
 private final JavaMailSender mail;private final String from;private final String publicUrl;
 AuthService(Users users,Tokens tokens,PasswordEncoder enc,JwtEncoder jwt,JavaMailSender mail,
  @Value("${app.mail-from}") String from,@Value("${app.public-url}") String publicUrl){
  this.users=users;this.tokens=tokens;this.enc=enc;this.jwt=jwt;this.mail=mail;this.from=from;this.publicUrl=publicUrl;
 }
 String normalize(String email){return email.trim().toLowerCase(Locale.ROOT);}
 public User me(String email){return users.findByEmail(normalize(email)).filter(u->u.enabled).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED));}
 public User me(Jwt token){
  User user=me(token.getSubject());
  Object uid=token.getClaim("uid");
  Object version=token.getClaim("sessionVersion");
  if(!(uid instanceof Number id) || id.longValue()!=user.id
    || (version instanceof Number v ? v.longValue() : 0)!=user.sessionVersion)
   throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Sessão expirada. Entre novamente.");
  return user;
 }
 public Map<String,Object> view(User user){return Map.ofEntries(Map.entry("mustChangePassword",user.mustChangePassword),Map.entry("id",user.id),
  Map.entry("name",user.name),Map.entry("email",user.email),Map.entry("verified",user.verified),
  Map.entry("goal",user.goal==null?"":user.goal),Map.entry("level",user.level==null?"":user.level),
  Map.entry("learningFormat",user.learningFormat==null?"":user.learningFormat),Map.entry("theme",user.theme),
  Map.entry("notifications",user.notifications),Map.entry("role",user.role.name()));}
 @Transactional public Map<String,Object> register(Dto.Register body){
  createAccount(body);
  return Map.of("message","Conta criada. Você já pode entrar.");
 }
 @Transactional public User createAccount(Dto.Register body){
  if(!body.consent())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Aceite os termos e a política de privacidade");
  String email=normalize(body.email());
  if(body.password().getBytes(StandardCharsets.UTF_8).length>72)
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"A senha deve ter no máximo 72 bytes");
  User user=new User();user.name=body.name().trim();user.email=email;user.passwordHash=enc.encode(body.password());
  try { return users.saveAndFlush(user); }
  catch (org.springframework.dao.DataIntegrityViolationException e) {
   if (e.getMostSpecificCause() instanceof java.sql.SQLException sql && "23505".equals(sql.getSQLState()))
    throw new ResponseStatusException(HttpStatus.CONFLICT,"E-mail já cadastrado");
   throw e;
  }
 }
 public Map<String,Object> login(Dto.Login body, boolean localRequest){
  User user=users.findByEmail(normalize(body.email())).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Credenciais inválidas"));
  if(!enc.matches(body.password(),user.passwordHash))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Credenciais inválidas");
  if(!user.enabled)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Conta desativada");
  if(isDefaultPassword(user,body.password()) && !localRequest)
   throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Troque a senha inicial no navegador do computador que executa o backend");
  Instant now=Instant.now();JwtClaimsSet claims=JwtClaimsSet.builder().issuer("money-here").issuedAt(now).expiresAt(now.plus(Duration.ofMinutes(30)))
   .subject(user.email).claim("uid",user.id).claim("sessionVersion",user.sessionVersion)
   .claim("temporaryPassword",user.mustChangePassword).build();
  String token=jwt.encode(JwtEncoderParameters.from(JwsHeader.with(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256).build(),claims)).getTokenValue();
  return Map.of("token",token,"user",view(user),"expiresIn",1800);
 }
 @Transactional public void sendToken(User user,String purpose,int minutes){
  byte[] data=new byte[32];new SecureRandom().nextBytes(data);String raw=HexFormat.of().formatHex(data);
  VerificationToken token=new VerificationToken();token.user=user;token.purpose=purpose;token.tokenHash=hash(raw);
  token.expiresAt=Instant.now().plus(Duration.ofMinutes(minutes));tokens.save(token);
  if(from.isBlank())return; // produção: configure SMTP para enviar; sem SMTP não simula entrega
  SimpleMailMessage message=new SimpleMailMessage();message.setFrom(from);message.setTo(user.email);
  boolean verify=purpose.equals("VERIFY");message.setSubject(verify?"Confirme seu e-mail - Money Here":"Redefinição de senha - Money Here");
  message.setText("Abra este link em até "+minutes+" minutos: "+publicUrl+(verify?"/verificar-email?token=":"/resetar-senha?token=")+raw);
  mail.send(message);
 }
 public String hash(String token){try{byte[] digest=MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));return HexFormat.of().formatHex(digest);}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
 @Transactional public void verify(String raw){VerificationToken token=valid(raw,"VERIFY");token.user.verified=true;token.used=true;}
 @Transactional public void reset(String raw,String password){
  VerificationToken token=valid(raw,"RESET");
  User user=users.findLockedByEmail(token.user.email).orElseThrow();
  if(user.role==AccountRole.ADMIN || !user.enabled)
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Token inválido ou expirado");
  valid(raw,"RESET").used=true;
  user.passwordHash=enc.encode(password); user.sessionVersion++; user.mustChangePassword=false;
 }
 private VerificationToken valid(String raw,String purpose){return tokens.findByTokenHashAndPurposeAndUsedFalse(hash(raw),purpose)
  .filter(t->t.expiresAt.isAfter(Instant.now())).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Token inválido ou expirado"));}
 @Transactional public void forgot(String email){
  if(from.isBlank())
   throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"A recuperação por e-mail está indisponível no momento.");
  users.findByEmail(normalize(email)).filter(u->u.enabled && u.role!=AccountRole.ADMIN)
   .ifPresent(user->sendToken(user,"RESET",30));
 }
 @Transactional public Map<String,Object> update(User user,Dto.Profile b){
  user=me(user.email);
  if(b.name()!=null&&!b.name().isBlank())user.name=b.name().trim();
  if(b.goal()!=null)user.goal=b.goal();if(b.level()!=null)user.level=b.level();
  if(b.learningFormat()!=null)user.learningFormat=b.learningFormat();
  if(b.theme()!=null && List.of("light","dark","system").contains(b.theme()))user.theme=b.theme();
  if(b.notifications()!=null)user.notifications=b.notifications();
  return view(user);
 }
 @Transactional public void changePassword(User user,Dto.Password b,boolean localRequest){
  User managed=users.findLockedByEmail(user.email).filter(u->u.enabled)
   .orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED));
  if(!enc.matches(b.currentPassword(),managed.passwordHash))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Senha atual incorreta");
  if(isDefaultPassword(managed,b.currentPassword()) && !localRequest)
   throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Troque a senha inicial no computador que executa o backend");
  if(b.newPassword().getBytes(StandardCharsets.UTF_8).length>72)
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"A senha deve ter no máximo 72 bytes");
  if((managed.mustChangePassword || managed.role==AccountRole.ADMIN)
    && (b.newPassword().length()<12 || b.newPassword().equals(b.currentPassword())))
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Escolha uma senha diferente com pelo menos 12 caracteres");
  managed.passwordHash=enc.encode(b.newPassword());
  managed.mustChangePassword=false;
 }
 private boolean isDefaultPassword(User user,String password){
  return user.mustChangePassword && "admin@123".equals(password);
 }
 @Transactional public void delete(User user){
  User account=users.findLockedById(user.id).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED));
  if(account.role==AccountRole.ADMIN)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"A conta de administração não pode ser excluída aqui");
  deleteAccount(account);
 }
 void deleteAccount(User user){
  if(user.primaryAdmin)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"O administrador principal só pode ser excluído diretamente no banco de dados");
  users.delete(user);
 }
}
