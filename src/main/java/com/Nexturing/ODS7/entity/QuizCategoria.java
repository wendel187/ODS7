package com.Nexturing.ODS7.entity;

public enum QuizCategoria {
	ECONOMIA_DOMESTICA("Economia doméstica"),
	ELETRODOMESTICOS("Eletrodomésticos"),
	ENERGIA_SOLAR("Energia solar"),
	ENERGIA_RENOVAVEL("Energia renovável"),
	SUSTENTABILIDADE("Sustentabilidade");

	private final String rotulo;

	QuizCategoria(String rotulo) {
		this.rotulo = rotulo;
	}

	public String getRotulo() {
		return rotulo;
	}
}
