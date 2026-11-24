-- =====================================================================
-- Agendamento de Vacinas - esquema do banco (MER + adequações)
-- Banco: MySQL, schema `vacinacao`
--
-- Adequações ao MER (justificadas no README):
--   * usuario_id / vacina_id em agendas  -> relacionamentos 1:N do MER
--   * tabela usuario_alergia             -> relacionamento N:N do MER
--   * coluna dose em agendas             -> número da dose de cada agenda
--   * situacao guarda AGENDADO/REALIZADO/CANCELADO (enum Java, STRING)
--   * hora como CHAR(5) "HH:mm"
--   * CHECK de coerência situacao x data_situacao
--
-- A tabela vacina_alergia (contraindicações por alergia) fica em complementos.sql.
-- Executar: mysql -u root -p < sql/schema.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS vacinacao CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE vacinacao;

-- Permite reexecutar o script do zero (ordem respeita as FKs)
DROP TABLE IF EXISTS vacina_alergia;
DROP TABLE IF EXISTS agendas;
DROP TABLE IF EXISTS usuario_alergia;
DROP TABLE IF EXISTS usuarios;
DROP TABLE IF EXISTS alergias;
DROP TABLE IF EXISTS vacinas;

CREATE TABLE vacinas (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    titulo        VARCHAR(60)  NOT NULL,
    descricao     VARCHAR(200),
    doses         INT          NOT NULL CHECK (doses >= 1),
    periodicidade INT          CHECK (periodicidade BETWEEN 1 AND 4), -- 1 dia, 2 semanas, 3 meses, 4 anos
    intervalo     INT          CHECK (intervalo >= 1)
);

CREATE TABLE alergias (
    id   INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(40) NOT NULL UNIQUE
);

CREATE TABLE usuarios (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    nome            VARCHAR(60) NOT NULL,
    data_nascimento DATE        NOT NULL,
    sexo            CHAR(1)     NOT NULL CHECK (sexo IN ('M','F')),
    logradouro      VARCHAR(60),
    numero          NUMERIC(10),
    setor           VARCHAR(40),
    cidade          VARCHAR(40),
    uf              CHAR(2)     NOT NULL
);

-- N:N Usuarios x Alergias (relacionamento do MER)
CREATE TABLE usuario_alergia (
    usuario_id INT NOT NULL,
    alergia_id INT NOT NULL,
    PRIMARY KEY (usuario_id, alergia_id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (alergia_id) REFERENCES alergias(id)
);

CREATE TABLE agendas (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    data          DATE         NOT NULL,
    hora          CHAR(5)      NOT NULL,          -- "HH:mm"
    situacao      CHAR(10)     NOT NULL DEFAULT 'AGENDADO',
    data_situacao DATE,
    observacoes   VARCHAR(200),
    usuario_id    INT          NOT NULL,
    vacina_id     INT          NOT NULL,
    dose          INT          NOT NULL,          -- adequação: número da dose
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (vacina_id)  REFERENCES vacinas(id),
    CHECK (situacao IN ('AGENDADO','REALIZADO','CANCELADO')),
    CHECK ((situacao = 'AGENDADO' AND data_situacao IS NULL)
        OR (situacao <> 'AGENDADO' AND data_situacao IS NOT NULL))
);
