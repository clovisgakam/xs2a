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

package de.adorsys.aspsp.xs2a.service.authorization;

import de.adorsys.aspsp.xs2a.config.factory.ScaStage;
import de.adorsys.aspsp.xs2a.consent.api.pis.authorisation.GetPisConsentAuthorisationResponse;
import de.adorsys.aspsp.xs2a.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import de.adorsys.aspsp.xs2a.consent.api.pis.authorisation.UpdatePisConsentPsuDataResponse;
import de.adorsys.aspsp.xs2a.service.PisConsentDataService;
import de.adorsys.aspsp.xs2a.service.authorization.pis.PisAuthorisationService;
import de.adorsys.aspsp.xs2a.service.mapper.consent.SpiCmsPisMapper;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.psu.SpiScaMethod;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static de.adorsys.aspsp.xs2a.consent.api.CmsScaStatus.SCAMETHODSELECTED;

@Slf4j
@Service("PSUAUTHENTICATED")
public class ScaAuthenticatedStage extends ScaStage<UpdatePisConsentPsuDataRequest, GetPisConsentAuthorisationResponse, UpdatePisConsentPsuDataResponse> {

    public ScaAuthenticatedStage(PaymentSpi paymentSpi, PisAuthorisationService pisAuthorisationService, SpiCmsPisMapper spiCmsPisMapper, PisConsentDataService pisConsentDataService) {
        super(paymentSpi, pisAuthorisationService, spiCmsPisMapper, pisConsentDataService);
    }

    @Override
    public UpdatePisConsentPsuDataResponse apply(UpdatePisConsentPsuDataRequest request, GetPisConsentAuthorisationResponse pisConsentAuthorisationResponse) {
        AspspConsentData aspspConsentData = paymentSpi.performStrongUserAuthorisation(request.getPsuId(), getMethod(request.getAuthenticationMethodId()),pisConsentDataService.getConsentDataByPaymentId(request.getPaymentId())).getAspspConsentData();
        pisConsentDataService.updateConsentData(aspspConsentData);
        request.setScaStatus(SCAMETHODSELECTED);
        return pisAuthorisationService.doUpdatePisConsentAuthorisation(request);
    }

    private SpiScaMethod getMethod(String method){ //TODO: https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332
        SpiScaMethod scaMethod =SpiScaMethod.PUSH_OTP;
        try {
            scaMethod = SpiScaMethod.valueOf(method);
        }
        catch (IllegalArgumentException e){
            log.error("Sca Method could not be parsed", e.getLocalizedMessage());
        }
        return scaMethod;
    }
}
