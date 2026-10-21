package com.aula.pos.comprasinternacionais.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class ClienteNaoEncontradoException extends ApiException {
    public ClienteNaoEncontradoException(UUID id) {
        super(HttpStatus.NOT_FOUND, "Cliente não encontrado: " + id);
    }
}
