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

package de.adorsys.psd2.xs2a.spi.domain.account;

import lombok.Data;

import java.util.List;

/**
 * Transaction report of Spi layer to be used as a container for account reference, transactions and balances
 */
@Data
public class SpiTransactionReport {
    private SpiAccountReference xs2aAccountReference;
    private List<SpiTransaction> transactions;
    private List<SpiAccountBalance> balances;
}
