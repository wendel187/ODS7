package com.Nexturing.ODS7.controller;

import com.Nexturing.ODS7.dto.ConfiguracaoRequest;
import com.Nexturing.ODS7.dto.ConfiguracaoResponse;
import com.Nexturing.ODS7.service.ConfiguracaoEnergiaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/configuracoes")
public class ConfiguracaoEnergiaController {

	private final ConfiguracaoEnergiaService configuracaoService;

	public ConfiguracaoEnergiaController(ConfiguracaoEnergiaService configuracaoService) {
		this.configuracaoService = configuracaoService;
	}

	@GetMapping
	public ConfiguracaoResponse obter() {
		return configuracaoService.obterConfiguracao();
	}

	@PutMapping
	public ConfiguracaoResponse atualizar(@Valid @RequestBody ConfiguracaoRequest request) {
		return configuracaoService.atualizarTarifa(request);
	}
}
