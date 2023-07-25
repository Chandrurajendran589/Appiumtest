package com.wsgc.ecommerce.ui.smoke.mfe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.wsgc.ecommerce.ui.pagemodel.registry.GuidedPipFlows;

import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.RichAddToCartOverlaySection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartHeaderRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.ProductGroup;

import tag.area.ShopArea;

/**
 * Smoke Tests for Adding Upholstered Items to the cart.
 */
@Category(ShopArea.class)
public class AddUpholsteredFurnitureToCartSmoke extends AbstractTest {
    /**
     * Verifies whether the shopper is able to add products when the products is upholstered furniture.
     */
    @Test
    public void testAddToCartWhenProductIsUpholsteredFurnitureExpectCartHasProduct() {
        // given
        assumeFeatureIsSupported(FeatureFlag.FURNITURE);
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP) || context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP_REDESIGN));
        final int itemIndex = 0;
        final int expectedTotalItem = 1;
        final int expectedQuantity = 3;
        final ProductGroup upholsteredProductGroup = dataService.findProductGroup("guided-pip-mfe", "upholstered");

        final String expectedSku = upholsteredProductGroup.getSkus().get(itemIndex);

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, upholsteredProductGroup);
        GuidedPipFlows.setGuidedPipQuantityAndAttributes(context, String.valueOf(expectedQuantity));
        RichAddToCartOverlaySection rac = productPage.addToCart();
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

        List<ShoppingCartHeaderRepeatableSection> headerSectionList = shoppingCartPage.getSuborderHeadersOnCart();
        assertEquals(String.format("The shopping cart should have %s header.", expectedTotalItem), expectedTotalItem,
                     headerSectionList.size());
    }
}
