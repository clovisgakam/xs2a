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

package de.adorsys.aspsp.aspspmockserver.service;

import de.adorsys.aspsp.aspspmockserver.domain.pis.AspspPayment;
import de.adorsys.aspsp.aspspmockserver.repository.PaymentRepository;
import de.adorsys.aspsp.aspspmockserver.service.mapper.PaymentMapper;
import de.adorsys.psd2.aspsp.mock.api.account.AspspAccountBalance;
import de.adorsys.psd2.aspsp.mock.api.account.AspspAccountDetails;
import de.adorsys.psd2.aspsp.mock.api.account.AspspAccountReference;
import de.adorsys.psd2.aspsp.mock.api.account.AspspBalanceType;
import de.adorsys.psd2.aspsp.mock.api.common.AspspAmount;
import de.adorsys.psd2.aspsp.mock.api.payment.AspspCancelPayment;
import de.adorsys.psd2.aspsp.mock.api.payment.AspspSinglePayment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceTest {
    private static final String PAYMENT_ID = "123456789";
    private static final String WRONG_PAYMENT_ID = "0";
    private static final String IBAN = "DE123456789";
    private static final String WRONG_IBAN = "wrong_iban";
    private static final Currency CURRENCY = Currency.getInstance("EUR");

    @InjectMocks
    private PaymentService paymentService;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private AccountService accountService;
    @Mock
    private PaymentMapper paymentMapper;

    @Before
    public void setUp() {
        when(paymentRepository.save(any(AspspPayment.class)))
            .thenReturn(getAspspPayment());
        when(paymentRepository.save(any(List.class)))
            .thenReturn(Collections.singletonList(getAspspPayment()));
        when(paymentRepository.exists(PAYMENT_ID))
            .thenReturn(true);
        when(paymentRepository.exists(WRONG_PAYMENT_ID))
            .thenReturn(false);
        when(accountService.getAccountsByIban(IBAN)).thenReturn(getAccountDetails());
        when(accountService.getAccountsByIban(WRONG_IBAN)).thenReturn(null);
        when(paymentMapper.mapToAspspPayment(any(), any())).thenReturn(new AspspPayment());
        when(paymentMapper.mapToAspspSinglePayment(any(AspspPayment.class))).thenReturn(getAspspSinglePayment(50));
        when(paymentService.cancelPayment(PAYMENT_ID)).thenReturn(buildAspspCancelPayment());
        when(paymentRepository.findOne(PAYMENT_ID)).thenReturn(new AspspPayment());
    }

    @Test
    public void addPayment_Success() {
        when(accountService.getAccountsByIban(IBAN)).thenReturn(getAccountDetails());
        //Given
        AspspSinglePayment expectedPayment = getAspspSinglePayment(50);

        //When
        Optional<AspspSinglePayment> aspspSinglePayment = paymentService.addPayment(expectedPayment);
        AspspSinglePayment actualPayment = aspspSinglePayment.orElse(null);

        //Then
        assertThat(actualPayment).isNotNull();
    }

    @Test
    public void addPayment_AmountsAreEqual() {
        //Given
        AspspSinglePayment expectedPayment = getAspspSinglePayment(100);

        //When
        Optional<AspspSinglePayment> actualPayment = paymentService.addPayment(expectedPayment);

        //Then
        assertThat(actualPayment).isNotNull();
    }

    @Test
    public void addPayment_Failure() {
        //Given
        AspspSinglePayment expectedPayment = getAspspSinglePayment(101);

        //When
        Optional<AspspSinglePayment> actualPayment = paymentService.addPayment(expectedPayment);

        //Then
        assertThat(actualPayment).isEqualTo(Optional.empty());
    }

    @Test
    public void getPaymentStatusById() {
        //Then
        assertThat(paymentService.isPaymentExist(PAYMENT_ID)).isTrue();
        assertThat(paymentService.isPaymentExist(WRONG_PAYMENT_ID)).isFalse();
    }

    @Test
    public void addBulkPayments() {
        //Given
        AspspBulkPayment expectedPayment = new AspspBulkPayment();
        expectedPayment.setPayments(new ArrayList<>());
        expectedPayment.getPayments().add(getAspspSinglePayment(50));

        //When
        AspspBulkPayment actualPayments = paymentService.addBulkPayments(expectedPayment).get();

        //Then
        assertThat(actualPayments).isNotNull();
    }

    @Test
    public void cancelPaymentSuccess() {
        //Given
        Optional<AspspCancelPayment> given = buildAspspCancelPayment();

        //When
        Optional<AspspCancelPayment> actual = paymentService.cancelPayment(PAYMENT_ID);

        //Then
        assertThat(given).isEqualTo(actual);
    }

    private Optional<AspspCancelPayment> buildAspspCancelPayment() {
        return Optional.of(new AspspCancelPayment());
    }

    private AspspSinglePayment getAspspSinglePayment(long amountToTransfer) {
        AspspSinglePayment payment = new AspspSinglePayment();
        AspspAmount amount = new AspspAmount(Currency.getInstance("EUR"), new BigDecimal(amountToTransfer));
        payment.setInstructedAmount(amount);
        payment.setDebtorAccount(getReference());
        payment.setCreditorName("Merchant123");
        payment.setPurposeCode("BEQNSD");
        payment.setCreditorAgent("sdasd");
        payment.setCreditorAccount(getReference());
        payment.setRemittanceInformationUnstructured("Ref Number Merchant");

        return payment;
    }

    private AspspPayment getAspspPayment() {
        AspspPayment payment = new AspspPayment();
        AspspAmount amount = new AspspAmount(Currency.getInstance("EUR"), new BigDecimal((long) 50));
        payment.setInstructedAmount(amount);
        payment.setDebtorAccount(getReference());
        payment.setCreditorName("Merchant123");
        payment.setPurposeCode("BEQNSD");
        payment.setCreditorAgent("sdasd");
        payment.setCreditorAccount(getReference());
        payment.setRemittanceInformationUnstructured("Ref Number Merchant");
        return payment;
    }

    private List<AspspAccountDetails> getAccountDetails() {
        return Collections.singletonList(
            new AspspAccountDetails("12345", IBAN, null, null, null, null, CURRENCY, "Peter", null, null, null, null, null, null, null, getBalances())
        );
    }

    private List<AspspAccountBalance> getBalances() {
        AspspAccountBalance balance = new AspspAccountBalance();
        balance.setSpiBalanceAmount(new AspspAmount(CURRENCY, BigDecimal.valueOf(100)));
        balance.setSpiBalanceType(AspspBalanceType.INTERIM_AVAILABLE);
        return Collections.singletonList(balance);
    }

    private AspspAccountReference getReference() {
        AspspAccountDetails details = getAccountDetails().get(0);
        return new AspspAccountReference(details.getIban(), null, null, null, null, details.getCurrency());
    }
}
