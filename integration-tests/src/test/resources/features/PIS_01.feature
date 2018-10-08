Feature: Payment Initiation Service

    ####################################################################################################################
    #                                                                                                                  #
    # Single Payment                                                                                                   #
    #                                                                                                                  #
    ####################################################################################################################
    Scenario Outline: Successful payment initiation request for single payments (redirect)
        Given PSU wants to initiate a single payment <single-payment> using the payment service <payment-service> and the payment product <payment-product>
        When PSU sends the single payment initiating request
        Then a successful response code and the appropriate payment response data are received
        And a redirect URL is delivered to the PSU
        Examples:
            | payment-service | payment-product       | single-payment                |
            | payments        | sepa-credit-transfers | singlePayInit-successful.json |
            | payments        | sepa-credit-transfers | singlePayInit-exceeding-amount.json |

    Scenario Outline: Failed payment initiation request for single payments (redirect)
        Given PSU initiates an errorful single payment <single-payment> using the payment service <payment-service> and the payment product <payment-product>
        When PSU sends the single payment initiating request with error
        Then an error response code and the appropriate error response are received
        Examples:
            | payment-service     | payment-product               | single-payment                                 |
#            | payments            | sepa-credit-transfers         | singlePayInit-incorrect-syntax.json            |
            | payments            | sepa-credit-trans             | singlePayInit-incorrect-payment-product.json   |
            | payments            | sepa-credit-transfers         | singlePayInit-no-request-id.json               |
            | payments            | sepa-credit-transfers         | singlePayInit-no-ip-address.json               |
            | payments            | sepa-credit-transfers         | singlePayInit-wrong-format-request-id.json     |
#            | payments            | sepa-credit-transfers         | singlePayInit-wrong-format-psu-ip-address.json |
#            | recurring-payments  | sepa-credit-transfers         | singlePayInit-wrong-payment-service.json       |

    ####################################################################################################################
    #                                                                                                                  #
    # Bulk Payment                                                                                                     #
    #                                                                                                                  #
    ####################################################################################################################
# Bulk Payment is currently not considered in the xs2a interface, hence the tests are commented. The response of the
# interface needs to be adapted to the new specification v1.2 see issue: https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/294

#    Scenario Outline: Payment initiation request for bulk payments (redirect)
#        Given PSU wants to initiate multiple payments <bulk-payment> using the payment service <payment-service> and the payment product <payment-product>
#        When PSU sends the bulk payment initiating request
#        Then a successful response code and the appropriate payment response data are received
#        And a redirect URL is delivered to the PSU
#        Examples:
#            | payment-service  | payment-product       | bulk-payment                |
#            | bulk-payments     | sepa-credit-transfers | bulkPayInit-successful.json |
#            |  bulk-payments    | sepa-credit-transfers | bulkPayInit-one-exceeding-amount.json        |

#    Scenario Outline: Failed payment initiation request for bulk payments (redirect)
#        Given PSU loads errorful multiple payments <bulk-payment> using the payment service <payment-service> and the payment product <payment-product>
#        When PSU sends the bulk payment initiating request with error
#        Then an error response code and the appropriate error response are received
#        Examples:
#          |  payment-service  | payment-product       | bulk-payment                                 |
#          |  bulk-payments    | sepa-credit-trans     | bulkPayInit-incorrect-payment-product.json   |
##          |  bulk-payments    | sepa-credit-transfers | bulkPayInit-no-request-id.json               |
##          |  bulk-payments    | sepa-credit-transfers | bulkPayInit-no-ip-address.json               |
##          |  bulk-payments    | sepa-credit-transfers | bulkPayInit-wrong-format-request-id.json     |
##          |  bulk-payments    | sepa-credit-transfers | bulkPayInit-wrong-format-psu-ip-address.json |
##          |  bulk-payments    | sepa-credit-transfers | bulkPayInit-one-incorrect-syntax.json        |


    ####################################################################################################################
    #                                                                                                                  #
    # Recurring Payments                                                                                               #
    #                                                                                                                  #
    ####################################################################################################################
    Scenario Outline: Payment initiation request for recurring payments (redirect)
        Given PSU wants to initiate a recurring payment <recurring-payment> using the payment service <payment-service> and the payment product <payment-product>
        When PSU sends the recurring payment initiating request
        Then a successful response code and the appropriate payment response data are received
        And a redirect URL is delivered to the PSU
        Examples:
           | payment-service   | payment-product       | recurring-payment          |
           | periodic-payments | sepa-credit-transfers | recPayInit-successful.json |
           | periodic-payments | sepa-credit-transfers | recPayInit-exceeding-amount.json            |

    Scenario Outline: Failed payment initiation request for recurring payments (redirect)
        Given PSU loads an errorful recurring payment <recurring-payment> using the payment service <payment-service> and the payment product <payment-product>
        When PSU sends the recurring payment initiating request with error
        Then an error response code and the appropriate error response are received
        Examples:
            | payment-service   | payment-product       | recurring-payment                           |
#            | periodic-payments | sepa-credit-transfers | recPayInit-incorrect-syntax.json            |
            | periodic-payments | sepa-credit-trans     | recPayInit-incorrect-payment-product.json   |
            | periodic-payments | sepa-credit-transfers | recPayInit-no-frequency.json                |
            | periodic-payments | sepa-credit-transfers | recPayInit-not-defined-frequency.json       |
            | periodic-payments | sepa-credit-transfers | recPayInit-no-request-id.json               |
#            | periodic-payments | sepa-credit-transfers | recPayInit-no-ip-address.json               |
            | periodic-payments | sepa-credit-transfers | recPayInit-wrong-format-request-id.json     |
