# MONEY HERE — Educação financeira Full Stack

Projeto acadêmico com **React Native / Expo SDK 54 (Android, iOS e web)**, **Spring Boot 3.5 / Java 21**, **JWT**, **PostgreSQL relacional em nuvem** como banco único, inclusive para conteúdo editorial.

O React Native/Expo chama a API Spring Boot por HTTP. A API usa Spring Data JPA para usuários, perfil, metas, orçamento, progresso e favoritos, e Spring JDBC para artigos e trilhas. O Neon PostgreSQL armazena esses dados. Os simuladores calculam os resultados no aplicativo.

## Requisitos no Windows

- Java 21 e Maven 3.9+ disponíveis no PATH.
- Node.js 20.19.4 ou superior e npm. Para celular, Expo Go compatível com SDK 54 e acesso ao computador pela rede local.
- Um banco Neon acessível pela rede. O backend e o frontend rodam diretamente no Windows, sem Docker ou banco local.

## Executar backend (PowerShell)

Use o arquivo existente `.env.local` na raiz, com `DATABASE_URL` (endpoint pooled) e `DATABASE_URL_UNPOOLED` (endpoint direto) do mesmo banco e branch. Para usar outra branch, passe seu arquivo com `-EnvFile`. Mantenha esses arquivos fora do Git.

A partir da raiz:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\backend\start-neon.ps1
```

O script converte as URLs do Neon para JDBC com TLS e validação de certificado e hostname (`sslmode=verify-full`). Define `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD` para o HikariCP, limitado a cinco conexões por instância. Define `SPRING_FLYWAY_URL`, `SPRING_FLYWAY_USER` e `SPRING_FLYWAY_PASSWORD` para a conexão direta do Flyway. Usuário e senha são enviados separadamente da URL. Para iniciar o Spring diretamente, configure essas seis variáveis no ambiente do processo.

Defina `JWT_SECRET` no ambiente ou no `.env.local` para manter os tokens válidos após reiniciar. Use uma chave aleatória de pelo menos 32 bytes. Sem essa variável, o script gera uma chave temporária por execução. A API armazena senhas com BCrypt.

O Flyway aplica `V1__init.sql` e `V2__educational_content_postgresql.sql` em esquema vazio. Com histórico Flyway válido, aplica somente versões pendentes. Para um esquema pré-existente sem histórico, confira tabelas e dados antes de estabelecer um baseline. O Hibernate valida os mapeamentos e o comando Flyway clean fica desabilitado.

Em outro terminal:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
Invoke-RestMethod http://localhost:8080/api/public/content/orcamento-50-30-20
Invoke-RestMethod http://localhost:8080/api/public/tracks
```

O primeiro comando deve retornar `UP` e os demais consultam o catálogo no Neon. Confira o histórico de migrações em `flyway_schema_history` na mesma branch. Para incluir o SMTP no health check, defina `MAIL_HEALTH_ENABLED=true` com um servidor de e-mail configurado.

### Criar a primeira conta de administrador

Para iniciar o backend e abrir o site com um único comando, execute na raiz. O script usa o Expo da pasta frontend ou reaproveita a instância que já estiver rodando. Na primeira execução, instale as dependências do frontend com npm.cmd install.

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\backend\start-neon.ps1 -DefaultAdmin
```

Abra o site em `http://localhost:8081` no computador que roda o backend. Entre como `admin@moneyhere.local` com a senha temporária `admin@123`, troque-a por uma nova de pelo menos 12 caracteres e entre novamente. O painel exige a nova sessão. O primeiro acesso com a senha padrão é aceito apenas no computador do backend. A opção preserva uma senha já alterada. Nas execuções seguintes, use o comando normal.

Para escolher suas próprias credenciais desde o início:


Em um PowerShell na raiz do projeto, escolha um e-mail válido que ainda não tenha sido cadastrado e uma senha exclusiva de pelo menos 12 caracteres. A senha é pedida sem aparecer na tela.

