package com.Nexturing.ODS7.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "quiz_respostas")
public class QuizResposta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tentativa_pergunta_id", nullable = false, unique = true)
	private QuizTentativaPergunta tentativaPergunta;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "alternativa_id", nullable = false)
	private QuizAlternativa alternativa;

	@Column(nullable = false)
	private boolean correta;

	@Column(name = "respondida_em", nullable = false)
	private Instant respondidaEm;

	protected QuizResposta() {
	}

	public QuizResposta(QuizTentativaPergunta tentativaPergunta, QuizAlternativa alternativa, boolean correta) {
		this.tentativaPergunta = tentativaPergunta;
		this.alternativa = alternativa;
		this.correta = correta;
		this.respondidaEm = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public QuizTentativaPergunta getTentativaPergunta() {
		return tentativaPergunta;
	}

	public QuizAlternativa getAlternativa() {
		return alternativa;
	}

	public boolean isCorreta() {
		return correta;
	}

	public Instant getRespondidaEm() {
		return respondidaEm;
	}
}
