Feature: Account Information Service
#
#    ####################################################################################################################
#    #                                                                                                                  #
#    # Consent Requests                                                                                                 #
#    #                                                                                                                  #
#    ####################################################################################################################
    Scenario Outline: Successful consent request creation (redirect)
        Given PSU wants to create a consent <consent-resource>
        When PSU sends the create consent request
        Then a successful response code and the appropriate consent response data is delivered to the PSU
        Examples:
            | consent-resource                     |
            | consent-dedicated-successful.json    |
#            | consent-all-psd2-accounts-successful.json  |
            | consent-all-accounts-successful.json |
#
##    #TODO Errorful Request
#
#    Scenario Outline: Failed consent request creation (redirect)
#        Given PSU wants to create a consent <consent-resource>
#        When PSU sends the create consent request with error
#        Then an error response code is displayed the appropriate error response
#        Examples:
#            | consent-resource                           |
#            | consent-all-psd2-no-psu-id.json            |
#            | consent-all-psd2-wrong-psu-id.json         |
#            #| consent-all-psd2-wrong-value.json          |
#            | consent-dedicated-incorrect-iban.json      |

#
#    Scenario Outline: Successful consent status request (redirect)
#        Given AISP wants to get the status of a consent <consent-id> and the data <consent-resource>
#        When AISP requests consent status
#        Then a successful response code and the appropriate consent status gets returned
#        Examples:
#            | consent-resource                       | consent-id    |
#            | consent-status-expired.json            | to-be-defined |
#            | consent-status-missing-consent-id.json | to-be-defined |
#            | consent-status-received.json           | to-be-defined |
#            | consent-status-rejected.json           | to-be-defined |
#            | consent-status-revoked-by-psu.json     | to-be-defined |
#            | consent-status-terminated-by-tpp.json  | to-be-defined |
#            | consent-status-valid.json              | to-be-defined |
#
#    Scenario Outline: Successful consent request (redirect)
#        Given PSU wants to get the content of a consent <consent-resource>
#        When PSU requests consent
#        Then a successful response code and the appropriate consent gets returned
#        Examples:
#            | consent-resource        |
#            | consent-successful.json |
#
#
#    Scenario Outline: Successful deletion of consent (redirect)
#        Given PSU wants to delete the consent <consent-resource>
#        When PSU deletes consent
#        Then a successful response code and the appropriate messages get returned
#        Examples:
#            | consent-resource                 |
#            | consent-deletion-successful.json |
#
#
#
    ####################################################################################################################
    #                                                                                                                  #
    # Account Request                                                                                                  #
    #                                                                                                                  #
    ####################################################################################################################
