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

package de.adorsys.aspsp.xs2a.web12;

import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.FundsConfirmationService;
import de.adorsys.aspsp.xs2a.service.mapper.ResponseMapper;
import de.adorsys.aspsp.xs2a.service.validator.AccountReferenceValidationService;
import de.adorsys.psd2.api.V1Api;
import de.adorsys.psd2.custom.AccountReference;
import de.adorsys.psd2.model.ConfirmationOfFunds;
import de.adorsys.psd2.model.InlineResponse200;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@RestController
public class FundsConfirmationController implements V1Api {

    private final FundsConfirmationService fundsConfirmationService;
    private final ResponseMapper responseMapper;
    private final AccountReferenceValidationService referenceValidationService;

    @Override
    public ResponseEntity<?> checkAvailabilityOfFunds(ConfirmationOfFunds confirmationOfFunds, UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate) {
        //TODO not bad request?
        Optional<MessageError> error = referenceValidationService.validateAccountReference((AccountReference) confirmationOfFunds.getAccount());
        return responseMapper.ok(
            error
                .map(e -> ResponseObject.<InlineResponse200>builder().fail(e).build())
                .orElse(fundsConfirmationService.fundsConfirmation(confirmationOfFunds)));

    }
}