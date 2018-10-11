/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.aspsp.xs2a.config;

import de.adorsys.aspsp.xs2a.service.authorization.ais.*;
import de.adorsys.aspsp.xs2a.service.authorization.pis.*;
import de.adorsys.aspsp.xs2a.service.consent.AisConsentDataService;
import de.adorsys.aspsp.xs2a.service.consent.AisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aPisConsentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.SpiResponseStatusToXs2aMessageErrorCodeMapper;
import de.adorsys.aspsp.xs2a.service.payment.*;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.aspsp.xs2a.spi.service.v2.AisConsentSpi;
import de.adorsys.psd2.aspsp.profile.domain.ScaApproach;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static de.adorsys.psd2.aspsp.profile.domain.ScaApproach.*;

// TODO refactor to AbstractFactory https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/298
@Configuration
@RequiredArgsConstructor
public class ScaAuthorizationConfig {
    private final AspspProfileServiceWrapper aspspProfileService;
    private final AisConsentDataService aisConsentDataService;

    @Bean
    public ScaPaymentService scaPaymentService(
                                               OauthScaPaymentService oauthScaPaymentService,
                                               RedirectScaPaymentService redirectScaPaymentService,
                                               EmbeddedScaPaymentService embeddedScaPaymentService,
                                               DecoupedScaPaymentService decoupedScaPaymentService) {
        ScaApproach scaApproach = getScaApproach();
        if (OAUTH == scaApproach) {
            return oauthScaPaymentService;
        }
        if (DECOUPLED == scaApproach) {
            return decoupedScaPaymentService;
        }
        if (EMBEDDED == scaApproach) {
            return embeddedScaPaymentService;
        }
        return redirectScaPaymentService;
    }

    @Bean
    public AisAuthorizationService aisAuthorizationService(AisConsentSpi aisConsentSpi, AisConsentService aisConsentService,
                                                           Xs2aAisConsentMapper aisConsentMapper, SpiResponseStatusToXs2aMessageErrorCodeMapper messageErrorCodeMapper
    ) {
        switch (getScaApproach()) {
            case OAUTH:
                return new OauthAisAuthorizationService();
            case DECOUPLED:
                return new DecoupledAisAuthorizationService();
            case EMBEDDED:
                return new EmbeddedAisAuthorizationService(aisConsentService, aisConsentMapper, aisConsentDataService, aisConsentSpi, messageErrorCodeMapper);
            default:
                return new RedirectAisAuthorizationService();
        }
    }

    @Bean
    public PisScaAuthorisationService pisAuthorizationService(PisAuthorisationService authorisationService,
                                                              Xs2aPisConsentMapper pisConsentMapper) {
        ScaApproach scaApproach = getScaApproach();
        if (OAUTH == scaApproach) {
            return new OauthPisScaAuthorisationService();
        }
        if (DECOUPLED == scaApproach) {
            return new DecoupledPisScaAuthorisationService();
        }
        if (EMBEDDED == scaApproach) {
            return new EmbeddedPisScaAuthorisationService(authorisationService, pisConsentMapper);
        }
        return new RedirectPisScaAuthorisationService(authorisationService, pisConsentMapper);
    }

    private ScaApproach getScaApproach() {
        return aspspProfileService.getScaApproach();
    }
}
