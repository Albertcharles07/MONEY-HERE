import React, {useCallback, useEffect, useState} from 'react';
import {router, useFocusEffect} from 'expo-router';
import {Screen, Card, Txt, Btn} from '../components/UI';
import {tracks, fallbackContent} from '../lib/catalog';
import {api} from '../lib/api';
import {useSession} from '../lib/session';

export default function Tracks() {
  const {user} = useSession();
  const [catalogTracks, setCatalogTracks] = useState(tracks);
  const [articles, setArticles] = useState(fallbackContent);
  const [done, setDone] = useState<string[]>([]);
  const [opened, setOpened] = useState<string[]>([]);
  const [msg, setMsg] = useState('');

  useEffect(() => {
    if (!user) return;
    let active = true;
    Promise.all([api('/public/tracks'), api('/public/content', {}, true)])
      .then(([remoteTracks, remoteArticles]) => {
        if (active) {
          setCatalogTracks(remoteTracks);
          setArticles(remoteArticles);
        }
      })
      .catch(() => {});
    return () => { active = false; };
  }, [user]);

  useFocusEffect(useCallback(() => {
    let active = true;
    if (!user) {
      setDone([]);
      setOpened([]);
    }
    if (user) {
      api('/progress').then(result => {
        if (active) {
          setDone(result.completed);
          setOpened(result.opened ?? []);
        }
      }).catch(error => { if (active) setMsg(error.message); });
    }
    return () => { active = false; };
  }, [user]));

  async function complete(key: string) {
    try {
      await api('/progress', {method: 'POST', body: JSON.stringify({key})});
      setDone(previous => previous.includes(key) ? previous : [...previous, key]);
    } catch (error) {
      setMsg(error instanceof Error ? error.message : 'Não foi possível salvar o progresso.');
    }
  }

  return (
    <Screen title="Trilhas de estudo" protectedPage>
      <Txt muted>Aprenda com módulos curtos e acompanhe seu progresso.</Txt>
      {catalogTracks.map(track => (
        <Card key={track.key}>
          <Txt size={22} bold>{track.title}</Txt>
          <Txt>{track.modules.filter(key => done.includes(key)).length} de {track.modules.length} módulos concluídos</Txt>
          {track.modules.map((key, index) => {
            const article = articles.find(item => item.key === key);
            const completed = done.includes(key);
            const canComplete = opened.includes(key);
            return (
              <Card key={key}>
                <Txt bold>{index + 1}. {article?.title ?? key}</Txt>
                <Txt muted>{article && `${article.minutes} min • `}{completed ? '✅ Concluído' : 'Pendente'}</Txt>
                <Btn title="Estudar módulo" secondary onPress={() => router.push({pathname: '/artigo/[key]', params: {key, fromTrack: '1'}})} />
                {user && <Btn title={completed ? 'Concluído' : canComplete ? 'Marcar concluído (+25 XP)' : 'Abra o módulo para concluir'} disabled={completed || !canComplete} onPress={() => complete(key)} />}
              </Card>
            );
          })}
        </Card>
      ))}
      {msg ? <Txt>{msg}</Txt> : null}
    </Screen>
  );
}
