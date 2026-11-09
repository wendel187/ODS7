CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    codigo VARCHAR(30) NOT NULL,
    nome VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_codigo UNIQUE (codigo)
);

CREATE TABLE IF NOT EXISTS aparelhos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(80) NOT NULL,
    potencia_watts DECIMAL(10, 2) NOT NULL,
    horas_por_dia DECIMAL(5, 2) NOT NULL,
    dias_por_mes INTEGER NOT NULL,
    usuario_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_aparelhos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE TABLE IF NOT EXISTS configuracoes_energia (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tarifa_kwh DECIMAL(10, 4),
    usuario_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_configuracoes_usuario UNIQUE (usuario_id),
    CONSTRAINT fk_configuracoes_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE TABLE IF NOT EXISTS quiz_perguntas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    categoria VARCHAR(40) NOT NULL,
    enunciado VARCHAR(500) NOT NULL,
    explicacao VARCHAR(1000) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS quiz_alternativas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    texto VARCHAR(300) NOT NULL,
    correta BOOLEAN NOT NULL,
    pergunta_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_quiz_alternativas_pergunta FOREIGN KEY (pergunta_id) REFERENCES quiz_perguntas (id)
);

CREATE TABLE IF NOT EXISTS quiz_tentativas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    iniciada_em TIMESTAMP(6) NOT NULL,
    finalizada_em TIMESTAMP(6) NULL,
    acertos INTEGER NOT NULL,
    pontuacao INTEGER NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_quiz_tentativas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE TABLE IF NOT EXISTS quiz_tentativa_perguntas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tentativa_id BIGINT NOT NULL,
    pergunta_id BIGINT NOT NULL,
    ordem INTEGER NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_quiz_tentativa_ordem UNIQUE (tentativa_id, ordem),
    CONSTRAINT uk_quiz_tentativa_pergunta UNIQUE (tentativa_id, pergunta_id),
    CONSTRAINT fk_quiz_tentativa_perguntas_tentativa FOREIGN KEY (tentativa_id) REFERENCES quiz_tentativas (id),
    CONSTRAINT fk_quiz_tentativa_perguntas_pergunta FOREIGN KEY (pergunta_id) REFERENCES quiz_perguntas (id)
);

CREATE TABLE IF NOT EXISTS quiz_respostas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tentativa_pergunta_id BIGINT NOT NULL,
    alternativa_id BIGINT NOT NULL,
    correta BOOLEAN NOT NULL,
    respondida_em TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_quiz_respostas_tentativa_pergunta UNIQUE (tentativa_pergunta_id),
    CONSTRAINT fk_quiz_respostas_tentativa_pergunta FOREIGN KEY (tentativa_pergunta_id) REFERENCES quiz_tentativa_perguntas (id),
    CONSTRAINT fk_quiz_respostas_alternativa FOREIGN KEY (alternativa_id) REFERENCES quiz_alternativas (id)
);
