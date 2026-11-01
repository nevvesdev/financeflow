ALTER TABLE lancamentos
    ADD COLUMN natureza VARCHAR(10) NOT NULL DEFAULT 'VARIAVEL',
    ADD CONSTRAINT ck_lancamentos_natureza CHECK (natureza IN ('FIXA', 'VARIAVEL'));
