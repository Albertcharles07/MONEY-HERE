# MONEY HERE — Educação financeira Full Stack

Projeto acadêmico com **React Native / Expo SDK 54 (Android, iOS e web)**, **Spring Boot 3.5 / Java 21**, **JWT**, **PostgreSQL relacional em nuvem** como banco único, inclusive para conteúdo editorial.

O React Native/Expo chama a API Spring Boot por HTTP. A API usa Spring Data JPA para usuários, perfil, metas, orçamento, progresso e favoritos, e Spring JDBC para artigos e trilhas. O Neon PostgreSQL armazena esses dados. Os simuladores calculam os resultados no aplicativo.

Veja a [documentação do projeto](docs/DOCUMENTACAO_DO_PROJETO.md) para uma visão das funcionalidades, da arquitetura e dos principais arquivos do código.

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
```

O primeiro comando deve retornar `UP` e o segundo consulta o catálogo no Neon. Confira o histórico de migrações em `flyway_schema_history` na mesma branch. Para incluir o SMTP no health check, defina `MAIL_HEALTH_ENABLED=true` com um servidor de e-mail configurado.

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

Entre com a conta criada e abra **Administração** no menu ou a rota `/admin`. Pesquise pelo nome ou e-mail e abra **Opções → Editar** para corrigir os dados de uma conta elegível. O menu também permite ativar ou desativar contas, definir uma senha temporária, apagar contas e alterar papéis. A conta do administrador principal é protegida e o painel oculta o menu **Opções** dela.

Quando o e-mail muda, a conta precisa entrar novamente com o novo endereço. Mudanças de acesso e senha também encerram a sessão existente. A pessoa que recebe uma senha temporária deve trocá-la ao entrar. Uma conta desativada perde acesso aos endpoints privados e pode voltar a entrar quando reativada. A conta administrativa permite troca de senha pela área autenticada.

### Recuperação de senha por e-mail

O cadastro e o login exigem somente um e-mail em formato válido e uma senha. Para enviar o link de recuperação de senha de contas comuns, configure SMTP. O link expira em 30 minutos. O script `backend/start-neon.ps1` lê as variáveis abaixo do ambiente ou do `.env.local` na raiz. No arquivo, use `NOME=valor` para cada configuração. Use como `PUBLIC_URL` o endereço do frontend que o destinatário consegue abrir.

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

Com o SMTP configurado, o backend envia um aviso depois que o painel redefine uma senha ou salva uma alteração no nome ou no e-mail da conta. O aviso informa o tipo de alteração, sem incluir a senha ou os novos dados. Se o endereço mudar, o aviso vai para o e-mail anterior. Contas administrativas mantêm o fluxo de recuperação pelo painel.

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
| GET | `/api/budget/summary[?month=AAAA-MM]` | Resumo do orçamento: receitas, despesas, saldo e despesas por categoria |
| GET/POST | `/api/progress` | Lista aberturas e conclusões. Exige abertura antes de concluir. |
| POST | `/api/progress/open` | Registra abertura de um módulo |
| GET/POST/DELETE | `/api/favorites[/{key}]` | Favoritos |
| GET | `/api/public/content` | Artigos cadastrados no PostgreSQL |
| GET | `/api/public/content/{key}` | Artigo individual |
| GET | `/api/public/tracks` | Trilhas, com autenticação JWT |
| GET/POST | `/api/admin/users` | Lista contas e cria usuários comuns |
| PUT | `/api/admin/users/{id}` | Corrige nome e e-mail de uma conta elegível |
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

Os testes frontend cobrem juros, reserva, dívida, aposentadoria e crédito. Nenhum dos dois manifests npm define o script build. O recebimento do e-mail de recuperação foi confirmado em teste manual com SMTP. A execução em Android e iOS deve ser validada nos dispositivos da apresentação.

## Matriz de requisitos e limites

| Recurso | Situação |
|---|---|
| React Native com Expo SDK 54 | Aplicativo configurado para Android, iOS e web. A compilação web foi validada. |
| Telas e navegação | Navegação pelo Expo Router, layout responsivo e cabeçalho visível durante a rolagem. Os atalhos de cadastro aparecem para visitantes sem sessão e para o administrador principal. |
| Tema e sessão | Tema claro/escuro persistente. React Context gerencia a sessão e o AsyncStorage guarda a preferência de tema e o token localmente. |
| API REST e autenticação | Spring Boot com Spring Security, login por e-mail e senha, hash BCrypt e JWT para rotas privadas. |
| Banco de dados | PostgreSQL hospedado no Neon, com migrações Flyway para contas, metas, orçamento, progresso e conteúdo educativo. |
| Catálogo | 28 artigos no catálogo padrão, em sete categorias, com busca, filtros, exemplos e exercícios. Os artigos são públicos. |
| Trilhas e favoritos | Trilhas exigem login. O aplicativo registra a abertura e a conclusão de módulos, atribui 25 XP por conclusão e permite salvar artigos como favoritos. |
| Metas e orçamento | Cadastro, consulta, edição e exclusão dos próprios registros. A interface formata valores em reais e aceita o prazo da meta em `DD/MM/AAAA`. A API oferece consulta por ID e resumo do orçamento com filtro mensal opcional. |
| Simuladores | Seis opções educativas para contas autenticadas, com cálculos de juros compostos, reserva, dívidas, aposentadoria, comparativo de investimentos e custo do crédito. |
| Recuperação de senha | Contas comuns podem solicitar um link por SMTP quando o remetente está configurado. O link expira em 30 minutos. O recebimento foi confirmado em um teste manual. |
| Administração de contas | Cadastro, pesquisa e edição de nome e e-mail, além de alteração de permissões, ativação, desativação, redefinição de senha e exclusão de contas elegíveis. A conta do administrador principal é protegida. |
| Perfil, privacidade e termos | Edição de perfil, troca de senha, exclusão de conta comum e páginas de Termos de Uso e Política de Privacidade da versão acadêmica. |

O JWT vence 30 minutos após a emissão. A execução local descrita neste README usa HTTP.

## Entrega em equipe

| Integrante | RM | Contribuição ou responsabilidade na entrega | Commits publicados |
|---|---|---|---|
| Charles Elias de Araujo | 1143175185 | Ambiente, execução da API, testes funcionais e publicação no GitHub | [`cc5047f`](https://github.com/Albertcharles07/MONEY-HERE/commit/cc5047f) |
| Carlos Daniel de Souza Gomes | 1143058270 | Banco de dados no Neon, migrações, autenticação JWT e administração de contas | [`6f044ff`](https://github.com/Albertcharles07/MONEY-HERE/commit/6f044ff) — registro da contribuição individual. [`4a898e8`](https://github.com/Albertcharles07/MONEY-HERE/commit/4a898e8) — correção da atribuição de banco de dados, autenticação e administração. |
| Lucas Rodrigues de Magalhaes | 1142712305 | Endpoints REST de metas e orçamento, resumo financeiro e testes da API | [`f0442aa`](https://github.com/Albertcharles07/MONEY-HERE/commit/f0442aa), [`cf165d3`](https://github.com/Albertcharles07/MONEY-HERE/commit/cf165d3) e [`691bede`](https://github.com/Albertcharles07/MONEY-HERE/commit/691bede) |
| João Pedro dias Araujo | 1143176420 | Tela de favoritos e armazenamento local da sessão e do tema |  |

Cada integrante deve registrar suas mudanças no Git. Antes da entrega, confira o histórico no GitHub, valide os fluxos no ambiente da apresentação e reúna as evidências de execução e testes.

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

### Carlos Daniel de Souza Gomes — RM: 1143058270

- Responsável pelo banco de dados, pela autenticação e pela administração de contas do Money Here.
- Configuração e manutenção do banco PostgreSQL hospedado no Neon. Na equipe, o acesso direto ao banco e a gestão das credenciais ficaram sob minha responsabilidade.
- Modelagem das tabelas e dos relacionamentos de usuários, metas, lançamentos, favoritos, progresso e conteúdo educativo ([migrações SQL](backend/src/main/resources/db/migration)).
- Configuração da conexão da API com o PostgreSQL por variáveis de ambiente e do Flyway para aplicar as migrações ([configuração do banco](backend/src/main/resources/application.properties)).
- Implementação do cadastro e do login com JWT, armazenamento de senhas com BCrypt e proteção das rotas privadas com Spring Security ([serviço de autenticação](backend/src/main/java/com/moneyhere/AuthService.java) e [configuração de segurança](backend/src/main/java/com/moneyhere/SecurityConfig.java)).
- Implementação das regras de administração de contas para alterar permissões, definir senhas temporárias, desativar, reativar e excluir contas elegíveis ([API administrativa](backend/src/main/java/com/moneyhere/AdminController.java)).
- Proteção da conta do administrador principal contra desativação, exclusão, redefinição de senha e remoção de privilégios por outros administradores ([testes da conta principal](backend/src/test/java/com/moneyhere/PrimaryAdminTest.java)).
- Preparação de testes unitários e de integração para autenticação, proteção da conta principal, revogação de sessões e acesso aos dados no PostgreSQL ([testes](backend/src/test/java/com/moneyhere)).

### Lucas Rodrigues de Magalhaes — RM: 1142712305

- Responsável pelo backend da API REST de metas e orçamento.
- Criação dos endpoints `GET /api/goals/{id}` e `GET /api/budget/{id}`, que retornam um registro específico apenas para o usuário dono dele (404 para registros de outros usuários) ([controller da API](backend/src/main/java/com/moneyhere/ApiController.java)).
- Refatoração das buscas por id no controller, reaproveitando o mesmo método na consulta, na edição e na exclusão.
- Criação do endpoint `GET /api/budget/summary`, que calcula receitas, despesas, saldo e gastos por categoria, com filtro opcional por mês (`?month=AAAA-MM`) e retorno 400 para mês inválido.

### João Pedro dias Araujo — RM: 1143176420

- Responsável pela tela **Meus favoritos** na entrega, incluindo a listagem, a abertura e a remoção de artigos salvos. [favoritos.tsx](frontend/app/app/favoritos.tsx)
- Responsável pelo armazenamento local da sessão e do tema com AsyncStorage, incluindo a recuperação do token ao abrir o app, sua remoção no logout e a preferência de tema. [session.tsx](frontend/lib/session.tsx)
