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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReference;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReport;
import de.adorsys.aspsp.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.aspsp.xs2a.domain.consent.Xs2aAccountAccessType;
import de.adorsys.aspsp.xs2a.exception.MessageCategory;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.consent.AisConsentDataService;
import de.adorsys.aspsp.xs2a.service.consent.AisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.AccountModelMapper;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.SpiXs2aAccountMapper;
import de.adorsys.aspsp.xs2a.service.validator.ValueValidatorService;
import de.adorsys.aspsp.xs2a.spi.domain.SpiResponse;
import de.adorsys.aspsp.xs2a.spi.domain.account.*;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import de.adorsys.psd2.consent.api.ActionStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccountServiceTest {
    private final String ACCOUNT_ID = "33333-999999999";
    private final String ACCOUNT_ID_1 = "77777-999999999";
    private final String WRONG_ACCOUNT_ID = "Wrong account";
    private final String IBAN = "DE123456789";
    private final String IBAN_1 = "DE987654321";
    private final Currency CURRENCY = Currency.getInstance("EUR");
    private final Currency CURRENCY_1 = Currency.getInstance("USD");
    private final String CONSENT_ID_WB = "111222333";
    private final String CONSENT_ID_WOB = "333222111";
    private final String CONSENT_ID_WT = "777999777";
    private final String WRONG_CONSENT_ID = "Wromg consent id";
    private final String TRANSACTION_ID = "0001";
    private final LocalDate DATE = LocalDate.parse("2019-03-03");
    private final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData();
    private final String TPP_ID = "Test TppId";

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountSpi accountSpi;
    @Mock
    private ConsentService consentService;
    @Mock
    private AisConsentService aisConsentService;
    @Mock
    private SpiXs2aAccountMapper spiXs2aAccountMapper;
    @Mock
    private ValueValidatorService valueValidatorService;
    @Mock
    private Xs2aAisConsentMapper aisConsentMapper;
    @Mock
    private TppService tppService;
    @Spy
    AccountModelMapper accountModelMapper = new AccountModelMapper(new ObjectMapper());
    @Mock
    AisConsentDataService aisConsentDataService;

    @Before
    public void setUp() {
        //Validation
        doNothing().when(valueValidatorService).validate(any(), any());
        //AccountMapping
        when(spiXs2aAccountMapper.mapToXs2aAccountDetails(getSpiAccountDetails(ACCOUNT_ID, IBAN))).thenReturn(getAccountDetails(ACCOUNT_ID, IBAN));
        when(spiXs2aAccountMapper.mapToXs2aAccountDetails(getSpiAccountDetails(ACCOUNT_ID_1, IBAN_1))).thenReturn(getAccountDetails(ACCOUNT_ID_1, IBAN_1));
        when(spiXs2aAccountMapper.mapToAccountDetailsListNoBalances(Arrays.asList(getAccountDetails(ACCOUNT_ID, IBAN), getAccountDetails(ACCOUNT_ID_1, IBAN_1))))
            .thenReturn(Arrays.asList(getAccountDetailsNoBalance(ACCOUNT_ID, IBAN), getAccountDetailsNoBalance(ACCOUNT_ID_1, IBAN_1)));
        when(spiXs2aAccountMapper.mapToXs2aAccountDetails(null)).thenReturn(null);
        when(spiXs2aAccountMapper.mapToXs2aAccountReport(Collections.singletonList(getSpiTransaction()))).thenReturn(Optional.of(getReport()));
        when(spiXs2aAccountMapper.mapToAccountDetailNoBalances(getAccountDetails(ACCOUNT_ID, IBAN))).thenReturn(getAccountDetailsNoBalance(ACCOUNT_ID, IBAN));
        when(spiXs2aAccountMapper.mapToAccountDetailNoBalances(getAccountDetails(ACCOUNT_ID_1, IBAN_1))).thenReturn(getAccountDetailsNoBalance(ACCOUNT_ID_1, IBAN_1));
        when(spiXs2aAccountMapper.mapToAccountDetailNoBalances(null)).thenReturn(null);
        //AisReporting
        doNothing().when(aisConsentService).consentActionLog(anyString(), anyString(), any(ActionStatus.class));
        //getAccountDetailsByAccountId_WoB_Success
        when(accountSpi.readAccountDetails(ACCOUNT_ID, ASPSP_CONSENT_DATA)).thenReturn(new SpiResponse<>(getSpiAccountDetails(ACCOUNT_ID, IBAN), ASPSP_CONSENT_DATA));
        when(consentService.getValidatedConsent(CONSENT_ID_WOB)).thenReturn(getAccessResponse(getReferences(IBAN, IBAN_1), null, null, false, false));
        when(consentService.isValidAccountByAccess(IBAN, CURRENCY, getReferences(IBAN, IBAN_1))).thenReturn(true);
        //getAccountDetailsByAccountId_WB_Success
        when(consentService.getValidatedConsent(CONSENT_ID_WB)).thenReturn(getAccessResponse(getReferences(IBAN, IBAN_1), getReferences(IBAN, IBAN_1), null, false, false));
        when(consentService.isValidAccountByAccess(IBAN, CURRENCY, getReferences(IBAN, IBAN_1))).thenReturn(true);
        //getAccountDetailsByAccountId_Failure_wrongAccount
        when(accountSpi.readAccountDetails(WRONG_ACCOUNT_ID, ASPSP_CONSENT_DATA)).thenReturn(new SpiResponse<>(null, ASPSP_CONSENT_DATA));
        //getAccountDetailsByAccountId_Failure_wrongConsent
        when(consentService.getValidatedConsent(WRONG_CONSENT_ID)).thenReturn(ResponseObject.<Xs2aAccountAccess>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.CONSENT_UNKNOWN_403))).build());

        //getAccountDetailsListByConsent_Success
        when(accountSpi.readAccountDetailsByIban(IBAN, ASPSP_CONSENT_DATA)).thenReturn(new SpiResponse<>(Collections.singletonList(getSpiAccountDetails(ACCOUNT_ID, IBAN)), ASPSP_CONSENT_DATA));
        when(accountSpi.readAccountDetailsByIban(IBAN_1, ASPSP_CONSENT_DATA)).thenReturn(new SpiResponse<>(Collections.singletonList(getSpiAccountDetails(ACCOUNT_ID_1, IBAN_1)), ASPSP_CONSENT_DATA));

        when(accountSpi.readAccountDetails(WRONG_ACCOUNT_ID, ASPSP_CONSENT_DATA)).thenReturn(new SpiResponse<>(null, ASPSP_CONSENT_DATA));

        //getAccountReport_ByTransactionId_Success
        when(consentService.getValidatedConsent(CONSENT_ID_WT)).thenReturn(getAccessResponse(getReferences(IBAN, IBAN_1), null, getReferences(IBAN, IBAN_1), false, false));
        when(accountSpi.readTransactionById(TRANSACTION_ID, ACCOUNT_ID, ASPSP_CONSENT_DATA)).thenReturn(new SpiResponse<>(Optional.of(getSpiTransaction()), ASPSP_CONSENT_DATA));

        when(accountSpi.readTransactionsByPeriod(ACCOUNT_ID, DATE, DATE, ASPSP_CONSENT_DATA)).thenReturn(new SpiResponse<>(Collections.singletonList(getSpiTransaction()), ASPSP_CONSENT_DATA));
        when(tppService.getTppId()).thenReturn(TPP_ID);
        when(aisConsentDataService.getAspspConsentDataByConsentId(anyString())).thenReturn(new AspspConsentData());
    }

    //Get Account By AccountId
    @Test
    public void getAccountDetailsByAccountId_WoB_Success() {
        //When:
        ResponseObject<Xs2aAccountDetails> response = accountService.getAccountDetails(CONSENT_ID_WOB, ACCOUNT_ID, false);

        //Then:
        assertThat(response.getBody().getId()).isEqualTo(ACCOUNT_ID);
        assertThat(response.getBody().getBalances()).isEqualTo(null);
    }

    @Test
    public void getAccountDetailsByAccountId_WB_Success() {
        //When:
        ResponseObject<Xs2aAccountDetails> response = accountService.getAccountDetails(CONSENT_ID_WB, ACCOUNT_ID, true);

        de.adorsys.psd2.model.AccountDetails details = accountModelMapper.mapToAccountDetails(response.getBody());

        //Then:
        assertThat(response.getBody().getId()).isEqualTo(ACCOUNT_ID);
        assertThat(response.getBody().getBalances()).isEqualTo(getBalancesList());
    }

    @Test
    public void getAccountDetailsByAccountId_Failure_wrongAccount() {
        //When:
        ResponseObject<Xs2aAccountDetails> response = accountService.getAccountDetails(CONSENT_ID_WB, WRONG_ACCOUNT_ID, true);

        //Then:
        assertThat(response.hasError()).isEqualTo(true);
        assertThat(response.getError().getTransactionStatus()).isEqualTo(Xs2aTransactionStatus.RJCT);
        assertThat(response.getError().getTppMessage().getMessageErrorCode()).isEqualTo(RESOURCE_UNKNOWN_404);
    }

    @Test
    public void getAccountDetailsByAccountId_Failure_wrongConsent() {
        //When:
        ResponseObject<Xs2aAccountDetails> response = accountService.getAccountDetails(WRONG_CONSENT_ID, ACCOUNT_ID, true);

        //Then:
        assertThat(response.hasError()).isEqualTo(true);
        assertThat(response.getError().getTransactionStatus()).isEqualTo(Xs2aTransactionStatus.RJCT);
        assertThat(response.getError().getTppMessage().getMessageErrorCode()).isEqualTo(CONSENT_UNKNOWN_403);
    }

    //Get AccountsList By Consent
    @Test
    public void getAccountDetailsListByConsent_Success_WOB() {
        //When:
        ResponseObject<Map<String, List<Xs2aAccountDetails>>> response = accountService.getAccountDetailsList(CONSENT_ID_WOB, false);

        //Then:
        assertThat(response.getBody().get("accountList").get(0).getId()).isEqualTo(ACCOUNT_ID);
        assertThat(response.getBody().get("accountList").get(1).getId()).isEqualTo(ACCOUNT_ID_1);
        assertThat(response.getBody().get("accountList").get(0).getBalances()).isEqualTo(null);
        assertThat(response.getBody().get("accountList").get(1).getBalances()).isEqualTo(null);
        assertThat(response.getBody().get("accountList").get(0).getLinks()).isEqualTo(new Links());
        assertThat(response.getBody().get("accountList").get(1).getLinks()).isEqualTo(new Links());
    }

    @Test
    public void getAccountDetailsListByConsent_Success_WB() {
        //When:
        ResponseObject<Map<String, List<Xs2aAccountDetails>>> response = accountService.getAccountDetailsList(CONSENT_ID_WB, true);

        //Then:
        assertThat(response.getBody().get("accountList").get(0).getId()).isEqualTo(ACCOUNT_ID);
        assertThat(response.getBody().get("accountList").get(1).getId()).isEqualTo(ACCOUNT_ID_1);
        assertThat(response.getBody().get("accountList").get(0).getBalances()).isEqualTo(getBalancesList());
        assertThat(response.getBody().get("accountList").get(1).getBalances()).isEqualTo(getBalancesList());
        assertThat(response.getBody().get("accountList").get(0).getLinks()).isEqualTo(getAccountDetails(ACCOUNT_ID, IBAN).getLinks());
        assertThat(response.getBody().get("accountList").get(1).getLinks()).isEqualTo(getAccountDetails(ACCOUNT_ID_1, IBAN_1).getLinks());
    }

    @Test
    public void getAccountDetailsListByConsent_Failure_Wrong_Consent() {
        //When:
        ResponseObject<Map<String, List<Xs2aAccountDetails>>> response = accountService.getAccountDetailsList(WRONG_CONSENT_ID, false);

        //Then:
        assertThat(response.hasError()).isEqualTo(true);
        assertThat(response.getError().getTransactionStatus()).isEqualTo(Xs2aTransactionStatus.RJCT);
        assertThat(response.getError().getTppMessage().getMessageErrorCode()).isEqualTo(CONSENT_UNKNOWN_403);
    }

    //Get Balances
    @Test
    public void getBalances_Success_Consent_WB() {
        //When:
        ResponseObject<List<Xs2aBalance>> response = accountService.getBalances(CONSENT_ID_WB, ACCOUNT_ID);

        //Then:
        assertThat(response.getBody()).isEqualTo(getBalancesList());
    }

    @Test
    public void getBalances_Failure_Consent_WOB() {
        //When:
        ResponseObject<List<Xs2aBalance>> response = accountService.getBalances(CONSENT_ID_WOB, ACCOUNT_ID);

        //Then:
        assertThat(response.hasError()).isEqualTo(true);
        assertThat(response.getError().getTransactionStatus()).isEqualTo(Xs2aTransactionStatus.RJCT);
        assertThat(response.getError().getTppMessage().getMessageErrorCode()).isEqualTo(CONSENT_INVALID);
    }

    @Test
    public void getBalances_Failure_Wrong_Consent() {
        //When:
        ResponseObject<List<Xs2aBalance>> response = accountService.getBalances(WRONG_CONSENT_ID, ACCOUNT_ID);

        //Then:
        assertThat(response.hasError()).isEqualTo(true);
        assertThat(response.getError().getTransactionStatus()).isEqualTo(Xs2aTransactionStatus.RJCT);
        assertThat(response.getError().getTppMessage().getMessageErrorCode()).isEqualTo(CONSENT_UNKNOWN_403);
    }

    @Test
    public void getBalances_Failure_Wrong_Account() {
        //When:
        ResponseObject<List<Xs2aBalance>> response = accountService.getBalances(CONSENT_ID_WB, WRONG_ACCOUNT_ID);

        //Then:
        assertThat(response.hasError()).isEqualTo(true);
        assertThat(response.getError().getTransactionStatus()).isEqualTo(Xs2aTransactionStatus.RJCT);
        assertThat(response.getError().getTppMessage().getMessageErrorCode()).isEqualTo(RESOURCE_UNKNOWN_404);
    }

    //Get Transaction By TransactionId
    @Test
    public void getAccountReport_ByTransactionId_Success() {
        //When:
        ResponseObject<Xs2aAccountReport> response = accountService.getAccountReport(CONSENT_ID_WT, ACCOUNT_ID, null, null, TRANSACTION_ID, false, Xs2aBookingStatus.BOTH, false, false);

        //Then:
        assertThat(response.getError()).isEqualTo(null);
        assertThat(response.getBody().getBooked()[0].getTransactionId()).isEqualTo(getTransaction().getTransactionId());
    }

    @Test
    public void getAccountReport_ByTransactionId_WrongConsent_Failure() {
        //When:
        ResponseObject response = accountService.getAccountReport(WRONG_CONSENT_ID, ACCOUNT_ID, null, null, TRANSACTION_ID, false, Xs2aBookingStatus.BOTH, false, false);

        //Then:
        assertThat(response.hasError()).isEqualTo(true);
        assertThat(response.getError().getTppMessage().getMessageErrorCode()).isEqualTo(CONSENT_UNKNOWN_403);
    }

    @Test
    public void getAccountReport_ByTransactionId_AccountMismatch_Failure() {
        //When:
        ResponseObject response = accountService.getAccountReport(CONSENT_ID_WOB, WRONG_ACCOUNT_ID, null, null, TRANSACTION_ID, false, Xs2aBookingStatus.BOTH, false, false);

        //Then:
        assertThat(response.hasError()).isEqualTo(true);
        assertThat(response.getError().getTppMessage().getMessageErrorCode()).isEqualTo(RESOURCE_UNKNOWN_404);
    }

    //Get Transactions By Period
    @Test
    public void getAccountReport_ByPeriod_Success() {
        //When:
        ResponseObject<Xs2aAccountReport> response = accountService.getAccountReport(CONSENT_ID_WT, ACCOUNT_ID, DATE, DATE, null, false, Xs2aBookingStatus.BOTH, false, false);

        //Then:
        assertThat(response.getError()).isEqualTo(null);
        assertThat(response.getBody().getBooked()[0].getTransactionId()).isEqualTo(getTransaction().getTransactionId());
    }

    @Test
    public void getAccountReport_ByPeriod_Failure_Wrong_Account() {
        //When:
        ResponseObject response = accountService.getAccountReport(CONSENT_ID_WB, WRONG_ACCOUNT_ID, DATE, DATE, null, false, Xs2aBookingStatus.BOTH, false, false);

        //Then:
        assertThat(response.hasError()).isEqualTo(true);
        assertThat(response.getError().getTransactionStatus()).isEqualTo(Xs2aTransactionStatus.RJCT);
        assertThat(response.getError().getTppMessage().getMessageErrorCode()).isEqualTo(RESOURCE_UNKNOWN_404);
    }

    @Test
    public void getAccountReport_ByPeriod_Failure_Wrong_Consent() {
        //When:
        ResponseObject response = accountService.getAccountReport(WRONG_CONSENT_ID, ACCOUNT_ID, DATE, DATE, null, false, Xs2aBookingStatus.BOTH, false, false);

        //Then:
        assertThat(response.hasError()).isEqualTo(true);
        assertThat(response.getError().getTransactionStatus()).isEqualTo(Xs2aTransactionStatus.RJCT);
        assertThat(response.getError().getTppMessage().getMessageErrorCode()).isEqualTo(CONSENT_UNKNOWN_403);
    }

    //Test Stuff
    private ResponseObject<Xs2aAccountAccess> getAccessResponse(List<Xs2aAccountReference> accounts, List<Xs2aAccountReference> balances, List<Xs2aAccountReference> transactions, boolean allAccounts, boolean allPsd2) {
        return ResponseObject.<Xs2aAccountAccess>builder().body(getAccessForMock(accounts, balances, transactions, allAccounts, allPsd2)).build();
    }

    private Xs2aAccountAccess getAccessForMock(List<Xs2aAccountReference> accounts, List<Xs2aAccountReference> balances, List<Xs2aAccountReference> transactions, boolean allAccounts, boolean allPsd2) {
        return new Xs2aAccountAccess(accounts, balances, transactions, allAccounts ? Xs2aAccountAccessType.ALL_ACCOUNTS : null, allPsd2 ? Xs2aAccountAccessType.ALL_ACCOUNTS : null);
    }

    private Xs2aAccountReference getAccountReference() {
        Xs2aAccountDetails details = getAccountDetails(ACCOUNT_ID, IBAN);
        Xs2aAccountReference rf = new Xs2aAccountReference();
        rf.setCurrency(details.getCurrency());
        rf.setIban(details.getIban());
        rf.setPan(details.getPan());
        rf.setMaskedPan(details.getMaskedPan());
        rf.setMsisdn(details.getMsisdn());
        rf.setBban(details.getBban());
        return rf;
    }

    private Xs2aAccountDetails getAccountDetails(String accountId, String iban) {
        return new Xs2aAccountDetails(
            accountId,
            iban,
            "zz22",
            null,
            null,
            null,
            CURRENCY,
            "David Muller",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            getBalancesList());
    }

    private Xs2aAccountDetails getAccountDetailsNoBalance(String accountId, String iban) {
        return new Xs2aAccountDetails(
            accountId,
            iban,
            "zz22",
            null,
            null,
            null,
            CURRENCY,
            "David Muller",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);
    }

    private List<Xs2aBalance> getBalancesList() {
        Xs2aBalance sb = new Xs2aBalance();
        Xs2aAmount amount = new Xs2aAmount();
        amount.setCurrency(CURRENCY);
        amount.setAmount("1000");
        sb.setBalanceAmount(amount);
        return Collections.singletonList(sb);
    }

    private SpiAccountDetails getSpiAccountDetails(String accountId, String iban) {
        return new SpiAccountDetails(
            accountId,
            iban,
            "zz22",
            null,
            null,
            null,
            iban.equals(IBAN)
                ? CURRENCY
                : CURRENCY_1,
            "David Muller",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            getSpiBalances());
    }

    private List<SpiAccountBalance> getSpiBalances() {
        SpiAccountBalance sb = new SpiAccountBalance();
        SpiAmount amount = new SpiAmount(CURRENCY, BigDecimal.valueOf(1000));
        sb.setSpiBalanceAmount(amount);
        sb.setSpiBalanceType(SpiBalanceType.INTERIM_AVAILABLE);
        return Collections.singletonList(sb);
    }

    private Transactions getTransaction() {
        Transactions transaction = new Transactions();
        transaction.setTransactionId(TRANSACTION_ID);
        transaction.setBookingDate(DATE);
        transaction.setValueDate(DATE);
        transaction.setCreditorAccount(getAccountReference());
        Xs2aAmount amount = new Xs2aAmount();
        amount.setAmount("1000");
        amount.setCurrency(CURRENCY);
        transaction.setAmount(amount);
        return transaction;
    }

    private SpiTransaction getSpiTransaction() {
        Transactions t = getTransaction();
        return new SpiTransaction(t.getTransactionId(), null, null, null, null, null, t.getBookingDate(),
            t.getValueDate(), new SpiAmount(t.getAmount().getCurrency(), new BigDecimal(t.getAmount().getAmount())), null, null,
            mapToSpiAccountRef(t.getCreditorAccount()), null, null,
            mapToSpiAccountRef(t.getDebtorAccount()), null, null,
            null, null, null, null);
    }

    private SpiAccountReference mapToSpiAccountRef(Xs2aAccountReference reference) {
        return Optional.ofNullable(reference).map(r -> new SpiAccountReference(r.getIban(), r.getBban(), r.getPan(),
            r.getMaskedPan(), r.getMsisdn(), r.getCurrency())).orElse(null);
    }

    private List<Xs2aAccountReference> getReferences(String iban, String iban1) {
        return Arrays.asList(getReference(iban), getReference(iban1));
    }

    private Xs2aAccountReference getReference(String iban) {
        Xs2aAccountReference reference = new Xs2aAccountReference();
        reference.setIban(iban);
        reference.setCurrency(iban.equals(IBAN) ? CURRENCY : CURRENCY_1);
        return reference;
    }

    private Xs2aAccountReport getReport() {
        return new Xs2aAccountReport(new Transactions[]{getTransaction()}, new Transactions[]{});
    }
}
