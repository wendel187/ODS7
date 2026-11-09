package com.Nexturing.ODS7.dto;

import java.math.BigDecimal;

public record SimulacaoResponse(
		Long aparelhoId,
		String nomeAparelho,
		BigDecimal consumoOriginalKwh,
		BigDecimal consumoSimuladoKwh,
		BigDecimal reducaoConsumoKwh,
		BigDecimal economiaEstimada,
		BigDecimal percentualReducao,
		boolean tarifaConfigurada) {
}
