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

package de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.embedded;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.aspsp.xs2a.integtest.util.PaymentUtils;
import de.adorsys.psd2.model.StartScaprocessResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.resourceToString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class StartAuthorisationSuccessfulSteps {

    @Autowired
    @Qualifier("xs2a")
    private RestTemplate restTemplate;

    @Autowired
    private Context context;

    @Autowired
    private ObjectMapper mapper;

    //  @Given("^PSU wants to initiate a single payment (.*) using the payment service (.*) and the payment product (.*)$")
    // See SinglePaymentSuccessfulSteps

    // @And("^PSU sends the single payment initiating request and receives the paymentId$")
    // See GlobalSuccessfulSteps

    @And("^PSU wants to start the authorisation using the (.*)$")
    public void loadAuthorisationData(String authorisationData) throws IOException {
        TestData<HashMap, StartScaprocessResponse> data = mapper.readValue(resourceToString(
            "/data-input/pis/embedded/" + authorisationData, UTF_8),
            new TypeReference<TestData<HashMap, StartScaprocessResponse>>() {
            });

        context.setTestData(data);
    }

    @When("^PSU sends the start authorisation request$")
    public void sendAuthorisationRequest() {
        HttpEntity entity = PaymentUtils.getHttpEntity(
            context.getTestData().getRequest(), context.getAccessToken());

        ResponseEntity<StartScaprocessResponse> response = restTemplate.exchange(
            context.getBaseUrl() + "/" + context.getPaymentService() + "/" + context.getPaymentId() + "/authorisations",
            HttpMethod.POST,
            entity,
            StartScaprocessResponse.class);

        context.setActualResponse(response);
    }

    @Then("^PSU checks if a link is received and the SCA status is correct$")
    public void checkLinkAndScaStatusEmbedded (){
        ResponseEntity<StartScaprocessResponse> actualResponse = context.getActualResponse();
        StartScaprocessResponse givenResponseBody = (StartScaprocessResponse) context.getTestData().getResponse().getBody();

        assertThat(actualResponse.getStatusCode(), equalTo(context.getTestData().getResponse().getHttpStatus()));
        assertThat(actualResponse.getBody().getScaStatus(), equalTo(givenResponseBody.getScaStatus()));
        assertThat(actualResponse.getBody().getLinks().get("startAuthorisationWithPsuAuthentication"), notNullValue());
    }
}
