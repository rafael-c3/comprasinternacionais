package com.aula.pos.comprasinternacionais.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "awesomeapi", url = "${integrations.awesomeapi.url}")
public interface AwesomeApiClient {

    @GetMapping("/json/last/USD-BRL")
    CotacaoResponse buscarCotacaoUsdBrl();
}
