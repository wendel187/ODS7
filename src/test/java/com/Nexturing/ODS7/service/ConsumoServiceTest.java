package com.Nexturing.ODS7.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsumoServiceTest {

	private final ConsumoService consumoService = new ConsumoService();

	@Test
	void calculaConsumoMensalConvertendoWattsParaQuilowatts() {
		BigDecimal consumo = consumoService.calcularConsumoMensal(
				new BigDecimal("100"), new BigDecimal("5"), 30);

		assertEquals(0, consumo.compareTo(new BigDecimal("15")));
	}

	@Test
	void preservaPrecisaoDoConsumoSemArredondamentoPrematuro() {
		BigDecimal consumo = consumoService.calcularConsumoMensal(
				new BigDecimal("50.5"), new BigDecimal("2.5"), 31);

		assertEquals(0, consumo.compareTo(new BigDecimal("3.91375")));
	}

	@Test
	void calculaCustoComDuasCasasDecimaisEArredondamentoMonetario() {
		BigDecimal custo = consumoService.calcularCusto(
				new BigDecimal("15"), new BigDecimal("0.90"));
		BigDecimal custoArredondado = consumoService.calcularCusto(
				new BigDecimal("1"), new BigDecimal("1.005"));

		assertEquals(new BigDecimal("13.50"), custo);
		assertEquals(new BigDecimal("1.01"), custoArredondado);
	}

	@Test
	void calculaPercentualETrataConsumoOriginalZero() {
		BigDecimal percentual = consumoService.calcularPercentualReducao(
				new BigDecimal("15"), new BigDecimal("6"));
		BigDecimal percentualSemConsumo = consumoService.calcularPercentualReducao(
				BigDecimal.ZERO, BigDecimal.ZERO);

		assertEquals(new BigDecimal("40.00"), percentual);
		assertEquals(new BigDecimal("0.00"), percentualSemConsumo);
	}
}
