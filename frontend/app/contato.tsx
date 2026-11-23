import React from 'react';
import {Link} from 'expo-router';
import {Card, Grid, Screen, Tile, Txt} from '../components/UI';

const supportEmail = process.env.EXPO_PUBLIC_SUPPORT_EMAIL?.trim();
const privacyEmail = process.env.EXPO_PUBLIC_PRIVACY_EMAIL?.trim();
const supportPhone = process.env.EXPO_PUBLIC_SUPPORT_PHONE?.trim();
const privacyPhone = process.env.EXPO_PUBLIC_PRIVACY_PHONE?.trim();

export default function Contact() {
  return (
    <Screen title="Contato">
      <Txt muted>Escolha um canal de acordo com o assunto.</Txt>
      <Grid>
        <Tile><Card style={{flex: 1}}>
          <Txt bold size={20}>Suporte e sugestões</Txt>
          <Txt>Para dúvidas sobre o app, sugestões de artigos ou erros de navegação, descreva o que aconteceu e em qual tela.</Txt>
          {supportEmail ? <Link href={`mailto:${supportEmail}`}><Txt bold>{supportEmail}</Txt></Link>
            : <Txt muted>E-mail de exemplo: suporte@moneyhere.example</Txt>}
          {supportPhone ? <Link href={`tel:${supportPhone.replace(/[^+\d]/g, '')}`}><Txt bold>{supportPhone}</Txt></Link>
            : <Txt muted>Telefone de exemplo: +55 (00) 90000-0000</Txt>}
        </Card></Tile>
        <Tile><Card style={{flex: 1}}>
          <Txt bold size={20}>Privacidade e dados pessoais</Txt>
          <Txt>Use esse canal para dúvidas sobre seus dados e para solicitar acesso, correção ou exclusão.</Txt>
          {privacyEmail ? <Link href={`mailto:${privacyEmail}`}><Txt bold>{privacyEmail}</Txt></Link>
            : <Txt muted>E-mail de exemplo: privacidade@moneyhere.example</Txt>}
          {privacyPhone ? <Link href={`tel:${privacyPhone.replace(/[^+\d]/g, '')}`}><Txt bold>{privacyPhone}</Txt></Link>
            : <Txt muted>Telefone de exemplo: +55 (00) 90000-0001</Txt>}
          <Link href="/privacidade"><Txt bold>Leia a Política de Privacidade</Txt></Link>
        </Card></Tile>
      </Grid>
      <Card>
        <Txt bold size={18}>Antes de enviar</Txt>
        {(!supportEmail || !privacyEmail || !supportPhone || !privacyPhone) &&
          <Txt>Os contatos marcados como exemplo são fictícios e não recebem mensagens nem ligações.</Txt>}
        <Txt>Evite compartilhar senhas, códigos de acesso, números de cartão ou dados bancários. Se enviar uma captura de tela, oculte suas informações pessoais.</Txt>
      </Card>
    </Screen>
  );
}
