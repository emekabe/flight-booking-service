package com.emekachukwulobe.flightbookingservice.dto.response;

import com.emekachukwulobe.flightbookingservice.domain.enums.FareClass;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class FareResponse {

    private UUID id;
    private FareClass fareClass;
    private BigDecimal price;
    private String currency;
}
