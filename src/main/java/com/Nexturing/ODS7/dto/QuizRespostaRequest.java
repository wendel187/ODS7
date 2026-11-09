package com.Nexturing.ODS7.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record QuizRespostaRequest(
		@NotNull(message = "Selecione uma alternativa.")
		@Positive(message = "A alternativa selecionada é inválida.")
		Long alternativaId) {
}
