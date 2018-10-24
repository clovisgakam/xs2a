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

package de.adorsys.psd2.xs2a.service.authorization.pis;

import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisConsentAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisConsentAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.config.rest.consent.PisConsentRemoteUrls;
import de.adorsys.psd2.xs2a.service.authorization.pis.stage.PisScaStage;
import de.adorsys.psd2.xs2a.service.config.factory.PisScaStageAuthorisationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static de.adorsys.psd2.xs2a.service.config.factory.PisScaStageAuthorisationFactory.SERVICE_PREFIX;

@Service
@RequiredArgsConstructor
//TODO this class takes low-level communication to Consent-management-system. Should be migrated to consent-services package. All XS2A business-logic should be removed from here to XS2A services. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332
public class PisAuthorisationService {
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final PisConsentRemoteUrls remotePisConsentUrls;
    private final PisScaStageAuthorisationFactory pisScaStageAuthorisationFactory;

    /**
     * Sends a POST request to CMS to store created consent authorization
     *
     * @param paymentId String representation of identifier of stored consent
     * @return long representation of identifier of stored consent authorization
     */
    public CreatePisConsentAuthorisationResponse createPisConsentAuthorisation(String paymentId) {
        return consentRestTemplate.postForEntity(remotePisConsentUrls.createPisConsentAuthorisation(),
                                                 null, CreatePisConsentAuthorisationResponse.class, paymentId)
                   .getBody();
    }

    /**
     * Updates PIS consent authorization according to psu's sca methods
     *
     * @param request Provides transporting data when updating consent authorization
     * @return sca status
     */
    public UpdatePisConsentPsuDataResponse updatePisConsentAuthorisation(UpdatePisConsentPsuDataRequest request) {
        GetPisConsentAuthorisationResponse response = consentRestTemplate.exchange(remotePisConsentUrls.getPisConsentAuthorisationById(), HttpMethod.GET, new HttpEntity<>(request), GetPisConsentAuthorisationResponse.class, request.getAuthorizationId())
                                                                         .getBody();
        PisScaStage<UpdatePisConsentPsuDataRequest, GetPisConsentAuthorisationResponse, UpdatePisConsentPsuDataResponse> service = pisScaStageAuthorisationFactory.getService(SERVICE_PREFIX + response.getScaStatus().name());
        return service.apply(request, response);
    }

    public UpdatePisConsentPsuDataResponse doUpdatePisConsentAuthorisation(UpdatePisConsentPsuDataRequest request) {
        return consentRestTemplate.exchange(remotePisConsentUrls.updatePisConsentAuthorisation(), HttpMethod.PUT, new HttpEntity<>(request),
                                            UpdatePisConsentPsuDataResponse.class, request.getAuthorizationId()).getBody();
    }

    /**
     * Sends a POST request to CMS to store created consent authorization cancellation
     *
     * @param paymentId String representation of identifier of payment ID
     * @return long representation of identifier of stored consent authorization cancellation
     */
    public CreatePisConsentAuthorisationResponse createPisConsentAuthorisationCancellation(String paymentId) {
        return consentRestTemplate.postForEntity(remotePisConsentUrls.createPisConsentAuthorisationCancellation(),
            null, CreatePisConsentAuthorisationResponse.class, paymentId)
                   .getBody();
    }

    /**
     * Sends a GET request to CMS to get authorization sub resources
     *
     * @param paymentId String representation of identifier of payment ID
     * @return long representation of identifier of stored consent authorization cancellation
     */
    public String getCancellationAuthorisationSubResources(String paymentId) {
        return consentRestTemplate.getForEntity(remotePisConsentUrls.getCancellationAuthorisationSubResources(), String.class, paymentId).getBody();
    }
}
