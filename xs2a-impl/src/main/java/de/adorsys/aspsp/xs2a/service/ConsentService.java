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

package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.ais.consent.*;
import de.adorsys.aspsp.xs2a.exception.MessageCategory;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.mapper.AccountMapper;
import de.adorsys.aspsp.xs2a.service.mapper.ConsentMapper;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import de.adorsys.aspsp.xs2a.spi.service.ConsentSpi;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.util.StringUtils.isEmpty;

@AllArgsConstructor
@Service
public class ConsentService {
    private String consentsLinkRedirectToSource;
    private ConsentSpi consentSpi;
    private ConsentMapper consentMapper;
    private AccountMapper accountMapper;
    private AccountSpi accountSpi;

    public ResponseObject<CreateConsentResp> createAccountConsentsWithResponse(CreateConsentReq createAccountConsentRequest, boolean withBalance, boolean tppRedirectPreferred, String psuId) {
        Optional<String> consentId = createAccountConsentsAndReturnId(createAccountConsentRequest, withBalance, tppRedirectPreferred, psuId);
        return consentId.isPresent()
                   ? ResponseObject.<CreateConsentResp>builder().body(new CreateConsentResp(TransactionStatus.RCVD, consentId.get(), null, getLinkToConsent(consentId.get()), null)).build()
                   : ResponseObject.<CreateConsentResp>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageCode.FORMAT_ERROR))).build();
    }

    public ResponseObject<TransactionStatus> getAccountConsentsStatusById(String consentId) {
        AccountConsent consent = consentMapper.mapFromSpiAccountConsent(consentSpi.getAccountConsentById(consentId));
        return isEmpty(consent)
                   ? ResponseObject.<TransactionStatus>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageCode.RESOURCE_UNKNOWN_404))).build()
                   : ResponseObject.<TransactionStatus>builder().body(consent.getTransactionStatus()).build();
    }

    public ResponseObject<Boolean> deleteAccountConsentsById(String consentId) {
        if (consentSpi.getAccountConsentById(consentId) != null) {
            consentSpi.deleteAccountConsentsById(consentId);
            return ResponseObject.<Boolean>builder().body(true).build();
        }

        return ResponseObject.<Boolean>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageCode.RESOURCE_UNKNOWN_404))).build();
    }

    public ResponseObject<AccountConsent> getAccountConsentsById(String consentId) {
        AccountConsent consent = consentMapper.mapFromSpiAccountConsent(consentSpi.getAccountConsentById(consentId));
        return isEmpty(consent)
                   ? ResponseObject.<AccountConsent>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageCode.RESOURCE_UNKNOWN_404))).build()
                   : ResponseObject.<AccountConsent>builder().body(consent).build();
    }

    private Optional<String> createAccountConsentsAndReturnId(CreateConsentReq req, boolean withBalance, boolean tppRedirectPreferred, String psuId) {

        Optional<AccountAccess> access = Optional.ofNullable(req.getAccess())
                                             .flatMap(acs -> createAccountAccess(acs, psuId));
        if (req.isRecurringIndicator() && access.isPresent()) {
            consentSpi.expireConsent(consentMapper.mapToSpiAccountAccess(access.get()));
        }
        return access.map(accountAccess -> saveAccountConsent(
            new AccountConsent(null, accountAccess, req.isRecurringIndicator(), req.getValidUntil(), req.getFrequencyPerDay(),
                null, TransactionStatus.ACCP, ConsentStatus.VALID, withBalance, tppRedirectPreferred)));
    }

    private String saveAccountConsent(AccountConsent consent) {
        return consentSpi.createAccountConsents(consentMapper.mapToSpiAccountConsent(consent));
    }

    private Optional<AccountAccess> createAccountAccess(AccountAccess access, String psuId) {
        return isAllAccountsOrAllPsd2(access.getAvailableAccounts(), access.getAllPsd2(), psuId)
                   ? getNewAccessByPsuId(access.getAvailableAccounts(), access.getAllPsd2(), psuId)
                   : getNewAccessByIbans(access);
    }

    private Optional<AccountAccess> getNewAccessByIbans(AccountAccess access) {
        Set<String> ibans = getIbanSetFromAccess(access);
        List<AccountDetails> accountDetails = accountMapper.mapFromSpiAccountDetailsList(accountSpi.readAccountDetailsByIbans(ibans));

        Set<AccountReference> accountsRef = extractReferenceSetFromDetailsList(access.getAccounts(), accountDetails);
        Set<AccountReference> balancesRef = extractReferenceSetFromDetailsList(access.getBalances(), accountDetails);
        Set<AccountReference> transactionsRef = extractReferenceSetFromDetailsList(access.getTransactions(), accountDetails);

        accountsRef = mergeSets(accountsRef, balancesRef, transactionsRef);

        return Optional.of(getNewAccountAccessByReferences(setToArray(accountsRef), setToArray(balancesRef), setToArray(transactionsRef), null, null));
    }

    private Set<AccountReference> extractReferenceSetFromDetailsList(AccountReference[] accountReferencesArr, List<AccountDetails> accountDetails) {
        return Optional.ofNullable(accountReferencesArr)
                   .map(arr -> Arrays.stream(arr)
                                   .flatMap(ref -> getReferenceFromDetailsByIban(ref.getIban(), ref.getCurrency(), accountDetails))
                                   .collect(Collectors.toSet()))
                   .orElse(Collections.emptySet());
    }

    private Stream<AccountReference> getReferenceFromDetailsByIban(String iban, Currency currency, List<AccountDetails> accountDetails) {
        return accountDetails.stream()
                   .filter(acc -> acc.getIban().equals(iban))
                   .filter(acc -> currency == null || acc.getCurrency() == currency)//TODO CHECK WITH PO
                   .map(this::mapAccountDetailsToReference);
    }

    private Optional<AccountAccess> getNewAccessByPsuId(AccountAccessType availableAccounts, AccountAccessType allPsd2, String psuId) {
        return Optional.ofNullable(accountMapper.mapFromSpiAccountDetailsList(accountSpi.readAccountsByPsuId(psuId)))
                   .map(this::mapAccountListToArrayOfReference)
                   .map(ref -> availableAccounts == AccountAccessType.ALL_ACCOUNTS
                                   ? getNewAccountAccessByReferences(ref, new AccountReference[]{}, new AccountReference[]{}, availableAccounts, null)
                                   : getNewAccountAccessByReferences(ref, ref, ref, null, allPsd2)
                   );
    }

    private AccountReference[] mapAccountListToArrayOfReference(List<AccountDetails> accountDetails) {
        return accountDetails.stream()
                   .map(this::mapAccountDetailsToReference)
                   .toArray(AccountReference[]::new);
    }

    private AccountAccess getNewAccountAccessByReferences(AccountReference[] accounts,
                                                          AccountReference[] balances,
                                                          AccountReference[] transactions,
                                                          AccountAccessType availableAccounts,
                                                          AccountAccessType allPsd2) {

        return new AccountAccess(accounts, balances, transactions, availableAccounts, allPsd2);
    }

    public Set<String> getIbanSetFromAccess(AccountAccess access) {
        if (isNotEmptyAccountAccess(access)) {
            return getIbansFromAccess(access);
        }
        return Collections.emptySet();
    }

    private Set<String> getIbansFromAccess(AccountAccess access) {
        return mergeSets(
            getIbansFromAccountReference(access.getAccounts()),
            getIbansFromAccountReference(access.getBalances()),
            getIbansFromAccountReference(access.getTransactions()));
    }

    private Set<String> getIbansFromAccountReference(AccountReference[] references) {
        return Optional.ofNullable(references)
                   .map(ar -> Arrays.stream(ar)
                                  .map(AccountReference::getIban)
                                  .collect(Collectors.toSet()))
                   .orElse(Collections.emptySet());
    }

    private boolean isNotEmptyAccountAccess(AccountAccess access) {
        return !(ArrayUtils.isEmpty(access.getAccounts())
                     && ArrayUtils.isEmpty(access.getBalances())
                     && ArrayUtils.isEmpty(access.getTransactions())
                     && access.getAllPsd2() == null
                     && access.getAvailableAccounts() == null);
    }

    private boolean isAllAccountsOrAllPsd2(AccountAccessType availableAccounts, AccountAccessType allPsd2, String psuId) {
        return !isEmpty(psuId)
                   && availableAccounts == AccountAccessType.ALL_ACCOUNTS
                   || allPsd2 == AccountAccessType.ALL_ACCOUNTS;

    }

    private AccountReference mapAccountDetailsToReference(AccountDetails details) {
        AccountReference reference = new AccountReference();
        reference.setIban(details.getIban());
        reference.setBban(details.getBban());
        reference.setPan(details.getPan());
        reference.setMaskedPan(details.getMaskedPan());
        reference.setMsisdn(details.getMsisdn());
        reference.setCurrency(details.getCurrency());
        return reference;
    }

    private Links getLinkToConsent(String consentId) {
        Links linksToConsent = new Links();

        // Response in case of the OAuth2 approach
        // todo figure out when we should return  OAuth2 response
        //String selfLink = linkTo(ConsentInformationController.class).slash(consentId).toString();
        //linksToConsent.setSelf(selfLink);

        // Response in case of a redirect
        String redirectLink = consentsLinkRedirectToSource + "/" + consentId;
        linksToConsent.setRedirect(redirectLink);

        return linksToConsent;
    }

    private static <T> Set<T> mergeSets(Set<T>... sets) {
        return Stream.of(sets)
                   .flatMap(Collection::stream)
                   .collect(Collectors.toSet());
    }

    private AccountReference[] setToArray(Set<AccountReference> set) {
        return set.stream().toArray(AccountReference[]::new);
    }
}
