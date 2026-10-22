package com.aula.pos.comprasinternacionais.controller;

import com.aula.pos.comprasinternacionais.dto.CompraRequest;
import com.aula.pos.comprasinternacionais.dto.CompraResponse;
import com.aula.pos.comprasinternacionais.dto.SimulacaoRequest;
import com.aula.pos.comprasinternacionais.dto.SimulacaoResponse;
import com.aula.pos.comprasinternacionais.service.CompraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;

    // Consulta a cotação e calcula, sem gravar
    @PostMapping("/simular")
    public ResponseEntity<SimulacaoResponse> simular(@Valid @RequestBody SimulacaoRequest request) {
        return ResponseEntity.ok(compraService.simular(request));
    }

    // Valida, consulta a cotação, verifica o limite e grava
    @PostMapping
    public ResponseEntity<CompraResponse> registrar(@Valid @RequestBody CompraRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(compraService.registrar(request));
    }
}
