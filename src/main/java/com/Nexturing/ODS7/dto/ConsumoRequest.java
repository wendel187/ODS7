package com.Nexturing.ODS7.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ConsumoRequest(
		@NotNull(message = "A potência é obrigatória.")
		@DecimalMin(value = "0.01", message = "A potência deve ser maior que zero.")
		@DecimalMax(value = "20000.00", message = "A potência não pode ultrapassar 20.000 W.")
		@Digits(integer = 5, fraction = 2, message = "A potência aceita até duas casas decimais.") BigDecimal potenciaWatts,
		@NotNull(message = "As horas por dia são obrigatórias.")
		@DecimalMin(value = "0.00", message = "As horas não podem ser negativas.")
		@DecimalMax(value = "24.00", message = "O uso diário não pode ultrapassar 24 horas.")
		@Digits(integer = 2, fraction = 2, message = "As horas aceitam até duas casas decimais.") BigDecimal horasPorDia,
		@NotNull(message = "Os dias de uso por mês são obrigatórios.")
		@DecimalMin(value = "0", message = "Os dias não podem ser negativos.")
		@DecimalMax(value = "31", message = "Os dias não podem ultrapassar 31.") Integer diasPorMes) {
}
