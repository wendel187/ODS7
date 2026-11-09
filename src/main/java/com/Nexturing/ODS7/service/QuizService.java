package com.Nexturing.ODS7.service;

import com.Nexturing.ODS7.dto.QuizAlternativaResponse;
import com.Nexturing.ODS7.dto.QuizHistoricoResponse;
import com.Nexturing.ODS7.dto.QuizPerguntaResponse;
import com.Nexturing.ODS7.dto.QuizRespostaRequest;
import com.Nexturing.ODS7.dto.QuizRespostaResponse;
import com.Nexturing.ODS7.dto.QuizResultadoResponse;
import com.Nexturing.ODS7.dto.QuizTentativaResponse;
import com.Nexturing.ODS7.entity.QuizAlternativa;
import com.Nexturing.ODS7.entity.QuizPergunta;
import com.Nexturing.ODS7.entity.QuizResposta;
import com.Nexturing.ODS7.entity.QuizTentativa;
import com.Nexturing.ODS7.entity.QuizTentativaPergunta;
import com.Nexturing.ODS7.entity.Usuario;
import com.Nexturing.ODS7.exception.BusinessRuleException;
import com.Nexturing.ODS7.exception.ResourceNotFoundException;
import com.Nexturing.ODS7.repository.QuizPerguntaRepository;
import com.Nexturing.ODS7.repository.QuizRespostaRepository;
import com.Nexturing.ODS7.repository.QuizTentativaPerguntaRepository;
import com.Nexturing.ODS7.repository.QuizTentativaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class QuizService {

	private static final int PERGUNTAS_POR_TENTATIVA = 10;

	private final QuizPerguntaRepository perguntaRepository;
	private final QuizTentativaRepository tentativaRepository;
	private final QuizTentativaPerguntaRepository tentativaPerguntaRepository;
	private final QuizRespostaRepository respostaRepository;
	private final DemoUsuarioService demoUsuarioService;

	public QuizService(QuizPerguntaRepository perguntaRepository, QuizTentativaRepository tentativaRepository,
			QuizTentativaPerguntaRepository tentativaPerguntaRepository, QuizRespostaRepository respostaRepository,
			DemoUsuarioService demoUsuarioService) {
		this.perguntaRepository = perguntaRepository;
		this.tentativaRepository = tentativaRepository;
		this.tentativaPerguntaRepository = tentativaPerguntaRepository;
		this.respostaRepository = respostaRepository;
		this.demoUsuarioService = demoUsuarioService;
	}

	@Transactional
	public QuizTentativaResponse iniciarTentativa() {
		List<QuizPergunta> bancoDePerguntas = new ArrayList<>(perguntaRepository.findAllByOrderByIdAsc());
		if (bancoDePerguntas.size() < PERGUNTAS_POR_TENTATIVA) {
			throw new BusinessRuleException("O quiz ainda não possui perguntas suficientes para iniciar uma rodada.");
		}
		Collections.shuffle(bancoDePerguntas);
		List<QuizPergunta> selecionadas = bancoDePerguntas.subList(0, PERGUNTAS_POR_TENTATIVA);
		Usuario usuario = demoUsuarioService.obterUsuarioDemo();
		QuizTentativa tentativa = tentativaRepository.save(new QuizTentativa(usuario));
		List<QuizTentativaPergunta> perguntasDaTentativa = new ArrayList<>();
		for (int indice = 0; indice < selecionadas.size(); indice++) {
			perguntasDaTentativa.add(new QuizTentativaPergunta(tentativa, selecionadas.get(indice), indice + 1));
		}
		tentativaPerguntaRepository.saveAll(perguntasDaTentativa);
		return converterTentativa(tentativa, perguntasDaTentativa);
	}

	@Transactional
	public QuizRespostaResponse responder(Long tentativaId, Long perguntaId, QuizRespostaRequest request) {
		Usuario usuario = demoUsuarioService.obterUsuarioDemo();
		QuizTentativa tentativa = tentativaRepository.findByIdAndUsuario_Id(tentativaId, usuario.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Tentativa do quiz não encontrada."));
		if (tentativa.isFinalizada()) {
			throw new BusinessRuleException("Esta rodada do quiz já foi finalizada.");
		}

		QuizTentativaPergunta tentativaPergunta = tentativaPerguntaRepository
				.findByTentativa_IdAndPergunta_Id(tentativaId, perguntaId)
				.orElseThrow(() -> new BusinessRuleException("A pergunta não pertence a esta rodada."));
		if (respostaRepository.existsByTentativaPergunta_Id(tentativaPergunta.getId())) {
			throw new BusinessRuleException("Esta pergunta já foi respondida nesta rodada.");
		}

		QuizAlternativa selecionada = tentativaPergunta.getPergunta().getAlternativas().stream()
				.filter(alternativa -> alternativa.getId().equals(request.alternativaId()))
				.findFirst()
				.orElseThrow(() -> new BusinessRuleException("A alternativa não pertence a esta pergunta."));
		boolean correta = selecionada.isCorreta();
		respostaRepository.save(new QuizResposta(tentativaPergunta, selecionada, correta));
		tentativa.registrarResposta(correta);

		long respostasRegistradas = respostaRepository.countByTentativaPergunta_Tentativa_Id(tentativaId);
		if (respostasRegistradas == PERGUNTAS_POR_TENTATIVA) {
			tentativa.finalizar();
		}
		tentativaRepository.save(tentativa);

		QuizAlternativa corretaAlternativa = tentativaPergunta.getPergunta().getAlternativas().stream()
				.filter(QuizAlternativa::isCorreta)
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("A pergunta cadastrada não possui resposta correta."));
		QuizResultadoResponse resultado = tentativa.isFinalizada() ? criarResultado(tentativa) : null;
		return new QuizRespostaResponse(perguntaId, selecionada.getId(), corretaAlternativa.getId(), correta,
				tentativaPergunta.getPergunta().getExplicacao(), tentativa.getAcertos(),
				(int) respostasRegistradas - tentativa.getAcertos(), tentativa.getPontuacao(),
				tentativa.isFinalizada(), resultado);
	}

	@Transactional(readOnly = true)
	public List<QuizHistoricoResponse> listarHistorico() {
		Usuario usuario = demoUsuarioService.obterUsuarioDemo();
		return tentativaRepository.findAllByUsuario_IdOrderByIniciadaEmDesc(usuario.getId()).stream()
				.map(this::converterHistorico)
				.toList();
	}

	private QuizTentativaResponse converterTentativa(QuizTentativa tentativa,
			List<QuizTentativaPergunta> perguntasDaTentativa) {
		List<QuizPerguntaResponse> perguntas = perguntasDaTentativa.stream()
				.map(item -> {
					QuizPergunta pergunta = item.getPergunta();
					List<QuizAlternativaResponse> alternativas = pergunta.getAlternativas().stream()
							.map(alternativa -> new QuizAlternativaResponse(alternativa.getId(), alternativa.getTexto()))
							.toList();
					return new QuizPerguntaResponse(pergunta.getId(), pergunta.getCategoria().getRotulo(),
							pergunta.getEnunciado(), alternativas);
				})
				.toList();
		return new QuizTentativaResponse(tentativa.getId(), PERGUNTAS_POR_TENTATIVA, perguntas);
	}

	private QuizHistoricoResponse converterHistorico(QuizTentativa tentativa) {
		int total = PERGUNTAS_POR_TENTATIVA;
		return new QuizHistoricoResponse(tentativa.getId(), tentativa.getIniciadaEm(), tentativa.getFinalizadaEm(),
				tentativa.getAcertos(), tentativa.isFinalizada() ? total - tentativa.getAcertos() : 0,
				tentativa.getPontuacao(), tentativa.getAcertos() * 10,
				classificacao(tentativa.getPontuacao()), tentativa.isFinalizada());
	}

	private QuizResultadoResponse criarResultado(QuizTentativa tentativa) {
		int erros = PERGUNTAS_POR_TENTATIVA - tentativa.getAcertos();
		return new QuizResultadoResponse(tentativa.getAcertos(), erros, tentativa.getPontuacao(),
				tentativa.getAcertos() * 10, classificacao(tentativa.getPontuacao()));
	}

	private String classificacao(int pontuacao) {
		if (pontuacao <= 20) {
			return "Aprendiz de Energia";
		}
		if (pontuacao <= 50) {
			return "Consciente de Energia";
		}
		if (pontuacao <= 80) {
			return "Especialista em Economia";
		}
		return "Embaixador da Energia Limpa";
	}
}
