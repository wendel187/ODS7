package com.Nexturing.ODS7.service;

import com.Nexturing.ODS7.dto.AparelhoResponse;
import com.Nexturing.ODS7.dto.DashboardResponse;
import com.Nexturing.ODS7.entity.Usuario;
import com.Nexturing.ODS7.repository.AparelhoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
public class DashboardService {

	private final AparelhoRepository aparelhoRepository;
	private final DemoUsuarioService demoUsuarioService;
	private final ConfiguracaoEnergiaService configuracaoService;
	private final AparelhoService aparelhoService;
	private final ConsumoService consumoService;

	public DashboardService(AparelhoRepository aparelhoRepository, DemoUsuarioService demoUsuarioService,
			ConfiguracaoEnergiaService configuracaoService, AparelhoService aparelhoService,
			ConsumoService consumoService) {
		this.aparelhoRepository = aparelhoRepository;
		this.demoUsuarioService = demoUsuarioService;
		this.configuracaoService = configuracaoService;
		this.aparelhoService = aparelhoService;
		this.consumoService = consumoService;
	}

	@Transactional
	public DashboardResponse obterDashboard() {
		Usuario usuario = demoUsuarioService.obterUsuarioDemo();
		BigDecimal tarifa = configuracaoService.obterTarifa(usuario);
		List<AparelhoResponse> aparelhos = aparelhoRepository
				.findAllByUsuario_IdOrderByNomeAsc(usuario.getId()).stream()
				.map(aparelho -> aparelhoService.converter(aparelho, tarifa))
				.toList();
		BigDecimal totalKwh = aparelhos.stream()
				.map(AparelhoResponse::consumoMensalKwh)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal custoTotal = tarifa == null ? null : consumoService.calcularCusto(totalKwh, tarifa);
		AparelhoResponse maiorConsumidor = aparelhos.stream()
				.max(Comparator.comparing(AparelhoResponse::consumoMensalKwh))
				.orElse(null);
		return new DashboardResponse(totalKwh, custoTotal, aparelhos.size(),
				maiorConsumidor, aparelhos, tarifa != null);
	}
}
