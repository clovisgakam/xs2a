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
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import org.springframework.stereotype.Service;

import static de.adorsys.aspsp.xs2a.consent.api.CmsScaStatus.FINALISED;

@Service("SCAMETHODSELECTED")
public class ScaMethodSelectedStage extends ScaStage<UpdatePisConsentPsuDataRequest, GetPisConsentAuthorisationResponse, UpdatePisConsentPsuDataResponse> {

    public ScaMethodSelectedStage(PaymentSpi paymentSpi, PisAuthorisationService pisAuthorisationService, SpiCmsPisMapper spiCmsPisMapper, PisConsentDataService pisConsentDataService) {
        super(paymentSpi, pisAuthorisationService, spiCmsPisMapper, pisConsentDataService);
    }

    @Override
    public UpdatePisConsentPsuDataResponse apply(UpdatePisConsentPsuDataRequest request, GetPisConsentAuthorisationResponse response) {
        AspspConsentData aspspConsentData = pisConsentDataService.getAspspConsentData(request.getPaymentId());
        aspspConsentData = paymentSpi.applyStrongUserAuthorisation(
            spiCmsPisMapper.buildSpiPaymentConfirmation(
                    request,
                    response.getConsentId()),
            aspspConsentData
            ).getAspspConsentData();
        pisConsentDataService.updateAspspConsentData(aspspConsentData);
        aspspConsentData = paymentSpi.executePayment(response.getPaymentType(), response.getPayments(), aspspConsentData).getAspspConsentData();
        pisConsentDataService.updateAspspConsentData(aspspConsentData);
        request.setScaStatus(FINALISED); // TODO check the paymentSpi result first https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/338
        return pisAuthorisationService.doUpdatePisConsentAuthorisation(request);
    }
}
