package com.Nexturing.ODS7.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
		BigDecimal consumoMensalTotalKwh,
		BigDecimal custoMensalEstimado,
		int quantidadeAparelhos,
		AparelhoResponse maiorConsumidor,
		List<AparelhoResponse> aparelhos,
		boolean tarifaConfigurada) {
}
