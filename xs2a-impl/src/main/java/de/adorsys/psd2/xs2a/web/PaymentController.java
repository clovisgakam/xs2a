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

package de.adorsys.psd2.xs2a.web;

import de.adorsys.psd2.api.PaymentApi;
import de.adorsys.psd2.model.PaymentInitiationCancelResponse200202;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentProduct;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.domain.pis.CancelPaymentResponse;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.ConsentService;
import de.adorsys.psd2.xs2a.service.PaymentService;
import de.adorsys.psd2.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.xs2a.web.mapper.ConsentModelMapper;
import de.adorsys.psd2.xs2a.web.mapper.PaymentModelMapperPsd2;
import de.adorsys.psd2.xs2a.web.mapper.PaymentModelMapperXs2a;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.FORMAT_ERROR;

@SuppressWarnings("unchecked") // This class implements autogenerated interface without proper return values generated
@RestController
@AllArgsConstructor
@Api(value = "v1", description = "Provides access to the payment initiation", tags = {"Payment Initiation Service (PIS)"})
public class PaymentController implements PaymentApi {
    private final PaymentService xs2aPaymentService;
    private final ResponseMapper responseMapper;
    private final PaymentModelMapperPsd2 paymentModelMapperPsd2;
    private final PaymentModelMapperXs2a paymentModelMapperXs2a;
    private final ConsentService consentService;
    private final ConsentModelMapper consentModelMapper;

    @Override
    public ResponseEntity getPaymentInitiationStatus(String paymentService, String paymentId, UUID xRequestID, String digest,
                                                     String signature, byte[] tpPSignatureCertificate, String psUIPAddress,
                                                     Object psUIPPort, String psUAccept, String psUAcceptCharset,
                                                     String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent,
                                                     String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        ResponseObject<TransactionStatus> response = PaymentType.getByValue(paymentService)
                                                         .map(pt -> xs2aPaymentService.getPaymentStatusById(pt, paymentId))
                                                         .orElseGet(ResponseObject.<TransactionStatus>builder()
                                                                        .fail(new MessageError(FORMAT_ERROR))::build);

        return responseMapper.ok(response, PaymentModelMapperPsd2::mapToStatusResponse12);
    }

    @Override
    public ResponseEntity getPaymentInformation(String paymentService, String paymentId, UUID xRequestID, String digest,
                                                String signature, byte[] tpPSignatureCertificate, String psUIPAddress,
                                                Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding,
                                                String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod,
                                                UUID psUDeviceID, String psUGeoLocation) {
        ResponseObject response = PaymentType.getByValue(paymentService)
                                      .map(pt -> xs2aPaymentService.getPaymentById(pt, paymentId))
                                      .orElseGet(ResponseObject.builder()
                                                     .fail(new MessageError(FORMAT_ERROR))::build);

        //TODO check for Optional.get() without check for value presence https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/380
        return response.hasError()
                   ? responseMapper.ok(response)
                   : responseMapper.ok(ResponseObject.builder().body(paymentModelMapperPsd2.mapToGetPaymentResponse12(response.getBody(), PaymentType.getByValue(paymentService).get(),
                                                                                                                      PaymentProduct.SEPA)).build());
    }

    @Override
    public ResponseEntity initiatePayment(Object body, String paymentService, String paymentProduct, UUID xRequestID,
                                          String psUIPAddress, String digest, String signature, byte[] tpPSignatureCertificate,
                                          String PSU_ID, String psUIDType, String psUCorporateID, String psUCorporateIDType,
                                          String consentID, Boolean tpPRedirectPreferred, String tpPRedirectURI,
                                          String tpPNokRedirectURI, Boolean tpPExplicitAuthorisationPreferred,
                                          Object psUIPPort, String psUAccept, String psUAcceptCharset,
                                          String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent,
                                          String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        PsuIdData psuData = new PsuIdData(PSU_ID, psUIDType, psUCorporateID, psUCorporateIDType);
        PaymentInitiationParameters paymentInitiationParameters = paymentModelMapperPsd2.mapToPaymentRequestParameters(paymentProduct, paymentService, tpPSignatureCertificate, tpPRedirectURI, tpPNokRedirectURI, BooleanUtils.isTrue(tpPExplicitAuthorisationPreferred), psuData);
        ResponseObject serviceResponse =
            xs2aPaymentService.createPayment(paymentModelMapperXs2a.mapToXs2aPayment(body, paymentInitiationParameters), paymentInitiationParameters);

        return serviceResponse.hasError()
                   ? responseMapper.created(serviceResponse)
                   : responseMapper.created(ResponseObject
                                                .builder()
                                                .body(paymentModelMapperPsd2.mapToPaymentInitiationResponse12(serviceResponse.getBody()))
                                                .build());
    }

