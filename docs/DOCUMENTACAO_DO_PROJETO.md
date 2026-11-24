# Documentação do projeto Money Here

O Money Here é um projeto acadêmico de educação financeira. Reúne artigos, trilhas de aprendizagem e simuladores com uma área pessoal para registrar receitas, despesas e metas. A interface roda em Android, iOS e web com React Native e Expo.

## Equipe

| Integrante | RM |
|---|---|
| Charles Elias de Araujo | 1143175185 |
| Carlos Daniel de Souza Gomes | 1143058270 |
| Lucas Rodrigues de Magalhaes | 1142712305 |
| João Pedro dias Araujo | 1143176420 |

João Pedro é responsável pela tela **Meus favoritos** e pelo armazenamento local da sessão e do tema na entrega. Os artigos salvos são consultados pela API. O AsyncStorage guarda no aparelho o token da sessão e a preferência de tema. [favoritos.tsx](../frontend/app/app/favoritos.tsx), [session.tsx](../frontend/lib/session.tsx)

## Funcionalidades

| Área | O que o usuário pode fazer |
|---|---|
| Conteúdo educativo | Ler artigos públicos, buscar por assunto e filtrar por categoria. |
| Trilhas e favoritos | Com uma conta, salvar artigos, abrir módulos e marcar conclusões. Cada conclusão vale 25 XP. |
| Metas | Cadastrar, consultar, editar e excluir as próprias metas financeiras. |
| Orçamento | Registrar receitas e despesas e consultar o saldo e os gastos por categoria, com filtro mensal opcional. |
| Simuladores | Calcular juros compostos, reserva de emergência, dívidas, aposentadoria, comparativo de investimentos e custo do crédito. |
| Conta | Criar conta, entrar, editar perfil, trocar senha e solicitar recuperação por e-mail quando o SMTP estiver configurado. |
| Administração | Com permissão de administrador, cadastrar e pesquisar usuários, alterar papéis, ativar ou desativar contas e definir senhas temporárias. |

## Arquitetura e código

O aplicativo Expo envia requisições HTTP para a API Spring Boot. O backend valida a sessão, aplica as regras de acesso e consulta o PostgreSQL hospedado no Neon. O Spring Data JPA persiste os dados das contas, e o Spring JDBC consulta artigos e trilhas. Os simuladores executam os cálculos no próprio aplicativo.

| Componente | Onde acontece no código |
|---|---|
| Expo SDK 54 | `"expo": "~54.0.0"` define a série do SDK e `"main": "expo-router/entry"` inicia a navegação. [package.json](../frontend/package.json) |
| Telas e navegação | O Expo Router monta as telas a partir dos arquivos em `frontend/app/`. O `_layout.tsx` envolve a pilha de navegação no `SessionProvider`. [Layout](../frontend/app/_layout.tsx) |
| Cliente HTTP | `api()` resolve a URL do backend, acrescenta `/api` e envia o token nas chamadas protegidas. [api.ts](../frontend/lib/api.ts) |
| Autenticação | O `AuthService` confere a senha com BCrypt e emite um JWT de 30 minutos. O Spring Security valida o token e controla o acesso. [AuthService.java](../backend/src/main/java/com/moneyhere/AuthService.java), [SecurityConfig.java](../backend/src/main/java/com/moneyhere/SecurityConfig.java) |
| Armazenamento local | O `AsyncStorage` guarda o token em `moneyhere_token` e o tema em `moneyhere_theme`. O logout remove o token local. [session.tsx](../frontend/lib/session.tsx) |
| Persistência | Entidades e repositórios representam dados de usuário, metas, orçamento, progresso e favoritos. O catálogo é consultado pelo `ContentController`. [Model.java](../backend/src/main/java/com/moneyhere/Model.java), [Repos.java](../backend/src/main/java/com/moneyhere/Repos.java), [ContentController.java](../backend/src/main/java/com/moneyhere/ContentController.java) |
| Conexão com o Neon | `start-neon.ps1` converte as URLs do PostgreSQL para JDBC e configura as conexões do Spring e do Flyway. [start-neon.ps1](../backend/start-neon.ps1), [application.properties](../backend/src/main/resources/application.properties) |
| Simuladores | As fórmulas usadas pela tela de ferramentas ficam em `calculators.cjs`. [ferramentas.tsx](../frontend/app/ferramentas.tsx), [calculators.cjs](../frontend/lib/calculators.cjs) |

No login, a tela `/login` chama `POST /api/auth/login`. O frontend guarda o JWT retornado e o inclui no cabeçalho `Authorization: Bearer <token>` nas próximas requisições protegidas. Quando o app reabre, ele recupera o token e consulta `GET /api/me` para carregar a conta. O login é gerenciado pelo Spring, com `auth: false` na configuração de serviços do Neon. [session.tsx](../frontend/lib/session.tsx), [neon.ts](../neon.ts)

