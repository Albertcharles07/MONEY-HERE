package com.moneyhere;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
class AuthServiceTest {
 @Test void normalizaEmail(){AuthService service=new AuthService(mock(Users.class),mock(Tokens.class),new BCryptPasswordEncoder(),mock(JwtEncoder.class),mock(JavaMailSender.class),"","http://localhost");assertEquals("email@teste.com",service.normalize(" EMAIL@TESTE.COM "));}
 @Test void hashNaoRevelaToken(){AuthService service=new AuthService(mock(Users.class),mock(Tokens.class),new BCryptPasswordEncoder(),mock(JwtEncoder.class),mock(JavaMailSender.class),"","http://localhost");assertEquals(64,service.hash("token-secreto").length());assertNotEquals("token-secreto",service.hash("token-secreto"));}
 @Test void smtpDesativadoInformaIndisponibilidadeSemConsultarConta() {
  Users users = mock(Users.class);
  Tokens tokens = mock(Tokens.class);
  JavaMailSender mail = mock(JavaMailSender.class);
  AuthService service = new AuthService(users, tokens, new BCryptPasswordEncoder(), mock(JwtEncoder.class), mail, "", "http://localhost");
  var error = assertThrows(ResponseStatusException.class, () -> service.forgot("x@exemplo.com"));
  assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
  verifyNoInteractions(users, tokens, mail);
 }

 @Test void smtpConfiguradoNaoEnviaMensagemParaContaDesconhecida() {
  Users users = mock(Users.class);
  JavaMailSender mail = mock(JavaMailSender.class);
  AuthService service = new AuthService(users, mock(Tokens.class), new BCryptPasswordEncoder(), mock(JwtEncoder.class), mail, "envios@example.invalid", "http://localhost");
  assertDoesNotThrow(() -> service.forgot("x@exemplo.com"));
  verifyNoInteractions(mail);
 }

 @Test void smtpConfiguradoEnviaLinkEArmazenaHashParaContaAtiva() {
  Users users = mock(Users.class);
  Tokens tokens = mock(Tokens.class);
  JavaMailSender mail = mock(JavaMailSender.class);
  User user = new User();
  user.email = "x@exemplo.com";
  when(users.findByEmail(user.email)).thenReturn(Optional.of(user));
  AuthService service = new AuthService(users, tokens, new BCryptPasswordEncoder(), mock(JwtEncoder.class), mail, "envios@example.invalid", "http://localhost:8081");
  service.forgot(" X@EXEMPLO.COM ");
  var message = ArgumentCaptor.forClass(SimpleMailMessage.class);
  var verification = ArgumentCaptor.forClass(VerificationToken.class);
  verify(mail).send(message.capture());
  verify(tokens).save(verification.capture());
  assertArrayEquals(new String[]{user.email}, message.getValue().getTo());
  String linkPrefix = "http://localhost:8081/resetar-senha?token=";
  String body = message.getValue().getText();
  assertNotNull(body);
  int linkStart = body.indexOf(linkPrefix);
  assertTrue(linkStart >= 0);
  String token = body.substring(linkStart + linkPrefix.length());
  assertEquals(64, token.length());
  assertEquals(service.hash(token), verification.getValue().tokenHash);
 }
}
