import React from 'react';
import {Stack} from 'expo-router';
import {SessionProvider} from '../lib/session';
import {StatusBar} from 'expo-status-bar';
export default function Layout(){return <SessionProvider><StatusBar style="auto"/><Stack screenOptions={{headerShown:false}}/></SessionProvider>}
