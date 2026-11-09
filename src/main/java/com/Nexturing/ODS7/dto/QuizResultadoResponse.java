package com.Nexturing.ODS7.dto;

public record QuizResultadoResponse(int acertos, int erros, int pontuacao, int percentual,
		String classificacao) {
}