```powershell
$env:MONEY_HERE_ADMIN_EMAIL = Read-Host 'E-mail novo do administrador'
$securePassword = Read-Host 'Senha do administrador' -AsSecureString
$env:MONEY_HERE_ADMIN_PASSWORD = [System.Net.NetworkCredential]::new('', $securePassword).Password
try {
    powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\backend\start-neon.ps1
} finally {
    Remove-Item Env:MONEY_HERE_ADMIN_EMAIL, Env:MONEY_HERE_ADMIN_PASSWORD
    $securePassword.Dispose()
}
```

Ao subir, o backend cria a conta com papel `ADMIN` e senha BCrypt. Em reinicializações seguintes, use o comando normal. Se o mesmo admin já existir, a configuração preserva a senha cadastrada. Uma conta comum que já existe precisa continuar separada da conta de administração.

Entre com a conta criada e abra **Administração** no menu ou a rota `/admin`. O painel lista contas e permite pesquisar pelo nome ou e-mail. No menu **Opções**, o administrador pode ativar ou desativar contas comuns e administradores secundários, definir uma senha temporária, apagar contas e alterar o papel entre usuário e administrador. O administrador principal tem uma conta protegida. As rotas administrativas bloqueiam qualquer alteração nessa conta, e o painel oculta o menu **Opções** dela. A pessoa que recebe uma senha temporária deve trocá-la ao entrar. Alterações de acesso e senha encerram a sessão existente da conta. Uma conta desativada perde acesso aos endpoints privados e volta a poder entrar quando reativada. A conta administrativa permite troca de senha pela área autenticada.

### Recuperação de senha por e-mail

O cadastro e o login exigem somente um e-mail em formato válido e uma senha. Para enviar o link de recuperação de senha de contas comuns, configure SMTP. O link expira em 30 minutos.

```powershell
$env:MAIL_HOST="smtp.seuprovedor.com"
$env:MAIL_PORT="587"
$env:MAIL_USERNAME="usuario"
$env:MAIL_PASSWORD="senha-de-aplicativo"
$env:MAIL_FROM="contato@seudominio.com"
$env:MAIL_AUTH="true"
$env:MAIL_TLS="true"
$env:PUBLIC_URL="http://localhost:8081"
```

SMTP é necessário somente para a recuperação de senha por e-mail. Contas administrativas não usam esse fluxo.

## Executar frontend (PowerShell)

```powershell
cd frontend
npm install
"EXPO_PUBLIC_API_URL=http://localhost:8080" | Set-Content -Encoding ascii .env
npx expo start --clear
```

No terminal Expo, pressione `w` para abrir web ou leia QR Code no Expo Go SDK 54. Você pode usar `npx expo start --web` para navegador. Use `ipconfig` para descobrir o IPv4 do computador e substitua `localhost` por ele em `frontend/.env` para testar no celular; reinicie o Expo após alterar variáveis. O frontend recebe somente o endereço público da API. Para web local, use `http://localhost:8080` como API. Libere a porta 8080 no firewall local quando permitido; em produção use HTTPS e hospedagem real.

**Nota:** o Expo Go instalado no celular precisa suportar SDK 54; versões posteriores podem não ser compatíveis. Verifique dependências com `npx expo install --check` após `npm install`.

### Celular na rede local

Conecte o computador e o celular ao mesmo Wi-Fi. Inicie o backend com o script e rode `npx.cmd expo start --lan --clear` em `frontend`. O aplicativo usa o endereço LAN mostrado pelo Expo para localizar a API na porta 8080. Para testar no navegador do celular, abra `http://IP-DO-COMPUTADOR:8080/actuator/health`. O retorno esperado é `{"status":"UP"}`. Se o navegador não alcançar a API, verifique se a rede Wi-Fi permite comunicação entre aparelhos e se o Windows libera a porta TCP 8080 na rede privada. Em túnel ou com uma API em outro servidor, configure `EXPO_PUBLIC_API_URL` com um endereço acessível ao celular.

## Estrutura

