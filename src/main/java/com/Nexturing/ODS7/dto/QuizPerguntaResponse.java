package com.Nexturing.ODS7.dto;

import java.util.List;

public record QuizPerguntaResponse(Long id, String categoria, String enunciado,
		List<QuizAlternativaResponse> alternativas) {
}
