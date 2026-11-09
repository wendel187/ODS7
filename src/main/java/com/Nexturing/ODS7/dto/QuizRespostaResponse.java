package com.Nexturing.ODS7.dto;

public record QuizRespostaResponse(Long perguntaId, Long alternativaSelecionadaId, Long alternativaCorretaId,
		boolean correta, String explicacao, int acertos, int erros, int pontuacao, boolean finalizada,
		QuizResultadoResponse resultado) {
}
