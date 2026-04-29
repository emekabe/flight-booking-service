package com.emekachukwulobe.flightbookingservice.integration;

import com.emekachukwulobe.flightbookingservice.domain.enums.ProviderType;
import com.emekachukwulobe.flightbookingservice.exception.PaymentException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resolves the correct {@link PaymentProvider} for a given {@link ProviderType}
 * at runtime. Providers register themselves automatically via Spring injection.
 */
@Component
public class PaymentProviderRegistry {

    private final Map<ProviderType, PaymentProvider> registry;

    public PaymentProviderRegistry(List<PaymentProvider> providers) {
        this.registry = providers.stream()
            .filter(p -> p.providerType() != null)
            .collect(Collectors.toMap(PaymentProvider::providerType, Function.identity()));
    }

    public PaymentProvider resolve(ProviderType type) {
        PaymentProvider provider = registry.get(type);
        if (provider == null) {
            throw new PaymentException("No payment provider registered for type: " + type);
        }
        return provider;
    }
}
