package com.Nexturing.ODS7.service;

import com.Nexturing.ODS7.dto.AparelhoRequest;
import com.Nexturing.ODS7.dto.AparelhoResponse;
import com.Nexturing.ODS7.entity.Aparelho;
import com.Nexturing.ODS7.entity.Usuario;
import com.Nexturing.ODS7.exception.ResourceNotFoundException;
import com.Nexturing.ODS7.repository.AparelhoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AparelhoService {

	private final AparelhoRepository aparelhoRepository;
	private final DemoUsuarioService demoUsuarioService;
	private final ConfiguracaoEnergiaService configuracaoService;
	private final ConsumoService consumoService;

	public AparelhoService(AparelhoRepository aparelhoRepository, DemoUsuarioService demoUsuarioService,
			ConfiguracaoEnergiaService configuracaoService, ConsumoService consumoService) {
		this.aparelhoRepository = aparelhoRepository;
		this.demoUsuarioService = demoUsuarioService;
		this.configuracaoService = configuracaoService;
		this.consumoService = consumoService;
	}

	@Transactional
	public List<AparelhoResponse> listar() {
		Usuario usuario = demoUsuarioService.obterUsuarioDemo();
		BigDecimal tarifa = configuracaoService.obterTarifa(usuario);
		return aparelhoRepository.findAllByUsuario_IdOrderByNomeAsc(usuario.getId()).stream()
				.map(aparelho -> converter(aparelho, tarifa))
				.toList();
	}

	@Transactional
	public AparelhoResponse buscar(Long id) {
		Usuario usuario = demoUsuarioService.obterUsuarioDemo();
		Aparelho aparelho = buscarEntidade(id, usuario);
		return converter(aparelho, configuracaoService.obterTarifa(usuario));
	}

	@Transactional
	public AparelhoResponse criar(AparelhoRequest request) {
		Usuario usuario = demoUsuarioService.obterUsuarioDemo();
		Aparelho aparelho = new Aparelho(request.nome().trim(), request.potenciaWatts(),
				request.horasPorDia(), request.diasPorMes(), usuario);
		Aparelho salvo = aparelhoRepository.save(aparelho);
		return converter(salvo, configuracaoService.obterTarifa(usuario));
	}

	@Transactional
	public AparelhoResponse atualizar(Long id, AparelhoRequest request) {
		Usuario usuario = demoUsuarioService.obterUsuarioDemo();
		Aparelho aparelho = buscarEntidade(id, usuario);
		aparelho.atualizar(request.nome().trim(), request.potenciaWatts(),
				request.horasPorDia(), request.diasPorMes());
		return converter(aparelhoRepository.save(aparelho), configuracaoService.obterTarifa(usuario));
	}

	@Transactional
	public void excluir(Long id) {
		Usuario usuario = demoUsuarioService.obterUsuarioDemo();
		aparelhoRepository.delete(buscarEntidade(id, usuario));
	}

	@Transactional
	public Aparelho buscarEntidadeDoUsuario(Long id) {
		return buscarEntidade(id, demoUsuarioService.obterUsuarioDemo());
	}

	private Aparelho buscarEntidade(Long id, Usuario usuario) {
		return aparelhoRepository.findByIdAndUsuario_Id(id, usuario.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Aparelho não encontrado."));
	}

	public AparelhoResponse converter(Aparelho aparelho, BigDecimal tarifa) {
		BigDecimal consumo = consumoService.calcularConsumoMensal(aparelho.getPotenciaWatts(),
				aparelho.getHorasPorDia(), aparelho.getDiasPorMes());
		BigDecimal custo = tarifa == null ? null : consumoService.calcularCusto(consumo, tarifa);
		return new AparelhoResponse(aparelho.getId(), aparelho.getNome(), aparelho.getPotenciaWatts(),
				aparelho.getHorasPorDia(), aparelho.getDiasPorMes(), consumo, custo);
	}
}
