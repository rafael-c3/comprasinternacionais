CREATE TABLE tb_clientes (
                             id BINARY(16) NOT NULL,
                             nome VARCHAR(150) NOT NULL,
                             email VARCHAR(150) NOT NULL,

                             PRIMARY KEY (id)
);

CREATE TABLE tb_compras (
                            id BINARY(16) NOT NULL,
                            valor_usd DECIMAL(19,2) NOT NULL,
                            cotacao_utilizada DECIMAL(19,4) NOT NULL,
                            valor_convertido DECIMAL(19,2) NOT NULL,
                            taxa_servico DECIMAL(19,2) NOT NULL,
                            tributo_simulado DECIMAL(19,2) NOT NULL,
                            valor_final_brl DECIMAL(19,2) NOT NULL,
                            data_cotacao DATE NOT NULL,
                            data_compra DATETIME(6) NOT NULL,
                            cliente_id BINARY(16) NOT NULL,

                            PRIMARY KEY (id),
                            CONSTRAINT fk_compras_cliente FOREIGN KEY (cliente_id) REFERENCES tb_clientes (id)
);

CREATE INDEX idx_compras_cliente_data ON tb_compras (cliente_id, data_compra);