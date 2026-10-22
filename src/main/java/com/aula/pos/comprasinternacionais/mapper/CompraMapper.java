package com.aula.pos.comprasinternacionais.mapper;

import com.aula.pos.comprasinternacionais.dto.CompraResponse;
import com.aula.pos.comprasinternacionais.entity.Compra;
import org.springframework.stereotype.Component;

@Component
public class CompraMapper {

    public CompraResponse toResponse(Compra compra) {
        return new CompraResponse(
                compra.getId(),
                compra.getCliente().getId(),
                compra.getValorUSD(),
                compra.getCotacaoUtilizada(),
                compra.getValorConvertido(),
                compra.getTaxaServico(),
                compra.getTributoSimulado(),
                compra.getValorFinalBRL(),
                compra.getDataCotacao(),
                compra.getDataCompra()
        );
    }
}