#    Scenario Outline: Request account list successfully
#        Given PSU already has an existing consent <consent-id>
#        And wants to get a list of accounts using <account-resource>
#        When PSU requests the list of accounts
#        Then a successful response code and the appropriate list of accounts get returned
#        Examples:
#            | account-resource                               | consent-id                   |
#            | accountList-successful.json                    | accounts-create-consent.json |
#            | accountList-with-more-accounts-successful.json | accounts-create-consent.json |
#
#    Scenario Outline: Request account list errorful
#        Given PSU already has an existing consent <consent-id>
#        And wants to get a list of accounts using <account-resource>
#        When PSU sends get request
#        Then an error response code is displayed the appropriate error response
#        Examples:
#            | account-resource                         | consent-id                   |
#            | accountList-no-request-id.json           | accounts-create-consent.json |
#            | accountList-wrong-format-request-id.json | accounts-create-consent.json |
#            | accountList-invalid-request-id.json      | accounts-create-consent.json |
#
#    Scenario Outline: Request account list with no consent errorful
#        Given PSU wants to get a list of accounts using <account-resource>
#        When PSU sends get request
#        Then an error response code is displayed the appropriate error response
#        Examples:
#            | account-resource            |
#            | accountList-no-consent.json |
#
#    Scenario Outline: Request account list with expired consent errorful
#        Given PSU created consent <consent> which is expired
#        And wants to get a list of accounts using <account-resource>
#        When PSU sends get request
#        Then an error response code is displayed the appropriate error response
#        Examples:
#            | account-resource                      | consent                              |
#            | accountList-with-expired-consent.json | accounts-create-expired-consent.json |
#
#    Scenario Outline: Request account details successfully
#        Given PSU already has an existing consent <consent-id>
#        And account id <account-id>
#        And wants to get a list of accounts using <account-resource>
#        When PSU requests the account details
#        Then a successful response code and the appropriate details of accounts get returned
#        Examples:
#            | account-resource              | account-id                           | consent-id                   |
#            | accountDetail-successful.json | 42fb4cc3-91cb-45ba-9159-b87acf6d8add | accounts-create-consent.json |
#
#    Scenario Outline: Request account details errorful
#        Given PSU already has an existing consent <consent-id>
#        And account id <account-id>
#        And wants to get a list of accounts using <account-resource>
#        When PSU requests the account details
#        Then an error response code is displayed the appropriate error response
#        Examples:
#            | account-resource                        | account-id                           | consent-id        |
#            | accountDetail-wrong-format-request.json | 42fb4cc3-91cb-45ba-9159-b87acf6d8add | accounts-create-consent.json |
#            | accountDetail-invalid-request-id.json   | 42fb4cc3-91cb-45ba-9159-b87acf6d8add | accounts-create-consent.json |
#            | accountDetail-no-request-id.json        | 42fb4cc3-91cb-45ba-9159-b87acf6d8add | accounts-create-consent.json |
#
#    Scenario Outline: Request account details with no consent errorful
#        Given PSU wants to get a list of accounts using <account-resource>
#        When PSU sends get request
#        Then an error response code is displayed the appropriate error response
#        Examples:
#            | account-resource              |
#            | accountDetail-no-consent.json |
#
#    Scenario Outline: Request account details with expired consent errorful
#        Given PSU created consent <consent> which is expired
#        And wants to get a list of accounts using <account-resource>
#        When PSU sends get request
#        Then an error response code is displayed the appropriate error response
#        Examples:
#            | account-resource                        | consent                              |
#            | accountDetail-with-expired-consent.json | accounts-create-expired-consent.json |

