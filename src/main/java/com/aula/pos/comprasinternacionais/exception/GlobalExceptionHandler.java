package com.aula.pos.comprasinternacionais.exception;

import com.aula.pos.comprasinternacionais.dto.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Exceções do projeto: 404, 422, 503 e 504
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException e) {
        return montar(e.getStatus(), e.getMessage(), List.of());
    }

    // Falha no @Valid: 400 com o detalhe de cada campo
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidacao(MethodArgumentNotValidException e) {
        List<ApiError.CampoInvalido> detalhes = e.getBindingResult().getFieldErrors().stream()
                .map(f -> new ApiError.CampoInvalido(f.getField(), f.getDefaultMessage()))
                .toList();
        return montar(HttpStatus.BAD_REQUEST, "Dados de entrada inválidos", detalhes);
    }

    // JSON ausente ou mal formatado: 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleJsonInvalido(HttpMessageNotReadableException e) {
        return montar(HttpStatus.BAD_REQUEST, "Corpo da requisição ausente ou mal formatado", List.of());
    }

    // Id que não é um UUID válido na URL: 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTipoInvalido(MethodArgumentTypeMismatchException e) {
        return montar(HttpStatus.BAD_REQUEST, "Valor inválido para o parâmetro '" + e.getName() + "'", List.of());
    }

    // Qualquer outro erro: 500, sem expor detalhes internos
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenerico(Exception e) {
        // Erros do próprio Spring (rota inexistente, método não permitido etc.) mantêm o status deles
        if (e instanceof org.springframework.web.ErrorResponse erroSpring) {
            HttpStatus status = HttpStatus.resolve(erroSpring.getStatusCode().value());
            if (status != null) {
                return montar(status, status.getReasonPhrase(), List.of());
            }
        }
        log.error("Erro inesperado", e);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor", List.of());
    }

    private ResponseEntity<ApiError> montar(HttpStatus status, String message, List<ApiError.CampoInvalido> detalhes) {
        ApiError body = new ApiError(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, detalhes);
        return ResponseEntity.status(status).body(body);
    }
}