- `frontend/app`: roteamento React Native + web; início, cadastro, login, artigos, trilhas, ferramentas, painel, metas, orçamento, favoritos, configurações, páginas legais.
- `frontend/lib`: API, sessão com AsyncStorage, tema persistido, conteúdos locais e cálculos financeiros.
- `backend/src/main/java/com/moneyhere`: API, JPA, segurança Spring JWT, tokens de e-mail e catálogo editorial no PostgreSQL.
- `backend/src/main/resources/db/migration`: SQL de criação de tabelas no PostgreSQL.
- `frontend/tests` e `backend/src/test`: testes automatizados.

## Endpoints REST

| Método | Rota | Uso |
|---|---|---|
| POST | `/api/auth/register` | Cadastro com acesso imediato |
| POST | `/api/auth/verify` | Aceita links antigos ainda válidos |
| POST | `/api/auth/login` | Login e JWT de 30 minutos |
| POST | `/api/auth/forgot` | Recuperação por e-mail |
| POST | `/api/auth/reset` | Nova senha por token |
| GET/PUT/DELETE | `/api/me` | Dados, edição e exclusão de conta |
| PUT | `/api/me/password` | Trocar senha |
| GET/POST/PUT/DELETE | `/api/goals[/{id}]` | CRUD de metas |
| GET/POST/PUT/DELETE | `/api/budget[/{id}]` | CRUD de orçamento |
| GET/POST | `/api/progress` | Lista aberturas e conclusões. Exige abertura antes de concluir. |
| POST | `/api/progress/open` | Registra abertura de um módulo |
| GET/POST/DELETE | `/api/favorites[/{key}]` | Favoritos |
| GET | `/api/public/content` | Artigos cadastrados no PostgreSQL |
| GET | `/api/public/content/{key}` | Artigo individual |
| GET | `/api/public/tracks` | Trilhas |
| GET/POST | `/api/admin/users` | Lista contas e cria usuários comuns |
| PUT | `/api/admin/users/{id}/status` | Desativa ou reativa uma conta, inclusive de administrador secundário |
| PUT | `/api/admin/users/{id}/role` | Altera o papel da conta |
| PUT | `/api/admin/users/{id}/password` | Define senha temporária |
| DELETE | `/api/admin/users/{id}` | Apaga a conta e os dados associados |

As rotas privadas validam JWT e filtram dados pelo usuário autenticado. Exemplo de login: `POST /api/auth/login` com JSON `{"email":"...","password":"..."}`. O token vai em `Authorization: Bearer <token>`.

## Inserir conteúdo educativo no PostgreSQL

Os artigos iniciais são carregados automaticamente pela migração `V2__educational_content_postgresql.sql`. Para acrescentar conteúdo, execute SQL com parâmetros apropriados ou use um cliente PostgreSQL autenticado; por exemplo:

```sql
insert into educational_contents(content_key, title, category, level, content_type, minutes, body)
values ('poupanca-para-criancas', 'O que é poupar?', 'Orçamento', 'Básico', 'artigo', 5,
        'Poupar é guardar uma parte do dinheiro para usar depois.');
```

Os endpoints públicos leem diretamente dessa tabela. Para incluir o novo conteúdo em uma trilha, crie uma linha em `educational_track_modules` referenciando uma trilha existente e uma posição livre.

## Testes

A partir da raiz:

```powershell
Push-Location backend
mvn.cmd clean verify
Pop-Location
Push-Location frontend
npm.cmd test
Pop-Location
```

