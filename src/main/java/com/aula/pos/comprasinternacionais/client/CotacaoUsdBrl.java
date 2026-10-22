package com.aula.pos.comprasinternacionais.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CotacaoUsdBrl(
        String code,
        String codein,
        String name,
        String bid,
        String ask,
        String high,
        String low,
        String timestamp,
        @JsonProperty("create_date") String createDate
) {
}