# Onde o código do Money Here acontece

O aplicativo Expo chama a API Spring Boot por HTTP. O backend autentica o usuário e consulta o PostgreSQL no Neon. No aparelho, o app guarda o token da sessão e o tema.

Os trechos abaixo foram recortados e, em alguns pontos, reformatados para leitura. Os links levam aos arquivos de origem.

## Expo SDK 54 e ponto de entrada

O campo `main` inicia o Expo Router, que cria as rotas a partir dos arquivos em `frontend/app/`. Por exemplo, `frontend/app/login.tsx` vira `/login`. A dependência `expo` seleciona a série `54.0.x`. [frontend/package.json, linhas 5 e 14](frontend/package.json)

```jsonc
"main": "expo-router/entry",
"scripts": {
  "start": "expo start"
  // Outros scripts omitidos
},
"dependencies": {
  "expo": "~54.0.0",
  "expo-router": "~6.0.0"
  // Outras dependências omitidas
}
```

O layout monta a pilha de telas (`Stack`) dentro do `SessionProvider`, que disponibiliza a sessão para as telas. [frontend/app/_layout.tsx, linha 5](frontend/app/_layout.tsx)

```tsx
export default function Layout() {
  return <SessionProvider>
    <StatusBar style="auto" />
    <Stack screenOptions={{ headerShown: false }} />
  </SessionProvider>;
}
```

## Chamada HTTP do frontend para a API

`api.ts` lê `EXPO_PUBLIC_API_URL` e resolve o endereço do backend. A função `api()` acrescenta `/api` ao endereço e envia o JWT quando há um token para a chamada. Com `path` igual a `/me`, por exemplo, a chamada local vai para `http://localhost:8080/api/me`. [frontend/lib/api.ts, linhas 10 a 19](frontend/lib/api.ts)

```ts
const configured = process.env.EXPO_PUBLIC_API_URL?.trim();
// O código resolve API_URL a partir da configuração e do host do Expo.

// Dentro da função api():
const token = publicRoute ? null : await AsyncStorage.getItem('moneyhere_token');
// O fetch abaixo fica dentro do try que trata falhas de conexão.
res = await fetch(API_URL + '/api' + path, {
  ...options,
  headers: {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: 'Bearer ' + token } : {}),
    ...options.headers,
  },
});
```

## Rota de login e autenticação JWT

No Spring, `@RequestMapping("/api")` define o prefixo do controller. `@PostMapping("/auth/login")` completa a rota `POST /api/auth/login` e encaminha a requisição para `AuthService.login`. [ApiController.java, linhas 12 a 18](backend/src/main/java/com/moneyhere/ApiController.java)

```java
@RestController @RequestMapping("/api") public class ApiController {
 // Campos, construtor e outras rotas omitidos.
 @PostMapping("/auth/login") public Map<String,Object> login(@Valid @RequestBody Dto.Login b,
  jakarta.servlet.http.HttpServletRequest request){return auth.login(b,isLoopback(request));}
}
```

O serviço procura o usuário pelo e-mail, compara a senha com o hash e, após as verificações da conta, assina um JWT de 30 minutos. O Spring Security valida esse token quando ele volta nas requisições protegidas. [AuthService.java, linhas 55 a 66](backend/src/main/java/com/moneyhere/AuthService.java), [SecurityConfig.java, linhas 24 a 34](backend/src/main/java/com/moneyhere/SecurityConfig.java)

```java
// Dentro de AuthService.login():
if (!enc.matches(body.password(), user.passwordHash))
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
// Verificações adicionais omitidas.
Instant now = Instant.now();
JwtClaimsSet claims = JwtClaimsSet.builder()
    .issuer("money-here").issuedAt(now).expiresAt(now.plus(Duration.ofMinutes(30)))
    .subject(user.email).claim("uid", user.id).claim("sessionVersion", user.sessionVersion)
    .claim("temporaryPassword", user.mustChangePassword).build();
String token = jwt.encode(JwtEncoderParameters.from(
    JwsHeader.with(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256).build(),
    claims)).getTokenValue();
return Map.of("token", token, "user", view(user), "expiresIn", 1800);
```

## Armazenamento local com AsyncStorage

Após o login, `SessionProvider` salva o JWT em `moneyhere_token`. Ao abrir o app, ele recupera o token e consulta `/api/me` para carregar o usuário. O logout remove o token, e o tema fica na chave `moneyhere_theme`. [frontend/lib/session.tsx, linhas 10 a 13](frontend/lib/session.tsx)

```tsx
// Dentro de login():
const data = await api('/auth/login', {
  method: 'POST',
  body: JSON.stringify({ email, password }),
}, true);
await AsyncStorage.setItem('moneyhere_token', data.token);

// Dentro de logout():
await AsyncStorage.removeItem('moneyhere_token');

// Dentro de toggleTheme():
AsyncStorage.setItem('moneyhere_theme', next);
```

## Conexão JDBC com o PostgreSQL no Neon

O script lê as variáveis do `.env.local` e converte as URLs do Neon para JDBC com validação de certificado e hostname. `DATABASE_URL` fornece a conexão com pool usada pela aplicação. `DATABASE_URL_UNPOOLED` fornece a conexão direta usada pelo Flyway para aplicar as migrações. [backend/start-neon.ps1, linhas 97 a 144 e 186 a 191](backend/start-neon.ps1)

```powershell
$direct = Convert-NeonConnection $values['DATABASE_URL_UNPOOLED'] 'DATABASE_URL_UNPOOLED'
$application = Convert-NeonConnection $values['DATABASE_URL'] 'DATABASE_URL'

# Dentro da tabela de variáveis passadas ao backend:
SPRING_DATASOURCE_URL = $application.Url
SPRING_FLYWAY_URL = $direct.Url
```

O Spring usa as variáveis do script e o Flyway lê os arquivos SQL de `db/migration`. [application.properties, linhas 7 a 23](backend/src/main/resources/application.properties), [V1__init.sql](backend/src/main/resources/db/migration/V1__init.sql)

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.flyway.locations=classpath:db/migration
spring.flyway.url=${SPRING_FLYWAY_URL:${SPRING_DATASOURCE_URL}}
```

Um exemplo de consulta acontece em `GET /api/public/content`. O controller usa `JdbcTemplate` para buscar artigos da tabela `educational_contents`. [ContentController.java, linhas 14 e 24 a 31](backend/src/main/java/com/moneyhere/ContentController.java)

```java
// Dentro do método all():
String base = "select content_key as key, title, category, level, content_type as type, minutes, body, url from educational_contents";
if (category == null || category.isBlank()) {
  return jdbc.queryForList(base + " order by id");
}
return jdbc.queryForList(base + " where lower(category) = lower(?) order by id", category);
```

## Configuração dos serviços no Neon

O `neon.ts` deixa o Neon Auth desativado. O login fica no `AuthService` do Spring. [neon.ts, linhas 3 a 5](neon.ts)

```ts
export default defineConfig({
  auth: false,
  // Política de branches omitida.
});
```
