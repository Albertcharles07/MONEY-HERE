import React from 'react';
import {View,Text,TextInput,Pressable,ScrollView,useWindowDimensions} from 'react-native';
import {router} from 'expo-router';
import {useColors,useSession} from '../lib/session';
export function Txt({children,size=16,bold=false,muted=false,style}:{children:React.ReactNode,size?:number,bold?:boolean,muted?:boolean,style?:any}){const c=useColors();return <Text style={[{fontSize:size,color:muted?c.muted:c.text,fontWeight:bold?'700':'400',lineHeight:size*1.5},style]}>{children}</Text>}
export function Btn({title,onPress,secondary=false,danger=false,disabled=false}:{title:string,onPress:()=>void,secondary?:boolean,danger?:boolean,disabled?:boolean}){const c=useColors();return <Pressable disabled={disabled} accessibilityRole="button" onPress={onPress} style={{backgroundColor:danger?c.danger:secondary?c.field:c.primary,borderRadius:12,paddingHorizontal:17,paddingVertical:12,alignItems:'center',opacity:disabled?.5:1,borderWidth:secondary?1:0,borderColor:c.border}}><Txt bold style={{color:danger?'#10151F':secondary?c.text:'#081B20'}}>{title}</Txt></Pressable>}
export function Input({label,value,onChangeText,placeholder,secureTextEntry=false,keyboardType='default',multiline=false}:{label:string,value:string,onChangeText:(v:string)=>void,placeholder?:string,secureTextEntry?:boolean,keyboardType?:any,multiline?:boolean}){const c=useColors();return <View style={{gap:5,marginBottom:12}}><Txt bold size={14}>{label}</Txt><TextInput accessibilityLabel={label} value={value} onChangeText={onChangeText} placeholder={placeholder} placeholderTextColor={c.muted} secureTextEntry={secureTextEntry} keyboardType={keyboardType} multiline={multiline} style={{borderColor:c.border,borderWidth:1,borderRadius:11,padding:12,minHeight:48,color:c.text,backgroundColor:c.field}}/></View>}
export function Card({children,style}:{children:React.ReactNode,style?:any}){const c=useColors();return <View style={[{padding:19,backgroundColor:c.panel,borderRadius:17,borderWidth:1,borderColor:c.border,gap:10},style]}>{children}</View>}
const nav=[['Home','/'],['Conteúdo','/conteudo'],['Trilhas','/trilhas'],['Ferramentas','/ferramentas'],['Sobre','/sobre'],['Contato','/contato']];
export function Screen({title,children,protectedPage=false}:{title?:string,children:React.ReactNode,protectedPage?:boolean}){
 const c=useColors();const {user,ready,canRegister,theme,toggleTheme,logout}=useSession();const {width}=useWindowDimensions();const wide=width>=800;
 return <ScrollView style={{flex:1,backgroundColor:c.bg}} contentContainerStyle={{flexGrow:1}} keyboardShouldPersistTaps="handled" stickyHeaderIndices={[0]}>
  <View style={{backgroundColor:c.panel,borderBottomColor:c.border,borderBottomWidth:1,paddingVertical:13,paddingHorizontal:18}}>
   <View style={{width:'100%',maxWidth:1150,alignSelf:'center',gap:10,flexDirection:'row',flexWrap:'wrap',alignItems:'center',justifyContent:'space-between'}}>
    <Pressable accessibilityRole="link" onPress={()=>router.push('/')}><Txt size={23} bold>◈ MONEY <Text style={{color:c.primary}}>HERE</Text></Txt></Pressable>
    <View style={{flexDirection:'row',flexWrap:'wrap',gap:wide?18:10,alignItems:'center'}}>{nav.map(([name,path])=><Pressable key={path} accessibilityRole="link" onPress={()=>router.push(path as any)}><Txt size={wide?14:12}>{name}</Txt></Pressable>)}</View>
    <View style={{flexDirection:'row',flexWrap:'wrap',alignItems:'center',gap:12}}>
     <Pressable accessibilityLabel="Alternar tema" accessibilityRole="button" onPress={toggleTheme}><Txt>{theme==='dark'?'☀️':'🌙'}</Txt></Pressable>
     {user ? <>
      <Pressable onPress={()=>router.push('/app')}><Txt bold size={14}>Meu painel</Txt></Pressable>
      {user.role==='ADMIN'&&!user.mustChangePassword&&<Pressable onPress={()=>router.push('/admin')}><Txt size={14}>Administração</Txt></Pressable>}
      <Pressable onPress={async()=>{await logout();router.replace('/');}}><Txt size={14}>Sair</Txt></Pressable>
     </> : ready && <Pressable onPress={()=>router.push('/login')}><Txt size={14}>Entrar</Txt></Pressable>}
     {canRegister && <Pressable onPress={()=>router.push('/cadastro')}><Txt size={14} bold>Criar conta</Txt></Pressable>}
    </View>
   </View>
  </View>
  <View style={{width:'100%',maxWidth:1050,alignSelf:'center',paddingHorizontal:18,paddingVertical:28,flex:1,gap:18}}>
   {title?<Txt size={30} bold>{title}</Txt>:null}
   {protectedPage && (!ready || !user) ? (
    <Card style={{width:'100%',maxWidth:520}}>
     {ready ? <>
      <Txt bold size={20}>Crie uma conta para acessar</Txt>
      <Txt muted>Se você já tem uma conta no Money Here, entre para continuar.</Txt>
      <View style={{flexDirection:'row',flexWrap:'wrap',gap:10}}>
       <Btn title="Criar conta" onPress={()=>router.push('/cadastro')}/>
       <Btn title="Já tenho conta" secondary onPress={()=>router.push('/login')}/>
      </View>
     </> : <Txt muted>Verificando sua sessão…</Txt>}
    </Card>
   ) : children}
  </View>
  <View style={{borderTopWidth:1,borderTopColor:c.border,padding:20,alignItems:'center',gap:6}}><Txt muted size={12}>Money Here · Educação financeira em linguagem simples</Txt><Txt muted size={12}>Artigos e simulações para fins educativos. O app não oferece recomendações de investimento.</Txt><View style={{flexDirection:'row',gap:16}}><Pressable accessibilityRole="link" onPress={()=>router.push('/privacidade')}><Txt size={12}>Privacidade</Txt></Pressable><Pressable accessibilityRole="link" onPress={()=>router.push('/termos')}><Txt size={12}>Termos</Txt></Pressable></View></View>
 </ScrollView>
}
export function Grid({children}:{children:React.ReactNode}){return <View style={{flexDirection:'row',flexWrap:'wrap',gap:14}}>{children}</View>}
export function Tile({children}:{children:React.ReactNode}){return <View style={{minWidth:235,flexBasis:265,flexGrow:1}}>{children}</View>}
export function ErrorText({message}:{message:string}){const c=useColors();return message?<Txt size={13} style={{color:c.danger}}>{message}</Txt>:null}
