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

package de.adorsys.aspsp.xs2a.web;

import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReference;
import de.adorsys.aspsp.xs2a.domain.consent.*;
import de.adorsys.aspsp.xs2a.service.AccountReferenceValidationService;
import de.adorsys.aspsp.xs2a.service.ConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.ResponseMapper;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Set;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/consents")
@Api(value = "api/v1/consents", tags = "AISP, Consents", description = "Provides access to the Psu Consents")
public class ConsentInformationController {
    private final ConsentService consentService;
    private final ResponseMapper responseMapper;
    private final AccountReferenceValidationService referenceValidationService;

    @ApiOperation(value = "Creates an account information consent resource at the ASPSP regarding access to accounts specified in this request.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "OK", response = CreateConsentResponse.class),
        @ApiResponse(code = 400, message = "Bad request")})
    @PostMapping
    @ApiImplicitParams({
        @ApiImplicitParam(name = "x-request-id", value = "2f77a125-aa7a-45c0-b414-cea25a116035", required = true, dataType = "UUID", paramType = "header"),
        @ApiImplicitParam(name = "date", value = "Sun, 11 Aug 2019 15:02:37 GMT", required = true, dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "psu-id", value = "31d50f56-a543-3664-b345-b9a822ae98a7", paramType = "header"),
        @ApiImplicitParam(name = "psu-id-type", value = "Type of the PSU-ID", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "psu-corporate-id", value = "Might be mandated in the ASPSP’s documentation", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "psu-corporate-id-type", value = "Might be mandated in the ASPSP’s documentation", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "tpp-redirect-uri", value = "http://example.com", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "digest", value = "730f75dafd73e047b86acb2dbd74e75dcb93272fa084a9082848f2341aa1abb6", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "signature", value = "98c0", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "tpp-signature-certificate", value = "some certificate", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "tpp-qwac-certificate", value = "qwac certificate", required = true, dataType = "String", paramType = "header")})
    public ResponseEntity<CreateConsentResponse> createAccountConsent(
        @RequestHeader(name = "psu-id", required = false) String psuId,
        @Valid @RequestBody CreateConsentReq createConsent) {
        Set<Xs2aAccountReference> references = createConsent.getAccountReferences();
        ResponseObject accountReferenceValidationResponse = references.isEmpty()
                                                                ? ResponseObject.builder().build()
                                                                : referenceValidationService.validateAccountReferences(createConsent.getAccountReferences());
        return responseMapper.created(
            accountReferenceValidationResponse.hasError()
                ? ResponseObject.<CreateConsentResponse>builder().fail(accountReferenceValidationResponse.getError()).build()
                : consentService.createAccountConsentsWithResponse(createConsent, psuId));
    }

    @ApiOperation(value = "Can check the status of an account information consent resource")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ConsentStatus.class),
        @ApiResponse(code = 400, message = "Bad request")})
    @GetMapping(path = "/{consent-id}/status")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "x-request-id", value = "2f77a125-aa7a-45c0-b414-cea25a116035", required = true, dataType = "UUID", paramType = "header"),
        @ApiImplicitParam(name = "date", value = "Sun, 11 Aug 2019 15:02:37 GMT", required = true, dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "digest", value = "730f75dafd73e047b86acb2dbd74e75dcb93272fa084a9082848f2341aa1abb6", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "signature", value = "98c0", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "tpp-signature-certificate", value = "some certificate", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "tpp-qwac-certificate", value = "qwac certificate", required = true, dataType = "String", paramType = "header")})
    public ResponseEntity<ConsentStatusResponse> getAccountConsentsStatusById(
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created resource", required = true)
        @PathVariable("consent-id") String consentId) {
        ResponseObject<ConsentStatusResponse> response = consentService.getAccountConsentsStatusById(consentId);
        return responseMapper.ok(response);
    }

    @ApiOperation(value = "Returns the content of an account information consent object")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AccountConsent.class),
        @ApiResponse(code = 400, message = "Bad request")})
    @GetMapping(path = "/{consent-id}")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "x-request-id", value = "2f77a125-aa7a-45c0-b414-cea25a116035", required = true, dataType = "UUID", paramType = "header"),
        @ApiImplicitParam(name = "date", value = "Sun, 11 Aug 2019 15:02:37 GMT", required = true, dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "digest", value = "730f75dafd73e047b86acb2dbd74e75dcb93272fa084a9082848f2341aa1abb6", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "signature", value = "98c0", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "tpp-signature-certificate", value = "some certificate", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "tpp-qwac-certificate", value = "qwac certificate", required = true, dataType = "String", paramType = "header")})
    public ResponseEntity<AccountConsent> getAccountConsentsInformationById(
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created resource", required = true)
        @PathVariable("consent-id") String consentId) {
        ResponseObject<AccountConsent> response = consentService.getAccountConsentById(consentId);
        return responseMapper.ok(response);
    }

    @ApiOperation(value = " Delete information consent object")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "No Content"),
        @ApiResponse(code = 403, message = "The Consent-ID cannot be matched by the ASPSP relative to the TPP.")})
    @DeleteMapping(path = "/{consent-id}")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "x-request-id", value = "2f77a125-aa7a-45c0-b414-cea25a116035", required = true, dataType = "UUID", paramType = "header"),
        @ApiImplicitParam(name = "date", value = "Sun, 11 Aug 2019 15:02:37 GMT", required = true, dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "tpp-qwac-certificate", value = "qwac certificate", required = true, dataType = "String", paramType = "header")})
    public ResponseEntity<Void> deleteAccountConsent(
        @ApiParam(name = "consent-id", value = "The resource-id of consent to be deleted", required = true)
        @PathVariable("consent-id") String consentId) {
        ResponseObject<Void> response = consentService.deleteAccountConsentsById(consentId);
        return responseMapper.delete(response);
    }

}
