-- =====================================================================
-- Carga de exemplo. Executar depois de schema.sql e item7.sql:
--   mysql -u root -p vacinacao < sql/dados.sql
-- As datas das agendas são relativas a CURDATE() para que sempre haja
-- agendas "de hoje", passadas (com baixa) e futuras.
-- =====================================================================

USE vacinacao;

INSERT INTO alergias (id, nome) VALUES
    (1, 'Ovo'),
    (2, 'Gelatina'),
    (3, 'Látex'),
    (4, 'Neomicina'),
    (5, 'Levedura');

-- periodicidade: 1 = dias, 2 = semanas, 3 = meses, 4 = anos
INSERT INTO vacinas (id, titulo, descricao, doses, periodicidade, intervalo) VALUES
    (1, 'Febre Amarela',  'Dose única para adultos',                           1, NULL, NULL),
    (2, 'Hepatite B',     'Esquema de 3 doses',                                 3, 3,    1),
    (3, 'HPV',            'Esquema de 2 doses com 6 meses de intervalo',        2, 3,    6),
    (4, 'Tríplice Viral', 'Sarampo, caxumba e rubéola',                         2, 3,    1),
    (5, 'Dupla Adulto',   'Difteria e tétano - reforço a cada 10 anos',         5, 4,    3),
    (6, 'Antirrábica',    'Esquema pós-exposição',                              4, 1,    3);

-- Item 7A: contraindicações (tabela de item7.sql)
INSERT INTO vacina_alergia (vacina_id, alergia_id) VALUES
    (1, 1),   -- Febre Amarela x Ovo
    (1, 2),   -- Febre Amarela x Gelatina
    (4, 2),   -- Tríplice Viral x Gelatina
    (4, 4);   -- Tríplice Viral x Neomicina

INSERT INTO usuarios (id, nome, data_nascimento, sexo, logradouro, numero, setor, cidade, uf) VALUES
    (1, 'Ana Beatriz Souza',   '1995-03-14', 'F', 'Rua 10',           120, 'Setor Oeste',     'Goiânia',           'GO'),
    (2, 'João Pedro Almeida',  '1988-11-02', 'M', 'Avenida T-63',     455, 'Setor Bueno',     'Goiânia',           'GO'),
    (3, 'Mariana Conceição',   '2010-07-21', 'F', 'Rua das Acácias',   33, 'Jardim América',  'Aparecida de Goiânia', 'GO'),
    (4, 'Carlos Eduardo Lima', '1972-01-30', 'M', 'Quadra 104 Sul',     8, 'Asa Sul',         'Brasília',          'DF');

INSERT INTO usuario_alergia (usuario_id, alergia_id) VALUES
    (2, 1),
    (2, 4),
    (3, 2);

-- Hepatite B (3 doses, mensal, intervalo 1) para Ana: 1ª realizada, 2ª hoje, 3ª futura
INSERT INTO agendas (data, hora, situacao, data_situacao, observacoes, usuario_id, vacina_id, dose) VALUES
    (DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '09:00', 'REALIZADO', DATE_SUB(CURDATE(), INTERVAL 1 MONTH), NULL, 1, 2, 1),
    (CURDATE(),                             '09:00', 'AGENDADO',  NULL,                                   NULL, 1, 2, 2),
    (DATE_ADD(CURDATE(), INTERVAL 1 MONTH), '09:00', 'AGENDADO',  NULL,                                   NULL, 1, 2, 3);

-- Febre Amarela (dose única) para João: realizada hoje
INSERT INTO agendas (data, hora, situacao, data_situacao, observacoes, usuario_id, vacina_id, dose) VALUES
    (CURDATE(), '08:30', 'REALIZADO', CURDATE(), NULL, 2, 1, 1);

-- HPV (2 doses, mensal, intervalo 6) para Mariana: 1ª cancelada hoje, 2ª futura
INSERT INTO agendas (data, hora, situacao, data_situacao, observacoes, usuario_id, vacina_id, dose) VALUES
    (CURDATE(),                             '10:15', 'CANCELADO', CURDATE(), 'Paciente com febre', 3, 3, 1),
    (DATE_ADD(CURDATE(), INTERVAL 6 MONTH), '10:15', 'AGENDADO',  NULL,      NULL,                 3, 3, 2);

-- Dupla Adulto (5 doses, anual, intervalo 3) para Carlos: exemplo do enunciado
INSERT INTO agendas (data, hora, situacao, data_situacao, observacoes, usuario_id, vacina_id, dose) VALUES
    (CURDATE(),                              '14:00', 'AGENDADO', NULL, NULL, 4, 5, 1),
    (DATE_ADD(CURDATE(), INTERVAL 3 YEAR),   '14:00', 'AGENDADO', NULL, NULL, 4, 5, 2),
    (DATE_ADD(CURDATE(), INTERVAL 6 YEAR),   '14:00', 'AGENDADO', NULL, NULL, 4, 5, 3),
    (DATE_ADD(CURDATE(), INTERVAL 9 YEAR),   '14:00', 'AGENDADO', NULL, NULL, 4, 5, 4),
    (DATE_ADD(CURDATE(), INTERVAL 12 YEAR),  '14:00', 'AGENDADO', NULL, NULL, 4, 5, 5);

-- Tríplice Viral (2 doses, mensal, intervalo 1) para João: 1ª atrasada sem baixa
INSERT INTO agendas (data, hora, situacao, data_situacao, observacoes, usuario_id, vacina_id, dose) VALUES
    (DATE_SUB(CURDATE(), INTERVAL 5 DAY),   '11:00', 'AGENDADO', NULL, NULL, 2, 4, 1),
    (DATE_ADD(CURDATE(), INTERVAL 26 DAY),  '11:00', 'AGENDADO', NULL, NULL, 2, 4, 2);

-- Item 7B: Antirrábica (4 doses, a cada 3 dias) para Carlos. A 1ª dose foi aplicada com 6 dias de
-- atraso; a 2ª ficou antes da data mínima (aplicação + 3 dias) -> o sistema sugere recalcular.
INSERT INTO agendas (data, hora, situacao, data_situacao, observacoes, usuario_id, vacina_id, dose) VALUES
    (DATE_SUB(CURDATE(), INTERVAL 10 DAY), '15:00', 'REALIZADO', DATE_SUB(CURDATE(), INTERVAL 4 DAY), NULL, 4, 6, 1),
    (DATE_SUB(CURDATE(), INTERVAL 7 DAY),  '15:00', 'AGENDADO',  NULL, NULL, 4, 6, 2),
    (DATE_SUB(CURDATE(), INTERVAL 4 DAY),  '15:00', 'AGENDADO',  NULL, NULL, 4, 6, 3),
    (DATE_SUB(CURDATE(), INTERVAL 1 DAY),  '15:00', 'AGENDADO',  NULL, NULL, 4, 6, 4);
