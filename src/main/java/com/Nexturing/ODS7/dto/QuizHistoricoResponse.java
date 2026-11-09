package com.Nexturing.ODS7.dto;

import java.time.Instant;

public record QuizHistoricoResponse(Long tentativaId, Instant iniciadaEm, Instant finalizadaEm,
		int acertos, int erros, int pontuacao, int percentual, String classificacao, boolean finalizada) {
}
