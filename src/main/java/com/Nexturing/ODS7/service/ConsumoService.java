package com.Nexturing.ODS7.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ConsumoService {

	private static final BigDecimal MIL = new BigDecimal("1000");

	public BigDecimal calcularConsumoMensal(BigDecimal potenciaWatts, BigDecimal horasPorDia,
			int diasPorMes) {
		return potenciaWatts.multiply(horasPorDia)
				.multiply(BigDecimal.valueOf(diasPorMes))
				.divide(MIL);
	}

	public BigDecimal calcularCusto(BigDecimal consumoKwh, BigDecimal tarifaKwh) {
		return consumoKwh.multiply(tarifaKwh).setScale(2, RoundingMode.HALF_UP);
	}

	public BigDecimal calcularPercentualReducao(BigDecimal consumoOriginal, BigDecimal reducao) {
		if (consumoOriginal.signum() == 0) {
			return BigDecimal.ZERO.setScale(2);
		}
		return reducao.divide(consumoOriginal, 6, RoundingMode.HALF_UP)
				.multiply(BigDecimal.valueOf(100))
				.setScale(2, RoundingMode.HALF_UP);
	}
}