#    ####################################################################################################################
#    #                                                                                                                  #
#    # Balance Request                                                                                                  #
#    #                                                                                                                  #
#    ####################################################################################################################
#
#    Scenario Outline: Read balances successfully
#        Given PSU already has an existing consent <consent>
#        And account id <account-id>
#        And wants to read all balances using <balance-resource>
#        When PSU requests the balances
#        Then a successful response code and the appropriate list of accounts get returned
#        Examples:
#            | consent                     | account-id                           | balance-resource            |
#            | balance-create-consent.json | 42fb4cc3-91cb-45ba-9159-b87acf6d8add | readBalance-successful.json |
#            | balance-create-consent.json | 868beafc-ef87-4fdb-ac0a-dd6c52b77ee6 | readBalance-successful.json |
#
#    Scenario Outline: Read balances errorful
#        Given PSU already has an existing consent <consent>
#        And account id <account-id>
#        And wants to read all balances using <balance-resource>
#        When PSU requests the balances
#        Then an error response code is displayed the appropriate error response
#        Examples:
#            | consent                     | account-id                           | balance-resource                         |
#            | balance-create-consent.json | 42fb4cc3-91cb-45ba-9159-b87acf6d8add | readBalance-invalid-request-id.json      |
#            | balance-create-consent.json | 42fb4cc3-91cb-45ba-9159-b87acf6d8add | readBalance-no-request-id                |
#            | balance-create-consent.json | 42fb4cc3-91cb-45ba-9159-b87acf6d8add | readBalance-wrong-format-request-id.json |
#
#    Scenario Outline: Read balances with no consent errorful
#        Given PSU wants to read all balances using <balance-resource>
#        And account id <account-id>
#        When PSU requests the balances
#        Then an error response code is displayed the appropriate error response
#        Examples:
#            | balance-resource            | account-id                           |
#            | readBalance-no-consent.json | 42fb4cc3-91cb-45ba-9159-b87acf6d8add |
#
#    Scenario Outline: Read balances with expired consent errorful
#        Given PSU created consent <consent> which is expired
#        And account id <account-id>
#        And wants to read all balances using <balance-resource>
#        When PSU requests the balances
#        Then an error response code is displayed the appropriate error response
#        Examples:
#            | consent                             | account-id                           | balance-resource                      |
#            | balance-create-expired-consent.json | 42fb4cc3-91cb-45ba-9159-b87acf6d8add | readBalance-with-expired-consent.json |
#
#    ####################################################################################################################
#    #                                                                                                                  #
#    # Transaction Request                                                                                              #
#    #                                                                                                                  #
#    ####################################################################################################################
#    Scenario: Read transaction list of a regular account
#        Given A consent resource with the following data exists at the ASPSP
#            | access                                                                                   | recurringIndicator | validUntil | frequencyPerDay | transactionStatus           | consentStatus | links                      |
#            | balances: [{iban: DE2310010010123456770}], transactions: [{iban: DE2310010010123456770}] | true               | 2017-11-01 | 4               | AcceptedTechnicalValidation | valid         | viewAccounts: /v1/accounts |
#        And AISP knows the account-id 3dc3d5b3-7023-4848-9853-f5400a64e111 of the required account
#        When AISP requests transaction list
#        Then response code 200
#        And an array of booked transactions
#            | transactionId | creditorName  | creditorAccount              | amount                            | bookingDate | value_Date | remittanceInformationUnstructured |
#            | 1234567       | John Miles    | iban: DE43533700240123456900 | {currency: EUR, content: -256.67} | 2017-10-25  | 2017-10-26 | Example 1                         |
#            | 1234568       | Paul Simpsons | iban: NL354543123456900      | {currency: EUR, content: 343.01}  | 2017-10-25  | 2017-10-26 | Example 2                         |
#        And an array of pending transactions
#            | transactionId | creditorName   | creditorAccount           | amount                           | bookingDate | value_Date | remittanceInformationUnstructured |
#            | 1234569       | Claude Renault | iban: FR33554543123456900 | {currency: EUR, content: -100.03 | 2017-10-25  | 2017-10-26 | Example 3                         |
#        And the link viewAccount = "/v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e111" is delivered to the AISP
#
#
#    Scenario: Read transaction list of a multi-currency account
#        Given A consent resource with the following data exists at the ASPSP
#            | access                                                                                   | recurringIndicator | validUntil | frequencyPerDay | transactionStatus           | consentStatus | links                      |
#            | balances: [{iban: DE2310010010123456760}], transactions: [{iban: DE2310010010123456760}] | true               | 2017-11-01 | 4               | AcceptedTechnicalValidation | valid         | viewAccounts: /v1/accounts |
#        And AISP knows the account-id 3dc3d5b3-7023-4848-9853-f5400a64e809 of the required account
#        When AISP requests transaction list
#        Then response code 200
#        And an array of booked transactions
#            | transactionId | creditorName  | creditorAccount              | amount                            | bookingDate | value_Date | remittanceInformationUnstructured |
#            | 1234567       | John Miles    | iban: DE43533700240123456900 | {currency: EUR, content: -256.67} | 2017-10-25  | 2017-10-26 | Example 1                         |
#            | 1234568       | Paul Simpsons | iban: NL354543123456900      | {currency: EUR, content: 343.01}  | 2017-10-25  | 2017-10-26 | Example 2                         |
#            | 1234569       | Pepe Martin   | iban: SE1234567891234        | {currency: USD, content: 100.00}  | 2017-10-25  | 2017-10-26 | Example 3                         |
#        And an array of pending transactions
#            | transactionId | creditorName   | creditorAccount           | amount                           | bookingDate | value_Date | remittanceInformationUnstructured |
#            | 1234569       | Claude Renault | iban: FR33554543123456900 | {currency: EUR, content: -100.03 | 2017-10-25  | 2017-10-26 | Example 3                         |
#        And the link viewAccount = "/v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e809" is delivered to the AISP
#
