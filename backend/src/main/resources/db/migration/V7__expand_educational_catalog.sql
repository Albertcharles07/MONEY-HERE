-- Atualiza os textos iniciais e preserva o conteúdo personalizado de uma instalação.
insert into educational_contents as content (content_key, title, category, level, content_type, minutes, body)
values
  ('orcamento-50-30-20', 'Para onde vai o seu dinheiro?', 'Orçamento', 'Básico', 'artigo', 4, $article$Um orçamento mostra quanto entra, quanto sai e o que sobra para suas prioridades. A regra 50/30/20 é um ponto de partida para organizar essas escolhas.
## Mapeie o mês
Anote a renda líquida, ou seja, o dinheiro que realmente entra. Reúna extratos, faturas e gastos em dinheiro. Separe necessidades, como moradia e alimentação, de desejos, como lazer e assinaturas. Reserve espaço para metas e pagamento de dívidas.
## Exemplo com R$ 3.000
- R$ 1.500 para necessidades pela divisão de referência.
- R$ 900 para desejos.
- R$ 600 para poupar ou reduzir dívidas.
Se as necessidades somarem R$ 1.950, elas ocupam 65% da renda. Ajuste os outros grupos ao valor disponível e use a divisão para observar sua evolução.
## Coloque em prática
Registre os últimos sete dias, agrupe os gastos e faça uma mudança que possa manter durante o mês. Na próxima revisão, compare o limite planejado com o gasto real.$article$),
  ('orcamento-fixos-e-variaveis', 'Gastos fixos, variáveis e anuais', 'Orçamento', 'Básico', 'artigo', 3, $article$Separe o que tem valor previsível, o que muda conforme o consumo e o que aparece poucas vezes por ano. Isso deixa o mês mais fácil de planejar.
## Três linhas no orçamento
- Fixos: aluguel e mensalidades costumam ter valores conhecidos.
- Variáveis: mercado, energia e transporte podem oscilar.
- Periódicos: manutenção, material escolar e renovação de serviços pedem planejamento antecipado.
Uma conta pode ser essencial e variável ao mesmo tempo. A classificação indica como estimar o valor.
## Exemplo de provisão
Um gasto previsto de R$ 720 daqui a seis meses equivale a R$ 120 por mês. No orçamento, registre a provisão mensal. Quando pagar, use o valor guardado e evite descontar a mesma despesa duas vezes no saldo disponível.
## Coloque em prática
Olhe os últimos dois meses e estime um limite para cada gasto variável. Liste também as três próximas despesas anuais e quando você precisará de cada valor.$article$),
  ('orcamento-renda-variavel', 'Como planejar com renda variável', 'Orçamento', 'Intermediário', 'artigo', 4, $article$Quem recebe por projeto, comissão ou trabalho autônomo pode ter meses bem diferentes. Um orçamento com renda de referência ajuda a distribuir o dinheiro entre eles.
## Defina uma renda de referência
Compare os valores líquidos recebidos nos últimos meses. Use um valor conservador para planejar gastos recorrentes. Para cada pagamento, separe os custos do trabalho e as obrigações que saem desse valor antes de calcular quanto está disponível para uso pessoal.
## Exemplo de três meses
Se você recebeu R$ 2.400, R$ 3.200 e R$ 2.800 líquidos, pode testar um orçamento com R$ 2.400. No mês de R$ 3.200, os R$ 800 adicionais podem ajudar a cobrir um mês mais fraco ou antecipar uma despesa prevista.
## Coloque em prática
Monte uma lista de gastos que precisam ser pagos mesmo em um mês fraco. Depois escolha como dividir a sobra dos meses melhores entre a reserva, as despesas futuras e suas metas.$article$),
  ('orcamento-metas', 'Transforme um objetivo em uma meta mensal', 'Orçamento', 'Básico', 'artigo', 3, $article$Um objetivo fica mais fácil de acompanhar quando tem valor, prazo e uma contribuição que cabe no orçamento.
## Do objetivo ao plano
Anote quanto a meta custa, quanto você já guardou e quando pretende alcançá-la. Subtraia o valor já guardado do alvo e divida a diferença pelo número de meses. Esse cálculo simples dá uma referência sem depender de rendimento.
## Exemplo de uma compra planejada
Uma meta de R$ 1.500 com R$ 300 guardados ainda precisa de R$ 1.200. Para alcançar em seis meses, planeje R$ 200 mensais. Se só couberem R$ 150, o prazo estimado passa a oito meses.
## Coloque em prática
Cadastre uma meta no painel. Escolha uma data no mês para revisar o saldo acumulado. Se o preço mudar ou um mês sair do plano, atualize o total, o prazo ou a contribuição.$article$),
  ('juros-compostos', 'Juros compostos sem mistério', 'Juros', 'Básico', 'artigo', 4, $article$Nos juros compostos, a taxa de cada período incide sobre o saldo acumulado. A mesma lógica pode aumentar o valor de uma aplicação ou de uma dívida sem pagamentos.
## Veja a conta acontecer
Com R$ 100 a 1% ao mês, sem movimentações, o primeiro mês termina em R$ 101. No segundo, 1% de R$ 101 é R$ 1,01. O saldo chega a R$ 102,01.
Nos juros simples, a taxa incide sempre sobre o valor inicial. Nesse exemplo, o segundo mês terminaria em R$ 102.
## O efeito do prazo
No começo, a diferença é pequena. Com mais períodos e taxas maiores, ela cresce. Use a mesma unidade para prazo e taxa. Por exemplo, uma taxa mensal deve ser aplicada ao número de meses.
## Coloque em prática
No simulador, teste R$ 100 a 1% ao mês, sem aportes, por 2 e 12 meses. Compare os saldos e separe o valor inicial dos juros acumulados.$article$),
  ('juros-taxa-mensal-anual', 'Taxa mensal e anual na mesma base', 'Juros', 'Intermediário', 'artigo', 4, $article$Para comparar duas taxas, coloque ambas no mesmo período. Multiplicar uma taxa mensal por 12 não dá a taxa anual efetiva quando existe capitalização composta.
## Como converter
Some 1 à taxa mensal em forma decimal, eleve o resultado à potência 12 e subtraia 1. Para 1% ao mês, a conta é 1,01 elevado a 12 menos 1, cerca de 12,68% ao ano.
## Exemplo de comparação
Uma opção com 1% ao mês tem taxa efetiva anual de cerca de 12,68%. Outra com 12% efetivos ao ano tem taxa menor nessa comparação. Para avaliar um crédito, examine também os custos incluídos no CET e o cronograma de pagamentos.
## Coloque em prática
Converta 2% ao mês usando 1,02 elevado a 12. Compare o resultado, cerca de 26,82% ao ano, com 24%, que seria a multiplicação simples.$article$),
  ('juros-cet', 'O que o CET mostra sobre um empréstimo', 'Juros', 'Intermediário', 'artigo', 4, $article$O Custo Efetivo Total, ou CET, reúne juros e outros custos da operação de crédito. Ele ajuda a comparar propostas com despesas diferentes.
## O que conferir
- Valor líquido que será liberado e data do recebimento.
- CET, quantidade de parcelas e datas de pagamento.
- Tarifas, encargos, tributos e seguros cobrados na operação.
Compare alternativas equivalentes e observe também quanto você efetivamente pagará até o fim.
## Exemplo de duas propostas
Imagine receber R$ 2.000 em ambas. Na primeira, você paga 10 parcelas de R$ 230, total de R$ 2.300. Na segunda, são 10 parcelas de R$ 220 e uma tarifa de R$ 150 paga à parte, total de R$ 2.350. A segunda tem parcela menor, mas custa R$ 50 a mais no total.
## Coloque em prática
Anote lado a lado valor liberado, CET, vencimentos e total pago em duas propostas fictícias. Explique qual informação cada número ajuda a responder.$article$),
  ('juros-inflacao', 'Inflação e o poder de compra', 'Juros', 'Básico', 'artigo', 3, $article$O saldo pode crescer e ainda assim comprar quase a mesma coisa. A inflação ajuda a medir quanto os preços mudam ao longo do tempo.
## Valor nominal e valor real
O percentual nominal mede a variação do dinheiro. O percentual real ajusta essa variação pela inflação do mesmo período. Para calcular, divida um mais a variação nominal por um mais a inflação e subtraia um.
## Exemplo com R$ 1.000
Se R$ 1.000 viram R$ 1.080 e a inflação do período é de 5%, o ganho real é de aproximadamente 2,86%, antes de custos ou impostos. Uma cesta que custava R$ 1.000 passaria, nesse exemplo, a custar R$ 1.050.
## Coloque em prática
Compare um aumento de renda de 4% com uma inflação hipotética de 6%. Faça a conta com 1,04 dividido por 1,06 para verificar se o poder de compra subiu ou caiu.$article$),
  ('reserva', 'Sua primeira reserva de emergência', 'Reserva', 'Básico', 'artigo', 4, $article$A reserva de emergência ajuda a pagar despesas essenciais quando aparece um imprevisto, como perda de renda ou um reparo urgente.
## Estime suas despesas essenciais
Some moradia, alimentação básica, saúde, transporte e compromissos indispensáveis. Escolha quantos meses quer cobrir levando em conta a estabilidade da renda e quantas pessoas dependem dela. Você pode começar com uma meta menor e ampliar aos poucos.
## Exemplo de dois marcos
Se o essencial custa R$ 1.800 por mês, um primeiro marco é juntar R$ 1.800. Uma meta de três meses soma R$ 5.400. Com R$ 300 por mês, o primeiro marco levaria seis meses, sem considerar rendimentos.
## Coloque em prática
Cadastre sua primeira meta e defina o que conta como emergência. Se precisar usar parte do dinheiro, registre o saque e inclua a reposição no orçamento dos meses seguintes.$article$),
  ('reserva-emergencia-ou-despesa', 'Emergência ou despesa planejada?', 'Reserva', 'Básico', 'artigo', 3, $article$Separar imprevistos de gastos que você já consegue antecipar ajuda a preservar a reserva para quando ela for necessária.
## Use três perguntas
- Eu sabia que essa despesa poderia chegar nesta época?
- Ela é essencial e precisa ser resolvida agora?
- Já tenho uma provisão separada para ela?
As respostas ajudam a escolher entre o orçamento do mês, uma provisão para gastos futuros ou a reserva.
## Exemplo de dois reparos
A manutenção anual de uma moto pode entrar em uma provisão. Se custar R$ 600 e estiver prevista para daqui a dez meses, são R$ 60 por mês. Um reparo urgente após uma avaria inesperada pode ter outro tratamento se o veículo for necessário para trabalhar.
## Coloque em prática
Classifique cinco despesas recentes como recorrentes, previsíveis ou emergenciais. Escolha uma previsível para começar a provisionar antes do próximo vencimento.$article$),
  ('reserva-liquidez', 'Liquidez da reserva na prática', 'Reserva', 'Intermediário', 'artigo', 3, $article$Liquidez indica a facilidade de transformar uma aplicação em dinheiro. Para uma reserva, vale entender quando o valor estará efetivamente disponível e quanto poderá oscilar.
## Leia a regra de resgate
Confira carência, horário de solicitação, dias úteis e prazo até o dinheiro chegar à conta. Avalie também o risco de resgatar por um valor menor do que o esperado. Produtos diferentes podem ter regras de liquidez diferentes.
## Exemplo de calendário
Se um resgate solicitado na sexta depois do horário de corte só for aceito na segunda e liquidado no próximo dia útil, o dinheiro pode chegar na terça. Uma emergência no sábado exige considerar esse intervalo.
## Coloque em prática
Desenhe dois cenários de saque, um em dia útil e outro antes de um feriado. Marque em qual dia você poderia usar o dinheiro em cada caso e quais condições poderiam atrasar o acesso.$article$),
  ('reserva-recompor', 'Como recompor a reserva depois de usar', 'Reserva', 'Básico', 'artigo', 3, $article$Usar a reserva em um imprevisto faz parte do plano. A recomposição começa pela diferença entre a meta e o valor que ainda está disponível.
## Recalcule o alvo
Atualize suas despesas essenciais e veja se o objetivo anterior ainda faz sentido. Defina uma contribuição que caiba no orçamento atual e acompanhe o progresso em etapas.
## Exemplo com um reparo
Uma reserva de R$ 4.000 que teve um saque de R$ 1.200 fica com R$ 2.800. Se a meta continuar em R$ 4.000, guardando R$ 200 por mês você recompõe o valor em seis meses, sem considerar rendimentos.
## Coloque em prática
Anote o saldo atual, a meta e a contribuição possível. Se a renda caiu, escolha um primeiro marco menor e recalcule o prazo quando o orçamento estabilizar.$article$),
  ('dividas', 'Como organizar dívidas', 'Dívidas', 'Intermediário', 'artigo', 4, $article$Uma lista de dívidas ajuda a decidir o que cabe pagar, o que precisa ser negociado e onde pagamentos extras podem fazer diferença.
## Monte o inventário
Registre credor, saldo devedor, parcela, vencimento, taxa e atraso. Separe o valor que precisa ficar disponível para despesas essenciais. Anote também consequências como a interrupção de um serviço relevante.
## Compare duas prioridades
Imagine uma dívida de R$ 800 com juros de 2% ao mês e outra de R$ 1.500 a 5% ao mês. Priorizar a maior taxa tende a reduzir juros quando as demais condições são comparáveis. Priorizar o menor saldo pode encerrar um compromisso mais rapidamente. Considere as obrigações de cada contrato e os riscos de atraso.
## Coloque em prática
Selecione até três dívidas. Compare a ordem por taxa com a ordem por saldo. Escolha qual pagamento adicional cabe no mês e registre a data em que vai revisar o plano.$article$),
  ('dividas-negociacao', 'Como preparar uma renegociação', 'Dívidas', 'Básico', 'artigo', 4, $article$Antes de aceitar uma proposta, descubra qual compromisso mensal cabe no orçamento e quanto será pago até o fim.
## Prepare a conversa
Confirme o credor e os canais oficiais de atendimento. Peça saldo atualizado, valor de entrada, parcelas, vencimentos, total pago e condições em caso de atraso. Guarde a proposta e o comprovante do acordo.
## Exemplo de capacidade de pagamento
Se após as despesas essenciais sobram R$ 300, uma parcela de R$ 290 deixa margem pequena para variações. Compare com uma opção de R$ 220 e veja também quanto ela aumenta o total, se tiver prazo maior.
## Coloque em prática
Monte duas propostas fictícias e coloque em uma tabela o valor de entrada, o total e as datas. Faça um teste com uma despesa inesperada de R$ 80 no mês e veja se cada parcela ainda cabe.$article$),
  ('dividas-cartao', 'Cartão de crédito e compromissos futuros', 'Dívidas', 'Básico', 'artigo', 4, $article$O cartão reúne compras em uma fatura. Controlar as parcelas futuras ajuda a saber quanto da renda dos próximos meses já está comprometido.
## Leia a fatura em duas partes
Veja o total que vence agora e a lista de parcelas que ainda serão cobradas. Se considerar financiar o saldo da fatura, compare encargos, prazo e total das opções.
## Exemplo de meses sobrepostos
Uma compra de R$ 600 em seis parcelas de R$ 100 coincide com outra de R$ 240 em três parcelas de R$ 80. Por três meses, elas somam R$ 180 por mês, além de todas as outras compras. Para enxergar isso antes de comprar, reserve o espaço no orçamento futuro.
## Coloque em prática
Monte uma tabela de três meses com as parcelas já contratadas. Para uma nova compra hipotética, preencha os meses afetados e veja se você ainda consegue pagar a fatura integral.$article$),
  ('dividas-parcela-ou-total', 'Parcela pequena, custo total maior?', 'Dívidas', 'Intermediário', 'artigo', 3, $article$Uma parcela mais baixa pode facilitar o fluxo mensal. Um prazo maior pode aumentar o total pago. Compare os dois efeitos antes de avaliar uma proposta.
## Exemplo com a mesma entrada
Imagine duas opções para o mesmo valor liberado. A primeira cobra 12 parcelas de R$ 260, total de R$ 3.120. A segunda cobra 18 parcelas de R$ 190, total de R$ 3.420. A segunda libera R$ 70 por mês durante os primeiros 12 meses e custa R$ 300 a mais no total.
## Verifique as condições
Compare CET, entrada, prazo e multas. Confira também o valor líquido que você recebe. Use a parcela para avaliar o orçamento e o total para entender o impacto da operação completa.
## Coloque em prática
Escolha um limite mensal hipotético e marque quais opções cabem. Se nenhuma couber, calcule quanto seria possível comprometer sem sacrificar as despesas essenciais.$article$),
  ('investir', 'Investir começa com planejamento', 'Investimentos', 'Básico', 'artigo', 4, $article$Escolher um investimento começa pelo objetivo, pelo prazo e pelo risco que você consegue assumir. Essas perguntas dão sentido às comparações de rentabilidade.
## Faça três perguntas
- Quando vou precisar do dinheiro?
- Como funciona o resgate e em quanto tempo recebo?
- O valor pode cair ou ficar indisponível quando eu precisar?
Compare também custos e os tributos que se aplicam à situação.
## Exemplo de dois objetivos
Uma mudança daqui a três meses e uma meta de longo prazo têm exigências diferentes. Se o dinheiro precisa estar disponível em data certa, atrasos ou perdas no resgate podem afetar a mudança. Em prazos maiores, você pode analisar cenários diferentes de oscilação e recuperação.
## Coloque em prática
Liste duas metas com datas distintas. Para cada uma, escreva qual prazo de resgate seria aceitável e como reagiria se o valor caísse perto da data de uso.$article$),
  ('investimentos-renda-fixa', 'Renda fixa e renda variável', 'Investimentos', 'Básico', 'artigo', 4, $article$Em renda fixa, a regra de remuneração é definida no início. Na renda variável, o retorno depende do desempenho do ativo e pode mudar ao longo do tempo. As duas têm riscos.
## Entenda as diferenças
Um título de renda fixa pode combinar taxa, índice e vencimento. Vendas antecipadas podem ocorrer por um preço diferente do esperado. Na renda variável, o preço pode oscilar conforme o mercado e as condições do negócio.
## Exemplo de prazo
Imagine um título comprado para vencer em três anos. Uma venda após seis meses pode ocorrer em condições diferentes das previstas para o vencimento. Em uma ação, a mudança de preço também pode produzir ganho ou perda no período.
## Coloque em prática
Compare dois produtos fictícios. Identifique quem promete pagar, como o retorno é calculado, se existe prazo de vencimento e o que pode acontecer se você precisar sair antes.$article$),
  ('investimentos-diversificacao', 'Como funciona a diversificação', 'Investimentos', 'Intermediário', 'artigo', 3, $article$Diversificar é distribuir o dinheiro entre riscos diferentes. A ideia é reduzir o impacto que um único ativo, emissor ou setor pode ter no resultado.
## Exposição em comum
Ter vários produtos pode manter uma concentração se todos dependerem do mesmo emissor ou responderem de forma parecida à economia. Observe o que cada produto tem por trás e quais riscos eles compartilham.
## Exemplo de impacto
Se uma posição representa 100% de R$ 1.000 e cai 20%, a queda é de R$ 200. Se ela representa só metade e o restante fica estável no mesmo período, a queda total é de R$ 100. O resultado muda se as duas posições caírem juntas.
## Coloque em prática
Desenhe uma carteira fictícia com três posições. Teste a queda de uma posição e depois de duas ao mesmo tempo. Compare o efeito no total e identifique onde ficou a concentração.$article$),
  ('investimentos-custos', 'Rentabilidade bruta e líquida', 'Investimentos', 'Intermediário', 'artigo', 3, $article$O rendimento bruto é medido antes de custos e tributos. Comparar o valor líquido ajuda a entender quanto sobra para o seu objetivo.
## O que colocar na conta
Compare alternativas com o mesmo valor inicial, prazo e datas de entrada e saída. Verifique taxas, tributos aplicáveis e condições de resgate. Compare o valor líquido com a inflação do mesmo período se quiser estimar o ganho real.
## Exemplo com R$ 1.000
Em um cenário hipotético, a opção A rende R$ 100 brutos e tem R$ 25 em custos e tributos, sobrando R$ 75 de ganho. A opção B rende R$ 90 brutos e tem R$ 10 de descontos, sobrando R$ 80. O resultado líquido depende dos valores efetivamente cobrados.
## Coloque em prática
Monte uma tabela com capital inicial, rendimento bruto, custos e saldo líquido. Aplique a tabela a duas opções fictícias com o mesmo prazo.$article$)
on conflict (content_key) do update
set minutes = excluded.minutes, body = excluded.body
where (content.content_key, content.body) in (
  ('orcamento-50-30-20', 'Anote receitas e despesas. Uma referência é separar até 50% para necessidades, 30% para desejos e 20% para poupar ou quitar dívidas. Adapte à sua realidade.'),
  ('juros-compostos', 'Juros compostos são os juros sobre o saldo acumulado. Com R$ 100 e 1% ao mês, após dois meses o saldo é R$ 102,01, sem novas contribuições.'),
  ('reserva', 'Comece estimando suas despesas essenciais mensais. Uma reserva pode cobrir de 3 a 12 meses conforme sua estabilidade e necessidades.'),
  ('dividas', 'Liste saldos, taxas e parcelas. Compare a estratégia de menor saldo com a de maior taxa. Evite contrair crédito caro para pagar dívida barata.'),
  ('investir', 'Antes de investir, avalie riscos, liquidez, prazos e custos. Rentabilidades futuras não são garantidas.')
);
