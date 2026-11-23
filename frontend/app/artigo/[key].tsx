import React, {useEffect, useState} from 'react';
import {View} from 'react-native';
import {useLocalSearchParams, router} from 'expo-router';
import {Screen, Txt, Card, Btn, ErrorText} from '../../components/UI';
import {api} from '../../lib/api';
import {fallbackContent, type Article as ArticleContent} from '../../lib/catalog';
import {useSession} from '../../lib/session';

export default function Article() {
  const {key, fromTrack, categoria} = useLocalSearchParams<{key: string; fromTrack?: string; categoria?: string}>();
  const {user} = useSession();
  const userId = user?.id;
  const [item, setItem] = useState<ArticleContent>();
  const [loading, setLoading] = useState(true);
  const [msg, setMsg] = useState('');
  const [visitError, setVisitError] = useState('');
  const [savingVisit, setSavingVisit] = useState(false);
  const [retry, setRetry] = useState(0);

  useEffect(() => {
    if (!key) return;
    let active = true;
    setItem(fallbackContent.find(article => article.key === key));
    setLoading(true);
    setMsg('');
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
        if (active) {
          setLoading(false);
          setSavingVisit(false);
        }
      }
    }
    void load();
    return () => { active = false; };
  }, [key, fromTrack, userId, retry]);

  return <Screen title={item?.title || 'Artigo'}>
    <View style={{width: '100%', maxWidth: 760, alignSelf: 'center', gap: 16}}>
      {item ? <Card>
        <Txt muted size={14}>{item.category} · {item.level} · {item.minutes} min</Txt>
        {item.body.split(/\r?\n/).map((line, index) => {
          const text = line.trim();
          if (text.startsWith('## ')) {
            return <Txt key={index} bold size={21} style={{marginTop: 12}}>{text.slice(3)}</Txt>;
          }
          if (text.startsWith('- ')) {
            return <Txt key={index} style={{paddingLeft: 12}}>• {text.slice(2)}</Txt>;
          }
          return text ? <Txt key={index}>{text}</Txt> : null;
        })}
      </Card> : <Card><Txt>{key && loading ? 'Carregando artigo…' : 'Conteúdo indisponível.'}</Txt></Card>}
      {msg ? <Txt>{msg}</Txt> : null}
      <ErrorText message={visitError} />
      {visitError ? <Btn title="Tentar registrar abertura novamente" secondary onPress={() => setRetry(value => value + 1)} /> : null}
      {item && <Btn title="Salvar nos favoritos" onPress={async () => {
        if (!user) { router.push('/login'); return; }
        try {
          await api('/favorites', {method: 'POST', body: JSON.stringify({key})});
          setMsg('Adicionado aos favoritos!');
        } catch (error) {
          setMsg(error instanceof Error ? error.message : 'Não foi possível salvar o favorito.');
        }
      }} />}
      <Btn title={fromTrack === '1' ? 'Voltar à trilha' : 'Voltar aos artigos'} secondary disabled={savingVisit}
        onPress={() => {
          if (fromTrack === '1') {
            if (router.canGoBack()) router.back();
            else router.replace('/trilhas');
            return;
          }
          router.replace({pathname: '/conteudo', params: categoria ? {categoria} : {}});
        }} />
    </View>
  </Screen>;
}
