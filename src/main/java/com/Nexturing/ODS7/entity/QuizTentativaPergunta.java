package com.Nexturing.ODS7.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "quiz_tentativa_perguntas", uniqueConstraints = {
		@UniqueConstraint(name = "uk_quiz_tentativa_ordem", columnNames = { "tentativa_id", "ordem" }),
		@UniqueConstraint(name = "uk_quiz_tentativa_pergunta", columnNames = { "tentativa_id", "pergunta_id" })
})
public class QuizTentativaPergunta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tentativa_id", nullable = false)
	private QuizTentativa tentativa;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "pergunta_id", nullable = false)
	private QuizPergunta pergunta;

	private int ordem;

	protected QuizTentativaPergunta() {
	}

	public QuizTentativaPergunta(QuizTentativa tentativa, QuizPergunta pergunta, int ordem) {
		this.tentativa = tentativa;
		this.pergunta = pergunta;
		this.ordem = ordem;
	}

	public Long getId() {
		return id;
	}

	public QuizTentativa getTentativa() {
		return tentativa;
	}

	public QuizPergunta getPergunta() {
		return pergunta;
	}

	public int getOrdem() {
		return ordem;
	}
}
