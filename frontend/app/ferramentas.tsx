import React, {useState} from 'react';
import {Pressable, View} from 'react-native';
import {Btn, Card, ErrorText, Input, Screen, Txt} from '../components/UI';
import {formatBRL, maskBRL, parseBRL, toInputBRL, validateAmount} from '../lib/currency';

const calc = require('../lib/calculators.cjs');

type Field = {label: string; initial: number; currency?: 'nonnegative' | 'positive'};
const compoundFields: Field[] = [
  {label: 'Valor inicial (R$)', initial: 1000, currency: 'nonnegative'},
  {label: 'Aporte mensal (R$)', initial: 100, currency: 'nonnegative'},
  {label: 'Prazo (anos)', initial: 5},
  {label: 'Taxa (% ao mês)', initial: 1},
];

const modes: {name: string; fields: Field[]}[] = [
  {name: 'Juros compostos', fields: compoundFields},
  {name: 'Reserva de emergência', fields: [
    {label: 'Gastos essenciais/mês (R$)', initial: 1000, currency: 'nonnegative'},
    {label: 'Meses de reserva', initial: 100},
    {label: 'Já guardado (R$)', initial: 5, currency: 'nonnegative'},
  ]},
  {name: 'Quitação de dívidas', fields: [
    {label: 'Saldo da dívida (R$)', initial: 1000, currency: 'positive'},
    {label: 'Juros (% ao ano)', initial: 100},
    {label: 'Parcela mensal (R$)', initial: 5, currency: 'positive'},
  ]},
  {name: 'Aposentadoria', fields: [
    {label: 'Meta de patrimônio (R$)', initial: 1000, currency: 'nonnegative'},
    {label: 'Valor já investido (R$)', initial: 100, currency: 'nonnegative'},
    {label: 'Prazo (anos)', initial: 5},
    {label: 'Taxa (% ao ano)', initial: 1},
  ]},
  {name: 'Comparativo de investimentos', fields: compoundFields},
  {name: 'Custo do crédito', fields: [
    {label: 'Preço à vista (R$)', initial: 1000, currency: 'positive'},
    {label: 'Número de parcelas', initial: 100},
    {label: 'Valor de cada parcela (R$)', initial: 5, currency: 'positive'},
  ]},
];

function readValue(field: Field, input: string) {
  if (field.currency) {
    const allowZero = field.currency === 'nonnegative';
    const value = allowZero && !input.trim() ? '0,00' : input;
    const error = validateAmount(value, allowZero);
    if (error) throw Error(`${field.label}: ${error}`);
    return parseBRL(value);
  }

  const value = Number(input.replace(',', '.'));
  if (!Number.isFinite(value)) throw Error(`${field.label}: informe um número válido.`);
  return value;
}

function simulate(mode: number, [x, y, z, w]: number[]) {
  switch (mode) {
    case 0:
    case 4: {
      const result = calc.compound(x, y, z * 12, w / 100);
      const summary = `Saldo final: ${formatBRL(result.total)} | Aportes: ${formatBRL(result.invested)} | Rendimento estimado: ${formatBRL(result.earnings)}. Simulação nominal, sem impostos e taxas.`;
      return mode === 4 ? summary + ' Altere a taxa para comparar cenários; isto não representa cotações reais.' : summary;
    }
    case 1: {
      const result = calc.emergency(x, y, z);
      return `Reserva alvo: ${formatBRL(result.target)} | Falta guardar: ${formatBRL(result.remaining)}`;
    }
    case 2: {
      const result = calc.debt(x, y, z);
      return `Prazo estimado: ${result.periods} meses | Juros: ${formatBRL(result.interest)} | Total pago: ${formatBRL(result.total)}. Cálculo de parcelas fixas; não é SAC nem algoritmo bola de neve.`;
    }
    case 3:
      return `Aporte mensal necessário: ${formatBRL(calc.retirement(x, y, z, w))}. Estimativa nominal de patrimônio, não de renda mensal vitalícia.`;
    case 5: {
      const result = calc.credit(x, y, z);
      return `Custo total: ${formatBRL(result.total)} | Diferença para preço à vista: ${formatBRL(result.extra)}. Não inclui tarifas extras.`;
    }
    default:
      throw Error('Selecione uma ferramenta.');
  }
}

export default function Tools() {
  const [mode, setMode] = useState(0);
  const [drafts, setDrafts] = useState<Partial<Record<number, string[]>>>({});
  const [result, setResult] = useState('');
  const [error, setError] = useState('');
  const {name, fields} = modes[mode];
  const defaults = fields.map(field => field.currency ? toInputBRL(field.initial) : String(field.initial));
  const values = drafts[mode] ?? defaults;

  function updateField(index: number, input: string) {
    setDrafts(previous => {
      const next = [...(previous[mode] ?? defaults)];
      next[index] = fields[index].currency ? maskBRL(input, next[index]) : input;
      return {...previous, [mode]: next};
    });
  }

  function calculate() {
    setError('');
    setResult('');
    try {
      const inputs = fields.map((field, index) => readValue(field, values[index]));
      setResult(simulate(mode, inputs));
    } catch (failure) {
      setError(failure instanceof Error ? failure.message : 'Não foi possível calcular.');
    }
  }

  return (
    <Screen title="Ferramentas financeiras" protectedPage>
      <Txt muted>Simulações educativas; resultados não garantem rentabilidade e não substituem orientação profissional.</Txt>
      <View style={{flexDirection: 'row', gap: 10, flexWrap: 'wrap'}}>
        {modes.map((tool, index) => (
          <Pressable key={tool.name} onPress={() => {
            if (index === mode) return;
            setMode(index);
            setResult('');
            setError('');
          }}>
            <Txt bold={mode === index}>{mode === index ? '● ' : ''}{tool.name}</Txt>
          </Pressable>
        ))}
      </View>
      <Card style={{maxWidth: 720}}>
        <Txt size={23} bold>{name}</Txt>
        <Txt size={13} muted>Informe os valores e toque em calcular. Não usamos taxas de mercado ao vivo.</Txt>
        {fields.map((field, index) => (
          <Input key={field.label} label={field.label} value={values[index]}
            placeholder={field.currency ? '0,00' : undefined}
            keyboardType={field.currency ? 'number-pad' : 'decimal-pad'}
            onChangeText={input => updateField(index, input)} />
        ))}
        <ErrorText message={error} />
        <Btn title="Calcular" onPress={calculate} />
        {result ? <Card><Txt bold>Resultado</Txt><Txt>{result}</Txt></Card> : null}
      </Card>
    </Screen>
  );
}
