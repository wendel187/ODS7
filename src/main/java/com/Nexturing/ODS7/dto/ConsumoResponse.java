package com.Nexturing.ODS7.dto;

import java.math.BigDecimal;

public record ConsumoResponse(
		BigDecimal consumoMensalKwh,
		BigDecimal custoMensalEstimado,
		boolean tarifaConfigurada) {
}
