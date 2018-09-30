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

package de.adorsys.psd2.aspsp.profile.service;

import de.adorsys.psd2.aspsp.profile.domain.*;

import java.util.List;

public interface AspspProfileUpdateService {
    void updateFrequencyPerDay(int frequencyPerDay);

    void updateCombinedServiceIndicator(boolean combinedServiceIndicator);

    void updateBankOfferedConsentSupport(boolean bankOfferedConsentSupport);

    void updateAvailablePaymentProducts(List<String> availablePaymentProducts);

    void updateAvailablePaymentTypes(List<String> availablePaymentTypes);

    void updateScaApproach(ScaApproach scaApproach);

    void updateTppSignatureRequired(boolean tppSignatureRequired);

    void updatePisRedirectUrlToAspsp(String redirectUrlToAspsp);

    void updateAisRedirectUrlToAspsp(String redirectUrlToAspsp);

    void updateMulticurrencyAccountLevel(MulticurrencyAccountLevel multicurrencyAccountLevel);

    void updateAvailableBookingStatuses(List<BookingStatus> availableBookingStatuses);

    void updateSupportedAccountReferenceFields(List<SupportedAccountReferenceField> fields);

    void updateConsentLifetime(int consentLifetime);

    void updateTransactionLifetime(int transactionLifetime);

    void updateAllPsd2Support(boolean allPsd2Support);

    void updateAuthorisationStartType(AuthorisationStartType authorisationStartType);

    void updateTransactionsWithoutBalancesSupported(boolean transactionsWithoutBalancesSupported);

    void updatePaymentCancellationAuthorisationMandated(boolean paymentCancellationAuthorisationMandated);
}
