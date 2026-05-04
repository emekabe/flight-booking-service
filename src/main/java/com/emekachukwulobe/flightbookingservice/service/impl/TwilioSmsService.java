package com.emekachukwulobe.flightbookingservice.service.impl;

import com.emekachukwulobe.flightbookingservice.service.TenantConfigService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Sends SMS messages via Twilio using per-tenant credentials from TenantConfig.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TwilioSmsService {

    private final TenantConfigService tenantConfigService;

    public void sendSms(UUID tenantId, String toNumber, String messageBody) {
        String accountSid = tenantConfigService.getConfigValue(tenantId, "SMS_ACCOUNT_SID", null);
        String authToken  = tenantConfigService.getConfigValue(tenantId, "SMS_AUTH_TOKEN", null);
        String fromNumber = tenantConfigService.getConfigValue(tenantId, "SMS_FROM_NUMBER", null);

        if (accountSid == null || authToken == null || fromNumber == null) {
            log.warn("Twilio not configured for tenant {}. Skipping SMS to {}", tenantId, toNumber);
            return;
        }

        Twilio.init(accountSid, authToken);

        Message.creator(
            new PhoneNumber(toNumber),
            new PhoneNumber(fromNumber),
            messageBody
        ).create();

        log.info("SMS sent to {} for tenant {}", toNumber, tenantId);
    }
}
