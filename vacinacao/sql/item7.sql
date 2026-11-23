-- =====================================================================
-- Item 7 - Nova funcionalidade (alterações no banco)
-- Executar DEPOIS de schema.sql e ANTES de dados.sql:
--   mysql -u root -p vacinacao < sql/item7.sql
--
-- 7A - Alerta de contraindicação por alergia:
--      nova tabela N:N vacina_alergia (alergias que contraindicam uma vacina).
--      Nenhuma tabela existente é alterada.
-- 7B - Controle de doses e próximas aplicações:
--      usa a coluna agendas.dose (já criada em schema.sql); apenas um índice
--      para as consultas por série (usuário + vacina + dose).
-- =====================================================================

USE vacinacao;

DROP TABLE IF EXISTS vacina_alergia;

-- Contraindicações: alergias que impedem/alertam a aplicação de uma vacina
CREATE TABLE vacina_alergia (
    vacina_id  INT NOT NULL,
    alergia_id INT NOT NULL,
    PRIMARY KEY (vacina_id, alergia_id),
    FOREIGN KEY (vacina_id)  REFERENCES vacinas(id) ON DELETE CASCADE,
    FOREIGN KEY (alergia_id) REFERENCES alergias(id)
);

-- 7B: consultas de série (usuário + vacina, ordenadas por dose)
CREATE INDEX idx_agendas_serie ON agendas (usuario_id, vacina_id, dose);
