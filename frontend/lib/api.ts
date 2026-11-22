import AsyncStorage from '@react-native-async-storage/async-storage';
import {Platform} from 'react-native';
import Constants from 'expo-constants';

const expoHost = Constants.expoConfig?.hostUri ?? Constants.expoGoConfig?.debuggerHost;
const host = Platform.OS === 'web' && typeof window !== 'undefined'
 ? window.location.hostname
 : expoHost?.match(/^(\[[^\]]+\]|[^:]+)(?::\d+)?$/)?.[1];
const apiHost = host && !host.endsWith('.exp.direct') ? host : undefined;
const configured = process.env.EXPO_PUBLIC_API_URL?.trim();
const base = configured && apiHost
 ? configured.replace(/^(https?:\/\/)(localhost|127\.0\.0\.1)(?=[:/]|$)/, `$1${apiHost}`)
 : configured || (apiHost ? `http://${apiHost}:8080` : Platform.OS === 'web' ? 'http://localhost:8080' : '');
export const API_URL=base.replace(/\/+$/,'');
export async function api(path:string, options:RequestInit={}, publicRoute=false):Promise<any>{
 if(!API_URL) throw new Error('Abra o Expo pela rede local ou configure EXPO_PUBLIC_API_URL.');
 const token=publicRoute?null:await AsyncStorage.getItem('moneyhere_token');
 let res:Response;
 try{res=await fetch(API_URL+'/api'+path,{...options,headers:{'Content-Type':'application/json',...(token?{Authorization:'Bearer '+token}:{}),...options.headers}});}catch{throw new Error(`Não foi possível conectar à API em ${API_URL}. Confira se o backend está ligado e na mesma rede.`);}
 const data=await res.json().catch(()=>null);
 if(!res.ok){
  const unauthorized=path==='/auth/login'
   ? 'E-mail ou senha incorretos.'
   : 'Sessão expirada. Entre novamente.';
  throw new Error(data?.fields?.email ? 'Informe um e-mail válido, como nome@empresa.com.br.' :
   data?.message || (res.status===401?unauthorized:`Erro HTTP ${res.status}`));
 }
 return data;
}
