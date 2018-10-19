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

import de.adorsys.aspsp.aspspmockserver.domain.spi.account.SpiAccountBalance;
import de.adorsys.aspsp.aspspmockserver.domain.spi.account.SpiAccountDetails;
import de.adorsys.aspsp.aspspmockserver.domain.spi.account.SpiAccountReference;
import de.adorsys.aspsp.aspspmockserver.domain.spi.account.SpiBalanceType;
import de.adorsys.aspsp.aspspmockserver.domain.spi.common.SpiAmount;
import de.adorsys.aspsp.aspspmockserver.domain.spi.payment.AspspPayment;
import de.adorsys.aspsp.aspspmockserver.domain.spi.payment.SpiBulkPayment;
import de.adorsys.aspsp.aspspmockserver.domain.spi.payment.SpiCancelPayment;
import de.adorsys.aspsp.aspspmockserver.domain.spi.payment.SpiSinglePayment;
import de.adorsys.aspsp.aspspmockserver.repository.PaymentRepository;
import de.adorsys.aspsp.aspspmockserver.service.mapper.PaymentMapper;
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
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceTest {
    private static final String PAYMENT_ID = "123456789";
    private static final String WRONG_PAYMENT_ID = "0";
    private static final String IBAN = "DE123456789";
    private static final String WRONG_IBAN = "wrong_iban";
    private static final long BALANCE_AMOUNT = 100;
    private static final long AMOUNT_TO_TRANSFER = BALANCE_AMOUNT / 2;
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
            .thenReturn(getAspspPayment(AMOUNT_TO_TRANSFER));
        when(paymentRepository.save(anyListOf(AspspPayment.class)))
            .thenReturn(Collections.singletonList(getAspspPayment(AMOUNT_TO_TRANSFER)));
        when(paymentRepository.exists(PAYMENT_ID))
            .thenReturn(true);
        when(paymentRepository.exists(WRONG_PAYMENT_ID))
            .thenReturn(false);
        when(accountService.getPsuIdByIban(IBAN)).thenReturn(Optional.of(getAccountDetails().get(0).getId()));
        when(accountService.getAccountsByIban(IBAN)).thenReturn(getAccountDetails());
        when(accountService.getAccountsByIban(WRONG_IBAN)).thenReturn(null);
        when(paymentMapper.mapToAspspPayment(any(), any())).thenReturn(new AspspPayment());
        when(paymentMapper.mapToSpiSinglePayment(any(AspspPayment.class))).thenReturn(getSpiSinglePayment(AMOUNT_TO_TRANSFER));
        when(paymentService.cancelPayment(PAYMENT_ID)).thenReturn(buildSpiCancelPayment());
        when(paymentRepository.findOne(PAYMENT_ID)).thenReturn(new AspspPayment());
    }

    @Test
    public void addPayment_Success() {
        when(accountService.getAccountsByIban(IBAN)).thenReturn(getAccountDetails());
        //Given
        SpiSinglePayment expectedPayment = getSpiSinglePayment(AMOUNT_TO_TRANSFER);

        //When
        Optional<SpiSinglePayment> spiSinglePayment = paymentService.addPayment(expectedPayment);
        SpiSinglePayment actualPayment = spiSinglePayment.orElse(null);

        //Then
        assertThat(actualPayment).isNotNull();
    }

    @Test
    public void addPayment_AmountsAreEqual() {
        //Given
        SpiSinglePayment expectedPayment = getSpiSinglePayment(BALANCE_AMOUNT);

        //When
        Optional<SpiSinglePayment> actualPayment = paymentService.addPayment(expectedPayment);

        //Then
        assertThat(actualPayment).isNotNull();
    }

    @Test
    public void addPayment_Failure() {
        //Given
        SpiSinglePayment expectedPayment = getSpiSinglePayment(BALANCE_AMOUNT + 1);

        //When
        Optional<SpiSinglePayment> actualPayment = paymentService.addPayment(expectedPayment);

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
    public void addBulkPayments_Success() {
        List<AspspPayment> payments = Collections.singletonList(getAspspPayment(AMOUNT_TO_TRANSFER));
        when(paymentMapper.mapToAspspPaymentList(any())).thenReturn(payments);
        when(paymentRepository.save(anyListOf(AspspPayment.class))).thenReturn(payments);
        when(paymentMapper.mapToSpiSinglePaymentList(anyListOf(AspspPayment.class)))
            .thenReturn(Collections.singletonList(getSpiSinglePayment(AMOUNT_TO_TRANSFER)));

        //Given
        SpiBulkPayment spiBulkPayment = new SpiBulkPayment();
        spiBulkPayment.setPayments(new ArrayList<>());
        spiBulkPayment.getPayments().add(getSpiSinglePayment(AMOUNT_TO_TRANSFER));

        //When
        Optional<SpiBulkPayment> actualPayment = paymentService.addBulkPayments(spiBulkPayment);

        //Then
        assertThat(actualPayment.isPresent()).isTrue();
        SpiBulkPayment bulkPayment = actualPayment.get();
        assertThat(bulkPayment.getPayments().size()).isEqualTo(1);
    }

    @Test
    public void addBulkPayments_Failure_InsufficientFunds() {
        when(paymentMapper.mapToAspspPaymentList(any()))
            .thenReturn(Arrays.asList(getAspspPayment(AMOUNT_TO_TRANSFER), getAspspPayment(AMOUNT_TO_TRANSFER + 1)));

        //Given
        SpiBulkPayment spiBulkPayment = new SpiBulkPayment();
        List<SpiSinglePayment> payments = Arrays.asList(getSpiSinglePayment(AMOUNT_TO_TRANSFER),
                                                        getSpiSinglePayment(AMOUNT_TO_TRANSFER + 1));
        spiBulkPayment.setPayments(payments);

        //When
        Optional<SpiBulkPayment> actualPayment = paymentService.addBulkPayments(spiBulkPayment);

        //Then
        assertThat(actualPayment.isPresent()).isFalse();
    }

    @Test
    public void cancelPaymentSuccess() {
        //Given
        Optional<SpiCancelPayment> given = buildSpiCancelPayment();

        //When
        Optional<SpiCancelPayment> actual = paymentService.cancelPayment(PAYMENT_ID);

        //Then
        assertThat(given).isEqualTo(actual);
    }

    private Optional<SpiCancelPayment> buildSpiCancelPayment() {
        return Optional.of(new SpiCancelPayment());
    }

    private SpiSinglePayment getSpiSinglePayment(long amountToTransfer) {
        SpiSinglePayment payment = new SpiSinglePayment();
        SpiAmount amount = new SpiAmount(Currency.getInstance("EUR"), new BigDecimal(amountToTransfer));
        payment.setInstructedAmount(amount);
        payment.setDebtorAccount(getReference());
        payment.setCreditorName("Merchant123");
        payment.setPurposeCode("BEQNSD");
        payment.setCreditorAgent("sdasd");
        payment.setCreditorAccount(getReference());
        payment.setRemittanceInformationUnstructured("Ref Number Merchant");

        return payment;
    }

    private AspspPayment getAspspPayment(long amountToTransfer) {
        AspspPayment payment = new AspspPayment();
        SpiAmount amount = new SpiAmount(Currency.getInstance("EUR"), new BigDecimal(amountToTransfer));
        payment.setInstructedAmount(amount);
        payment.setDebtorAccount(getReference());
        payment.setCreditorName("Merchant123");
        payment.setPurposeCode("BEQNSD");
        payment.setCreditorAgent("sdasd");
        payment.setCreditorAccount(getReference());
        payment.setRemittanceInformationUnstructured("Ref Number Merchant");
        return payment;
    }

    private List<SpiAccountDetails> getAccountDetails() {
        return Collections.singletonList(
            new SpiAccountDetails("12345", IBAN, null, null, null, null, CURRENCY, "Peter", null, null, null, null, null, null, null, getBalances())
        );
    }

    private List<SpiAccountBalance> getBalances() {
        SpiAccountBalance balance = new SpiAccountBalance();
        balance.setSpiBalanceAmount(new SpiAmount(CURRENCY, BigDecimal.valueOf(BALANCE_AMOUNT)));
        balance.setSpiBalanceType(SpiBalanceType.INTERIM_AVAILABLE);
        return Collections.singletonList(balance);
    }

    private SpiAccountReference getReference() {
        SpiAccountDetails details = getAccountDetails().get(0);
        return new SpiAccountReference(details.getIban(), null, null, null, null, details.getCurrency());
    }
}
