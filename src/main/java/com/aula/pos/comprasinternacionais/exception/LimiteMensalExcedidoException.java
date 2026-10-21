package com.aula.pos.comprasinternacionais.exception;

import org.springframework.http.HttpStatus;

public class LimiteMensalExcedidoException extends ApiException {
    public LimiteMensalExcedidoException(String message) {
        super(HttpStatus.UNPROCESSABLE_CONTENT, message);
    }
}
