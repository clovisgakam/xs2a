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

package de.adorsys.aspsp.xs2a.spi.impl.v2;

import de.adorsys.aspsp.xs2a.component.JsonConverter;
import de.adorsys.aspsp.xs2a.exception.RestException;
import de.adorsys.aspsp.xs2a.spi.config.rest.AspspRemoteUrls;
import de.adorsys.aspsp.xs2a.spi.domain.SpiAspspAuthorisationData;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.impl.service.KeycloakInvokerService;
import de.adorsys.aspsp.xs2a.spi.mapper.SpiPeriodicPaymentMapper;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.*;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.PeriodicPaymentSpi;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus.FAILURE;
import static de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus.SUCCESS;
import static de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse.VoidResponse;

@Service
@AllArgsConstructor
public class PeriodicPaymentSpiImpl implements PeriodicPaymentSpi {
    @Qualifier("aspspRestTemplate")
    private final RestTemplate aspspRestTemplate;
    private final SpiPeriodicPaymentMapper spiPeriodicPaymentMapper;
    private final AspspRemoteUrls aspspRemoteUrls;
    private final KeycloakInvokerService keycloakInvokerService;
    private final JsonConverter jsonConverter;

    @Override
    @NotNull
    public SpiResponse<SpiPaymentInitialisationResponse> initiatePayment(@NotNull SpiPsuData psuData, @NotNull SpiPeriodicPayment payment, @NotNull AspspConsentData aspspConsentData) {
        de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment request = spiPeriodicPaymentMapper.mapToAspspSpiPeriodicPayment(payment);

        try {
            ResponseEntity<de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment> aspspResponse =
                aspspRestTemplate.postForEntity(aspspRemoteUrls.createPeriodicPayment(), request, de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment.class);
            SpiPaymentInitialisationResponse spiPaymentInitialisationResponse = spiPeriodicPaymentMapper.mapToSpiPaymentResponse(aspspResponse.getBody());

            return new SpiResponse<>(spiPaymentInitialisationResponse, aspspConsentData);

        } catch (RestException e) {

            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {

                return SpiResponse.<SpiPaymentInitialisationResponse>builder()
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }

            return SpiResponse.<SpiPaymentInitialisationResponse>builder()
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }

    @Override
    @NotNull
    public SpiResponse<SpiPeriodicPayment> getPaymentById(@NotNull SpiPsuData psuData, @NotNull SpiPeriodicPayment payment, @NotNull AspspConsentData aspspConsentData) {
        try {
            ResponseEntity<List<de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment>> aspspResponse =
                aspspRestTemplate.exchange(aspspRemoteUrls.getPaymentById(), HttpMethod.GET, null, new ParameterizedTypeReference<List<de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment>>() {
                }, payment.getPaymentType(), payment.getPaymentProduct(), payment.getPaymentId());
            de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment periodic = aspspResponse.getBody().get(0);
            SpiPeriodicPayment spiPeriodicPayment = spiPeriodicPaymentMapper.mapToSpiPeriodicPayment(periodic, payment.getPaymentProduct());

            return new SpiResponse<>(spiPeriodicPayment, aspspConsentData);

        } catch (RestException e) {

            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {

                return SpiResponse.<SpiPeriodicPayment>builder()
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }

            return SpiResponse.<SpiPeriodicPayment>builder()
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }

    @Override
    @NotNull
    public SpiResponse<SpiTransactionStatus> getPaymentStatusById(@NotNull SpiPsuData psuData, @NotNull SpiPeriodicPayment payment, @NotNull AspspConsentData aspspConsentData) {
        try {
            ResponseEntity<SpiTransactionStatus> aspspResponse = aspspRestTemplate.getForEntity(aspspRemoteUrls.getPaymentStatus(), SpiTransactionStatus.class, payment.getPaymentId());
            SpiTransactionStatus status = aspspResponse.getBody();

            return new SpiResponse<>(status, aspspConsentData);

        } catch (RestException e) {

            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {

                return SpiResponse.<SpiTransactionStatus>builder()
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }

            return SpiResponse.<SpiTransactionStatus>builder()
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }

    @Override
    @NotNull
    public SpiResponse<SpiAuthorisationStatus> authorisePsu(@NotNull SpiPsuData psuData, String password, SpiPeriodicPayment businessObject, AspspConsentData aspspConsentData) {
        Optional<SpiAspspAuthorisationData> accessToken = keycloakInvokerService.obtainAuthorisationData(psuData.getPsuId(), password);
        SpiAuthorisationStatus spiAuthorisationStatus = accessToken.map(t -> SUCCESS)
                                                            .orElse(FAILURE);
        byte[] payload = accessToken.flatMap(jsonConverter::toJson)
                             .map(String::getBytes)
                             .orElse(null);

        return new SpiResponse<>(spiAuthorisationStatus, aspspConsentData.respondWith(payload));
    }

    @Override
    @NotNull
    public SpiResponse<List<SpiScaMethod>> requestAvailableScaMethods(@NotNull SpiPsuData psuData, SpiPeriodicPayment businessObject, AspspConsentData aspspConsentData) {
        ResponseEntity<List<SpiScaMethod>> aspspResponse = aspspRestTemplate.exchange(aspspRemoteUrls.getScaMethods(), HttpMethod.GET, null, new ParameterizedTypeReference<List<SpiScaMethod>>() {
        }, psuData.getPsuId());

        List<SpiScaMethod> spiScaMethods = Optional.ofNullable(aspspResponse.getBody())
                                               .orElseGet(Collections::emptyList);

        return new SpiResponse<>(spiScaMethods, aspspConsentData);
    }

    @Override
    @NotNull
    public SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(@NotNull SpiPsuData psuData, @NotNull SpiScaMethod scaMethod, @NotNull SpiPeriodicPayment businessObject, @NotNull AspspConsentData aspspConsentData) {
        try {
            aspspRestTemplate.exchange(aspspRemoteUrls.getGenerateTanConfirmation(), HttpMethod.POST, null, Void.class, psuData.getPsuId(), scaMethod);

            return SpiResponse.<SpiAuthorizationCodeResult>builder()
                       .success();

        } catch (RestException e) {
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {

                return SpiResponse.<SpiAuthorizationCodeResult>builder()
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }

            return SpiResponse.<SpiAuthorizationCodeResult>builder()
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }

    @Override
    @NotNull
    public SpiResponse<VoidResponse> verifyAuthorisationCodeAndExecuteRequest(@NotNull SpiPsuData psuData, @NotNull SpiScaConfirmation spiScaConfirmation, @NotNull SpiPeriodicPayment businessObject, @NotNull AspspConsentData aspspConsentData) {
        try {
            aspspRestTemplate.exchange(aspspRemoteUrls.applyStrongUserAuthorisation(), HttpMethod.PUT, new HttpEntity<>(spiScaConfirmation), ResponseEntity.class);

            return SpiResponse.<VoidResponse>builder()
                       .success();

        } catch (RestException e) {
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {

                return SpiResponse.<VoidResponse>builder()
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }

            return SpiResponse.<VoidResponse>builder()
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }
}
