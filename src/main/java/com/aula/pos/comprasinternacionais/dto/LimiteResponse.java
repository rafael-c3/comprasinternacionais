package com.aula.pos.comprasinternacionais.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record LimiteResponse(
        UUID clienteId,
        String mesReferencia,
        BigDecimal limiteMensal,
        BigDecimal totalUtilizado,
        BigDecimal saldoDisponivel
) {
}
