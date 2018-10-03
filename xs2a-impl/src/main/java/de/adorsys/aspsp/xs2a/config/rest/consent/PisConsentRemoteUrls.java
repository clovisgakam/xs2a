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

package de.adorsys.aspsp.xs2a.config.rest.consent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PisConsentRemoteUrls implements AspspConsentDataRemoteUrls {
    @Value("${consent-service.baseurl:http://localhost:38080/api/v1}")
    private String consentServiceBaseUrl;

    /**
     * Returns URL-string to CMS endpoint that creates pis consent
     *
     * @return String
     */
    public String createPisConsent() {
        return consentServiceBaseUrl + "/pis/consent/";
    }

    /**
     * Returns URL-string to CMS endpoint that updates pis consent status
     *
     * @return String
     */
    public String updatePisConsentStatus() {
        return consentServiceBaseUrl + "/pis/consent/{consentId}/status/{status}";
    }

    /**
     * Returns URL-string to CMS endpoint that gets pis consent status by ID
     *
     * @return String
     */
    public String getPisConsentStatusById() {
        return consentServiceBaseUrl + "/pis/consent/{consentId}/status";
    }

    /**
     * Returns URL-string to CMS endpoint that gets pis consent by ID
     *
     * @return String
     */
    public String getPisConsentById() {
        return consentServiceBaseUrl + "/pis/consent/{consentId}";
    }

    /**
     * @return String consentId
     * Method: POST
     * PathVariables: String paymentId
     */
    public String createPisConsentAuthorisation() {
        return consentServiceBaseUrl + "/pis/consent/{payment-id}/authorisations";
    }

    public String updatePisConsentAuthorisation() {
        return consentServiceBaseUrl + "/pis/consent/authorisations/{authorisation-id}";
    }

    public String getPisConsentAuthorisationById() {
        return consentServiceBaseUrl + "/pis/consent/authorisations/{authorisation-id}";
    }

    /**
     * Returns URL-string to CMS endpoint that gets aspsp consent data by payment ID
     *
     * @return String
     */
    @Override
    public String getAspspConsentData() {
        return consentServiceBaseUrl + "/pis/payment/{payment-id}/aspsp-consent-data";
    }

    /**
     * Returns URL-string to CMS endpoint that updates aspsp consent data by consent ID
     *
     * @return String
     */
    @Override
    public String updateAspspConsentData() {
        return consentServiceBaseUrl + "/pis/consent/{consent-id}/aspsp-consent-data";
    }
}
