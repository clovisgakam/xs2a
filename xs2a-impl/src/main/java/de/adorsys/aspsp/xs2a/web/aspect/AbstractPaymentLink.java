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

package de.adorsys.aspsp.xs2a.web.aspect;

import de.adorsys.aspsp.xs2a.component.JsonConverter;
import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentType;
import de.adorsys.aspsp.xs2a.service.message.MessageService;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.aspsp.profile.domain.ScaApproach;

import java.util.EnumSet;
import java.util.List;

import static de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus.RJCT;
import static de.adorsys.aspsp.xs2a.domain.consent.Xs2aAuthorisationStartType.EXPLICIT;
import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.PERIODIC;
import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.SINGLE;

public abstract class AbstractPaymentLink<T> extends AbstractLinkAspect<T> {

    public AbstractPaymentLink(int maxNumberOfCharInTransactionJson, AspspProfileServiceWrapper aspspProfileService, JsonConverter jsonConverter, MessageService messageService) {
        super(maxNumberOfCharInTransactionJson, aspspProfileService, jsonConverter, messageService);
    }

    @SuppressWarnings("unchecked")
    protected ResponseObject<?> enrichLink(ResponseObject<?> result, PaymentType paymentType, String psuId) {
        Object body = result.getBody();
        if (EnumSet.of(SINGLE, PERIODIC).contains(paymentType)) {
            doEnrichLink(paymentType, (PaymentInitialisationResponse) body, psuId);
        } else {
            ((List<PaymentInitialisationResponse>) body)
                .forEach(r -> doEnrichLink(paymentType, r, psuId));
        }
        return result;
    }

    private void doEnrichLink(PaymentType paymentType, PaymentInitialisationResponse body, String psuId) {
        body.setLinks(buildPaymentLinks(body, paymentType.getValue(), psuId));
    }

    //TODO encode payment id with base64 encoding and add decoders to every endpoint links lead https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/382
    private Links buildPaymentLinks(PaymentInitialisationResponse body, String paymentService, String psuId) {
        if (RJCT == body.getTransactionStatus()) {
            return null;
        }
        String paymentId = body.getPaymentId();

        Links links = new Links();
        links.setSelf(buildPath("/v1/{paymentService}/{paymentId}", paymentService, paymentId));
        links.setStatus(buildPath("/v1/{paymentService}/{paymentId}/status", paymentService, paymentId));
        if (aspspProfileService.getScaApproach() == ScaApproach.EMBEDDED) {
            return addEmbeddedRelatedLinks(links, paymentService, paymentId, body.getAuthorisationId());
        } else if (aspspProfileService.getScaApproach() == ScaApproach.REDIRECT) {
            links.setScaRedirect(aspspProfileService.getPisRedirectUrlToAspsp() + body.getPisConsentId() + "/" + paymentId + "/" + psuId);
        } else if (aspspProfileService.getScaApproach() == ScaApproach.OAUTH) {
            links.setScaOAuth("scaOAuth"); //TODO generate link for oauth https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/326
        }
        return links;
    }

    private Links addEmbeddedRelatedLinks(Links links, String paymentService, String paymentId, String authorisationId) {
        if (EXPLICIT == aspspProfileService.getAuthorisationStartType()) {
            links.setStartAuthorisation(buildPath("/v1/{payment-service}/{paymentId}/authorisations", paymentService, paymentId));
        } else {
            links.setScaStatus(
                buildPath("/v1/{paymentService}/{paymentId}/authorisations/{authorisationId}", paymentService, paymentId, authorisationId));
            links.setStartAuthorisationWithPsuAuthentication(
                buildPath("/v1/{paymentService}/{paymentId}/authorisations/{authorisationId}", paymentService, paymentId, authorisationId));
        }

        return links;
    }
}
