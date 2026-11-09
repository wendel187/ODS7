package com.Nexturing.ODS7.controller;

import com.Nexturing.ODS7.dto.SimulacaoRequest;
import com.Nexturing.ODS7.dto.SimulacaoResponse;
import com.Nexturing.ODS7.service.SimulacaoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/simulacoes")
public class SimulacaoController {

	private final SimulacaoService simulacaoService;

	public SimulacaoController(SimulacaoService simulacaoService) {
		this.simulacaoService = simulacaoService;
	}

	@PostMapping
	public SimulacaoResponse simular(@Valid @RequestBody SimulacaoRequest request) {
		return simulacaoService.simular(request);
	}
}
