package com.aula.pos.comprasinternacionais.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CompraResponse(
        UUID id,
        UUID clienteId,
        BigDecimal valorUSD,
        BigDecimal cotacaoUtilizada,
        BigDecimal valorConvertido,
        BigDecimal taxaServico,
        BigDecimal tributoSimulado,
        BigDecimal valorFinalBRL,
        LocalDate dataCotacao,
        LocalDateTime dataCompra
) {
}
