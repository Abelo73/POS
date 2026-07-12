package com.novapos.shared.web;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        String code,
        String message,
        Map<String, Object> details,
        String traceId,
        Instant timestamp
) {
    public ErrorResponse {
        if (details == null) {
            details = Map.of();
        }
    }
}
