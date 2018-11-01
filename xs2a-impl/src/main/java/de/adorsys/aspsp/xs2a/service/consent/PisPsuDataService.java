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

package de.adorsys.aspsp.xs2a.service.consent;

import de.adorsys.psd2.consent.api.service.PisConsentService;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PisPsuDataService {
    private final PisConsentService pisConsentService;

    public PsuIdData getPsuDataByPaymentId(String paymentId) {
        return pisConsentService.getPsuDataByPaymentId(paymentId)
                   .orElse(null);
    }

    public PsuIdData getPsuDataByConsentId(String consentId) {
        return pisConsentService.getPsuDataByConsentId(consentId)
                   .orElse(null);
    }
}
