# Plano de evolução

## Fase 1 — MVP acadêmico (arquivos no ZIP)
Cadastro, verificação por SMTP, login JWT, artigos, trilhas, progresso, temas, dashboard, metas, orçamento, favoritos, ferramentas com cálculos aproximados e testes unitários.

## Fase 2 — Produto educacional completo
- Quiz com perguntas/respostas, acompanhamento de acertos, certificação com regras de aprovação.
- Onboarding em três passos e recomendador por objetivo/nível/progresso.
- Comparativos com dados atualizados e fontes com timestamps, simulações SAC/bola de neve e gráficos mensais.
- Vídeos/podcasts reais, upload/gestão editorial e página de administração.
- Login Google e Apple com OAuth/OpenID Connect, MFA opcional, expiração por inatividade e sessão renovável.
- Push opt-in, e-mails, newsletter e preferências granulares.
- Consentimento parental para menores, avaliação LGPD, política de cookies web, exportação de dados pessoais.
- HTTPS/TLS, implantação, backups e monitoramento, rate limiting, controles antibruteforce.
- SEO, sitemap, OG tags, auditoria WCAG 2.1 AA e testes Lighthouse/Core Web Vitals.
- Testes de integração Testcontainers, testes E2E em dispositivos e pipeline CI.

## Modelo da navegação
Home → Conteúdo → Artigo; Home → Trilhas → Artigo → Marcar concluído; Home → Ferramentas; Login/Cadastro → Painel → Metas/Orçamento/Favoritos/Configurações.

## Testes de aceitação manuais
1. Criar conta com SMTP de teste, receber e abrir o link; confirmar e-mail, entrar.
2. Criar/editar/excluir meta e lançamento; atualizar página e conferir persistência.
3. Completar módulo, voltar ao painel e confirmar XP.
4. Favoritar e desfavoritar conteúdo; conferir conta separada para outro usuário.
5. Alternar tema e recarregar app; confirmar persistência.
6. Testar web, Android e iOS com a API configurada; verificar falhas de rede.
7. Excluir a conta e confirmar indisponibilidade dos dados privados.
