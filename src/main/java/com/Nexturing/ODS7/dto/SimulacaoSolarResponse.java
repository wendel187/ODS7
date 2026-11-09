package com.Nexturing.ODS7.dto;

import java.math.BigDecimal;

public record SimulacaoSolarResponse(
		String cidade,
		String estado,
		BigDecimal consumoMensalAtualKwh,
		boolean consumoEstimadoPeloValorDaConta,
		BigDecimal geracaoSolarMensalKwh,
		BigDecimal energiaCompensavelKwh,
		BigDecimal custoMensalSemSolar,
		BigDecimal custoMensalComSolar,
		BigDecimal economiaMensal,
		BigDecimal economiaAnual,
		BigDecimal percentualEconomia,
		BigDecimal investimentoInicial,
		BigDecimal retornoEstimadoMeses,
		BigDecimal tarifaKwh,
		BigDecimal percentualTarifaCompensavel,
		BigDecimal custoMensalNaoCompensavel,
		boolean geracaoInformada) {
}
