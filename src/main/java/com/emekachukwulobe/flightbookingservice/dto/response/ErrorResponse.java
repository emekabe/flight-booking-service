package com.emekachukwulobe.flightbookingservice.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class ErrorResponse {

    private final boolean success = false;
    private final int status;
    private final String error;
    private final String message;
    private final OffsetDateTime timestamp;

    /** Field-level validation errors from @Valid constraints. */
    private final Map<String, List<String>> validationErrors;
}
