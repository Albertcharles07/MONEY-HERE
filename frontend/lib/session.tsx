import React,{createContext,useContext,useEffect,useState} from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import {api} from './api';
export type ThemeMode='light'|'dark';
type ContextValue={user:any;token:string|null;theme:ThemeMode;ready:boolean;login:(email:string,password:string)=>Promise<boolean>;logout:()=>Promise<void>;toggleTheme:()=>void;reload:()=>Promise<void>};
const Session=createContext<ContextValue>(null as any);
export function SessionProvider({children}:{children:React.ReactNode}){
 const [user,setUser]=useState<any>(null);const [token,setToken]=useState<string|null>(null);
 const [theme,setTheme]=useState<ThemeMode>('dark');const [ready,setReady]=useState(false);
 useEffect(()=>{(async()=>{try{const t=await AsyncStorage.getItem('moneyhere_theme');if(t==='light'||t==='dark')setTheme(t);const saved=await AsyncStorage.getItem('moneyhere_token');if(saved){setToken(saved);setUser(await api('/me'));}}catch{await AsyncStorage.removeItem('moneyhere_token');setToken(null);setUser(null);}finally{setReady(true);}})();},[]);
 async function login(email:string,password:string){const data=await api('/auth/login',{method:'POST',body:JSON.stringify({email,password})},true);await AsyncStorage.setItem('moneyhere_token',data.token);setToken(data.token);setUser(data.user);return data.user.mustChangePassword===true;}
 async function logout(){await AsyncStorage.removeItem('moneyhere_token');setToken(null);setUser(null);}
 function toggleTheme(){const next=theme==='dark'?'light':'dark';setTheme(next);AsyncStorage.setItem('moneyhere_theme',next);}
 async function reload(){setUser(await api('/me'));}
 return <Session.Provider value={{user,token,theme,ready,login,logout,toggleTheme,reload}}>{children}</Session.Provider>;
}
export const useSession=()=>useContext(Session);
export function useColors(){const {theme}=useSession();return theme==='dark'?{bg:'#0B1220',panel:'#162235',text:'#F0F7FF',muted:'#A8BBD0',primary:'#57E1B6',border:'#34445C',danger:'#FF9292',field:'#0E1A2A'}:{bg:'#F5F8FC',panel:'#FFFFFF',text:'#122539',muted:'#506176',primary:'#087F66',border:'#D5DFE8',danger:'#C22C36',field:'#F1F5F8'};}
