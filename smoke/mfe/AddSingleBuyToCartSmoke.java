package com.wsgc.ecommerce.ui.smoke.mfe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.RichAddToCartOverlaySection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartHeaderRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartPage;
import com.wsgc.evergreen.entity.api.ProductGroup;

import tag.area.ShopArea;

/**
 * Smoke Tests for Adding Single Buy Items to the cart.
 */
@Category(ShopArea.class)
public class AddSingleBuyToCartSmoke extends AbstractTest {
    /**
     * Verifies whether the shopper is able to add products when the products is single buy.
     */
    @Test
    public void testAddToCartWhenProductIsSingleBuyExpectCartHasProduct() {
        // given
        final int itemIndex = 0;
        final int expectedTotalItem = 1;
        final int expectedQuantity = 3;
        final ProductGroup expectedProductGroup = dataService
                .findProductGroup(productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null,
                                  "simple-pip-mfe", "!attributes", "!monogrammable");
        final String expectedSku = expectedProductGroup.getSkus().get(itemIndex);

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        productPage.setQuantity(expectedQuantity);
        RichAddToCartOverlaySection overlaySection = productPage.addToCart();
        ShoppingCartPage shoppingCartPage = overlaySection.goToShoppingCartPage();
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
