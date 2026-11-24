import React from 'react';import {View} from 'react-native';import {router} from 'expo-router';
import {Screen,Txt,Btn,Card,Grid,Tile} from '../components/UI';
import {useSession} from '../lib/session';
const categories = [
  {icon: '💵', name: 'Orçamento'},
  {icon: '📈', name: 'Investimentos'},
  {icon: '💳', name: 'Dívidas'},
  {icon: '🧾', name: 'Impostos'},
  {icon: '🚀', name: 'Empreendedorismo'},
];
export default function Home(){
 const {canRegister} = useSession();
 return <Screen><View style={{gap:14,paddingVertical:24}}><Txt size={14} bold>APRENDA • PLANEJE • CONQUISTE</Txt><Txt size={39} bold>Seu dinheiro. Suas escolhas. Seu futuro.</Txt><Txt size={19} muted>Educação financeira com linguagem simples para crianças, jovens e adultos. Aprenda no seu ritmo e pratique com simuladores.</Txt><View style={{flexDirection:'row',flexWrap:'wrap',gap:12}}>{canRegister && <Btn title="Começar agora" onPress={()=>router.push('/cadastro')}/>}<Btn title="Explorar conteúdos" secondary onPress={()=>router.push('/conteudo')}/></View></View>
 <Txt size={23} bold>Explore por assunto</Txt>
 <Grid>{categories.map(({icon, name}) => (
   <Tile key={name}>
     <Card>
       <Txt size={20} bold>{icon} {name}</Txt>
       <Btn title="Ver conteúdos" secondary onPress={() => router.push({pathname: '/conteudo', params: {categoria: name}})} />
     </Card>
   </Tile>
 ))}</Grid>
 <Txt size={23} bold>Primeiros passos</Txt><Grid><Tile><Card><Txt size={21} bold>📖 Trilhas de estudo</Txt><Txt>Aprenda em módulos curtos e acompanhe o seu progresso.</Txt><Btn title="Ver trilhas" onPress={()=>router.push('/trilhas')}/></Card></Tile><Tile><Card><Txt size={21} bold>🧮 Ferramentas financeiras</Txt><Txt>Juros, reserva, dívidas e aposentadoria com exemplos.</Txt><Btn title="Abrir calculadoras" onPress={()=>router.push('/ferramentas')}/></Card></Tile><Tile><Card><Txt size={21} bold>🎯 Minhas metas</Txt><Txt>Organize seus objetivos e acompanhe seu avanço.</Txt><Btn title="Ver minhas metas" onPress={()=>router.push('/app/metas')}/></Card></Tile></Grid>
 {canRegister && <Card><Txt size={22} bold>Aprender a cuidar do dinheiro começa aqui.</Txt><Txt>Crie sua conta gratuitamente e registre suas metas.</Txt><Btn title="Criar minha conta" onPress={()=>router.push('/cadastro')}/></Card>}
 </Screen>}
