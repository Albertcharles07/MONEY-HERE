import React, {useEffect, useState} from 'react';
import {Screen, Card, Input, Btn, Txt, ErrorText} from '../../components/UI';
import {api} from '../../lib/api';
import {formatBRL, maskBRL, parseBRL, toInputBRL, validateAmount} from '../../lib/currency';
import {useSession} from '../../lib/session';

type Goal = {id: number; title: string; targetAmount: number | string; currentAmount: number | string; deadline: string | null};

function parseDeadline(value: string) {
  const brazilianDate = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec(value);
  const iso = brazilianDate ? `${brazilianDate[3]}-${brazilianDate[2]}-${brazilianDate[1]}` : value;
  if (!/^\d{4}-\d{2}-\d{2}$/.test(iso) || iso.startsWith('0000-')) return null;

  const date = new Date(`${iso}T00:00:00Z`);
  return Number.isFinite(date.getTime()) && date.toISOString().slice(0, 10) === iso ? iso : null;
}

function formatDeadline(value: string | null) {
  return value?.replace(/^(\d{4})-(\d{2})-(\d{2})$/, '$3/$2/$1') ?? '';
}

export default function Goals() {
  const {user} = useSession();
  const [goals, setGoals] = useState<Goal[]>([]);
  const [id, setId] = useState<number | null>(null);
  const [title, setTitle] = useState('');
  const [target, setTarget] = useState(toInputBRL(1000));
  const [current, setCurrent] = useState(toInputBRL(0));
  const [deadline, setDeadline] = useState('');
  const [error, setError] = useState('');

  function load() {
    return api('/goals').then(setGoals).catch((failure: Error) => setError(failure.message));
  }

  useEffect(() => {
    if (user) void load();
  }, [user]);

  function reset() {
    setId(null);
    setTitle('');
    setTarget(toInputBRL(1000));
    setCurrent(toInputBRL(0));
    setDeadline('');
    setError('');
  }

  async function save() {
    const currentValue = current || '0,00';
    const targetError = validateAmount(target);
    const currentError = validateAmount(currentValue, true);
    if (targetError || currentError) {
      setError(targetError ? 'Valor alvo: ' + targetError : 'Valor já acumulado: ' + currentError);
      return;
    }

    const deadlineInput = deadline.trim();
    const parsedDeadline = parseDeadline(deadlineInput);
    if (deadlineInput && !parsedDeadline) {
      setError('Informe um prazo válido no formato DD/MM/AAAA, como 02/05/2028.');
      return;
    }

    try {
      await api(id === null ? '/goals' : '/goals/' + id, {
        method: id === null ? 'POST' : 'PUT',
        body: JSON.stringify({
          title,
          targetAmount: parseBRL(target),
          currentAmount: parseBRL(currentValue),
          deadline: parsedDeadline,
        }),
      });
      reset();
      await load();
    } catch (failure) {
      setError(failure instanceof Error ? failure.message : 'Não foi possível salvar a meta.');
    }
  }

  function edit(goal: Goal) {
    setId(goal.id);
    setTitle(goal.title);
    setTarget(toInputBRL(goal.targetAmount));
    setCurrent(toInputBRL(goal.currentAmount));
    setDeadline(formatDeadline(goal.deadline));
    setError('');
  }

  return (
    <Screen title="Minhas metas" protectedPage>
      <Card>
        <Txt size={20} bold>{id === null ? 'Criar meta' : 'Editar meta'}</Txt>
        <Input label="Objetivo" value={title} onChangeText={setTitle} />
        <Input label="Valor alvo (R$)" value={target} placeholder="0,00" keyboardType="number-pad"
          onChangeText={value => setTarget(previous => maskBRL(value, previous))} />
        <Input label="Valor já acumulado (R$)" value={current} placeholder="0,00" keyboardType="number-pad"
          onChangeText={value => setCurrent(previous => maskBRL(value, previous))} />
        <Input label="Prazo (opcional)" placeholder="DD/MM/AAAA" value={deadline} onChangeText={setDeadline} />
        <ErrorText message={error} />
        <Btn title={id === null ? 'Adicionar meta' : 'Salvar edição'} onPress={save} />
        {id !== null && <Btn title="Cancelar edição" secondary onPress={reset} />}
      </Card>
      {goals.map(goal => (
        <Card key={goal.id}>
          <Txt size={20} bold>{goal.title}</Txt>
          <Txt>{formatBRL(goal.currentAmount)} de {formatBRL(goal.targetAmount)} • {Math.min(100, Math.round(Number(goal.currentAmount) / Number(goal.targetAmount) * 100))}%</Txt>
          <Txt muted>Prazo: {formatDeadline(goal.deadline) || 'Sem prazo definido'}</Txt>
          <Btn title="Editar" secondary onPress={() => edit(goal)} />
          <Btn title="Excluir" danger onPress={async () => {
            try {
              await api('/goals/' + goal.id, {method: 'DELETE'});
              await load();
            } catch (failure) {
              setError(failure instanceof Error ? failure.message : 'Não foi possível excluir a meta.');
            }
          }} />
        </Card>
      ))}
    </Screen>
  );
}
