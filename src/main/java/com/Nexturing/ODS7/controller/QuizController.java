package com.Nexturing.ODS7.controller;

import com.Nexturing.ODS7.dto.QuizHistoricoResponse;
import com.Nexturing.ODS7.dto.QuizRespostaRequest;
import com.Nexturing.ODS7.dto.QuizRespostaResponse;
import com.Nexturing.ODS7.dto.QuizTentativaResponse;
import com.Nexturing.ODS7.service.QuizService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
@Validated
public class QuizController {

	private final QuizService quizService;

	public QuizController(QuizService quizService) {
		this.quizService = quizService;
	}

	@PostMapping("/tentativas")
	public QuizTentativaResponse iniciarTentativa() {
		return quizService.iniciarTentativa();
	}

	@PostMapping("/tentativas/{tentativaId}/perguntas/{perguntaId}/respostas")
	public QuizRespostaResponse responder(
			@PathVariable @Positive(message = "O identificador da tentativa deve ser positivo.") Long tentativaId,
			@PathVariable @Positive(message = "O identificador da pergunta deve ser positivo.") Long perguntaId,
			@Valid @RequestBody QuizRespostaRequest request) {
		return quizService.responder(tentativaId, perguntaId, request);
	}

	@GetMapping("/historico")
	public List<QuizHistoricoResponse> listarHistorico() {
		return quizService.listarHistorico();
	}
}
