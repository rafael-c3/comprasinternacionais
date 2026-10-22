package com.aula.pos.comprasinternacionais.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CompraRequest(
        @NotNull(message = "Id do cliente é obrigatório")
        UUID clienteId,
        @NotNull(message = "Valor em dólar é obrigatório")
        @Positive(message = "Valor em dólar deve ser maior que zero")
        @Digits(integer = 15, fraction = 2, message = "Valor em dólar deve ter no máximo 2 casas decimais")
        BigDecimal valorUSD
) {
}
