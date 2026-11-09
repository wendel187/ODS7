package com.Nexturing.ODS7.service;

import com.Nexturing.ODS7.entity.Usuario;
import com.Nexturing.ODS7.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemoUsuarioService {

	private static final String CODIGO_DEMO = "demo";
	private static final String NOME_DEMO = "Perfil de demonstração";

	private final UsuarioRepository usuarioRepository;

	public DemoUsuarioService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@Transactional
	public Usuario obterUsuarioDemo() {
		return usuarioRepository.findByCodigo(CODIGO_DEMO)
				.orElseGet(() -> usuarioRepository.save(new Usuario(CODIGO_DEMO, NOME_DEMO)));
	}
}
