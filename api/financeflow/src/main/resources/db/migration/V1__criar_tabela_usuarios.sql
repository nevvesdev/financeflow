CREATE TABLE usuarios (
    id UUID PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(160) NOT NULL,
    senha_hash VARCHAR(100) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_usuarios_email UNIQUE (email)
);
