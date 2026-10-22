package com.aula.pos.comprasinternacionais.service;

import com.aula.pos.comprasinternacionais.client.AwesomeApiClient;
import com.aula.pos.comprasinternacionais.client.CotacaoResponse;
import com.aula.pos.comprasinternacionais.client.CotacaoUsdBrl;
import com.aula.pos.comprasinternacionais.dto.Cotacao;
import com.aula.pos.comprasinternacionais.exception.CotacaoIndisponivelException;
import com.aula.pos.comprasinternacionais.exception.CotacaoInvalidaException;
import com.aula.pos.comprasinternacionais.exception.CotacaoTimeoutException;
import feign.FeignException;
import feign.RetryableException;
import feign.codec.DecodeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CotacaoService {

    private static final DateTimeFormatter FORMATO_DATA_API =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AwesomeApiClient awesomeApiClient;

    public Cotacao buscarCotacaoVenda() {
        return interpretar(consultarApi());
    }

    private CotacaoResponse consultarApi() {
        try {
            return awesomeApiClient.buscarCotacaoUsdBrl();
        } catch (RetryableException e) {
            if (causaEhTimeout(e)) {
                throw new CotacaoTimeoutException("Tempo esgotado ao consultar a cotação do dólar");
            }
            throw new CotacaoIndisponivelException("Serviço de cotação indisponível no momento");
        } catch (DecodeException e) {
            throw new CotacaoInvalidaException("Resposta da cotação em formato inválido");
        } catch (FeignException e) {
            log.warn("AwesomeAPI respondeu com status {}", e.status());
            if (e.status() == 504) {
                throw new CotacaoTimeoutException("Tempo esgotado ao consultar a cotação do dólar");
            }
            throw new CotacaoIndisponivelException("Serviço de cotação indisponível no momento");
        } catch (RuntimeException e) {
            log.warn("Falha inesperada ao consultar a AwesomeAPI", e);
            throw new CotacaoIndisponivelException("Serviço de cotação indisponível no momento");
        }
    }

    private Cotacao interpretar(CotacaoResponse resposta) {
        if (resposta == null || resposta.usdbrl() == null) {
            throw new CotacaoInvalidaException("Cotação USDBRL ausente na resposta");
        }
        CotacaoUsdBrl usd = resposta.usdbrl();

        if (usd.ask() == null || usd.ask().isBlank()) {
            throw new CotacaoInvalidaException("Cotação de venda (ask) ausente na resposta");
        }
        if (usd.createDate() == null || usd.createDate().isBlank()) {
            throw new CotacaoInvalidaException("Data da cotação (create_date) ausente na resposta");
        }

        BigDecimal ask;
        try {
            ask = new BigDecimal(usd.ask().trim());
        } catch (NumberFormatException e) {
            throw new CotacaoInvalidaException("Cotação de venda (ask) inválida");
        }
        if (ask.signum() <= 0) {
            throw new CotacaoInvalidaException("Cotação de venda (ask) deve ser maior que zero");
        }

        LocalDateTime dataHora;
        try {
            dataHora = LocalDateTime.parse(usd.createDate().trim(), FORMATO_DATA_API);
        } catch (DateTimeParseException e) {
            throw new CotacaoInvalidaException("Data da cotação (create_date) inválida");
        }

        return new Cotacao(ask, dataHora);
    }

    private boolean causaEhTimeout(Throwable erro) {
        Throwable atual = erro;
        while (atual != null) {
            if (atual instanceof SocketTimeoutException || atual instanceof HttpTimeoutException) {
                return true;
            }
            atual = atual.getCause();
        }
        return false;
    }
}