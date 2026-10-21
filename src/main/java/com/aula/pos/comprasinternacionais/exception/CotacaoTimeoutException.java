package com.aula.pos.comprasinternacionais.exception;

import org.springframework.http.HttpStatus;

public class CotacaoTimeoutException extends ApiException {
    public CotacaoTimeoutException(String message) {
        super(HttpStatus.GATEWAY_TIMEOUT, message);
    }
}