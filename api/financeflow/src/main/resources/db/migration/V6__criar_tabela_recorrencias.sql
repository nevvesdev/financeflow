CREATE TABLE recorrencias (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL,
    categoria_id UUID NOT NULL,
    descricao VARCHAR(160) NOT NULL,
    valor_estimado NUMERIC(19, 2) NOT NULL,
    dia_vencimento INTEGER NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    ultima_competencia_gerada DATE,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_recorrencias_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_recorrencias_categoria FOREIGN KEY (categoria_id) REFERENCES categorias (id) ON DELETE RESTRICT,
    CONSTRAINT ck_recorrencias_valor CHECK (valor_estimado > 0),
    CONSTRAINT ck_recorrencias_dia CHECK (dia_vencimento BETWEEN 1 AND 31),
    CONSTRAINT ck_recorrencias_periodo CHECK (data_fim IS NULL OR data_fim >= data_inicio)
);

CREATE INDEX ix_recorrencias_usuario ON recorrencias (usuario_id);

CREATE INDEX ix_recorrencias_categoria ON recorrencias (categoria_id);
