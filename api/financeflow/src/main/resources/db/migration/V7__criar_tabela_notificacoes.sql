CREATE TABLE notificacoes (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL,
    lancamento_id UUID NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    mensagem VARCHAR(300) NOT NULL,
    criada_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    enviada_em TIMESTAMPTZ,
    lida_em TIMESTAMPTZ,
    CONSTRAINT fk_notificacoes_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_notificacoes_lancamento FOREIGN KEY (lancamento_id) REFERENCES lancamentos (id) ON DELETE CASCADE,
    CONSTRAINT ck_notificacoes_tipo CHECK (tipo IN ('VENCE_EM_BREVE', 'VENCE_HOJE', 'VENCIDA')),
    CONSTRAINT uk_notificacoes_lancamento_tipo UNIQUE (lancamento_id, tipo)
);

CREATE INDEX ix_notificacoes_usuario_criada ON notificacoes (usuario_id, criada_em DESC);

CREATE INDEX ix_notificacoes_pendentes_envio ON notificacoes (criada_em) WHERE enviada_em IS NULL;

CREATE INDEX ix_notificacoes_nao_lidas ON notificacoes (usuario_id) WHERE lida_em IS NULL;
