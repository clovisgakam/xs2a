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

package de.adorsys.psd2.consent.server.service.security;

import de.adorsys.psd2.consent.server.domain.CryptoAlgorithm;
import de.adorsys.psd2.consent.server.repository.CryptoAlgorithmRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CryptoProviderFactory {
    @Qualifier(value = "cryptoProviderId")
    private final CryptoProvider cryptoProviderId;
    @Qualifier(value = "cryptoProviderConsentData")
    private final CryptoProvider cryptoProviderConsentData;
    private final CryptoAlgorithmRepository cryptoAlgorithmRepository;

    public Optional<CryptoProvider> getCryptoProviderByAlgorithmVersion(String algorithmVersion) {
        return cryptoAlgorithmRepository.findByExternalId(algorithmVersion)
                   .map(CryptoAlgorithm::getAlgorithm)
                   .flatMap(this::mapCryptoProviderByAlgorithmName);
    }

    public CryptoProvider getActualIdentifierCryptoProvider() {
        return cryptoProviderId;
    }

    public CryptoProvider getActualConsentDataCryptoProvider() {
        return cryptoProviderConsentData;
    }

    private Optional<CryptoProvider> mapCryptoProviderByAlgorithmName(String algorithm) {
        if (algorithm.equals(cryptoProviderId.getAlgorithmVersion().getAlgorithmName())) {
            return Optional.of(cryptoProviderId);
        } else if (algorithm.equals(cryptoProviderConsentData.getAlgorithmVersion().getAlgorithmName())) {
            return Optional.of(cryptoProviderConsentData);
        } else {
            return Optional.empty();
        }
    }
}
