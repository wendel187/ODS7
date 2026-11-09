package com.Nexturing.ODS7.service;

import com.Nexturing.ODS7.dto.ConfiguracaoRequest;
import com.Nexturing.ODS7.dto.ConfiguracaoResponse;
import com.Nexturing.ODS7.entity.ConfiguracaoEnergia;
import com.Nexturing.ODS7.entity.Usuario;
import com.Nexturing.ODS7.repository.ConfiguracaoEnergiaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ConfiguracaoEnergiaService {

	private final ConfiguracaoEnergiaRepository configuracaoRepository;
	private final DemoUsuarioService demoUsuarioService;

	public ConfiguracaoEnergiaService(ConfiguracaoEnergiaRepository configuracaoRepository,
			DemoUsuarioService demoUsuarioService) {
		this.configuracaoRepository = configuracaoRepository;
		this.demoUsuarioService = demoUsuarioService;
	}

	@Transactional
	public ConfiguracaoResponse obterConfiguracao() {
		Usuario usuario = demoUsuarioService.obterUsuarioDemo();
		ConfiguracaoEnergia configuracao = configuracaoRepository.findByUsuario_Id(usuario.getId())
				.orElseGet(() -> configuracaoRepository.save(new ConfiguracaoEnergia(usuario, null)));
		return converter(configuracao);
	}

	@Transactional
	public ConfiguracaoResponse atualizarTarifa(ConfiguracaoRequest request) {
		Usuario usuario = demoUsuarioService.obterUsuarioDemo();
		ConfiguracaoEnergia configuracao = configuracaoRepository.findByUsuario_Id(usuario.getId())
				.orElseGet(() -> new ConfiguracaoEnergia(usuario, null));
		configuracao.atualizarTarifa(request.tarifaKwh());
		return converter(configuracaoRepository.save(configuracao));
	}

	@Transactional(readOnly = true)
	public BigDecimal obterTarifa(Usuario usuario) {
		return configuracaoRepository.findByUsuario_Id(usuario.getId())
				.map(ConfiguracaoEnergia::getTarifaKwh)
				.orElse(null);
	}

	private ConfiguracaoResponse converter(ConfiguracaoEnergia configuracao) {
		return new ConfiguracaoResponse(configuracao.getId(), configuracao.getTarifaKwh());
	}
}
