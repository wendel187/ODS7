package com.Nexturing.ODS7.controller;

import com.Nexturing.ODS7.dto.SimulacaoSolarRequest;
import com.Nexturing.ODS7.dto.SimulacaoSolarResponse;
import com.Nexturing.ODS7.service.SimulacaoSolarService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/simulacoes/solar")
public class SimulacaoSolarController {

	private final SimulacaoSolarService simulacaoSolarService;

	public SimulacaoSolarController(SimulacaoSolarService simulacaoSolarService) {
		this.simulacaoSolarService = simulacaoSolarService;
	}

	@PostMapping
	public SimulacaoSolarResponse simular(@Valid @RequestBody SimulacaoSolarRequest request) {
		return simulacaoSolarService.simular(request);
	}
}
