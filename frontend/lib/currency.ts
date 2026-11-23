const brl = new Intl.NumberFormat('pt-BR', {style: 'currency', currency: 'BRL'});
const decimal = new Intl.NumberFormat('pt-BR', {minimumFractionDigits: 2, maximumFractionDigits: 2});
const maxAmount = 9_999_999_999_999.99;

export function formatBRL(value: number | string) {
  return brl.format(Number(value));
}

export function toInputBRL(value: number | string) {
  return decimal.format(Number(value));
}

export function maskBRL(value: string, previous = '') {
  if (value.includes('-')) return previous;
  const digits = value.replace(/\D/g, '');
  if (!digits || (previous === '0,00' && value.length < previous.length && !/[1-9]/.test(digits))) {
    return '';
  }

  const cents = digits.replace(/^0+(?=\d)/, '').padStart(3, '0');
  const whole = cents.slice(0, -2).replace(/\B(?=(\d{3})+(?!\d))/g, '.');
  return whole + ',' + cents.slice(-2);
}

export function parseBRL(value: string) {
  const amount = value.trim();
  if (!/^(?:\d+|\d{1,3}(?:\.\d{3})+)(?:,\d{1,2})?$/.test(amount)) return NaN;
  return Number(amount.replace(/\./g, '').replace(',', '.'));
}

export function validateAmount(value: string, allowZero = false) {
  if (!value.trim()) return 'Informe o valor.';
  const amount = parseBRL(value);
  if (!Number.isFinite(amount)) return 'Informe um valor válido em reais.';
  if (amount === 0 && !allowZero) return 'O valor precisa ser maior que R$ 0,00.';
  if (amount > maxAmount) return `O valor máximo é ${formatBRL(maxAmount)}.`;
  return null;
}
