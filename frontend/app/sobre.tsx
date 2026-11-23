import React from 'react';
import {router} from 'expo-router';
import {Btn, Card, Grid, Screen, Tile, Txt} from '../components/UI';

export default function About() {
  return (
    <Screen title="Sobre o Money Here">
      <Card>
        <Txt bold size={23}>Por que o Money Here existe</Txt>
        <Txt>Montar um orçamento, comparar duas parcelas ou começar uma reserva são decisões comuns. Criamos o Money Here para ensinar o que observar em cada uma delas, com exemplos em reais e exercícios que você pode repetir no seu ritmo.</Txt>
        <Txt muted>O Money Here é um projeto acadêmico de educação financeira.</Txt>
      </Card>
      <Grid>
        <Tile><Card style={{flex: 1}}>
          <Txt bold size={19}>Aprenda um assunto de cada vez</Txt>
          <Txt>O catálogo aberto reúne artigos sobre orçamento, juros, reserva, dívidas, investimentos, impostos e empreendedorismo.</Txt>
        </Card></Tile>
        <Tile><Card style={{flex: 1}}>
          <Txt bold size={19}>Pratique com números</Txt>
          <Txt>Com uma conta, você pode testar cenários nos simuladores, acompanhar módulos nas trilhas e organizar suas metas e lançamentos.</Txt>
        </Card></Tile>
        <Tile><Card style={{flex: 1}}>
          <Txt bold size={19}>Escolha por onde começar</Txt>
          <Txt>Se você quer entender para onde vai seu dinheiro, comece pelo orçamento. Se quer se preparar para imprevistos, explore os artigos sobre reserva.</Txt>
        </Card></Tile>
      </Grid>
      <Card>
        <Txt bold size={19}>Envie uma sugestão</Txt>
        <Txt>Quer propor um tema ou avisar que encontrou um erro? Veja os canais de contato do projeto.</Txt>
        <Btn title="Ver canais de contato" secondary onPress={() => router.push('/contato')} />
      </Card>
    </Screen>
  );
}
