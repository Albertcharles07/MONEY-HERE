import React,{useState} from 'react';import {router} from 'expo-router';import {Screen,Card,Btn,Txt,Input,ErrorText} from '../../components/UI';import {api} from '../../lib/api';import {useSession} from '../../lib/session';
export default function Settings(){const{user,theme,toggleTheme,reload,logout}=useSession();const[name,setName]=useState(user?.name||''),[goal,setGoal]=useState(user?.goal||''),[level,setLevel]=useState(user?.level||''),[currentPassword,setCurrentPassword]=useState(''),[newPassword,setNewPassword]=useState(''),[error,setError]=useState(''),[message,setMessage]=useState(''),[confirmDelete,setConfirmDelete]=useState(false);
 if(user?.mustChangePassword) return <Screen title="Trocar senha inicial" protectedPage>
  <Card>
   <Txt>Escolha uma nova senha de pelo menos 12 caracteres para liberar a administração.</Txt>
   <Input label="Senha inicial" value={currentPassword} onChangeText={setCurrentPassword} secureTextEntry/>
   <Input label="Nova senha" value={newPassword} onChangeText={setNewPassword} secureTextEntry/>
   <ErrorText message={error}/>
   <Btn title="Trocar senha e voltar ao login" onPress={async()=>{
    setError('');
    try {
     await api('/me/password',{method:'PUT',body:JSON.stringify({currentPassword,newPassword})});
     await logout();
     router.replace('/login');
    } catch(e) {
     setError(e instanceof Error?e.message:'Não foi possível trocar a senha.');
    }
   }}/>
  </Card>
 </Screen>;
 return <Screen title="Configurações" protectedPage><Card><Txt bold size={20}>Perfil e preferências</Txt><Txt muted>E-mail: {user?.email}</Txt><Input label="Nome" value={name} onChangeText={setName}/><Input label="Meu objetivo" value={goal} onChangeText={setGoal} placeholder="Ex.: montar reserva"/><Input label="Meu nível" value={level} onChangeText={setLevel} placeholder="Ex.: básico"/><Btn title={'Tema: '+(theme==='dark'?'escuro':'claro')+' (alternar)'} secondary onPress={toggleTheme}/><Btn title="Salvar perfil" onPress={async()=>{try{await api('/me',{method:'PUT',body:JSON.stringify({name,goal,level,theme})});await reload();setMessage('Perfil atualizado')}catch(e:any){setError(e.message)}}}/></Card><Card><Txt bold size={20}>Segurança</Txt><Input label="Senha atual" value={currentPassword} onChangeText={setCurrentPassword} secureTextEntry/><Input label="Nova senha" value={newPassword} onChangeText={setNewPassword} secureTextEntry/><Btn title="Trocar senha" onPress={async()=>{try{await api('/me/password',{method:'PUT',body:JSON.stringify({currentPassword,newPassword})});setMessage('Senha alterada');setCurrentPassword('');setNewPassword('')}catch(e:any){setError(e.message)}}}/></Card>
  {user?.role !== 'ADMIN' && (
   <Card>
    <Txt bold size={20}>Privacidade e dados</Txt>
    <Txt>Se você excluir sua conta, seus dados pessoais, metas, orçamento e progresso associados serão apagados do PostgreSQL.</Txt>
    <Btn title={confirmDelete?'Confirmar exclusão definitiva':'Excluir minha conta'} danger onPress={async()=>{if(!confirmDelete){setConfirmDelete(true);return}try{await api('/me',{method:'DELETE'});await logout();router.replace('/')}catch(e:any){setError(e.message)}}}/>
    <Btn title="Cancelar exclusão" secondary onPress={()=>setConfirmDelete(false)}/>
   </Card>
  )}
  <ErrorText message={error}/><Txt>{message}</Txt></Screen>}
