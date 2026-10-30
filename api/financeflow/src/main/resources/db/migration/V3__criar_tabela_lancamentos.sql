CREATE TABLE lancamentos (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL,
    categoria_id UUID NOT NULL,
    descricao VARCHAR(160) NOT NULL,
    valor NUMERIC(19, 2) NOT NULL,
    tipo VARCHAR(10) NOT NULL,
    data_lancamento DATE NOT NULL,
    data_vencimento DATE,
    paga BOOLEAN NOT NULL DEFAULT FALSE,
    data_pagamento DATE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_lancamentos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_lancamentos_categoria FOREIGN KEY (categoria_id) REFERENCES categorias (id) ON DELETE RESTRICT,
    CONSTRAINT ck_lancamentos_valor CHECK (valor > 0),
    CONSTRAINT ck_lancamentos_tipo CHECK (tipo IN ('RECEITA', 'DESPESA')),
    CONSTRAINT ck_lancamentos_pagamento CHECK (
        (paga AND data_pagamento IS NOT NULL) OR (NOT paga AND data_pagamento IS NULL)
        )
);

CREATE INDEX ix_lancamentos_usuario_data ON lancamentos (usuario_id, data_lancamento DESC);

CREATE INDEX ix_lancamentos_abertos_vencimento ON lancamentos (data_vencimento, usuario_id)
    WHERE paga = FALSE AND data_vencimento IS NOT NULL;
