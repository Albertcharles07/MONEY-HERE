import React from 'react';
import {Link} from 'expo-router';
import {Card, Screen, Txt} from '../components/UI';

const controller = process.env.EXPO_PUBLIC_PRIVACY_CONTROLLER?.trim();
const privacyEmail = process.env.EXPO_PUBLIC_PRIVACY_EMAIL?.trim();

export default function Privacy() {
  return (
    <Screen title="Política de privacidade">
      <Txt muted>Atualizada em 23 de novembro de 2026</Txt>
      <Card>
        <Txt bold size={20}>1. Quem cuida dos dados</Txt>
        <Txt>{controller
          ? `${controller} é responsável pelo tratamento dos dados pessoais no Money Here.`
          : 'A equipe responsável pelo projeto acadêmico Money Here administra os dados usados no aplicativo.'}</Txt>
      </Card>
      <Card>
        <Txt bold size={20}>2. Quais dados usamos e por quê</Txt>
        <Txt>O cadastro pede nome, e-mail e senha. Usamos esses dados para criar e identificar a conta, autenticar o acesso e proteger as áreas privadas. A senha é armazenada como hash BCrypt.</Txt>
        <Txt>Preferências, metas, lançamentos de orçamento, favoritos e progresso nas trilhas são registrados conforme você usa o app. Servem para exibir seus dados e retomar suas atividades. O orçamento usa valores inseridos manualmente, sem conexão com sua conta bancária.</Txt>
        <Txt>A leitura de artigos públicos dispensa cadastro. Os valores digitados nos simuladores são calculados no próprio aplicativo.</Txt>
      </Card>
      <Card>
        <Txt bold size={20}>3. Armazenamento e fornecedores</Txt>
        <Txt>Os dados da conta e o progresso ficam no banco PostgreSQL hospedado no Neon. O aplicativo guarda localmente o token de sessão e a preferência de tema. Ao sair da conta, o token local é removido.</Txt>
        <Txt>Quando o envio de e-mail está configurado, o serviço de e-mail recebe os dados necessários para entregar mensagens da conta, como links de recuperação. O acesso administrativo é usado para gerenciar contas e permissões.</Txt>
      </Card>
      <Card>
        <Txt bold size={20}>4. Retenção e exclusão</Txt>
        <Txt>Os registros ficam associados à conta enquanto ela existir para que você possa consultar ou atualizar seus dados. Em Meu painel, abra Configurações e escolha Excluir minha conta. A operação remove a conta comum e os registros vinculados da base ativa.</Txt>
        <Link href="/app/configuracoes"><Txt bold>Abrir configurações da conta</Txt></Link>
      </Card>
      <Card>
        <Txt bold size={20}>5. Seus direitos e atendimento</Txt>
        <Txt>Nos termos da LGPD, você pode solicitar confirmação de tratamento, acesso, correção, informações sobre compartilhamento e, quando aplicável, exclusão ou outros direitos previstos em lei.</Txt>
        <Txt>{privacyEmail
          ? 'Use o canal de privacidade abaixo. A equipe pode pedir informações proporcionais para confirmar que você é o titular ou seu representante.'
          : 'Nesta instalação acadêmica, use Configurações para editar o perfil ou excluir uma conta comum. O canal de privacidade externo ainda não está ativo.'}</Txt>
        {privacyEmail && <Link href={`mailto:${privacyEmail}`}><Txt bold>{privacyEmail}</Txt></Link>}
        <Link href="/contato"><Txt bold>Ver canais do projeto</Txt></Link>
        <Link href="https://www.gov.br/anpd/pt-br/assuntos/titular-de-dados/direito-dos-titulares"><Txt bold>Direitos dos titulares na ANPD</Txt></Link>
      </Card>
      <Card>
        <Txt bold size={20}>6. Público e atualizações</Txt>
        <Txt>As contas são destinadas a adultos. Crianças e adolescentes podem consultar os conteúdos públicos com orientação de um responsável. Esta página é atualizada quando o tratamento de dados ou as funcionalidades mudarem.</Txt>
      </Card>
    </Screen>
  );
}
