package com.Nexturing.ODS7.dto;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(
		Instant timestamp,
		int status,
		String erro,
		String mensagem,
		Map<String, String> detalhes) {
}
