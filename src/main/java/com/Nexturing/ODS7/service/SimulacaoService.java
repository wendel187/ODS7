package com.Nexturing.ODS7.service;

import com.Nexturing.ODS7.dto.SimulacaoRequest;
import com.Nexturing.ODS7.dto.SimulacaoResponse;
import com.Nexturing.ODS7.entity.Aparelho;
import com.Nexturing.ODS7.entity.Usuario;
import com.Nexturing.ODS7.exception.BusinessRuleException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class SimulacaoService {

	private final AparelhoService aparelhoService;
	private final DemoUsuarioService demoUsuarioService;
	private final ConfiguracaoEnergiaService configuracaoService;
	private final ConsumoService consumoService;

	public SimulacaoService(AparelhoService aparelhoService, DemoUsuarioService demoUsuarioService,
			ConfiguracaoEnergiaService configuracaoService, ConsumoService consumoService) {
		this.aparelhoService = aparelhoService;
		this.demoUsuarioService = demoUsuarioService;
		this.configuracaoService = configuracaoService;
		this.consumoService = consumoService;
	}

	@Transactional
	public SimulacaoResponse simular(SimulacaoRequest request) {
		Aparelho aparelho = aparelhoService.buscarEntidadeDoUsuario(request.aparelhoId());
		if (request.horasPorDiaSimuladas().compareTo(aparelho.getHorasPorDia()) > 0) {
			throw new BusinessRuleException(
					"As horas simuladas não podem ultrapassar as horas atuais do aparelho.");
		}

		BigDecimal consumoOriginal = consumoService.calcularConsumoMensal(aparelho.getPotenciaWatts(),
				aparelho.getHorasPorDia(), aparelho.getDiasPorMes());
		BigDecimal consumoSimulado = consumoService.calcularConsumoMensal(aparelho.getPotenciaWatts(),
				request.horasPorDiaSimuladas(), aparelho.getDiasPorMes());
		BigDecimal reducao = consumoOriginal.subtract(consumoSimulado);
		Usuario usuario = demoUsuarioService.obterUsuarioDemo();
		BigDecimal tarifa = configuracaoService.obterTarifa(usuario);
		BigDecimal economia = tarifa == null ? null : consumoService.calcularCusto(reducao, tarifa);
		BigDecimal percentual = consumoService.calcularPercentualReducao(consumoOriginal, reducao);

		return new SimulacaoResponse(aparelho.getId(), aparelho.getNome(), consumoOriginal,
				consumoSimulado, reducao, economia, percentual, tarifa != null);
	}
}
