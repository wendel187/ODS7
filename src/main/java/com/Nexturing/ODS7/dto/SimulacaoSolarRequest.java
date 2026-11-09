package com.Nexturing.ODS7.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SimulacaoSolarRequest(
		@NotNull(message = "Informe o valor médio mensal da conta.")
		@DecimalMin(value = "0.01", message = "O valor da conta deve ser maior que zero.")
		@DecimalMax(value = "10000000.00", message = "O valor da conta informado é muito alto.")
		@Digits(integer = 8, fraction = 2, message = "O valor da conta aceita até duas casas decimais.") BigDecimal valorContaMensal,
		@DecimalMin(value = "0.00", message = "O consumo não pode ser negativo.")
		@DecimalMax(value = "1000000.00", message = "O consumo informado é muito alto.")
		@Digits(integer = 7, fraction = 2, message = "O consumo aceita até duas casas decimais.") BigDecimal consumoMensalKwh,
		@NotBlank(message = "Informe a cidade.")
		@Size(max = 80, message = "A cidade deve ter no máximo 80 caracteres.") String cidade,
		@NotBlank(message = "Informe a sigla do estado.")
		@Pattern(regexp = "(?i)^[A-Z]{2}$", message = "Informe a sigla do estado com duas letras.") String estado,
		@NotNull(message = "Informe a tarifa estimada por kWh.")
		@DecimalMin(value = "0.0001", message = "A tarifa deve ser maior que zero.")
		@DecimalMax(value = "100.0000", message = "A tarifa informada é muito alta.")
		@Digits(integer = 3, fraction = 4, message = "A tarifa aceita até quatro casas decimais.") BigDecimal tarifaKwh,
		@DecimalMin(value = "0.00", message = "O investimento não pode ser negativo.")
		@DecimalMax(value = "100000000.00", message = "O investimento informado é muito alto.")
		@Digits(integer = 9, fraction = 2, message = "O investimento aceita até duas casas decimais.") BigDecimal investimentoInicial,
		@DecimalMin(value = "0.00", message = "A geração solar não pode ser negativa.")
		@DecimalMax(value = "1000000.00", message = "A geração mensal informada é muito alta.")
		@Digits(integer = 7, fraction = 2, message = "A geração aceita até duas casas decimais.") BigDecimal geracaoMensalKwh,
		@NotNull(message = "Informe os custos mensais que não serão compensados; use zero somente se não tiver uma estimativa.")
		@DecimalMin(value = "0.00", message = "O custo não compensável não pode ser negativo.")
		@DecimalMax(value = "10000000.00", message = "O custo não compensável informado é muito alto.")
		@Digits(integer = 8, fraction = 2, message = "O custo não compensável aceita até duas casas decimais.") BigDecimal custoMensalNaoCompensavel,
		@NotNull(message = "Informe a parcela estimada da tarifa compensável.")
		@DecimalMin(value = "0.00", message = "A parcela compensável não pode ser negativa.")
		@DecimalMax(value = "100.00", message = "A parcela compensável não pode ultrapassar 100%.")
		@Digits(integer = 3, fraction = 2, message = "A parcela compensável aceita até duas casas decimais.") BigDecimal percentualTarifaCompensavel) {
}
