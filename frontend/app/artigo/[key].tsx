import React, {useEffect, useState} from 'react';
import {useLocalSearchParams, router} from 'expo-router';
import {Screen, Txt, Card, Btn, ErrorText} from '../../components/UI';
import {api} from '../../lib/api';
import {fallbackContent} from '../../lib/catalog';
import {useSession} from '../../lib/session';

export default function Article() {
  const {key, fromTrack} = useLocalSearchParams<{key: string; fromTrack?: string}>();
  const {user} = useSession();
  const userId = user?.id;
  const [item, setItem] = useState<(typeof fallbackContent)[number]>();
  const [msg, setMsg] = useState('');
  const [visitError, setVisitError] = useState('');
  const [savingVisit, setSavingVisit] = useState(false);
  const [retry, setRetry] = useState(0);

  useEffect(() => {
    if (!key) return;
    let active = true;
    setItem(fallbackContent.find(article => article.key === key));
    setVisitError('');
    const recordVisit = fromTrack === '1' && Boolean(userId);
    setSavingVisit(recordVisit);
    async function load() {
      try {
        const article = await api('/public/content/' + encodeURIComponent(key), {}, true);
        if (!active) return;
        setItem(article);
        if (recordVisit) await api('/progress/open', {method: 'POST', body: JSON.stringify({key})});
      } catch (error) {
        if (active && recordVisit) setVisitError(error instanceof Error ? error.message : 'Não foi possível registrar a abertura.');
      } finally {
        if (active) setSavingVisit(false);
      }
    }
    void load();
    return () => { active = false; };
  }, [key, fromTrack, userId, retry]);

  return <Screen title={item?.title || 'Artigo'}>
    <Txt muted>Home / Conteúdo / {item?.category || ''}</Txt>
    <Card>
      <Txt muted>{item?.level} • {item?.minutes} minutos</Txt>
      <Txt size={18}>{item?.body || 'Conteúdo indisponível'}</Txt>
    </Card>
    {msg ? <Txt>{msg}</Txt> : null}
    <ErrorText message={visitError} />
    {visitError ? <Btn title="Tentar registrar abertura novamente" secondary onPress={() => setRetry(value => value + 1)} /> : null}
    <Btn title="Salvar nos favoritos" onPress={async () => {
      if (!user) { router.push('/login'); return; }
      try {
        await api('/favorites', {method: 'POST', body: JSON.stringify({key})});
        setMsg('Adicionado aos favoritos!');
      } catch (error) {
        setMsg(error instanceof Error ? error.message : 'Não foi possível salvar o favorito.');
      }
    }} />
    <Btn title={fromTrack === '1' ? 'Voltar à trilha' : 'Voltar aos artigos'} secondary disabled={savingVisit}
      onPress={() => fromTrack === '1' && router.canGoBack() ? router.back() : router.replace(fromTrack === '1' ? '/trilhas' : '/conteudo')} />
  </Screen>;
}