import React from 'react';
import {Link} from 'expo-router';
import {Card, Screen, Txt} from '../components/UI';

export default function Terms() {
  return (
    <Screen title="Termos de uso">
      <Txt muted>Atualizado em 23 de novembro de 2026</Txt>
      <Card>
        <Txt bold size={20}>1. Uso do Money Here</Txt>
        <Txt>O Money Here é um projeto acadêmico de educação financeira. Você pode consultar o catálogo de artigos sem cadastro. Trilhas, simuladores, metas, orçamento e favoritos exigem uma conta e uma sessão ativa.</Txt>
        <Txt>As contas são destinadas a pessoas com 18 anos ou mais. Crianças e adolescentes podem consultar os materiais públicos com orientação de um responsável.</Txt>
      </Card>
      <Card>
        <Txt bold size={20}>2. Conteúdo e simulações</Txt>
        <Txt>Artigos e ferramentas ajudam a entender conceitos e comparar cenários. As simulações usam os valores informados e podem simplificar custos, impostos e condições de mercado. O Money Here não oferece serviços bancários, intermediação de investimentos ou recomendações personalizadas.</Txt>
        <Txt>Confira as condições reais do produto ou contrato antes de tomar uma decisão financeira.</Txt>
      </Card>
      <Card>
        <Txt bold size={20}>3. Conta e uso responsável</Txt>
        <Txt>Use um endereço de e-mail ao qual você tenha acesso, guarde sua senha e saia da conta em dispositivos compartilhados. Use apenas a sua conta e respeite a privacidade das demais pessoas. Não tente acessar dados de terceiros, interferir no funcionamento do app ou usar a plataforma para fraude.</Txt>
        <Txt>A equipe pode restringir contas por abuso ou para proteger o serviço e seus usuários. Contas comuns podem pedir a exclusão em Configurações.</Txt>
      </Card>
      <Card>
        <Txt bold size={20}>4. Atualizações e contato</Txt>
        <Txt>Este texto pode ser atualizado quando as funcionalidades ou as regras de uso mudarem. A data no início da página identifica a versão em vigor.</Txt>
        <Link href="/privacidade"><Txt bold>Política de Privacidade</Txt></Link>
        <Link href="/contato"><Txt bold>Página de contato</Txt></Link>
      </Card>
    </Screen>
  );
}
