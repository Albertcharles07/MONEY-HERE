import React, { useEffect, useState } from 'react';
import { router } from 'expo-router';
import { Screen, Card, Btn, Txt, ErrorText } from '../../components/UI';
import { api } from '../../lib/api';
import { fallbackContent } from '../../lib/catalog';
import { useSession } from '../../lib/session';

export default function Favorites() {
  const { user } = useSession();

  return (
    <Screen title="Meus favoritos" protectedPage>
      {user && <FavoritesList />}
    </Screen>
  );
}

function FavoritesList() {
  const [list, setList] = useState<string[] | null>(null);
  const [loadFailed, setLoadFailed] = useState(false);
  const [removeFailed, setRemoveFailed] = useState(false);

  async function load() {
    setLoadFailed(false);
    try {
      setList(await api('/favorites'));
    } catch {
      setLoadFailed(true);
    }
  }

  useEffect(() => {
    void load();
  }, []);

  async function remove(key: string) {
    setRemoveFailed(false);
    try {
      await api('/favorites/' + key, { method: 'DELETE' });
      setList(current => current?.filter(item => item !== key) ?? null);
    } catch {
      setRemoveFailed(true);
    }
  }

  if (loadFailed) {
    return (
      <Card>
        <ErrorText message="Não foi possível carregar seus favoritos." />
        <Btn title="Tentar novamente" onPress={load} />
      </Card>
    );
  }

  if (list === null) {
    return <Txt muted>Carregando favoritos…</Txt>;
  }

  if (list.length === 0) {
    return (
      <Card>
        <Txt bold>Você ainda não tem favoritos.</Txt>
        <Txt muted>Explore os conteúdos e salve os que quiser ler de novo.</Txt>
        <Btn title="Explorar conteúdos" onPress={() => router.push('/conteudo')} />
      </Card>
    );
  }

  return (
    <>
      <ErrorText message={removeFailed ? 'Não foi possível remover o favorito. Tente novamente.' : ''} />
      {list.map(key => (
        <Card key={key}>
          <Txt bold>{fallbackContent.find(content => content.key === key)?.title || key}</Txt>
          <Btn
            title="Abrir conteúdo"
            onPress={() => router.push({ pathname: '/artigo/[key]', params: { key } })}
          />
          <Btn title="Remover" secondary onPress={() => remove(key)} />
        </Card>
      ))}
    </>
  );
}