Para incluir o teste de integração, use preferencialmente as URLs de uma branch de desenvolvimento em um arquivo separado:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\backend\start-neon.ps1 -Mode VerifyNeon -EnvFile .\.env.neon-test
```

O teste executa o Flyway no banco selecionado e verifica BCrypt, cadastro sem confirmação, login JWT e autorização do painel administrativo. Cria uma meta pela API e confere a leitura pela API e pelo SQL. As linhas de teste são revertidas ao final da transação. Sequências PostgreSQL podem avançar mesmo com rollback.

Os testes frontend cobrem juros, reserva, dívida, aposentadoria e crédito. Nenhum dos dois manifests npm define o script build. Entrega real de e-mail e execução em Android/iOS dependem de testes próprios.

## Matriz de requisitos e limites

| Recurso | Situação |
|---|---|
| Expo SDK 54 + React Native + web | Implementado em código; depende de instalação |
| 5+ telas, navegação e responsividade por flex-wrap | Implementado |
| Dark/light persistente | Implementado |
| JWT Spring Security, API REST | Implementado |
| AsyncStorage e Context API | Implementado |
| PostgreSQL relacional + Flyway (inclui artigos e trilhas) | Configurado para Neon |
| CRUD de metas e orçamento | Implementado |
| Trilhas, progresso, favoritos, XP por módulo | MVP implementado |
| 6 categorias de simuladores | Cálculos educativos simplificados; dívida não implementa SAC/bola de neve, comparativo não lê taxas reais |
| Recuperação de senha por SMTP | Opcional para contas comuns |
| Google/Apple OAuth, 2FA | **Não implementado** |
| Quiz completo, certificados, notificações push, ranking | **Não implementado** |
| Vídeos, podcasts, newsletter, chat, download planilha | **Não implementado** |
| Onboarding de 3 passos | Perfil editável, onboarding guiado **não implementado** |
| Cookies, WCAG AA formal, SEO/sitemap/OG, Core Web Vitals | **Não auditados / não implementados integralmente** |
| HTTPS em produção | **Exige hospedagem TLS; HTTP somente desenvolvimento local** |
| LGPD | Excluir conta, rascunhos legais; **precisa avaliação jurídica e política real** |
| Menores de idade | **Antes de lançar**, implantar consentimento parental verificável, adequação etária e controles de privacidade |

**Segurança de produção:** usar HTTPS, CORS estrito, rate limiting, política para dados de menores, consentimento, gestão de sessão, monitoramento, backup, segregação de ambientes e armazenamento de token via SecureStore no mobile em vez de AsyncStorage. O MVP guarda JWT em AsyncStorage para demonstrar o requisito acadêmico. Sessão JWT expira após 30 minutos de emissão (não há renovação nem expiração real por inatividade). Não inserir dados bancários reais nem números de cartões.

## Entrega em equipe

| Integrante | RM | Contribuição verificável |
|---|---|---|
| A definir | A definir | A definir |

Cada integrante deve fazer commits com mudanças reais. Exemplo de distribuição: backend/auth, interface e acessibilidade, banco e Flyway, simuladores e testes, documentação e publicação. **Não fabricar commits**. Antes de submeter, validar execução, prints e testes e publicar no GitHub com histórico legítimo.

## Referências técnicas

- Expo SDK 54: https://expo.dev/sdk/54
- Expo SDK 54 changelog: https://expo.dev/changelog/sdk-54
- Spring Boot: https://docs.spring.io/spring-boot/index.html
- PostgreSQL: https://www.postgresql.org/docs/

Projeto educativo — taxas e exemplos são ilustrativos. Não é aconselhamento financeiro.
## Contribuições individuais

### Charles Elias de Araujo — RM: 1143175185

- Preparação e configuração do ambiente de desenvolvimento.
- Instalação e configuração do Java 21.
- Instalação das dependências do frontend e backend.
- Compilação e validação do backend utilizando Maven.
- Configuração e execução da API Spring Boot.
- Execução dos testes automatizados das calculadoras, com 7 testes aprovados.
- Testes funcionais de cadastro e login.
- Testes de criação, edição e exclusão de metas.
- Testes de receitas e despesas.
- Inicialização do Git, configuração do repositório remoto e publicação do projeto no GitHub.

#### Minha contribuição no backend

Fui responsável pelo desenvolvimento do backend do Money Here. Desenvolvi a API em Java 21 com Spring Boot, organizei a persistência no PostgreSQL e implementei as regras de autenticação e administração de contas.

##### `backend/`

- Configurei o projeto Maven no [pom.xml](backend/pom.xml), com as dependências da API REST, do Spring Security, da validação de dados, do Spring Data JPA, do PostgreSQL, do Flyway e dos testes.
- Organizei a execução no Windows com o [start-neon.ps1](backend/start-neon.ps1). O script lê as configurações locais, prepara as conexões do aplicativo e das migrações com o Neon e inicia o Spring Boot.

##### `backend/src/main/java/com/moneyhere/`

- Configurei o ponto de entrada da API em [MoneyHereApplication.java](backend/src/main/java/com/moneyhere/MoneyHereApplication.java). Em [ApiController.java](backend/src/main/java/com/moneyhere/ApiController.java), implementei as rotas de cadastro, login, perfil, favoritos e as quatro operações de metas e orçamento. A consulta, a edição e a exclusão de registros são limitadas à conta dona dos dados.
- Implementei o fluxo de autenticação em [AuthService.java](backend/src/main/java/com/moneyhere/AuthService.java) e [SecurityConfig.java](backend/src/main/java/com/moneyhere/SecurityConfig.java). A senha é armazenada com BCrypt, o login emite um JWT e a API verifica o token para liberar rotas protegidas. A recuperação por e-mail para contas comuns depende de SMTP configurado.
- Implementei a administração de contas em [AdminController.java](backend/src/main/java/com/moneyhere/AdminController.java) e [AdminBootstrap.java](backend/src/main/java/com/moneyhere/AdminBootstrap.java). O painel permite pesquisar contas, alterar permissões, desativar ou reativar usuários e definir senhas temporárias. A API impede que outros administradores alterem, desativem, redefinam a senha ou excluam a conta principal.
- Implementei as consultas de artigos e trilhas em [ContentController.java](backend/src/main/java/com/moneyhere/ContentController.java) e o registro de visitas e conclusões de módulos em [ProgressController.java](backend/src/main/java/com/moneyhere/ProgressController.java).
- Modelei usuários, metas, lançamentos, progresso e favoritos em [Model.java](backend/src/main/java/com/moneyhere/Model.java), com acesso ao banco pelos repositórios [Repos.java](backend/src/main/java/com/moneyhere/Repos.java), [Users.java](backend/src/main/java/com/moneyhere/Users.java) e [Tokens.java](backend/src/main/java/com/moneyhere/Tokens.java). Em [Dto.java](backend/src/main/java/com/moneyhere/Dto.java) e [ApiErrors.java](backend/src/main/java/com/moneyhere/ApiErrors.java), organizei a validação das entradas e as respostas de erro da API.

##### `backend/src/main/resources/`

- Configurei a leitura dos parâmetros do PostgreSQL e do Flyway por variáveis de ambiente em [application.properties](backend/src/main/resources/application.properties). Em [application.yml](backend/src/main/resources/application.yml), configurei as origens permitidas do frontend, o JWT, o serviço de e-mail e os endpoints de monitoramento.

##### `backend/src/main/resources/db/migration/`

- Escrevi as [migrações SQL versionadas](backend/src/main/resources/db/migration) para criar as tabelas e os relacionamentos de usuários, metas, lançamentos, progresso, favoritos e conteúdo educativo. As versões seguintes acrescentam papéis de acesso, troca de senha temporária, controle de sessões e a identificação da conta de administração principal.

##### `backend/src/test/java/com/moneyhere/`

- Organizei os testes unitários de autenticação e cadastro em [AuthServiceTest.java](backend/src/test/java/com/moneyhere/AuthServiceTest.java) e [DtoTest.java](backend/src/test/java/com/moneyhere/DtoTest.java). Em [PrimaryAdminTest.java](backend/src/test/java/com/moneyhere/PrimaryAdminTest.java), cobri o bloqueio de alterações e exclusão da conta principal e a gestão de administradores secundários.
- Incluí em [NeonPersistenceTest.java](backend/src/test/java/com/moneyhere/NeonPersistenceTest.java) cenários de integração que exercitam os endpoints com PostgreSQL, incluindo cadastro, login, acesso às metas e revogação de sessão. A suíte de integração é executada quando o ambiente de teste do Neon está configurado.

##### Como explicar o fluxo de uma meta

Quando o usuário cria uma meta, o frontend envia `POST /api/goals` com o JWT. O Spring Security valida o token. O `ApiController` identifica o usuário, valida os dados recebidos e associa a meta à conta dele. O repositório `Goals` grava o registro na tabela `financial_goals` do PostgreSQL. Quando a tela consulta as metas, o backend retorna os registros daquela conta.
