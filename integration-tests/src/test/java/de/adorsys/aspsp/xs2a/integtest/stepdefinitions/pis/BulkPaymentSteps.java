
package de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.aspsp.xs2a.integtest.util.PaymentUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class BulkPaymentSteps {

    @Autowired
    @Qualifier("xs2a")
    private RestTemplate restTemplate;

    @Autowired
    private Context<List<SinglePayments>, List<HashMap>, List<PaymentInitialisationResponse>> context;

    @Given("^PSU wants to initiate multiple payments (.*) using the payment product (.*)$")
    public void loadTestDataBulkPayment(String dataFileName, String paymentProduct) throws IOException {
        context.setPaymentProduct(paymentProduct);

        File jsonFile = new File("src/test/resources/data-input/pis/bulk/" + dataFileName);

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        TestData<List<SinglePayments>, List<HashMap>> data = mapper.readValue(jsonFile, new TypeReference<TestData<List<SinglePayments>, List<HashMap>>>() {
        });

        context.setTestData(data);
    }

    @When("^PSU sends the bulk payment initiating request$")
    public void sendBulkPaymentInitiatingRequest() {
        HttpEntity<List<SinglePayments>> entity = PaymentUtils.getPaymentsHttpEntity(context.getTestData().getRequest(), context.getAccessToken());

        ResponseEntity<List<PaymentInitialisationResponse>> response = restTemplate.exchange(
            context.getBaseUrl() + "/bulk-payments/" + context.getPaymentProduct(),
            HttpMethod.POST, entity, new ParameterizedTypeReference<List<PaymentInitialisationResponse>>() {
            });

        context.setActualResponse(response);
    }

    @Then("^a successful response code and the appropriate bulk payment response data$")
    public void checkResponseCodeBulkPayment() {
        ResponseEntity<List<PaymentInitialisationResponse>> actualResponse = context.getActualResponse();
        List<HashMap> givenResponseBody = context.getTestData().getResponse().getBody();

        HttpStatus compareStatus = HttpStatus.valueOf(context.getTestData().getResponse().getCode());
        assertThat(actualResponse.getStatusCode(), equalTo(compareStatus));

        assertThat(actualResponse.getBody().get(0).getTransactionStatus().name(), equalTo(givenResponseBody.get(0).get("transactionStatus")));
        assertThat(actualResponse.getStatusCode(), notNullValue());

        assertThat(actualResponse.getBody().get(1).getTransactionStatus().name(), equalTo(givenResponseBody.get(1).get("transactionStatus")));

    }

    @And("^a redirect URL for every payment of the Bulk payment is delivered to the PSU$")
    public void checkRedirectUrlBulkPayment() {
        ResponseEntity<List<PaymentInitialisationResponse>> actualResponse = context.getActualResponse();

        assertThat(actualResponse.getBody().get(0).getLinks().getScaRedirect(), notNullValue());
        assertThat(actualResponse.getBody().get(1).getLinks().getScaRedirect(), notNullValue());
    }
}

