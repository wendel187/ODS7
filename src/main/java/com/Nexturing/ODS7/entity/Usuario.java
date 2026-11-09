package com.Nexturing.ODS7.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 30)
	private String codigo;

	@Column(nullable = false, length = 100)
	private String nome;

	@OneToMany(mappedBy = "usuario")
	private List<Aparelho> aparelhos = new ArrayList<>();

	@OneToOne(mappedBy = "usuario")
	private ConfiguracaoEnergia configuracaoEnergia;

	protected Usuario() {
	}

	public Usuario(String codigo, String nome) {
		this.codigo = codigo;
		this.nome = nome;
	}

	public Long getId() {
		return id;
	}

	public String getCodigo() {
		return codigo;
	}

	public String getNome() {
		return nome;
	}
}
