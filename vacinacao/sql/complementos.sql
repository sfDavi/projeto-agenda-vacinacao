-- =====================================================================
-- Complementos ao modelo: contraindicações por alergia e controle de doses
-- Executar DEPOIS de schema.sql e ANTES de dados.sql:
--   mysql -u root -p vacinacao < sql/complementos.sql
--
-- * Contraindicações por alergia: nova tabela N:N vacina_alergia
--   (alergias que contraindicam uma vacina). Nenhuma tabela existente é alterada.
-- * Controle de doses: usa a coluna agendas.dose (criada em schema.sql);
--   aqui há apenas um índice para as consultas por série (usuário + vacina + dose).
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

-- Controle de doses: consultas de série (usuário + vacina, ordenadas por dose)
CREATE INDEX idx_agendas_serie ON agendas (usuario_id, vacina_id, dose);
