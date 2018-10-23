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

package de.adorsys.aspsp.xs2a.spi.mapper;

import de.adorsys.psd2.aspsp.mock.api.payment.AspspSinglePayment;
import de.adorsys.psd2.xs2a.core.profile.PaymentProduct;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiSinglePaymentInitiationResponse;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SpiSinglePaymentMapper {
    private final SpiPaymentMapper spiPaymentMapper;

    public de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment mapToAspspSpiSinglePayment(@NotNull SpiSinglePayment payment) {
        de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment single = new de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment();
        single.setPaymentId(payment.getPaymentId());
        single.setEndToEndIdentification(payment.getEndToEndIdentification());
        single.setDebtorAccount(payment.getDebtorAccount());
        single.setInstructedAmount(payment.getInstructedAmount());
        single.setCreditorAccount(payment.getCreditorAccount());
        single.setCreditorAgent(payment.getCreditorAgent());
        single.setCreditorName(payment.getCreditorName());
        single.setCreditorAddress(payment.getCreditorAddress());
        single.setRemittanceInformationUnstructured(payment.getRemittanceInformationUnstructured());
        single.setPaymentStatus(SpiTransactionStatus.RCVD);
        return single;
    }

    public SpiSinglePayment mapToSpiSinglePayment(@NotNull AspspSinglePayment payment, PaymentProduct paymentProduct) {
        SpiSinglePayment single = new SpiSinglePayment(paymentProduct);
        single.setPaymentId(payment.getPaymentId());
        single.setEndToEndIdentification(payment.getEndToEndIdentification());
        single.setDebtorAccount(spiPaymentMapper.mapToSpiAccountReference(payment.getDebtorAccount()));
        single.setInstructedAmount(spiPaymentMapper.mapToSpiAmount(payment.getInstructedAmount()));
        single.setCreditorAccount(spiPaymentMapper.mapToSpiAccountReference(payment.getCreditorAccount()));
        single.setCreditorAgent(payment.getCreditorAgent());
        single.setCreditorName(payment.getCreditorName());
        single.setCreditorAddress(spiPaymentMapper.mapToSpiAddress(payment.getCreditorAddress()));
        single.setRemittanceInformationUnstructured(payment.getRemittanceInformationUnstructured());
        single.setPaymentStatus(SpiTransactionStatus.RCVD);
        return single;
    }

    public SpiSinglePaymentInitiationResponse mapToSpiSinglePaymentResponse(@NotNull AspspSinglePayment payment) {
        SpiSinglePaymentInitiationResponse spi = new SpiSinglePaymentInitiationResponse();
        spi.setPaymentId(payment.getPaymentId());
        if (payment.getPaymentId() == null) {
            spi.setTransactionStatus(SpiTransactionStatus.RJCT);
        } else {
            spi.setTransactionStatus(SpiTransactionStatus.RCVD);
        }
        return spi;
    }

    public AspspSinglePayment mapToAspspSinglePayment(@NotNull SpiSinglePayment spiSinglePayment) {
        AspspSinglePayment mockSingle = new AspspSinglePayment();
        mockSingle.setPaymentId(spiSinglePayment.getPaymentId());
        mockSingle.setEndToEndIdentification(spiSinglePayment.getEndToEndIdentification());
        mockSingle.setDebtorAccount(spiPaymentMapper.mapToAspspAccountReference(spiSinglePayment.getDebtorAccount()));
        mockSingle.setUltimateDebtor(null);
        mockSingle.setInstructedAmount(spiPaymentMapper.mapToAspspAmount(spiSinglePayment.getInstructedAmount()));
        mockSingle.setCreditorAccount(spiPaymentMapper.mapToAspspAccountReference(spiSinglePayment.getCreditorAccount()));
        mockSingle.setCreditorAgent(spiSinglePayment.getCreditorAgent());
        mockSingle.setCreditorName(spiSinglePayment.getCreditorName());
        mockSingle.setCreditorAddress(spiPaymentMapper.mapToAspspAddress(spiSinglePayment.getCreditorAddress()));
        mockSingle.setUltimateCreditor(null);
        mockSingle.setPurposeCode(null);
        mockSingle.setRemittanceInformationUnstructured(spiSinglePayment.getRemittanceInformationUnstructured());
        mockSingle.setRemittanceInformationStructured(null);
        mockSingle.setRequestedExecutionDate(null);
        mockSingle.setRequestedExecutionTime(null);
        mockSingle.setPaymentStatus(spiPaymentMapper.mapToAspspTransactionStatus(spiSinglePayment.getPaymentStatus()));
        return mockSingle;
    }

    public AspspSinglePayment mapToAspspSinglePayment(@NotNull de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment spiSinglePayment) {
        AspspSinglePayment mockSingle = new AspspSinglePayment();
        mockSingle.setPaymentId(spiSinglePayment.getPaymentId());
        mockSingle.setEndToEndIdentification(spiSinglePayment.getEndToEndIdentification());
        mockSingle.setDebtorAccount(spiPaymentMapper.mapToAspspAccountReference(spiSinglePayment.getDebtorAccount()));
        mockSingle.setUltimateDebtor(spiSinglePayment.getUltimateDebtor());
        mockSingle.setInstructedAmount(spiPaymentMapper.mapToAspspAmount(spiSinglePayment.getInstructedAmount()));
        mockSingle.setCreditorAccount(spiPaymentMapper.mapToAspspAccountReference(spiSinglePayment.getCreditorAccount()));
        mockSingle.setCreditorAgent(spiSinglePayment.getCreditorAgent());
        mockSingle.setCreditorName(spiSinglePayment.getCreditorName());
        mockSingle.setCreditorAddress(spiPaymentMapper.mapToAspspAddress(spiSinglePayment.getCreditorAddress()));
        mockSingle.setUltimateCreditor(spiSinglePayment.getUltimateCreditor());
        mockSingle.setPurposeCode(spiSinglePayment.getPurposeCode());
        mockSingle.setRemittanceInformationUnstructured(spiSinglePayment.getRemittanceInformationUnstructured());
        mockSingle.setRemittanceInformationStructured(spiPaymentMapper.mapToAspspRemittance(spiSinglePayment.getRemittanceInformationStructured()));
        mockSingle.setRequestedExecutionDate(spiSinglePayment.getRequestedExecutionDate());
        mockSingle.setRequestedExecutionTime(spiSinglePayment.getRequestedExecutionTime().toLocalDateTime());
        mockSingle.setPaymentStatus(spiPaymentMapper.mapToAspspTransactionStatus(spiSinglePayment.getPaymentStatus()));
        return mockSingle;
    }
}
