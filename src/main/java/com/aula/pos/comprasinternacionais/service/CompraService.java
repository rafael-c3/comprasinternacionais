package com.aula.pos.comprasinternacionais.service;

import com.aula.pos.comprasinternacionais.dto.*;
import com.aula.pos.comprasinternacionais.entity.Cliente;
import com.aula.pos.comprasinternacionais.entity.Compra;
import com.aula.pos.comprasinternacionais.exception.ClienteNaoEncontradoException;
import com.aula.pos.comprasinternacionais.exception.LimiteMensalExcedidoException;
import com.aula.pos.comprasinternacionais.mapper.CompraMapper;
import com.aula.pos.comprasinternacionais.repository.ClienteRepository;
import com.aula.pos.comprasinternacionais.repository.CompraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompraService {

    private static final BigDecimal LIMITE_MENSAL = new BigDecimal("10000.00");
    private static final BigDecimal ZERO_BRL = new BigDecimal("0.00");
    private static final BigDecimal VALOR_CORTE_USD = new BigDecimal("100");
    private static final BigDecimal TAXA_VALOR_BAIXO = new BigDecimal("0.05");
    private static final BigDecimal TAXA_VALOR_ALTO = new BigDecimal("0.02");
    private static final BigDecimal TRIBUTO_SIMULADO = new BigDecimal("0.10");

    private final ClienteRepository clienteRepository;
    private final CompraRepository compraRepository;
    private final CotacaoService cotacaoService;
    private final CompraMapper compraMapper;

    // POST /compras/simular: consulta a cotação e calcula, sem gravar
    public SimulacaoResponse simular(SimulacaoRequest request) {
        Calculo calculo = calcular(request.valorUSD());

        return new SimulacaoResponse(
                request.valorUSD(),
                calculo.cotacao(),
                calculo.valorConvertido(),
                calculo.taxaServico(),
                calculo.tributoSimulado(),
                calculo.valorFinalBRL(),
                calculo.dataCotacao()
        );
    }

    // POST /compras: valida, consulta a cotação, verifica o limite e grava
    public CompraResponse registrar(CompraRequest request) {
        Cliente cliente = buscarCliente(request.clienteId());
        Calculo calculo = calcular(request.valorUSD());

        LocalDateTime agora = LocalDateTime.now();
        BigDecimal totalUtilizado = totalUtilizadoNoMes(cliente.getId(), YearMonth.from(agora));
        BigDecimal totalComNovaCompra = totalUtilizado.add(calculo.valorFinalBRL());

        if (totalComNovaCompra.compareTo(LIMITE_MENSAL) > 0) {
            BigDecimal saldo = LIMITE_MENSAL.subtract(totalUtilizado).max(ZERO_BRL);
            throw new LimiteMensalExcedidoException(
                    "Limite mensal de R$ 10.000,00 excedido. Saldo disponível: R$ "
                            + saldo.toPlainString() + ", valor final da compra: R$ "
                            + calculo.valorFinalBRL().toPlainString());
        }

        Compra compra = new Compra(
                null,
                request.valorUSD().setScale(2, RoundingMode.HALF_UP),
                calculo.cotacao(),
                calculo.valorConvertido(),
                calculo.taxaServico(),
                calculo.tributoSimulado(),
                calculo.valorFinalBRL(),
                calculo.dataCotacao(),
                agora,
                cliente
        );

        return compraMapper.toResponse(compraRepository.save(compra));
    }

    // GET /clientes/{id}/compras
    @Transactional(readOnly = true)
    public List<CompraResponse> historico(UUID clienteId) {
        buscarCliente(clienteId);

        return compraRepository.findByClienteIdOrderByDataCompraDesc(clienteId).stream()
                .map(compraMapper::toResponse)
                .toList();
    }

    // GET /clientes/{id}/limite
    public LimiteResponse limite(UUID clienteId) {
        buscarCliente(clienteId);

        YearMonth mes = YearMonth.now();
        BigDecimal totalUtilizado = totalUtilizadoNoMes(clienteId, mes);
        BigDecimal saldo = LIMITE_MENSAL.subtract(totalUtilizado).max(ZERO_BRL);

        return new LimiteResponse(clienteId, mes.toString(), LIMITE_MENSAL, totalUtilizado, saldo);
    }

    private Calculo calcular(BigDecimal valorUSD) {
        Cotacao cotacaoApi = cotacaoService.buscarCotacaoVenda();
        // A cotação gravada é a mesma usada no cálculo (4 casas, como a coluna do banco)
        BigDecimal cotacao = cotacaoApi.valorVenda().setScale(4, RoundingMode.HALF_UP);

        // RN02: conversão
        BigDecimal valorConvertido = valorUSD.multiply(cotacao).setScale(2, RoundingMode.HALF_UP);

        // RN03: taxa de serviço (5% abaixo de US$ 100, 2% a partir de US$ 100)
        BigDecimal percentualTaxa = valorUSD.compareTo(VALOR_CORTE_USD) < 0
                ? TAXA_VALOR_BAIXO
                : TAXA_VALOR_ALTO;
        BigDecimal taxaServico = valorConvertido.multiply(percentualTaxa).setScale(2, RoundingMode.HALF_UP);

        // RN04: tributo didático de 10%
        BigDecimal tributoSimulado = valorConvertido.multiply(TRIBUTO_SIMULADO).setScale(2, RoundingMode.HALF_UP);

        // RN05: valor final
        BigDecimal valorFinalBRL = valorConvertido.add(taxaServico).add(tributoSimulado);

        return new Calculo(
                cotacao,
                cotacaoApi.dataHora().toLocalDate(),
                valorConvertido,
                taxaServico,
                tributoSimulado,
                valorFinalBRL
        );
    }

    // RN06: soma dos valores finais do cliente no mesmo mês-calendário
    private BigDecimal totalUtilizadoNoMes(UUID clienteId, YearMonth mes) {
        LocalDateTime inicio = mes.atDay(1).atStartOfDay();
        LocalDateTime fim = mes.plusMonths(1).atDay(1).atStartOfDay();

        BigDecimal total = compraRepository.somarValorFinalNoPeriodo(clienteId, inicio, fim);
        return total != null ? total : ZERO_BRL;
    }

    private Cliente buscarCliente(UUID id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNaoEncontradoException(id));
    }

    private record Calculo(
            BigDecimal cotacao,
            LocalDate dataCotacao,
            BigDecimal valorConvertido,
            BigDecimal taxaServico,
            BigDecimal tributoSimulado,
            BigDecimal valorFinalBRL
    ) {
    }
}
