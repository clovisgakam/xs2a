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

package de.adorsys.aspsp.xs2a.service.payment;

import de.adorsys.aspsp.xs2a.domain.TppInfo;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aPaymentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPeriodicPaymentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiSinglePaymentInitiateResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PeriodicPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RedirectAndEmbeddedPaymentService implements ScaPaymentService {
    private final PaymentSpi paymentSpi;
    private final PaymentMapper paymentMapper;

    private final SinglePaymentSpi singlePaymentSpi;
    private final PeriodicPaymentSpi periodicPaymentSpi;
    private final Xs2aToSpiPaymentMapper xs2aToSpiPaymentMapper;
    private final Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper;
    private final SpiToXs2aPaymentMapper spiToXs2aPaymentMapper;

    @Override
    public SinglePaymentInitiateResponse createSinglePayment(SinglePayment payment, TppInfo tppInfo, PaymentProduct paymentProduct) {
        // TODO Read and update aspspConsentData before and after initiatePayment  https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/391
        // TODO don't create AspspConsentData without consentId https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332

        SpiPsuData psuData = new SpiPsuData(null, null, null, null); // TODO get it from XS2A Interface https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332
        SpiResponse<SpiSinglePaymentInitiateResponse> response = singlePaymentSpi.initiatePayment(psuData, xs2aToSpiPaymentMapper.mapToSpiSinglePayment(payment, paymentProduct), new AspspConsentData());
        return spiToXs2aPaymentMapper.mapToSinglePaymentResponse(response.getPayload());
    }

    @Override
    public PaymentInitialisationResponse createPeriodicPayment(PeriodicPayment payment, TppInfo tppInfo, String paymentProduct) {
        //TODO don't create AspspConsentData without consentId https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332
        AspspConsentData aspspConsentData = new AspspConsentData();
        SpiPsuData psuData = new SpiPsuData(null, null, null, null); // TODO get it from XS2A Interface https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332
        SpiPeriodicPayment spiPeriodicPayment = xs2aToSpiPeriodicPaymentMapper.mapToSpiPeriodicPayment(payment, paymentProduct);
        SpiResponse<SpiPaymentInitialisationResponse> response = periodicPaymentSpi.initiatePayment(psuData, spiPeriodicPayment, aspspConsentData);
        SpiPaymentInitialisationResponse paymentInitialisationResponse = response.getPayload();

        return paymentMapper.mapToPaymentInitializationResponse(paymentInitialisationResponse, response.getAspspConsentData());
    }

    @Override
    public List<PaymentInitialisationResponse> createBulkPayment(BulkPayment bulkPayment, TppInfo tppInfo, String paymentProduct) {
        SpiResponse<List<SpiPaymentInitialisationResponse>> response = paymentSpi.createBulkPayments(paymentMapper.mapToSpiBulkPayment(bulkPayment), new AspspConsentData()); //TODO don't create AspspConsentData without consentId https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332
        final AspspConsentData aspspConsentData = response.getAspspConsentData();
        return response.getPayload()
                   .stream()
                   .map((SpiPaymentInitialisationResponse resp) ->
                            paymentMapper.mapToPaymentInitializationResponse(resp, aspspConsentData))
                   .collect(Collectors.toList());
    }
}
