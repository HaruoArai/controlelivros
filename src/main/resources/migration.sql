-- Converte a coluna status de ENUM para VARCHAR(30).
-- Necessário porque o H2 criou a coluna como ENUM com 4 valores fixos
-- e não aceita PRORROGACAO_SOLICITADA sem essa migração.
ALTER TABLE emprestimos ALTER COLUMN status VARCHAR(30) NOT NULL DEFAULT 'PENDENTE';
