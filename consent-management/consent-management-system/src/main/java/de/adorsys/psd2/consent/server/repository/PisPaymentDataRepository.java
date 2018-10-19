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

package de.adorsys.psd2.consent.server.repository;

import de.adorsys.psd2.consent.api.CmsConsentStatus;
import de.adorsys.psd2.consent.server.domain.payment.PisPaymentData;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface PisPaymentDataRepository extends CrudRepository<PisPaymentData, Long> {
    Optional<List<PisPaymentData>> findByPaymentIdAndConsent_ConsentStatus(String paymentId, CmsConsentStatus cmsConsentStatus);
    Optional<PisPaymentData> findByPaymentId(String paymentId);
}
