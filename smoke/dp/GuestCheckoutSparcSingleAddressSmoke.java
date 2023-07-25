package com.wsgc.ecommerce.ui.smoke.dp;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.checkout.sparc.SinglePageResponsiveCheckoutFlows;
import com.wsgc.ecommerce.ui.endtoend.shop.shoppingcart.CheckoutSignInFlows;
import com.wsgc.ecommerce.ui.endtoend.shop.shoppingcart.ShoppingCartFlows;
import com.wsgc.ecommerce.ui.pagemodel.checkout.signin.CheckoutSignInPage;
import com.wsgc.ecommerce.ui.pagemodel.checkout.singlepageresponsivecheckout.SinglePageResponsiveCheckoutAddNewAddressOverlay;
import com.wsgc.ecommerce.ui.pagemodel.checkout.singlepageresponsivecheckout.SinglePageResponsiveCheckoutBillingPage;
import com.wsgc.ecommerce.ui.pagemodel.checkout.singlepageresponsivecheckout.SinglePageResponsiveCheckoutDeliveryAndGiftOptionsPage;
import com.wsgc.ecommerce.ui.pagemodel.checkout.singlepageresponsivecheckout.SinglePageResponsiveCheckoutPaymentPage;
import com.wsgc.ecommerce.ui.pagemodel.checkout.singlepageresponsivecheckout.SinglePageResponsiveCheckoutShippingPage;
import com.wsgc.ecommerce.ui.pagemodel.checkout.singlepageresponsivecheckout.SinglePageResponsiveCheckoutSmartLoginPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.CustomerAccount;
import com.wsgc.evergreen.entity.api.ProductGroup;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tag.area.CheckoutArea;

/**
 * Smoke Tests that validate an order can be placed successfully on the
 * Brand web sites using Guest Checkout via SPARC (Single PAge Responsive
 * Checkout).
 */
@Category(CheckoutArea.class)
public class GuestCheckoutSparcSingleAddressSmoke extends AbstractTest {

    /**
     * Prepares data fixtures & ensures feature flag is enabled.
     */
    @Before
    public void setUp() {
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.SINGLE_PAGE_RESPONSIVE_CHECKOUT));
    }

    /**
     * Smoke test that validates a customer adding item a Guided PIP to Cart
     * and Proceeding with Guest Checkout successfully. Verifies that the correct page 
     * elements are displayed for a Guided PIP.
     */
    @Test
    public void testSparcGuestCheckoutWithGuidedPipExpectOrderConfirmationPage() {    

        // given
        assumeFeatureIsSupported(FeatureFlag.FURNITURE);
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP)
                || context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP_REDESIGN));
        CustomerAccount account = dataService.findAccount("guest", "checkout");
        final int expectedQuantity = 3;
        final ProductGroup upholsteredProductGroup = dataService.findProductGroup("guided-pip", "upholstered");

        // when
        ShoppingCartPage shoppingCartPage = context.getPage(ShoppingCartPage.class);
        shoppingCartPage.addToCartViaService(upholsteredProductGroup.getSkus().get(0), String.valueOf(expectedQuantity));
        shoppingCartPage.scrollIntoCartSection();

        // then

        if (context.supportsFeature(FeatureFlag.SMART_LOGIN_SPARC)) {
            ShoppingCartFlows.checkout(context, SinglePageResponsiveCheckoutSmartLoginPage.class);
        } else {
            ShoppingCartFlows.checkout(context, CheckoutSignInPage.class);
        }

        SinglePageResponsiveCheckoutShippingPage sparcShippingPage = CheckoutSignInFlows.signInAsGuest(context, account, SinglePageResponsiveCheckoutShippingPage.class);

        SinglePageResponsiveCheckoutFlows.setShippingAddress(sparcShippingPage, account);
        sparcShippingPage.uncheckUseAsBilling();
        SinglePageResponsiveCheckoutDeliveryAndGiftOptionsPage sparcDgoPage = sparcShippingPage.submitShippingAddress(true);
        SinglePageResponsiveCheckoutBillingPage sparcBillingPage = sparcDgoPage.submitDeliveryAndGiftOptions(SinglePageResponsiveCheckoutBillingPage.class);

        SinglePageResponsiveCheckoutAddNewAddressOverlay addAddressOverlay = sparcBillingPage.addAddress();
        SinglePageResponsiveCheckoutFlows.setAddNewAddressOverlayBillingAddress(addAddressOverlay, account);
        SinglePageResponsiveCheckoutPaymentPage sparcPaymentPage = addAddressOverlay.submitAddress(true, SinglePageResponsiveCheckoutPaymentPage.class);

        SinglePageResponsiveCheckoutFlows.setCreditCardInformation(sparcPaymentPage, account);

        if (!context.supportsFeature(FeatureFlag.SMART_LOGIN_SPARC)) {
            sparcPaymentPage.setEmail(account.getEmail());
        }
    }
}
