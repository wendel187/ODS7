package com.Nexturing.ODS7.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AparelhoRequest(
		@NotBlank(message = "O nome do aparelho é obrigatório.")
		@Size(max = 80, message = "O nome deve ter no máximo 80 caracteres.") String nome,
		@NotNull(message = "A potência é obrigatória.")
		@DecimalMin(value = "0.01", message = "A potência deve ser maior que zero.")
		@DecimalMax(value = "20000.00", message = "A potência não pode ultrapassar 20.000 W.")
		@Digits(integer = 5, fraction = 2, message = "A potência aceita até duas casas decimais.") BigDecimal potenciaWatts,
		@NotNull(message = "As horas de uso por dia são obrigatórias.")
		@DecimalMin(value = "0.00", message = "As horas de uso não podem ser negativas.")
		@DecimalMax(value = "24.00", message = "O uso diário não pode ultrapassar 24 horas.")
		@Digits(integer = 2, fraction = 2, message = "As horas aceitam até duas casas decimais.") BigDecimal horasPorDia,
		@NotNull(message = "Os dias de uso por mês são obrigatórios.")
		@DecimalMin(value = "0", message = "Os dias de uso não podem ser negativos.")
		@DecimalMax(value = "31", message = "Os dias de uso não podem ultrapassar 31.") Integer diasPorMes) {
}
