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

package de.adorsys.psd2.consent.web.aspsp;

import de.adorsys.psd2.consent.aspsp.api.piis.CmsAspspPiisService;
import de.adorsys.psd2.consent.aspsp.api.piis.PiisConsent;
import de.adorsys.psd2.consent.web.aspsp.domain.CreatePiisConsentRequest;
import de.adorsys.psd2.consent.web.aspsp.domain.CreatePiisConsentResponse;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CmsAspspPiisControllerTest {
    private static final String CONSENT_ID = "5bcf664f-68ce-498d-9a93-fe0cce32f6b6";
    private static final String WRONG_CONSENT_ID = "efe6d8bd-c6bc-4866-81a3-87ac755ffa4b";
    private static final String PSU_ID = "PSU-ID-1";
    private static final String WRONG_PSU_ID = "PSU-ID-2";

    @Mock
    private CmsAspspPiisService cmsAspspPiisService;

    @InjectMocks
    private CmsAspspPiisController cmsAspspPiisController;

    @Before
    public void setUp() {
        when(cmsAspspPiisService.createConsent(any(), any(), any(), any(), anyInt()))
            .thenReturn(Optional.of(CONSENT_ID));
        when(cmsAspspPiisService.getConsentsForPsu(buildPsuIdData(PSU_ID))).thenReturn(buildPiisConsentList());
        when(cmsAspspPiisService.getConsentsForPsu(buildPsuIdData(WRONG_PSU_ID))).thenReturn(Collections.emptyList());
        when(cmsAspspPiisService.terminateConsent(eq(CONSENT_ID))).thenReturn(true);
        when(cmsAspspPiisService.terminateConsent(eq(WRONG_CONSENT_ID))).thenReturn(false);
    }

    @Test
    public void createConsent_Success() {
        //When
        ResponseEntity<CreatePiisConsentResponse> actual =
            cmsAspspPiisController.createConsent(getCreatePiisConsentRequest(), null, null, null, null);

        //Then
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(actual.getBody().getConsentId()).isEqualTo(CONSENT_ID);
    }

    @Test
    public void createConsent_Failure() {
        when(cmsAspspPiisService.createConsent(any(), any(), any(), any(), anyInt())).thenReturn(Optional.empty());

        //When
        ResponseEntity<CreatePiisConsentResponse> actual =
            cmsAspspPiisController.createConsent(getCreatePiisConsentRequest(), null, null, null, null);

        //Then
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(actual.getBody()).isNull();
    }

    @Test
    public void getConsentsForPsu_Success() {
        // Given
        List<PiisConsent> expected = buildPiisConsentList();

        //When
        ResponseEntity<List<PiisConsent>> actual =
            cmsAspspPiisController.getConsentsForPsu(PSU_ID, null, null, null);

        //Then
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual.getBody()).isEqualTo(expected);
    }

    @Test
    public void getConsentsForPsu_Failure() {
        //When
        ResponseEntity<List<PiisConsent>> actual =
            cmsAspspPiisController.getConsentsForPsu(WRONG_PSU_ID, null, null, null);

        //Then
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual.getBody().isEmpty()).isTrue();
    }


    @Test
    public void terminateConsent_Success() {
        //When
        ResponseEntity<Boolean> actual =
            cmsAspspPiisController.terminateConsent(CONSENT_ID);

        //Then
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual.getBody()).isTrue();
    }

    @Test
    public void terminateConsent_Failure() {
        //When
        ResponseEntity<Boolean> actual =
            cmsAspspPiisController.terminateConsent(WRONG_CONSENT_ID);

        //Then
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual.getBody()).isFalse();
    }

    private CreatePiisConsentRequest getCreatePiisConsentRequest() {
        return new CreatePiisConsentRequest();
    }

    private PsuIdData buildPsuIdData(String id) {
        return new PsuIdData(id, null, null, null);
    }

    private List<PiisConsent> buildPiisConsentList() {
        return Collections.singletonList(new PiisConsent());
    }
}
