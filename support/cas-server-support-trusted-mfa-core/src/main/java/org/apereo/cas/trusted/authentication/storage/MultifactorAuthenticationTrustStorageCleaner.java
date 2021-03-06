package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.configuration.model.support.mfa.TrustedDevicesMultifactorProperties;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.DateTimeUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import java.time.LocalDateTime;

/**
 * This is {@link MultifactorAuthenticationTrustStorageCleaner}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(transactionManager = "transactionManagerMfaAuthnTrust")
@Slf4j
@RequiredArgsConstructor
@Getter
public class MultifactorAuthenticationTrustStorageCleaner {
    private final TrustedDevicesMultifactorProperties trustedProperties;
    private final MultifactorAuthenticationTrustStorage storage;

    /**
     * Clean up expired records.
     */
    @Scheduled(initialDelayString = "${cas.authn.mfa.trusted.cleaner.schedule.startDelay:PT10S}",
        fixedDelayString = "${cas.authn.mfa.trusted.cleaner.schedule.repeatInterval:PT60S}")
    public void clean() {

        if (!trustedProperties.getCleaner().getSchedule().isEnabled()) {
            LOGGER.debug("[{}] is disabled. Expired trusted authentication records will not automatically be cleaned up by CAS", getClass().getName());
            return;
        }

        try {
            LOGGER.debug("Proceeding to clean up expired trusted authentication records...");
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
            val validDate = LocalDateTime.now().minus(trustedProperties.getExpiration(),
                DateTimeUtils.toChronoUnit(trustedProperties.getTimeUnit()));
            LOGGER.debug("Expiring records that are on/before [{}]", validDate);
            this.storage.expire(validDate);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
