package com.aula.pos.comprasinternacionais.exception;

import org.springframework.http.HttpStatus;

public class CotacaoInvalidaException extends ApiException {
    public CotacaoInvalidaException(String message) {
        super(HttpStatus.UNPROCESSABLE_CONTENT, message);
    }
}