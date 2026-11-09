package com.Nexturing.ODS7.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record SimulacaoRequest(
		@NotNull(message = "O identificador do aparelho é obrigatório.")
		@Positive(message = "O identificador do aparelho deve ser positivo.") Long aparelhoId,
		@NotNull(message = "As horas simuladas são obrigatórias.")
		@DecimalMin(value = "0.00", message = "As horas simuladas não podem ser negativas.")
		@DecimalMax(value = "24.00", message = "As horas simuladas não podem ultrapassar 24.")
		@Digits(integer = 2, fraction = 2, message = "As horas aceitam até duas casas decimais.") BigDecimal horasPorDiaSimuladas) {
}
