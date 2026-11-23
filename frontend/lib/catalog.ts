import articles from './articles.json';

export type Article = {
  key: string;
  title: string;
  category: string;
  level: string;
  minutes: number;
  body: string;
};

export const fallbackContent: Article[] = articles;

export const tracks = [
  {key: 'fundamentos', title: 'Fundamentos de Finanças', modules: ['orcamento-50-30-20', 'juros-compostos', 'reserva', 'dividas', 'investir']},
  {key: 'criancas', title: 'Dinheiro para pequenos exploradores', modules: ['orcamento-50-30-20', 'reserva', 'juros-compostos']},
];

export function articleSummary(body: string) {
  return body.split(/\r?\n/, 1)[0].trim();
}

export function normalizeSearch(value: string) {
  return value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().trim();
}
