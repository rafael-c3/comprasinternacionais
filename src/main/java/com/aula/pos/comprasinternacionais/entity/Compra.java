package com.aula.pos.comprasinternacionais.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_compras")
public class Compra {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "valor_usd", nullable = false, precision = 19, scale = 2)
    private BigDecimal valorUSD;
    @Column(name = "cotacao_utilizada", nullable = false, precision = 19, scale = 4)
    private BigDecimal cotacaoUtilizada;
    @Column(name = "valor_convertido", nullable = false, precision = 19, scale = 2)
    private BigDecimal valorConvertido;
    @Column(name = "taxa_servico", nullable = false, precision = 19, scale = 2)
    private BigDecimal taxaServico;
    @Column(name = "tributo_simulado", nullable = false, precision = 19, scale = 2)
    private BigDecimal tributoSimulado;
    @Column(name = "valor_final_brl", nullable = false, precision = 19, scale = 2)
    private BigDecimal valorFinalBRL;
    @Column(name = "data_cotacao", nullable = false)
    private LocalDate dataCotacao;
    @Column(name = "data_compra", nullable = false)
    private LocalDateTime dataCompra;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
}