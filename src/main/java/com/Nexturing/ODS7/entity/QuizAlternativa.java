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

@Entity
@Table(name = "quiz_alternativas")
public class QuizAlternativa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 300)
	private String texto;

	@Column(nullable = false)
	private boolean correta;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "pergunta_id", nullable = false)
	private QuizPergunta pergunta;

	protected QuizAlternativa() {
	}

	QuizAlternativa(String texto, boolean correta, QuizPergunta pergunta) {
		this.texto = texto;
		this.correta = correta;
		this.pergunta = pergunta;
	}

	public Long getId() {
		return id;
	}

	public String getTexto() {
		return texto;
	}

	public boolean isCorreta() {
		return correta;
	}

	public QuizPergunta getPergunta() {
		return pergunta;
	}
}
