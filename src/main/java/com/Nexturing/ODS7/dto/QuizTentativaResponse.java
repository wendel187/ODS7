package com.Nexturing.ODS7.dto;

import java.util.List;

public record QuizTentativaResponse(Long id, int totalPerguntas, List<QuizPerguntaResponse> perguntas) {
}
