package com.Nexturing.ODS7.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_perguntas")
public class QuizPergunta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private QuizCategoria categoria;

	@Column(nullable = false, length = 500)
	private String enunciado;

	@Column(nullable = false, length = 1000)
	private String explicacao;

	@OneToMany(mappedBy = "pergunta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<QuizAlternativa> alternativas = new ArrayList<>();

	protected QuizPergunta() {
	}

	public QuizPergunta(QuizCategoria categoria, String enunciado, String explicacao, List<String> textos,
			int indiceCorreto) {
		this.categoria = categoria;
		this.enunciado = enunciado;
		this.explicacao = explicacao;
		if (textos.size() != 4 || indiceCorreto < 0 || indiceCorreto >= textos.size()) {
			throw new IllegalArgumentException("Uma pergunta precisa de quatro alternativas e uma resposta correta.");
		}
		for (int indice = 0; indice < textos.size(); indice++) {
			alternativas.add(new QuizAlternativa(textos.get(indice), indice == indiceCorreto, this));
		}
	}

	public Long getId() {
		return id;
	}

	public QuizCategoria getCategoria() {
		return categoria;
	}

	public String getEnunciado() {
		return enunciado;
	}

	public String getExplicacao() {
		return explicacao;
	}

	public List<QuizAlternativa> getAlternativas() {
		return List.copyOf(alternativas);
	}
}
