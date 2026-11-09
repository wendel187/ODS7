package com.Nexturing.ODS7.dto;

import java.math.BigDecimal;

public record AparelhoResponse(
		Long id,
		String nome,
		BigDecimal potenciaWatts,
		BigDecimal horasPorDia,
		Integer diasPorMes,
		BigDecimal consumoMensalKwh,
		BigDecimal custoMensalEstimado) {
}
