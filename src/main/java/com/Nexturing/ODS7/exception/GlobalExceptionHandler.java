package com.Nexturing.ODS7.exception;

import com.Nexturing.ODS7.dto.ApiErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> tratarNaoEncontrado(ResourceNotFoundException exception) {
		return criarResposta(HttpStatus.NOT_FOUND, exception.getMessage(), Map.of());
	}

	@ExceptionHandler(BusinessRuleException.class)
	public ResponseEntity<ApiErrorResponse> tratarRegraNegocio(BusinessRuleException exception) {
		return criarResposta(HttpStatus.BAD_REQUEST, exception.getMessage(), Map.of());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> tratarValidacao(MethodArgumentNotValidException exception) {
		Map<String, String> detalhes = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors().forEach(error ->
				detalhes.putIfAbsent(error.getField(), error.getDefaultMessage()));
		return criarResposta(HttpStatus.BAD_REQUEST, "Os dados enviados são inválidos.", detalhes);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiErrorResponse> tratarViolacao(ConstraintViolationException exception) {
		return criarResposta(HttpStatus.BAD_REQUEST, "Os dados enviados são inválidos.",
				Map.of("detalhes", exception.getMessage()));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> tratarCorpoInvalido(HttpMessageNotReadableException exception) {
		return criarResposta(HttpStatus.BAD_REQUEST, "O corpo da requisição está ausente ou malformado.", Map.of());
	}

	private ResponseEntity<ApiErrorResponse> criarResposta(HttpStatus status, String mensagem,
			Map<String, String> detalhes) {
		ApiErrorResponse resposta = new ApiErrorResponse(
				Instant.now(), status.value(), nomeErro(status), mensagem, detalhes);
		return ResponseEntity.status(status).body(resposta);
	}

	private String nomeErro(HttpStatus status) {
		return switch (status) {
			case BAD_REQUEST -> "Requisição inválida";
			case NOT_FOUND -> "Não encontrado";
			default -> status.getReasonPhrase();
		};
	}
}