#            | periodic-payments | sepa-credit-transfers | recPayInit-wrong-format-psu-ip-address.json |
#            | periodic-payments | sepa-credit-transfers | recPayInit-start-date-in-past.json          |
#            | periodic-payments | sepa-credit-transfers | recPayInit-end-date-before-start-date.json  |


    ####################################################################################################################
    #                                                                                                                  #
    # Payment Status                                                                                                   #
    #                                                                                                                  #
    ####################################################################################################################
#    Scenario Outline: Successful payment status request
#        Given Psu wants to request the payment status of a payment with payment-id <payment-id> by using the payment-service <payment-service>
#        And the set of data <payment-status>
#        When PSU requests the status of the payment
#        Then an appropriate response code and the status is delivered to the PSU
#        Examples:
#            | payment-id                           | payment-service | payment-status                |
#            | a9115f14-4f72-4e4e-8798-202808e85238 | payments        | paymentStatus-RCVD-successful.json |
#            | 68147b90-e4ef-41c6-9c8b-c848c1e93700 | payments        | paymentStatus-PDNG-successful.json |
#            | 97694f0d-32e2-43a4-9e8d-261f2fc28236 | payments        | paymentStatus-RJCT-successful.json |

    Scenario Outline: Failed payment status request
        Given Psu requests the payment status of a payment with payment-id <payment-id> by using the payment-service <payment-service>
        And the errorful set of data <payment-status>
        When PSU requests the status of the payment with error
        Then an error response code and the appropriate error response are received
        Examples:
            | payment-id                           | payment-service      | payment-status                             |
#            | 529e0507-7539-4a65-9b74-bdf87061e99b | payments             | paymentStatus-not-existing-id.json         |
            | a9115f14-4f72-4e4e-8798-202808e85238 | payments             | paymentStatus-no-request-id.json           |
            | a9115f14-4f72-4e4e-8798-202808e85238 | payments             | paymentStatus-wrong-format-request-id.json |
            #| a9115f14-4f72-4e4e-8798-202808e85238 | recurring-payments   | paymentStatus-wrong-payment-service.json   |

    ####################################################################################################################
    #                                                                                                                  #
    # Payment Information                                                                                              #
    #                                                                                                                  #
    ####################################################################################################################

#    Scenario Outline: Successful Payment Information Request
#        Given Psu wants to request the payment information of a payment with payment-id <payment-id> by using the payment-service <payment-service>
#        And the set of payment information data <payment-information>
#        When PSU requests the information of the payment
#        Then an appropriate response code and the payment information is delivered to the PSU
#        Examples:
#            | payment-id                           | payment-service              | payment-information                    |
 #           | a9115f14-4f72-4e4e-8798-202808e85238 | payments                     | singlePayInformation-successful.json   |
 #           | b8115f14-4f72-4e4e-8798-202808e85289 | bulk-payments                | bulkPayInformation-successful.json     |
 #           | p7115f14-4f72-4e4e-8798-202808e85232 | periodic-payments            | periodicPayInformation-successful.json |

    Scenario Outline: Failed Payment Information Request
        Given PSU wants to request the payment information <payment-information> of a payment with payment-id <payment-id> by using the payment-service <payment-service>
        When PSU requests the information of the payment with error
        Then an error response code and the appropriate error response are received
        Examples:
            | payment-id                           | payment-service              | payment-information                        |
#            | 11111111-aaaa-xxxx-1111-1x1x1x1x1x1x | payments                     | singlePayInformation-not-existing-id.json  |
            | a9115f14-4f72-4e4e-8798-202808e85238 | payments                     | singlePayInformation-no-request-id.json     |
            | a9115f14-4f72-4e4e-8798-202808e85238 | payments                     | singlePayInformation-wrong-format-request-id.json |
#            | a9115f14-4f72-4e4e-8798-202808e85238 | recurring-payments           | singlePayInformation-wrong-payment-service.json |


    ####################################################################################################################
    #                                                                                                                  #
    # Payment Cancellation                                                                                             #
    #                                                                                                                  #
    ####################################################################################################################

#    Scenario Outline: Successful payment cancellation request
#        Given PSU wants to cancel an existing payment <payment-cancellation> with payment-id <payment-id> using the payment service <payment-service>
#        When PSU initiates the cancellation of the payment
#        Then an successful response code and the appropriate transaction status is delivered to the PSU
#        Examples:
#            | payment-id                           | payment-service | payment-cancellation                |
#            | a9115f14-4f72-4e4e-8798-202808e85238 | payments        | paymentCancellation-successful.json |
#
#   Scenario Outline: Failed payment cancellation request
#        Given PSU wants to cancel a payment <payment-cancellation> with payment-id <payment-id> using the payment service <payment-service>
#        When PSU initiates the cancellation of the payment with error
#        Then an error response code and the appropriate error response are received
#        Examples:
#            | payment-id                           | payment-service    | payment-cancellation                             |
#            | 11111111-aaaa-xxxx-1111-1x1x1x1x1x1x | payments           | paymentCancellation-not-existing-id.json         |
#            | 68147b90-e4ef-41c6-9c8b-c848c1e93700 | payments           | paymentCancellation-no-request-id.json           |
#            | 68147b90-e4ef-41c6-9c8b-c848c1e93700 | payments           | paymentCancellation-wrong-format-request-id.json |
#            | 68147b90-e4ef-41c6-9c8b-c848c1e93700 | recurring-payments | paymentCancellation-wrong-payment-service.json   |
