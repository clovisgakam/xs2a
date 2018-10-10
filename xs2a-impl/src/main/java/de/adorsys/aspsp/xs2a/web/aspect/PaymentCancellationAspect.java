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
import de.adorsys.aspsp.xs2a.domain.pis.CancelPaymentResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentType;
import de.adorsys.aspsp.xs2a.service.message.MessageService;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.aspsp.xs2a.web.PaymentController;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PaymentCancellationAspect extends AbstractLinkAspect<PaymentController> {
    public PaymentCancellationAspect(int maxNumberOfCharInTransactionJson, AspspProfileServiceWrapper aspspProfileService, JsonConverter jsonConverter, MessageService messageService) {
        super(maxNumberOfCharInTransactionJson, aspspProfileService, jsonConverter, messageService);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.aspsp.xs2a.service.PaymentService.cancelPayment(..)) && args(paymentType,paymentId)", returning = "result", argNames = "result,paymentType,paymentId")
    public ResponseObject<CancelPaymentResponse> cancelPayment(ResponseObject<CancelPaymentResponse> result, PaymentType paymentType, String paymentId) {
        if (!result.hasError()) {
            CancelPaymentResponse response = result.getBody();
            response.setLinks(buildCancellationLinks(response.isStartAuthorisationRequired(), paymentType, paymentId));
            return result;
        }
        return enrichErrorTextMessage(result);
    }

    private Links buildCancellationLinks(boolean startAuthorisationRequired, PaymentType paymentType, String paymentId) {
        Links links = new Links();
        if (startAuthorisationRequired) {
            links.setStartAuthorisation(buildPath("/v1/{payment-service}/{payment-id}/authorisations", paymentType.getValue(), paymentId));
        }
        return links;
    }
}
