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

package de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.redirect;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.And;
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.FeatureFileSteps;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.aspsp.xs2a.integtest.util.PaymentUtils;
import de.adorsys.psd2.model.PeriodicPaymentInitiationSctJson;
import de.adorsys.psd2.model.TppMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.resourceToString;

@FeatureFileSteps
public class PeriodicPaymentErrorfulSteps {

    private static final long DAYS_OFFSET = 100L;

    @Autowired
    @Qualifier("xs2a")
    private RestTemplate restTemplate;

    @Autowired
    private Context<PeriodicPaymentInitiationSctJson, TppMessages> context;

    @Autowired
    private ObjectMapper mapper;

    private String dataFileName;

    @And("^PSU loads an errorful recurring payment (.*) using the payment service (.*) and the payment product (.*)$")
    public void loadTestDataForErrorfulPeriodicPayment(String dataFileName, String paymentService, String paymentProduct) throws IOException {
        context.setPaymentProduct(paymentProduct);
        context.setPaymentService(paymentService);
        this.dataFileName = dataFileName;

        TestData<PeriodicPaymentInitiationSctJson, TppMessages> data = mapper.readValue(
            resourceToString("/data-input/pis/recurring/" + dataFileName, UTF_8),
            new TypeReference<TestData<PeriodicPaymentInitiationSctJson, TppMessages>>() {
            });

        context.setTestData(data);
        context.getTestData().getRequest().getBody().setEndDate(LocalDate.now().plusDays(DAYS_OFFSET));
    }

    @When("^PSU sends the recurring payment initiating request with error$")
    public void sendFalsePeriodicPaymentInitiatingRequest() throws IOException {
        HttpEntity entity = PaymentUtils.getHttpEntity(
            context.getTestData().getRequest(), context.getAccessToken());

        if (dataFileName.contains("end-date-before-start-date")) {
            makeEndDateBeforeStartDate(entity);
        }

        try {
             restTemplate.exchange(
                context.getBaseUrl() + "/" + context.getPaymentService() + "/" + context.getPaymentProduct(),
                HttpMethod.POST,
                entity,
                HashMap.class);

        } catch (RestClientResponseException restClientResponseException) {
            context.handleRequestError(restClientResponseException);
        }
    }

    private void makeEndDateBeforeStartDate(HttpEntity<PeriodicPaymentInitiationSctJson> entity) {
        entity.getBody().setEndDate(entity.getBody().getStartDate().minusDays(DAYS_OFFSET));
    }

    // @Then("^an error response code and the appropriate error response are received$")
    // See GlobalErrorfulSteps
}
