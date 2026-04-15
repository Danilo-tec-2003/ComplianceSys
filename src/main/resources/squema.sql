-- ESQUEMA SQL PARA GW Compliance System (Jornada do Caminhoneiro)

CREATE TABLE ponto (
    id BIGSERIAL PRIMARY KEY,
    motorista_id BIGINT NOT NULL,
    registro TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    data_criacao TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
    mensagem_conformidade VARCHAR(1000)
);

-- CRIAÇÃO DE ÍNDICES
CREATE INDEX idx_ponto_motorista_registro
ON ponto (motorista_id, registro);