    @Override
    public ResponseEntity cancelPayment(String paymentService, String paymentId, UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        ResponseObject<CancelPaymentResponse> serviceResponse = PaymentType.getByValue(paymentService)
                                                                    .map(type -> xs2aPaymentService.cancelPayment(type, paymentId))
                                                                    .orElseGet(ResponseObject.<CancelPaymentResponse>builder()
                                                                                   .fail(new MessageError(FORMAT_ERROR))::build);

        if (serviceResponse.hasError()) {
            return responseMapper.ok(serviceResponse);
        }

        CancelPaymentResponse cancelPayment = serviceResponse.getBody();
        PaymentInitiationCancelResponse200202 response = paymentModelMapperPsd2.mapToPaymentInitiationCancelResponse(cancelPayment);

        return cancelPayment.isStartAuthorisationRequired()
                   ? responseMapper.accepted(ResponseObject.builder().body(response).build())
                   : responseMapper.ok(ResponseObject.builder().body(response).build());
    }

    @Override
    public ResponseEntity getPaymentCancellationScaStatus(String paymentService, String paymentId, String cancellationId, UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        return null; //TODO implement
    }

    @Override
    public ResponseEntity getPaymentInitiationAuthorisation(String paymentService, String paymentId, UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        return null; //TODO implement
    }

    @Override
    public ResponseEntity getPaymentInitiationCancellationAuthorisationInformation(String paymentService, String paymentId, UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        //Todo map response to CancellationList, when it will be possible https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/402
        return responseMapper.ok(consentService.getPaymentInitiationCancellationAuthorisationInformation(paymentId));
    }

    @Override
    public ResponseEntity getPaymentInitiationScaStatus(String paymentService, String paymentId, String authorisationId, UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        return null; //TODO implement
    }

    @Override
    public ResponseEntity startPaymentAuthorisation(String paymentService, String paymentId, UUID xRequestID, String PSU_ID, String psUIDType, String psUCorporateID, String psUCorporateIDType, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        PsuIdData psuData = new PsuIdData(PSU_ID, psUIDType, psUCorporateID, psUCorporateIDType);
        //TODO check for Optional.get() without check for value presence https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/380
        return responseMapper.created(consentService.createPisConsentAuthorization(paymentId, PaymentType.getByValue(paymentService).get(), psuData), consentModelMapper::mapToStartScaProcessResponse);
    }

    @Override
    public ResponseEntity startPaymentInitiationCancellationAuthorisation(String paymentService, String paymentId, UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate, String PSU_ID, String psUIDType, String psUCorporateID, String psUCorporateIDType, String psUIPAddress, Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        return responseMapper.created(consentService.createPisConsentCancellationAuthorization(paymentId, PaymentType.getByValue(paymentService).get()), consentModelMapper::mapToStartScaProcessResponse);
    }

    @Override
    public ResponseEntity updatePaymentCancellationPsuData(String paymentService, String paymentId, String cancellationId, UUID xRequestID, Object body, String digest, String signature, byte[] tpPSignatureCertificate, String PSU_ID, String psUIDType, String psUCorporateID, String psUCorporateIDType, String psUIPAddress, Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        PsuIdData psuData = new PsuIdData(PSU_ID, psUIDType, psUCorporateID, psUCorporateIDType);
        ResponseObject<Xs2aUpdatePisConsentPsuDataResponse> response = consentService.updatePisConsentCancellationPsuData(consentModelMapper.mapToPisUpdatePsuData(psuData, paymentId, cancellationId, paymentService, (Map) body));
        return responseMapper.ok(response, consentModelMapper::mapToUpdatePsuAuthenticationResponse);
    }

    @Override
    public ResponseEntity updatePaymentPsuData(String paymentService, String paymentId, String authorisationId, UUID xRequestID, Object body, String digest, String signature, byte[] tpPSignatureCertificate, String PSU_ID, String psUIDType, String psUCorporateID, String psUCorporateIDType, String psUIPAddress, Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        PsuIdData psuData = new PsuIdData(PSU_ID, psUIDType, psUCorporateID, psUCorporateIDType);
        return responseMapper.ok(consentService.updatePisConsentPsuData(consentModelMapper.mapToPisUpdatePsuData(psuData, paymentId, authorisationId, paymentService, (Map) body)), consentModelMapper::mapToUpdatePsuAuthenticationResponse);
    }
}
