package com.Nexturing.ODS7.controller;

import com.Nexturing.ODS7.dto.AparelhoRequest;
import com.Nexturing.ODS7.dto.AparelhoResponse;
import com.Nexturing.ODS7.service.AparelhoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/aparelhos")
@Validated
public class AparelhoController {

	private final AparelhoService aparelhoService;

	public AparelhoController(AparelhoService aparelhoService) {
		this.aparelhoService = aparelhoService;
	}

	@GetMapping
	public List<AparelhoResponse> listar() {
		return aparelhoService.listar();
	}

	@GetMapping("/{id}")
	public AparelhoResponse buscar(@PathVariable @Positive(message = "O identificador deve ser positivo.") Long id) {
		return aparelhoService.buscar(id);
	}

	@PostMapping
	public ResponseEntity<AparelhoResponse> criar(@Valid @RequestBody AparelhoRequest request) {
		AparelhoResponse resposta = aparelhoService.criar(request);
		URI localizacao = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(resposta.id())
				.toUri();
		return ResponseEntity.created(localizacao).body(resposta);
	}

	@PutMapping("/{id}")
	public AparelhoResponse atualizar(@PathVariable @Positive(message = "O identificador deve ser positivo.") Long id,
			@Valid @RequestBody AparelhoRequest request) {
		return aparelhoService.atualizar(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluir(@PathVariable @Positive(message = "O identificador deve ser positivo.") Long id) {
		aparelhoService.excluir(id);
		return ResponseEntity.noContent().build();
	}
}
