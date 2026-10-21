package com.aula.pos.comprasinternacionais.exception;

import org.springframework.http.HttpStatus;

public class CotacaoIndisponivelException extends ApiException {
    public CotacaoIndisponivelException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message);
    }
}
