package com.backbase.testing.dataloader.configurators;

import com.backbase.dbs.presentation.paymentorder.rest.spec.v2.paymentorders.InitiatePaymentOrder;
import com.backbase.presentation.productsummary.rest.spec.v2.productsummary.ArrangementsByBusinessFunctionGetResponseBody;
import com.backbase.testing.dataloader.clients.common.LoginRestClient;
import com.backbase.testing.dataloader.clients.payment.PaymentOrderPresentationRestClient;
import com.backbase.testing.dataloader.clients.productsummary.ProductSummaryPresentationRestClient;
import com.backbase.testing.dataloader.data.PaymentsDataGenerator;
import com.backbase.testing.dataloader.dto.ProductSummaryQueryParameters;
import com.backbase.testing.dataloader.utils.CommonHelpers;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_PAYMENTS_MAX;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_PAYMENTS_MIN;
import static org.apache.http.HttpStatus.SC_ACCEPTED;

public class PaymentsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentsConfigurator.class);
    private static GlobalProperties globalProperties = GlobalProperties.getInstance();

    private Random random = new Random();
    private PaymentOrderPresentationRestClient paymentOrderPresentationRestClient = new PaymentOrderPresentationRestClient();
    private LoginRestClient loginRestClient = new LoginRestClient();
    private ProductSummaryPresentationRestClient productSummaryPresentationRestClient = new ProductSummaryPresentationRestClient();
    private PaymentsDataGenerator paymentsDataGenerator = new PaymentsDataGenerator();

    private static final String ENTITLEMENTS_PAYMENTS_FUNCTION_NAME = "SEPA CT";
    private static final String ENTITLEMENTS_PAYMENTS_RESOURCE_NAME = "Payments";
    private static final String PRIVILEGE_CREATE = "create";

    public void ingestPaymentOrders(String externalUserId) {
        List<ArrangementsByBusinessFunctionGetResponseBody> sepaArrangements = getSepaArrangementsByExternalUserId(externalUserId);

        for (int i = 0; i < CommonHelpers.generateRandomNumberInRange(globalProperties.getInt(PROPERTY_PAYMENTS_MIN), globalProperties.getInt(PROPERTY_PAYMENTS_MAX)); i++) {
            ArrangementsByBusinessFunctionGetResponseBody randomArrangement = sepaArrangements.get(random.nextInt(sepaArrangements.size()));
            InitiatePaymentOrder initiatePaymentOrder = paymentsDataGenerator.generateInitiatePaymentOrder(randomArrangement.getId());
            paymentOrderPresentationRestClient.initiatePaymentOrder(initiatePaymentOrder)
                    .then()
                    .statusCode(SC_ACCEPTED);

            LOGGER.info(String.format("Payment order ingested for debtor account [%s]", initiatePaymentOrder.getDebtorAccount().getIdentification().getIdentification()));
        }
    }

    public List<ArrangementsByBusinessFunctionGetResponseBody> getSepaArrangementsByExternalUserId(String externalUserId) {
        loginRestClient.login(externalUserId, externalUserId);

        ArrangementsByBusinessFunctionGetResponseBody[] arrangements = productSummaryPresentationRestClient.getProductSummaryContextArrangements(new ProductSummaryQueryParameters()
                .withBusinessFunction(ENTITLEMENTS_PAYMENTS_FUNCTION_NAME)
                .withResourceName(ENTITLEMENTS_PAYMENTS_RESOURCE_NAME)
                .withPrivilege(PRIVILEGE_CREATE)
                .withSize(999))
                .then()
                .extract()
                .as(ArrangementsByBusinessFunctionGetResponseBody[].class);

        // Make sure the Regex is in sync with payment-order-presentation-service/src/main/resources/application.yml (property: sepacountries)
        return Arrays.stream(arrangements)
                .filter(arrangement -> arrangement.getIBAN()
                        .matches("^(AT|BE|BG|CH|CY|CZ|DE|DK|EE|ES|FI|FR|GB|GI|GR|HR|HU|IE|IS|IT|LI|LT|LU|LV|MC|MT|NL|NO|PL|PT|RO|SE|SI|SK|SM)[a-zA-Z0-9_.-]*"))
                .collect(Collectors.toList());
    }
}