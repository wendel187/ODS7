package com.Nexturing.ODS7.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "configuracoes_energia")
public class ConfiguracaoEnergia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "tarifa_kwh", precision = 10, scale = 4)
	private BigDecimal tarifaKwh;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false, unique = true)
	private Usuario usuario;

	protected ConfiguracaoEnergia() {
	}

	public ConfiguracaoEnergia(Usuario usuario, BigDecimal tarifaKwh) {
		this.usuario = usuario;
		this.tarifaKwh = tarifaKwh;
	}

	public void atualizarTarifa(BigDecimal tarifaKwh) {
		this.tarifaKwh = tarifaKwh;
	}

	public Long getId() {
		return id;
	}

	public BigDecimal getTarifaKwh() {
		return tarifaKwh;
	}
}
