package com.Nexturing.ODS7.service;

import com.Nexturing.ODS7.dto.SimulacaoSolarRequest;
import com.Nexturing.ODS7.dto.SimulacaoSolarResponse;
import com.Nexturing.ODS7.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulacaoSolarServiceTest {

	private final SimulacaoSolarService service = new SimulacaoSolarService();

	@Test
	void calculaCreditoEconomiaAnualPercentualERetornoComTarifaParcialmenteCompensavel() {
		SimulacaoSolarResponse result = service.simular(request(
				"400.00", "500.00", "0.80", "18000.00", "300.00", "60.00", "80.00"));

		assertDecimal("500.00", result.consumoMensalAtualKwh());
		assertDecimal("300.00", result.energiaCompensavelKwh());
		assertDecimal("208.00", result.custoMensalComSolar());
		assertDecimal("192.00", result.economiaMensal());
		assertDecimal("2304.00", result.economiaAnual());
		assertDecimal("48.00", result.percentualEconomia());
		assertDecimal("93.75", result.retornoEstimadoMeses());
		assertEquals("SP", result.estado());
	}

	@Test
	void estimaConsumoPelaContaMasIdentificaExplicitamenteAproximacao() {
		SimulacaoSolarResponse result = service.simular(request(
				"300.00", null, "0.50", null, "500.00", "50.00", "80.00"));

		assertTrue(result.consumoEstimadoPeloValorDaConta());
		assertDecimal("500.00", result.consumoMensalAtualKwh());
		assertDecimal("500.00", result.energiaCompensavelKwh());
		assertDecimal("100.00", result.custoMensalComSolar());
		assertDecimal("200.00", result.economiaMensal());
		assertNull(result.retornoEstimadoMeses());
	}

	@Test
	void naoInventageracaoNemEconomiaQuandoGeracaoNaoFoiInformada() {
		SimulacaoSolarResponse result = service.simular(request(
				"400.00", "500.00", "0.80", "18000.00", null, "60.00", "80.00"));

		assertTrue(!result.geracaoInformada());
		assertNull(result.geracaoSolarMensalKwh());
		assertNull(result.energiaCompensavelKwh());
		assertNull(result.custoMensalComSolar());
		assertNull(result.economiaMensal());
		assertNull(result.economiaAnual());
		assertNull(result.percentualEconomia());
		assertNull(result.retornoEstimadoMeses());
	}

	@Test
	void naoCalculaRetornoQuandoEconomiaMensalEZero() {
		SimulacaoSolarResponse result = service.simular(request(
				"400.00", "500.00", "0.80", "18000.00", "0.00", "60.00", "80.00"));

		assertDecimal("0.00", result.economiaMensal());
		assertDecimal("0.00", result.economiaAnual());
		assertNull(result.retornoEstimadoMeses());
	}

	@Test
	void limitaContaSolarAosCustosNaoCompensaveisInformados() {
		SimulacaoSolarResponse result = service.simular(request(
				"400.00", "500.00", "0.80", "18000.00", "1000.00", "350.00", "100.00"));

		assertDecimal("350.00", result.custoMensalComSolar());
		assertDecimal("50.00", result.economiaMensal());
		assertDecimal("12.50", result.percentualEconomia());
		assertDecimal("600.00", result.economiaAnual());
		assertDecimal("360.00", result.retornoEstimadoMeses());
	}

	@Test
	void rejeitaCustoNaoCompensavelMaiorQueConta() {
		SimulacaoSolarRequest request = request(
				"100.00", "200.00", "0.80", null, "200.00", "101.00", "100.00");

		assertThrows(BusinessRuleException.class, () -> service.simular(request));
	}

	private SimulacaoSolarRequest request(String bill, String consumption, String tariff,
			String investment, String generation, String nonCompensable, String compensablePercent) {
		return new SimulacaoSolarRequest(
				new BigDecimal(bill),
				consumption == null ? null : new BigDecimal(consumption),
				" Campinas ",
				"sp",
				new BigDecimal(tariff),
				investment == null ? null : new BigDecimal(investment),
				generation == null ? null : new BigDecimal(generation),
				new BigDecimal(nonCompensable),
				new BigDecimal(compensablePercent));
	}

	private void assertDecimal(String expected, BigDecimal actual) {
		assertEquals(0, new BigDecimal(expected).compareTo(actual),
				() -> "Esperado " + expected + ", recebido " + actual);
	}
}