## Banco de dados e migrações

O script carrega `DATABASE_URL` e `DATABASE_URL_UNPOOLED` do `.env.local` na raiz. A primeira variável usa o endpoint com pool para as consultas da aplicação. A segunda usa o endpoint direto do mesmo banco e branch para as migrações Flyway. O script configura JDBC com `sslmode=verify-full`. [.env.example](../.env.example), [start-neon.ps1](../backend/start-neon.ps1)

O esquema reúne contas (`app_users`), metas (`financial_goals`), lançamentos (`budget_entries`), favoritos (`favorites`), progresso (`learning_progress`) e catálogo (`educational_contents`, `educational_tracks` e `educational_track_modules`). O Flyway registra as migrações aplicadas em `flyway_schema_history`. [Migrações SQL](../backend/src/main/resources/db/migration)

## Rotas principais da API

A API local usa `http://localhost:8080` por padrão. As rotas de artigos e de autenticação são públicas. Trilhas, dados pessoais e progresso exigem JWT. A administração exige permissão de administrador.

| Método | Endpoint | Uso |
|---|---|---|
| GET | `/actuator/health` | Verificar a disponibilidade do backend. |
| POST | `/api/auth/register`, `/api/auth/login` | Criar conta e entrar. |
| POST | `/api/auth/forgot`, `/api/auth/reset` | Solicitar e concluir a redefinição de senha. |
| GET, PUT, DELETE | `/api/me` | Consultar, editar ou excluir a própria conta. |
| GET, POST | `/api/goals`, `/api/budget` | Listar e criar metas ou lançamentos. |
| GET, PUT, DELETE | `/api/goals/{id}`, `/api/budget/{id}` | Consultar, editar ou excluir um registro próprio. |
| GET | `/api/budget/summary` | Consultar o resumo financeiro. Aceita `?month=AAAA-MM`. |
| GET | `/api/public/content`, `/api/public/content/{key}`, `/api/public/tracks` | Consultar artigos e trilhas. |
| GET, POST | `/api/progress`, `/api/favorites` | Consultar e registrar progresso ou favoritos. |
| POST | `/api/progress/open` | Registrar a abertura de um módulo. |
| GET, POST | `/api/admin/users` | Listar e criar usuários comuns. |

A lista completa inclui exclusão de favoritos e as operações de administração por usuário. [Referência REST no README](../README.md#endpoints-rest)

## Execução local no Windows

Use Java 21, Maven 3.9 ou superior, Node.js 20.19.4 ou superior e npm. Configure no `.env.local` as duas URLs do Neon, usando o arquivo de exemplo como referência. Defina `JWT_SECRET` para manter as sessões válidas após reiniciar o backend. [.env.example](../.env.example), [configuração do backend](../README.md#executar-backend-powershell)

Na raiz do repositório, inicie o backend em um terminal PowerShell.

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\backend\start-neon.ps1
```

Em outro terminal, também a partir da raiz, inicie o frontend.

```powershell
Set-Location frontend
npm.cmd ci
npx.cmd expo start
```

Pressione `w` no terminal do Expo para abrir no navegador. Para abrir no celular, conecte os dois aparelhos à mesma rede e leia o QR Code com uma versão do Expo Go compatível com o SDK 54. Se precisar definir a API manualmente em `frontend/.env`, use `EXPO_PUBLIC_API_URL` com o endereço do backend alcançável pelo aparelho. [Instruções do Expo no README](../README.md#executar-frontend-powershell), [frontend/.env.example](../frontend/.env.example)

Para conferir se a API responde, consulte o health check. A segunda chamada verifica a leitura de um artigo do PostgreSQL.

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
Invoke-RestMethod http://localhost:8080/api/public/content/orcamento-50-30-20
```

## Testes e demonstração

Na raiz do repositório, execute as suítes do backend e do frontend.

```powershell
Push-Location backend
mvn.cmd clean verify
Pop-Location

Push-Location frontend
npm.cmd test
Pop-Location
```

A suíte Java cobre autenticação, regras de acesso, conteúdo e operações da API. A suíte JavaScript cobre cálculos financeiros. O teste de integração com o Neon usa as URLs de um ambiente de teste. [Testes do backend](../backend/src/test/java/com/moneyhere), [Testes do frontend](../frontend/tests/calculators.test.cjs), [Teste de integração no README](../README.md#testes)

Para demonstrar o fluxo, abra um artigo como visitante, entre com uma conta, cadastre uma meta e uma despesa e confira o resumo do orçamento. Em seguida, favorite um artigo, abra e conclua um módulo de trilha e confira o progresso no painel.
