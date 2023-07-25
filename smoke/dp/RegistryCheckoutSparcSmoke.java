package com.wsgc.ecommerce.ui.smoke.dp;

import static org.junit.Assert.assertEquals;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.account.AccountFlows;
import com.wsgc.ecommerce.ui.endtoend.checkout.legacy.CheckoutFlows;
import com.wsgc.ecommerce.ui.endtoend.checkout.sparc.SinglePageResponsiveCheckoutFlows;
import com.wsgc.ecommerce.ui.endtoend.shop.shoppingcart.ShoppingCartFlows;
import com.wsgc.ecommerce.ui.pagemodel.account.AccountHomePage;
import com.wsgc.ecommerce.ui.pagemodel.checkout.singlepageresponsivecheckout.SinglePageResponsiveCheckoutBillingPage;
import com.wsgc.ecommerce.ui.pagemodel.checkout.singlepageresponsivecheckout.SinglePageResponsiveCheckoutDeliveryAndGiftOptionsPage;
import com.wsgc.ecommerce.ui.pagemodel.checkout.singlepageresponsivecheckout.SinglePageResponsiveCheckoutPaymentPage;
import com.wsgc.ecommerce.ui.pagemodel.checkout.singlepageresponsivecheckout.SinglePageResponsiveCheckoutShippingPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.RichAddToCartOverlaySection;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.CustomerAccount;
import com.wsgc.evergreen.entity.api.MutableAccount;
import com.wsgc.evergreen.entity.api.Registry;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tag.area.CheckoutArea;

/**
 * Smoke Tests that validates an item can be added from registry and 
 * checkout to place order via SPARC.
 *
 * @author jkrishnan1
 */
@Category(CheckoutArea.class)
public class RegistryCheckoutSparcSmoke extends AbstractTest {


    /**
     * Prepares data fixtures & ensures feature flag is enabled.
     */
    @Before
    public void setUp() {
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.SINGLE_PAGE_RESPONSIVE_CHECKOUT));
    }

    /**
     * Verifies if a signed in user is able to add an item to cart from a registry and 
     * proceed to checkout till payment page using PayPal as payment.
     */
    @Test
    public void testSparcCheckoutWhenSignedInUserAddsItemFromRegistryToCartAndCheckoutUsingPaypalExpectPaymentPage() {
        // given
        Assume.assumeTrue(context.supportsFeature(FeatureFlag.REGISTRY_V2));
        Registry registry = dataService.findRegistry("registry-checkout");
        CustomerAccount account = dataService.findAccount("guest", "checkout");
        MutableAccount mutableAccount = getMutableAccount("default");
        CustomerAccount newAccount = accountCreator.getRandomEmailAccount(mutableAccount);
        final String PaypalBillingInfoText = "Using PayPal billing address";
        String registrantName = registry.getRegistrant().getFullName().toLowerCase();
        String eventType = registry.getEventType().toLowerCase();
        String eventDate = registry.getUnmappedAttribute("eventDate");
        String registryInformation;

        // when
        AccountFlows.goToLoginPageFlow(context);
        AccountFlows.createAccount(context, newAccount, AccountHomePage.class);
        
        RichAddToCartOverlaySection richAddToCartOverlaySection = CheckoutFlows.addRegistryItemToCart(context, registry);
        richAddToCartOverlaySection.goToShoppingCartPage();

        SinglePageResponsiveCheckoutShippingPage sparcShippingPage = ShoppingCartFlows.checkout(context, SinglePageResponsiveCheckoutShippingPage.class);
        if (registry.getCoRegistrant() != null) {
            String coRegistrantName = registry.getCoRegistrant().getFullName().toLowerCase();
            registryInformation = String.format("%s and %s - %s - %s", registrantName, coRegistrantName, eventType, eventDate);
        } else {
            registryInformation = String.format("%s - %s - %s", registrantName, eventType, eventDate);
        }
        assertEquals("Registry information is not present in shipping view", registryInformation, sparcShippingPage.getRegistryInformationInShippingView(registry));
        SinglePageResponsiveCheckoutDeliveryAndGiftOptionsPage sparcDgoPage = sparcShippingPage.submitShippingAddress();
        SinglePageResponsiveCheckoutBillingPage sparcBillingPage = sparcDgoPage.submitDeliveryAndGiftOptions(SinglePageResponsiveCheckoutBillingPage.class);
        SinglePageResponsiveCheckoutFlows.setBillingAddress(sparcBillingPage, account);
        SinglePageResponsiveCheckoutPaymentPage sparcPaymentPage = sparcBillingPage.submitBillingAddress(SinglePageResponsiveCheckoutPaymentPage.class);
        sparcPaymentPage.selectPaymentMethod("paypal");
        assertEquals("Paypal Billing Information is not present in billing view", PaypalBillingInfoText, sparcPaymentPage.getPaypalInformationFromBillingView());
    }

}
