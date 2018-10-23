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

package de.adorsys.aspsp.xs2a.spi.service;

import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiTransaction;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @deprecated since 1.8. Will be removed in 1.10
 * @see de.adorsys.psd2.xs2a.spi.service.AisConsentSpi
 * @see de.adorsys.psd2.xs2a.spi.service.AccountSpi
 */
@Deprecated
public interface AccountSpi {
    /**
     * Queries ASPSP to get List of transactions dependant on period and accountId
     *
     * @param accountId        String representation of ASPSP account primary identifier
     * @param dateFrom         Date representing the beginning of the search period
     * @param dateTo           Date representing the ending of the search period
     * @param aspspConsentData Encrypted data that may be stored in the consent management system in the consent linked to a request.<br>
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return List of transactions
     */
    SpiResponse<List<SpiTransaction>> readTransactionsByPeriod(String accountId, LocalDate dateFrom, LocalDate dateTo, AspspConsentData aspspConsentData);

    /**
     * Queries ASPSP to (GET) transaction by its primary identifier and account identifier
     *
     * @param transactionId    String representation of ASPSP primary identifier of transaction
     * @param accountId        String representation of ASPSP account primary identifier
     * @param aspspConsentData Encrypted data that may be stored in the consent management system in the consent linked to a request.<br>
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return Transaction
     */
    SpiResponse<Optional<SpiTransaction>> readTransactionById(String transactionId, String accountId, AspspConsentData aspspConsentData);

    /**
     * Queries ASPSP to (GET) AccountDetails by primary ASPSP account identifier
     *
     * @param accountId        String representation of ASPSP account primary identifier
     * @param aspspConsentData Encrypted data that may be stored in the consent management system in the consent linked to a request.<br>
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return Account details
     */
    SpiResponse<SpiAccountDetails> readAccountDetails(String accountId, AspspConsentData aspspConsentData);

    /**
     * Queries ASPSP to (GET) a list of account details of a certain PSU by identifier
     *
     * @param psuId            String representing ASPSP`s primary identifier of PSU
     * @param aspspConsentData Encrypted data that may be stored in the consent management system in the consent linked to a request.<br>
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return List of account details
     */
    SpiResponse<List<SpiAccountDetails>> readAccountsByPsuId(String psuId, AspspConsentData aspspConsentData);

    /**
     * Queries ASPSP to (GET) List of AccountDetails by IBAN
     *
     * @param iban             String representation of Account IBAN
     * @param aspspConsentData Encrypted data that may be stored in the consent management system in the consent linked to a request.<br>
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return List of account details
     */
    SpiResponse<List<SpiAccountDetails>> readAccountDetailsByIban(String iban, AspspConsentData aspspConsentData);

    /**
     * Queries ASPSP to (GET) list of account details with certain account IBANS
     *
     * @param ibans            a collection of Strings representing account IBANS
     * @param aspspConsentData Encrypted data that may be stored in the consent management system in the consent linked to a request.<br>
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return List of account details
     */
    SpiResponse<List<SpiAccountDetails>> readAccountDetailsByIbans(Collection<String> ibans, AspspConsentData aspspConsentData);

    /**
     * Queries ASPSP to (GET) list of allowed payment products for current PSU by its account reference
     *
     * @param reference        Account reference
     * @param aspspConsentData Encrypted data that may be stored in the consent management system in the consent linked to a request.<br>
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return a list of allowed payment products
     */
    SpiResponse<List<String>> readPsuAllowedPaymentProductList(SpiAccountReference reference, AspspConsentData aspspConsentData);
}
