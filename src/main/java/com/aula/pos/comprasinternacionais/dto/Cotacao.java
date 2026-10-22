package com.aula.pos.comprasinternacionais.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Cotacao(
        BigDecimal valorVenda,
        LocalDateTime dataHora
) {
}
