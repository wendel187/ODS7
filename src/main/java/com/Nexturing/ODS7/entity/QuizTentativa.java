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

import java.time.Instant;

@Entity
@Table(name = "quiz_tentativas")
public class QuizTentativa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@Column(name = "iniciada_em", nullable = false)
	private Instant iniciadaEm;

	@Column(name = "finalizada_em")
	private Instant finalizadaEm;

	@Column(nullable = false)
	private int acertos;

	@Column(nullable = false)
	private int pontuacao;

	protected QuizTentativa() {
	}

	public QuizTentativa(Usuario usuario) {
		this.usuario = usuario;
		this.iniciadaEm = Instant.now();
	}

	public void registrarResposta(boolean correta) {
		if (correta) {
			acertos++;
			pontuacao += 10;
		}
	}

	public void finalizar() {
		finalizadaEm = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public Instant getIniciadaEm() {
		return iniciadaEm;
	}

	public Instant getFinalizadaEm() {
		return finalizadaEm;
	}

	public int getAcertos() {
		return acertos;
	}

	public int getPontuacao() {
		return pontuacao;
	}

	public boolean isFinalizada() {
		return finalizadaEm != null;
	}
}
