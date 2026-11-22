package com.moneyhere;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.mail.javamail.JavaMailSender;
import java.util.*;
class AuthServiceTest {
 @Test void normalizaEmail(){AuthService service=new AuthService(mock(Users.class),mock(Tokens.class),new BCryptPasswordEncoder(),mock(JwtEncoder.class),mock(JavaMailSender.class),"","http://localhost");assertEquals("email@teste.com",service.normalize(" EMAIL@TESTE.COM "));}
 @Test void hashNaoRevelaToken(){AuthService service=new AuthService(mock(Users.class),mock(Tokens.class),new BCryptPasswordEncoder(),mock(JwtEncoder.class),mock(JavaMailSender.class),"","http://localhost");assertEquals(64,service.hash("token-secreto").length());assertNotEquals("token-secreto",service.hash("token-secreto"));}
 @Test void mensagemDeRecuperacaoNaoRevelaConta(){Users users=mock(Users.class);when(users.findByEmail("x@exemplo.com")).thenReturn(Optional.empty());AuthService service=new AuthService(users,mock(Tokens.class),new BCryptPasswordEncoder(),mock(JwtEncoder.class),mock(JavaMailSender.class),"","http://localhost");assertDoesNotThrow(()->service.forgot("x@exemplo.com"));}
}
