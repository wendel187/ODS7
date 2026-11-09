package com.Nexturing.ODS7.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ConfiguracaoRequest(
		@NotNull(message = "A tarifa por kWh é obrigatória.")
		@DecimalMin(value = "0.0001", message = "A tarifa deve ser maior que zero.")
		@DecimalMax(value = "100.0000", message = "A tarifa não pode ultrapassar R$ 100,00 por kWh.")
		@Digits(integer = 3, fraction = 4, message = "A tarifa aceita até quatro casas decimais.") BigDecimal tarifaKwh) {
}
