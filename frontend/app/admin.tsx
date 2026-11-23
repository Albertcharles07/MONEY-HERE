import React, {useEffect, useState} from 'react';
import {Switch, View} from 'react-native';
import {Btn, Card, ErrorText, Input, Screen, Txt} from '../components/UI';
import {api} from '../lib/api';
import {useSession} from '../lib/session';

type Account = {id: number; name: string; email: string; enabled: boolean; role: 'USER' | 'ADMIN'; primaryAdmin: boolean};
type Accounts = {content: Account[]; first: boolean; last: boolean};
type Action = 'status' | 'role' | 'password' | 'delete';

const accountActions: readonly Action[] = ['status', 'role', 'password', 'delete'];

function actionLabel(account: Account, action: Action) {
  if (action === 'password') return 'Resetar senha';
  if (action === 'delete') return 'Apagar usuário';
  if (action === 'role') return account.role === 'ADMIN' ? 'Remover acesso de admin' : 'Tornar administrador';
  return account.enabled ? 'Desativar' : 'Reativar';
}

export default function Admin() {
  const {user} = useSession();
  const isAdmin = user?.role === 'ADMIN' && !user.mustChangePassword;
  const [accounts, setAccounts] = useState<Accounts>();
  const [page, setPage] = useState(0);
  const [revision, setRevision] = useState(0);
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [consent, setConsent] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const [search, setSearch] = useState('');
  const [menu, setMenu] = useState<number>();
  const [selection, setSelection] = useState<{account: Account; action: Action}>();
  const [temporaryPassword, setTemporaryPassword] = useState('');

  useEffect(() => {
    if (!isAdmin) return;
    let active = true;
    const timer = setTimeout(() => {
      api('/admin/users?page=' + page + '&search=' + encodeURIComponent(search.trim()))
        .then(result => {
          if (!active) return;
          if (page && !result.content.length) setPage(page - 1);
          else setAccounts(result);
        })
        .catch(failure => { if (active) setError(failure.message); });
    }, 200);
    return () => { active = false; clearTimeout(timer); };
  }, [isAdmin, page, revision, search]);

  async function mutate(request: () => Promise<void>) {
    setBusy(true);
    setError('');
    setMessage('');
    try {
      await request();
      setRevision(value => value + 1);
    } catch (failure) {
      setError(failure instanceof Error ? failure.message : 'Falha ao atualizar a conta.');
    } finally {
      setBusy(false);
    }
  }

  async function create() {
    await mutate(async () => {
      await api('/admin/users', {
        method: 'POST', body: JSON.stringify({name, email: email.trim(), password, consent}),
      });
      setName('');
      setEmail('');
      setPassword('');
      setConsent(false);
      setPage(0);
      setMessage('Conta criada.');
    });
  }

  function clearSelection() {
    setSelection(undefined);
    setTemporaryPassword('');
    setError('');
  }

  async function confirm() {
    if (!selection) return;
    const {account, action} = selection;
    const path = '/admin/users/' + account.id;
    await mutate(async () => {
      if (action === 'delete') {
        await api(path, {method: 'DELETE'});
      } else {
        const body = action === 'status' ? {enabled: !account.enabled}
          : action === 'role' ? {role: account.role === 'ADMIN' ? 'USER' : 'ADMIN'}
          : {password: temporaryPassword};
        await api(path + '/' + action, {method: 'PUT', body: JSON.stringify(body)});
      }
      clearSelection();
      setMessage(action === 'password' ? 'Senha temporária definida. A pessoa deverá trocá-la no próximo login.' : 'Alteração concluída.');
    });
  }

  return (
    <Screen title="Administração de usuários" protectedPage>
      {isAdmin ? <>
        <Card>
          <Txt bold>Criar usuário</Txt>
          <Input label="Nome" value={name} onChangeText={setName} />
          <Input label="E-mail" value={email} onChangeText={setEmail} keyboardType="email-address" />
          <Input label="Senha inicial (mínimo 8 caracteres)" value={password} onChangeText={setPassword} secureTextEntry />
          <Txt>A pessoa aceitou os Termos e a Política de Privacidade</Txt>
          <Switch accessibilityLabel="Aceite da pessoa" value={consent} onValueChange={setConsent} />
          <Btn title="Criar usuário" disabled={busy || !consent} onPress={create} />
          {!selection && <ErrorText message={error} />}
        </Card>
        <Input label="Pesquisar usuários" placeholder="Nome ou e-mail" value={search}
          onChangeText={value => {
            setSearch(value);
            setPage(0);
            setAccounts(undefined);
            setMenu(undefined);
            clearSelection();
          }} />
        {message ? <Txt>{message}</Txt> : null}
        {accounts && !accounts.content.length ? <Txt muted>Nenhum usuário encontrado.</Txt> : null}
        {accounts?.content.map(account => (
          <Card key={account.id}>
            <Txt bold>{account.name}</Txt>
            <Txt>{account.email}</Txt>
            <Txt muted>{account.primaryAdmin ? 'Administrador principal' :
              [account.role === 'ADMIN' && 'Administrador', account.enabled ? 'Conta ativa' : 'Conta desativada']
                .filter(Boolean).join(' · ')}</Txt>
            {!account.primaryAdmin && account.id !== user.id && (
              <Btn title={menu === account.id ? 'Opções ▴' : 'Opções ▾'} secondary disabled={busy} onPress={() => {
                setMenu(menu === account.id ? undefined : account.id);
                clearSelection();
              }} />
            )}
            {!account.primaryAdmin && menu === account.id && (
              <View accessibilityLabel="Opções do usuário" style={{flexDirection: 'row', flexWrap: 'wrap', gap: 8}}>
                {accountActions
                  .map(action => <Btn key={action} title={actionLabel(account, action)} secondary danger={action === 'delete'}
                    onPress={() => {
                      clearSelection();
                      setSelection({account, action});
                      setMenu(undefined);
                      setMessage('');
                    }} />)}
              </View>
            )}
            {!account.primaryAdmin && selection?.account.id === account.id && (
              <View style={{gap: 8}}>
                <Txt bold>{actionLabel(account, selection.action)}</Txt>
                {selection.action === 'password' && <Input label="Senha temporária (mínimo 12 caracteres)"
                  value={temporaryPassword} onChangeText={setTemporaryPassword} secureTextEntry />}
                {selection.action === 'delete' && <Txt>Apagar esta conta também remove os dados associados.</Txt>}
                <ErrorText message={error} />
                <Btn title="Confirmar" danger={selection.action === 'delete'} onPress={confirm}
                  disabled={busy || (selection.action === 'password' && temporaryPassword.length < 12)} />
                <Btn title="Cancelar" secondary disabled={busy} onPress={clearSelection} />
              </View>
            )}
          </Card>
        ))}
        {accounts && !accounts.first && <Btn title="Página anterior" onPress={() => setPage(value => Math.max(0, value - 1))} />}
        {accounts && !accounts.last && <Btn title="Próxima página" onPress={() => setPage(value => value + 1)} />}
      </> : <Txt>Acesso restrito a administradores.</Txt>}
    </Screen>
  );
}
