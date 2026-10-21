package com.aula.pos.comprasinternacionais.repository;

import com.aula.pos.comprasinternacionais.entity.Compra;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CompraRepository extends JpaRepository<Compra, UUID> {

    // Histórico de compras do cliente, da mais recente para a mais antiga
    List<Compra> findByClienteIdOrderByDataCompraDesc(UUID clienteId);

    // Soma dos valores finais do cliente dentro de um período (usada no limite mensal)
    @Query("""
            SELECT SUM(c.valorFinalBRL)
            FROM Compra c
            WHERE c.cliente.id = :clienteId
              AND c.dataCompra >= :inicio
              AND c.dataCompra < :fim
            """)
    BigDecimal somarValorFinalNoPeriodo(
            @Param("clienteId") UUID clienteId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);
}
