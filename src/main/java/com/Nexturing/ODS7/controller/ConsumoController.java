package com.Nexturing.ODS7.controller;

import com.Nexturing.ODS7.dto.ConsumoRequest;
import com.Nexturing.ODS7.dto.ConsumoResponse;
import com.Nexturing.ODS7.entity.Usuario;
import com.Nexturing.ODS7.service.ConsumoService;
import com.Nexturing.ODS7.service.ConfiguracaoEnergiaService;
import com.Nexturing.ODS7.service.DemoUsuarioService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/consumo")
public class ConsumoController {

	private final ConsumoService consumoService;
	private final ConfiguracaoEnergiaService configuracaoService;
	private final DemoUsuarioService demoUsuarioService;

	public ConsumoController(ConsumoService consumoService,
			ConfiguracaoEnergiaService configuracaoService,
			DemoUsuarioService demoUsuarioService) {
		this.consumoService = consumoService;
		this.configuracaoService = configuracaoService;
		this.demoUsuarioService = demoUsuarioService;
	}

	@PostMapping
	public ConsumoResponse calcular(@Valid @RequestBody ConsumoRequest request) {
		BigDecimal consumo = consumoService.calcularConsumoMensal(request.potenciaWatts(),
				request.horasPorDia(), request.diasPorMes());
		Usuario usuario = demoUsuarioService.obterUsuarioDemo();
		BigDecimal tarifa = configuracaoService.obterTarifa(usuario);
		BigDecimal custo = tarifa == null ? null : consumoService.calcularCusto(consumo, tarifa);
		return new ConsumoResponse(consumo, custo, tarifa != null);
	}
}
