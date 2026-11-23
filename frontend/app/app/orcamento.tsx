import React, {useEffect, useState} from 'react';
import {Screen, Card, Input, Btn, Txt, ErrorText} from '../../components/UI';
import {api} from '../../lib/api';
import {formatBRL, maskBRL, parseBRL, toInputBRL, validateAmount} from '../../lib/currency';
import {useSession} from '../../lib/session';

type EntryKind = 'INCOME' | 'EXPENSE';
type Entry = {id: number; title: string; category: string; kind: EntryKind; amount: number | string; entryDate: string};

export default function Budget() {
  const {user} = useSession();
  const [entries, setEntries] = useState<Entry[]>([]);
  const [id, setId] = useState<number | null>(null);
  const [title, setTitle] = useState('');
  const [category, setCategory] = useState('Alimentação');
  const [kind, setKind] = useState<EntryKind>('EXPENSE');
  const [amount, setAmount] = useState('');
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10));
  const [error, setError] = useState('');

  function load() {
    return api('/budget').then(setEntries).catch((failure: Error) => setError(failure.message));
  }

  useEffect(() => {
    if (user) void load();
  }, [user]);

  function reset() {
    setId(null);
    setTitle('');
    setAmount('');
  }

  async function save() {
    const validation = validateAmount(amount);
    if (validation) {
      setError(validation);
      return;
    }

    try {
      await api(id === null ? '/budget' : '/budget/' + id, {
        method: id === null ? 'POST' : 'PUT',
        body: JSON.stringify({title, category, kind, amount: parseBRL(amount), entryDate: date}),
      });
      reset();
      setError('');
      await load();
    } catch (failure) {
      setError(failure instanceof Error ? failure.message : 'Não foi possível salvar o lançamento.');
    }
  }

  function edit(entry: Entry) {
    setId(entry.id);
    setTitle(entry.title);
    setCategory(entry.category);
    setAmount(toInputBRL(entry.amount));
    setDate(entry.entryDate);
    setKind(entry.kind);
    setError('');
  }

  function totalInCents(entryKind: EntryKind) {
    return entries.filter(entry => entry.kind === entryKind)
      .reduce((sum, entry) => sum + Math.round(Number(entry.amount) * 100), 0);
  }

  const income = totalInCents('INCOME');
  const expenses = totalInCents('EXPENSE');

  return (
    <Screen title="Meu orçamento" protectedPage>
      <Card>
        <Txt size={21} bold>Resumo</Txt>
        <Txt>Receitas: {formatBRL(income / 100)} | Despesas: {formatBRL(expenses / 100)}</Txt>
        <Txt bold>Saldo: {formatBRL((income - expenses) / 100)}</Txt>
      </Card>
      <Card>
        <Txt size={19} bold>{id === null ? 'Novo lançamento' : 'Editar lançamento'}</Txt>
        <Btn title={kind === 'EXPENSE' ? 'Tipo: despesa' : 'Tipo: receita'} secondary
          onPress={() => setKind(value => value === 'EXPENSE' ? 'INCOME' : 'EXPENSE')} />
        <Input label="Descrição" value={title} onChangeText={setTitle} />
        <Input label="Categoria" value={category} onChangeText={setCategory} />
        <Input label="Valor (R$)" value={amount} placeholder="0,00" keyboardType="number-pad"
          onChangeText={value => setAmount(previous => maskBRL(value, previous))} />
        <Input label="Data (AAAA-MM-DD)" value={date} onChangeText={setDate} />
        <ErrorText message={error} />
        <Btn title={id === null ? 'Adicionar' : 'Salvar'} onPress={save} />
      </Card>
      {entries.map(entry => (
        <Card key={entry.id}>
          <Txt bold>{entry.kind === 'INCOME' ? '🟢 Receita' : '🔴 Despesa'}: {entry.title}</Txt>
          <Txt>{formatBRL(entry.amount)} • {entry.category} • {entry.entryDate}</Txt>
          <Btn title="Editar" secondary onPress={() => edit(entry)} />
          <Btn title="Excluir" danger onPress={async () => {
            try {
              await api('/budget/' + entry.id, {method: 'DELETE'});
              await load();
            } catch (failure) {
              setError(failure instanceof Error ? failure.message : 'Não foi possível excluir o lançamento.');
            }
          }} />
        </Card>
      ))}
    </Screen>
  );
}
