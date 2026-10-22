package com.aula.pos.comprasinternacionais.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CotacaoResponse(
        @JsonProperty("USDBRL") CotacaoUsdBrl usdbrl
) {
}
