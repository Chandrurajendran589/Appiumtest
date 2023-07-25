package com.wsgc.ecommerce.ui.smoke.dp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.shop.PipFlows;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.shop.MonogramPersonalizationPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.RichAddToCartOverlaySection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartHeaderRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.ProductGroup;

import tag.area.ShopArea;

/**
 * Smoke Tests for adding an item that is monogrammed via the mono/pz
 * interstitial page to the cart.
 */
@Category(ShopArea.class)
public class AddMonogramItemToCartViaInterstitialSmoke extends AbstractTest {
    /**
     * Prepares data fixtures & ensures feature flag is enabled.
     */
    @Before
    public void setUp() {
        assumeFeatureIsSupported(FeatureFlag.MONO_PZ);
        super.setUp();
    }

    /**
     * Verifies whether a customer is able to monogram a product via the
     * mono/pz interstitial page and add it to the cart.
     */
    @Test
    public void testAddToCartWhenUsingTheMonogramInterstitialPageExpectCartHasMonogrammedProduct() {
        // given
        final int itemIndex = 0;
        final int expectedTotalItem = 1;
        final int expectedQuantity = 1;
        final ProductGroup monogrammableProduct = dataService.findProductGroup("simple-pip", "attributes",
                                                                               "monogrammable", "monogram-interstitial");
        final String expectedSku = monogrammableProduct.getSkus().get(itemIndex);
        final String monoPzFont = monogrammableProduct.getUnmappedAttribute("monoPzFont");
        final String monoPzText = monogrammableProduct.getUnmappedAttribute("monoPzText");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, monogrammableProduct);
        PipFlows.setSimplePipAttributeSelections(productPage);
        productPage.setQuantity(expectedQuantity);
        productPage.clickMonogramIfPresent();
        MonogramPersonalizationPage monoPzPage = productPage.addToCart(MonogramPersonalizationPage.class);
        monoPzPage.setIsMonogramRequested(true);
        if (monoPzFont != null) {
            monoPzPage.selectFontByName(monoPzFont);
        }
        monoPzPage.enterText(monoPzText);
        RichAddToCartOverlaySection rac = monoPzPage.clickContinueAndGoToRichAddToCart();
        ShoppingCartPage shoppingCartPage = rac.goToShoppingCartPage();
        shoppingCartPage.scrollIntoCartSection();

        // then
        List<ShoppingCartItemRepeatableSection> itemSectionList = shoppingCartPage.getShoppingCartItems();
        assertEquals(String.format("The shopping cart should have %s item.", expectedTotalItem), expectedTotalItem,
                itemSectionList.size());
        ShoppingCartItemRepeatableSection item = shoppingCartPage.getFirstShoppingCartItem();

        assertTrue(String.format("The shopping cart item's sku should ends with '%s'. Actual sku: '%s'", expectedSku,
                item.getSku()), item.getSku().endsWith(expectedSku));

        assertEquals(String.format("The shopping cart item's quantity should be '%s'", expectedQuantity),
                String.valueOf(expectedQuantity), item.getQuantity());

        assertTrue(String.format("The monogram should be with '%s'. Actual monogram text: '%s'", monoPzText,
                item.getMonogrammedText()), item.getMonogrammedText().toUpperCase().endsWith(monoPzText));

        assertTrue("The Edit Personalization link was not displayed.", item.isEditPersonalizationLinkPresent());

        List<ShoppingCartHeaderRepeatableSection> headerSectionList = shoppingCartPage.getSuborderHeadersOnCart();
        assertEquals(String.format("The shopping cart should have %s header.", expectedTotalItem), expectedTotalItem,
                headerSectionList.size());
    }
}
