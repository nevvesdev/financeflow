CREATE TABLE categorias (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL,
    nome VARCHAR(60) NOT NULL,
    tipo VARCHAR(10) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_categorias_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT ck_categorias_tipo CHECK (tipo IN ('RECEITA', 'DESPESA'))
);

CREATE UNIQUE INDEX uk_categorias_usuario_tipo_nome ON categorias (usuario_id, tipo, lower(nome));
