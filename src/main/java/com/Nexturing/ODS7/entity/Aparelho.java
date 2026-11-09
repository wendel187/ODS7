package com.Nexturing.ODS7.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "aparelhos")
public class Aparelho {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 80)
	private String nome;

	@Column(name = "potencia_watts", nullable = false, precision = 10, scale = 2)
	private BigDecimal potenciaWatts;

	@Column(name = "horas_por_dia", nullable = false, precision = 5, scale = 2)
	private BigDecimal horasPorDia;

	@Column(name = "dias_por_mes", nullable = false)
	private Integer diasPorMes;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	protected Aparelho() {
	}

	public Aparelho(String nome, BigDecimal potenciaWatts, BigDecimal horasPorDia,
			Integer diasPorMes, Usuario usuario) {
		atualizar(nome, potenciaWatts, horasPorDia, diasPorMes);
		this.usuario = usuario;
	}

	public void atualizar(String nome, BigDecimal potenciaWatts, BigDecimal horasPorDia,
			Integer diasPorMes) {
		this.nome = nome;
		this.potenciaWatts = potenciaWatts;
		this.horasPorDia = horasPorDia;
		this.diasPorMes = diasPorMes;
	}

	public Long getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public BigDecimal getPotenciaWatts() {
		return potenciaWatts;
	}

	public BigDecimal getHorasPorDia() {
		return horasPorDia;
	}

	public Integer getDiasPorMes() {
		return diasPorMes;
	}

	public Usuario getUsuario() {
		return usuario;
	}
}
