-- Catálogo editorial: migração de artigos e trilhas para o único banco PostgreSQL.
create table educational_contents (
 id bigserial primary key,
 content_key varchar(100) not null unique,
 title varchar(250) not null,
 category varchar(80) not null,
 level varchar(30) not null,
 content_type varchar(30) not null default 'artigo',
 minutes integer not null check(minutes > 0),
 body text not null,
 url text
);
create index ix_educational_contents_category on educational_contents(category);

create table educational_tracks (
 id bigserial primary key,
 track_key varchar(100) not null unique,
 title varchar(250) not null
);
create table educational_track_modules (
 track_id bigint not null references educational_tracks(id) on delete cascade,
 content_key varchar(100) not null references educational_contents(content_key) on delete cascade,
 position integer not null check(position > 0),
 primary key(track_id, content_key),
 unique(track_id, position)
);

insert into educational_contents(content_key,title,category,level,content_type,minutes,body) values
('orcamento-50-30-20','Para onde vai o seu dinheiro?','Orçamento','Básico','artigo',6,'Anote receitas e despesas. Uma referência é separar até 50% para necessidades, 30% para desejos e 20% para poupar ou quitar dívidas. Adapte à sua realidade.'),
('juros-compostos','Juros compostos sem mistério','Juros','Básico','artigo',5,'Juros compostos são os juros sobre o saldo acumulado. Com R$ 100 e 1% ao mês, após dois meses o saldo é R$ 102,01, sem novas contribuições.'),
('reserva','Sua primeira reserva de emergência','Reserva','Básico','artigo',7,'Comece estimando suas despesas essenciais mensais. Uma reserva pode cobrir de 3 a 12 meses conforme sua estabilidade e necessidades.'),
('dividas','Como organizar dívidas','Dívidas','Intermediário','artigo',8,'Liste saldos, taxas e parcelas. Compare a estratégia de menor saldo com a de maior taxa. Evite contrair crédito caro para pagar dívida barata.'),
('investir','Investir começa com planejamento','Investimentos','Básico','artigo',7,'Antes de investir, avalie riscos, liquidez, prazos e custos. Rentabilidades futuras não são garantidas.');

insert into educational_tracks(track_key,title) values
('fundamentos','Fundamentos de Finanças'),
('criancas','Dinheiro para pequenos exploradores');

insert into educational_track_modules(track_id,content_key,position)
select t.id, v.content_key, v.position from educational_tracks t join (values
('fundamentos','orcamento-50-30-20',1),
('fundamentos','juros-compostos',2),
('fundamentos','reserva',3),
('fundamentos','dividas',4),
('fundamentos','investir',5),
('criancas','orcamento-50-30-20',1),
('criancas','reserva',2),
('criancas','juros-compostos',3)
) as v(track_key,content_key,position) on t.track_key = v.track_key;
