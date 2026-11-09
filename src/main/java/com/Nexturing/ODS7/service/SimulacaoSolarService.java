package com.Nexturing.ODS7.service;

import com.Nexturing.ODS7.dto.SimulacaoSolarRequest;
import com.Nexturing.ODS7.dto.SimulacaoSolarResponse;
import com.Nexturing.ODS7.exception.BusinessRuleException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class SimulacaoSolarService {

	private static final BigDecimal ZERO = BigDecimal.ZERO;
	private static final BigDecimal CEM = new BigDecimal("100");
	private static final BigDecimal DOZE = new BigDecimal("12");

	public SimulacaoSolarResponse simular(SimulacaoSolarRequest request) {
		if (request.custoMensalNaoCompensavel().compareTo(request.valorContaMensal()) > 0) {
			throw new BusinessRuleException(
					"O custo mensal não compensável não pode ser maior que o valor da conta.");
		}

		boolean consumoEstimado = request.consumoMensalKwh() == null;
		BigDecimal consumo = consumoEstimado
				? estimarConsumoPeloValorDaConta(request)
				: request.consumoMensalKwh();

		if (request.geracaoMensalKwh() == null) {
			return new SimulacaoSolarResponse(
					request.cidade().trim(),
					request.estado().trim().toUpperCase(),
					consumo,
					consumoEstimado,
					null,
					null,
					request.valorContaMensal().setScale(2, RoundingMode.HALF_UP),
					null,
					null,
					null,
					null,
					escalaMonetaria(request.investimentoInicial()),
					null,
					request.tarifaKwh(),
					request.percentualTarifaCompensavel(),
					request.custoMensalNaoCompensavel().setScale(2, RoundingMode.HALF_UP),
					false);
		}

		BigDecimal energiaCompensavel = consumo.min(request.geracaoMensalKwh());
		BigDecimal parcelaTarifaCompensavel = request.percentualTarifaCompensavel()
				.divide(CEM, 8, RoundingMode.HALF_UP);
		BigDecimal creditoEstimado = energiaCompensavel
				.multiply(request.tarifaKwh())
				.multiply(parcelaTarifaCompensavel);
		BigDecimal custoSemSolar = request.valorContaMensal().setScale(2, RoundingMode.HALF_UP);
		BigDecimal custoComSolar = custoSemSolar.subtract(creditoEstimado)
				.max(request.custoMensalNaoCompensavel())
				.max(ZERO)
				.setScale(2, RoundingMode.HALF_UP);
		BigDecimal economiaMensal = custoSemSolar.subtract(custoComSolar)
				.max(ZERO)
				.setScale(2, RoundingMode.HALF_UP);
		BigDecimal economiaAnual = economiaMensal.multiply(DOZE).setScale(2, RoundingMode.HALF_UP);
		BigDecimal percentualEconomia = custoSemSolar.signum() == 0
				? ZERO.setScale(2)
				: economiaMensal.divide(custoSemSolar, 8, RoundingMode.HALF_UP)
						.multiply(CEM)
						.setScale(2, RoundingMode.HALF_UP);
		BigDecimal retornoMeses = request.investimentoInicial() != null
				&& request.investimentoInicial().signum() > 0
				&& economiaMensal.signum() > 0
						? request.investimentoInicial().divide(economiaMensal, 2, RoundingMode.HALF_UP)
						: null;

		return new SimulacaoSolarResponse(
				request.cidade().trim(),
				request.estado().trim().toUpperCase(),
				consumo,
				consumoEstimado,
				request.geracaoMensalKwh(),
				energiaCompensavel,
				custoSemSolar,
				custoComSolar,
				economiaMensal,
				economiaAnual,
				percentualEconomia,
				escalaMonetaria(request.investimentoInicial()),
				retornoMeses,
				request.tarifaKwh(),
				request.percentualTarifaCompensavel(),
				request.custoMensalNaoCompensavel().setScale(2, RoundingMode.HALF_UP),
				true);
	}

	private BigDecimal estimarConsumoPeloValorDaConta(SimulacaoSolarRequest request) {
		BigDecimal valorEnergiaEstimado = request.valorContaMensal()
				.subtract(request.custoMensalNaoCompensavel())
				.max(ZERO);
		return valorEnergiaEstimado.divide(request.tarifaKwh(), 2, RoundingMode.HALF_UP);
	}

	private BigDecimal escalaMonetaria(BigDecimal valor) {
		return valor == null ? null : valor.setScale(2, RoundingMode.HALF_UP);
	}
}
