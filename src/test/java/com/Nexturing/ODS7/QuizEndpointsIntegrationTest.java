package com.Nexturing.ODS7;

import com.Nexturing.ODS7.dto.QuizPerguntaResponse;
import com.Nexturing.ODS7.dto.QuizTentativaResponse;
import com.Nexturing.ODS7.entity.QuizAlternativa;
import com.Nexturing.ODS7.entity.QuizCategoria;
import com.Nexturing.ODS7.entity.QuizPergunta;
import com.Nexturing.ODS7.repository.QuizPerguntaRepository;
import com.Nexturing.ODS7.repository.QuizRespostaRepository;
import com.Nexturing.ODS7.repository.QuizTentativaPerguntaRepository;
import com.Nexturing.ODS7.repository.QuizTentativaRepository;
import com.Nexturing.ODS7.service.QuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class QuizEndpointsIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private QuizPerguntaRepository perguntaRepository;

	@Autowired
	private QuizService quizService;

	@Autowired
	private QuizTentativaRepository tentativaRepository;

	@Autowired
	private QuizTentativaPerguntaRepository tentativaPerguntaRepository;

	@Autowired
	private QuizRespostaRepository respostaRepository;

	@BeforeEach
	void limparTentativas() {
		respostaRepository.deleteAll();
		tentativaPerguntaRepository.deleteAll();
		tentativaRepository.deleteAll();
	}

	@Test
	void iniciaRodadaComDezPerguntasSemExporGabaritoEComTrintaPerguntasCategorizadas() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/quiz/tentativas"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalPerguntas").value(10))
				.andExpect(jsonPath("$.perguntas.length()").value(10))
				.andExpect(jsonPath("$.perguntas[0].alternativas.length()").value(4))
				.andReturn();
		String body = result.getResponse().getContentAsString();
		assertFalse(body.contains("\"correta\""));
		assertFalse(body.contains("\"explicacao\""));

		Map<QuizCategoria, Long> quantidades = perguntaRepository.findAllByOrderByIdAsc().stream()
				.collect(Collectors.groupingBy(QuizPergunta::getCategoria, Collectors.counting()));
		assertEquals(30, perguntaRepository.count());
		for (QuizCategoria categoria : QuizCategoria.values()) {
			assertEquals(6L, quantidades.get(categoria));
		}
	}

	@Test
	void validaRespostaCalculaPontuacaoImpedeDuplicidadeFinalizaERegistraHistorico() throws Exception {
		QuizTentativaResponse tentativa = iniciarTentativa();
		Map<Long, QuizPergunta> perguntas = perguntaRepository.findAllByOrderByIdAsc().stream()
				.collect(Collectors.toMap(QuizPergunta::getId, pergunta -> pergunta));

		QuizPerguntaResponse primeira = tentativa.perguntas().get(0);
		QuizAlternativa corretaPrimeira = alternativaCorreta(perguntas.get(primeira.id()));
		responder(tentativa.id(), primeira.id(), corretaPrimeira.getId())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.correta").value(true))
				.andExpect(jsonPath("$.pontuacao").value(10))
				.andExpect(jsonPath("$.finalizada").value(false))
				.andExpect(jsonPath("$.explicacao").isNotEmpty());

		responder(tentativa.id(), primeira.id(), corretaPrimeira.getId())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("Esta pergunta já foi respondida nesta rodada."));

		for (int indice = 1; indice < tentativa.perguntas().size(); indice++) {
			QuizPerguntaResponse pergunta = tentativa.perguntas().get(indice);
			QuizPergunta entidade = perguntas.get(pergunta.id());
			Long alternativaErrada = entidade.getAlternativas().stream()
					.filter(alternativa -> !alternativa.isCorreta())
					.findFirst()
					.orElseThrow()
					.getId();
			var resposta = responder(tentativa.id(), pergunta.id(), alternativaErrada);
			if (indice == tentativa.perguntas().size() - 1) {
				resposta.andExpect(status().isOk())
						.andExpect(jsonPath("$.correta").value(false))
						.andExpect(jsonPath("$.acertos").value(1))
						.andExpect(jsonPath("$.erros").value(9))
						.andExpect(jsonPath("$.pontuacao").value(10))
						.andExpect(jsonPath("$.finalizada").value(true))
						.andExpect(jsonPath("$.resultado.percentual").value(10))
						.andExpect(jsonPath("$.resultado.classificacao").value("Aprendiz de Energia"));
			} else {
				resposta.andExpect(status().isOk());
			}
		}

		mockMvc.perform(get("/api/quiz/historico"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].tentativaId").value(tentativa.id()))
				.andExpect(jsonPath("$[0].acertos").value(1))
				.andExpect(jsonPath("$[0].erros").value(9))
				.andExpect(jsonPath("$[0].pontuacao").value(10))
				.andExpect(jsonPath("$[0].finalizada").value(true));

		responder(tentativa.id(), tentativa.perguntas().get(tentativa.perguntas().size() - 1).id(), 1L)
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("já foi finalizada")));
		assertEquals(10, respostaRepository.countByTentativaPergunta_Tentativa_Id(tentativa.id()));
		assertNotNull(tentativaRepository.findById(tentativa.id()).orElseThrow().getFinalizadaEm());
	}

	@Test
	void rejeitaAlternativaNulaOuQueNaoPertenceAPergunta() throws Exception {
		QuizTentativaResponse tentativa = iniciarTentativa();
		QuizPerguntaResponse pergunta = tentativa.perguntas().get(0);

		mockMvc.perform(post("/api/quiz/tentativas/{tentativaId}/perguntas/{perguntaId}/respostas",
						tentativa.id(), pergunta.id())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"alternativaId\":null}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detalhes.alternativaId").exists());

		Long alternativaDeOutraPergunta = tentativa.perguntas().stream()
				.filter(item -> !item.id().equals(pergunta.id()))
				.findFirst()
				.map(item -> item.alternativas().get(0).id())
				.orElseThrow();
		responder(tentativa.id(), pergunta.id(), alternativaDeOutraPergunta)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("A alternativa não pertence a esta pergunta."));
		assertTrue(respostaRepository.findAll().isEmpty());
	}

	@Test
	void atribuiClassificacaoDeEmbaixadorAoAlcancarCemPontos() throws Exception {
		QuizTentativaResponse tentativa = iniciarTentativa();
		Map<Long, QuizPergunta> perguntas = perguntaRepository.findAllByOrderByIdAsc().stream()
				.collect(Collectors.toMap(QuizPergunta::getId, pergunta -> pergunta));

		for (int indice = 0; indice < tentativa.perguntas().size(); indice++) {
			QuizPerguntaResponse pergunta = tentativa.perguntas().get(indice);
			Long alternativaCorreta = alternativaCorreta(perguntas.get(pergunta.id())).getId();
			var resposta = responder(tentativa.id(), pergunta.id(), alternativaCorreta);
			if (indice == tentativa.perguntas().size() - 1) {
				resposta.andExpect(status().isOk())
						.andExpect(jsonPath("$.resultado.acertos").value(10))
						.andExpect(jsonPath("$.resultado.erros").value(0))
						.andExpect(jsonPath("$.resultado.pontuacao").value(100))
						.andExpect(jsonPath("$.resultado.percentual").value(100))
						.andExpect(jsonPath("$.resultado.classificacao").value("Embaixador da Energia Limpa"));
			} else {
				resposta.andExpect(status().isOk());
			}
		}
	}

	private QuizTentativaResponse iniciarTentativa() {
		return quizService.iniciarTentativa();
	}

	private QuizAlternativa alternativaCorreta(QuizPergunta pergunta) {
		return pergunta.getAlternativas().stream()
				.filter(QuizAlternativa::isCorreta)
				.findFirst()
				.orElseThrow();
	}

	private org.springframework.test.web.servlet.ResultActions responder(Long tentativaId, Long perguntaId,
			Long alternativaId) throws Exception {
		return mockMvc.perform(post("/api/quiz/tentativas/{tentativaId}/perguntas/{perguntaId}/respostas",
						tentativaId, perguntaId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"alternativaId\":" + alternativaId + "}"));
	}
}
