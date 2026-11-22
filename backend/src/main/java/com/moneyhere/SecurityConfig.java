package com.moneyhere;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;
@Configuration public class SecurityConfig {
 @Bean PasswordEncoder encoder(){return new BCryptPasswordEncoder();}
 @Bean JwtEncoder jwtEncoder(@Value("${app.jwt-secret}") String secret){validate(secret);return new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256")));}
 @Bean JwtDecoder jwtDecoder(@Value("${app.jwt-secret}") String secret){validate(secret);return NimbusJwtDecoder.withSecretKey(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256")).macAlgorithm(MacAlgorithm.HS256).build();}
 private void validate(String secret){if(secret.getBytes(StandardCharsets.UTF_8).length<32)throw new IllegalArgumentException("JWT_SECRET precisa ter pelo menos 32 bytes");}
 @Bean SecurityFilterChain chain(HttpSecurity http) throws Exception {
  return http.csrf(c->c.disable()).cors(c->{}).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(a->a.requestMatchers("/api/auth/**","/api/public/**","/actuator/health").permitAll()
   .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
   .requestMatchers(HttpMethod.GET,"/api/me").authenticated()
   .requestMatchers(HttpMethod.PUT,"/api/me/password").authenticated()
   .anyRequest().access((authentication, context) ->
    new org.springframework.security.authorization.AuthorizationDecision(
     authentication.get() instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken jwt
     && jwt.isAuthenticated() && !Boolean.TRUE.equals(jwt.getToken().getClaim("temporaryPassword")))))
   .oauth2ResourceServer(o->o.jwt(j->{})).build();
 }
 @Bean UrlBasedCorsConfigurationSource corsConfigurationSource(@Value("${app.frontend-origins}") String origins,
  @Value("${MONEY_HERE_LOCAL_LAN:false}") boolean localLan) throws java.net.SocketException {
  List<String> allowed=new ArrayList<>(Arrays.stream(origins.split(",")).map(String::trim).toList());
  if(localLan) {
   java.net.NetworkInterface.networkInterfaces().flatMap(java.net.NetworkInterface::inetAddresses)
    .filter(ip->ip instanceof java.net.Inet4Address && ip.isSiteLocalAddress())
    .map(java.net.InetAddress::getHostAddress)
    .forEach(ip->{allowed.add("http://"+ip+":8081");allowed.add("http://"+ip+":19006");});
  }
  CorsConfiguration conf=new CorsConfiguration();conf.setAllowedOrigins(allowed);
  conf.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));conf.setAllowedHeaders(List.of("Authorization","Content-Type"));
  conf.setMaxAge(3600L); UrlBasedCorsConfigurationSource source=new UrlBasedCorsConfigurationSource();source.registerCorsConfiguration("/**",conf);return source;
 }
}
