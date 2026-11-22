export const fallbackContent=[
 {key:'orcamento-50-30-20',title:'Para onde vai o seu dinheiro?',category:'Orçamento',level:'Básico',minutes:6,body:'Registre quanto recebe e quanto gasta. Separe necessidades, desejos e poupança de acordo com a sua realidade.'},
 {key:'juros-compostos',title:'Juros compostos sem mistério',category:'Juros',level:'Básico',minutes:5,body:'Juros compostos significam juros sobre o total acumulado. Exemplo: R$ 100 a 1% ao mês vira R$ 102,01 em dois meses.'},
 {key:'reserva',title:'Monte sua primeira reserva',category:'Reserva',level:'Básico',minutes:7,body:'Uma reserva serve para imprevistos. Planeje quantos meses de despesas essenciais você deseja cobrir.'},
 {key:'dividas',title:'Dívidas: como começar a sair',category:'Dívidas',level:'Intermediário',minutes:8,body:'Liste suas dívidas, taxas e parcelas. Priorize pagamentos com estratégia, sem esquecer despesas essenciais.'},
 {key:'investir',title:'Seu primeiro investimento',category:'Investimentos',level:'Básico',minutes:7,body:'Investimentos têm riscos. Compare liquidez, prazos, taxas e custos antes de decidir.'},
];
export const tracks=[{key:'fundamentos',title:'Fundamentos de Finanças',modules:fallbackContent.map(c=>c.key)},{key:'criancas',title:'Pequenos Exploradores',modules:['orcamento-50-30-20','reserva','juros-compostos']}];
