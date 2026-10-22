package com.aula.pos.comprasinternacionais.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SimulacaoResponse(
        BigDecimal valorUSD,
        BigDecimal cotacaoUtilizada,
        BigDecimal valorConvertido,
        BigDecimal taxaServico,
        BigDecimal tributoSimulado,
        BigDecimal valorFinalBRL,
        LocalDate dataCotacao
) {
}
