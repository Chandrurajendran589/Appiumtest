package com.wsgc.ecommerce.ui.smoke.mfe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;

import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.shop.QuicklookOverlay;
import com.wsgc.ecommerce.ui.pagemodel.shop.RichAddToCartQuicklookOverlay;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartHeaderRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.CategoryTree;
import com.wsgc.evergreen.entity.api.ProductGroup;
import java.util.List;

import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tag.area.ShopArea;

/**
 * Smoke Tests for adding an item to the cart via the QuickLook overlay.
 */
@Category(ShopArea.class)
public class AddProductViaQuicklookToCartSmoke extends AbstractTest {

    /**
     * Verifies whether as customer is able to add an item to the cart from
     * the QuickLook Overlay surfaced from the Category page.
     */
    @Test
    public void testAddToCartWhenUsingQuickLookOverlayFromCategoryPageExpectCartHasProduct() {

        // given
        assumeFeatureIsSupported(FeatureFlag.QUICKLOOK);
        final int itemIndex = 0;
        final int expectedTotalItem = 1;
        final String expectedQuantity = "3";
        final ProductGroup quicklookItem = dataService.findProductGroup(productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null, "simple-pip",
                "!attributes", "!monogrammable", "quicklook");
        final String productSku = quicklookItem.getSkus().get(itemIndex);

        // when
        QuicklookOverlay quickLook = ShopFlows.goToQuicklookFlow(context, quicklookItem);
        quickLook.setQuantity(expectedQuantity);
        RichAddToCartQuicklookOverlay rac = quickLook.addToCart();
        ShoppingCartPage shoppingCartPage = rac.checkout();
        shoppingCartPage.scrollIntoCartSection();

        // then
        List<ShoppingCartItemRepeatableSection> itemSectionList = shoppingCartPage.getShoppingCartItems();
        assertEquals(String.format("The shopping cart should have %s item.", expectedTotalItem), expectedTotalItem,
                itemSectionList.size());
        ShoppingCartItemRepeatableSection item = shoppingCartPage.getFirstShoppingCartItem();
        assertTrue(String.format("The shopping cart item's sku should ends with '%s'. Actual sku: '%s'", productSku,
                item.getSku()), item.getSku().endsWith(productSku));
        assertEquals(String.format("The shopping cart item's quantity should be '%s'", expectedQuantity),
                String.valueOf(expectedQuantity), item.getQuantity());

        List<ShoppingCartHeaderRepeatableSection> headerSectionList = shoppingCartPage.getSuborderHeadersOnCart();
        assertEquals(String.format("The shopping cart should have %s header.", expectedTotalItem), expectedTotalItem,
                headerSectionList.size());
    }

   /**
    * Smoke test for ATC on Shop/Search when Feature is enabled and simple pip is selected.
    *
    * @throws InterruptedException will throw if the index passed to addToCartFromQuickLookOverlay is outside of 1-20
    *
    */
    @Test
    public void testAddToCartWhenUsingAddToCartQuickLookButtonExpectProductInCart() throws InterruptedException {

        // given
        final CategoryPage categoryPage;
        final ShoppingCartItemRepeatableSection shoppingCartItemRepeatableSection;
        final String productSkuFromRichAddToCartQuicklookOverlay;
        final String productSkuFromShoppingCart;
        final int EXPECTED_CART_COUNT = 1;

        Assume.assumeTrue(context.supportsFeature(FeatureFlag.ADD_TO_CART_ON_SHOP_AND_SEARCH) && context.isFullSiteExperience());
        final CategoryTree categoryTree = dataService.findCategoryTree("quicklook-category");
        String superCategoryType = categoryTree.getSuperCategory().getUrlPathSegment();
        String categoryType = categoryTree.getCategory().getUrlPathSegment();

        categoryPage = ShopFlows.navigateToCategoryPage(context, superCategoryType + "/" + categoryType);

        // when
        // hover and click quicklook item
        QuicklookOverlay quickLookOverlay = categoryPage.addToCartFromQuickLookOverlay(1);

        // add to cart from Quicklook Overlay
        RichAddToCartQuicklookOverlay richAddToCartQuicklookOverlay = quickLookOverlay.addToCart();

        // get sku from RAC Overlay
        productSkuFromRichAddToCartQuicklookOverlay = richAddToCartQuicklookOverlay.getSkuText();

        // navigate to shopping cart page
        ShoppingCartPage shoppingCartPage = richAddToCartQuicklookOverlay.checkout();

        shoppingCartPage.scrollIntoCartSection();
        shoppingCartItemRepeatableSection = shoppingCartPage.getFirstShoppingCartItem();
        productSkuFromShoppingCart = shoppingCartItemRepeatableSection.getSku();

        // then
        assertEquals(String.format("The shopping cart should have %s item.", EXPECTED_CART_COUNT), String.valueOf(EXPECTED_CART_COUNT),
                shoppingCartItemRepeatableSection.getQuantity());

        assertEquals("Product SKU displayed in the RAC Quicklook Overlay matches with the product SKU from Shopping Cart page",
                productSkuFromRichAddToCartQuicklookOverlay, productSkuFromShoppingCart);
    }
}
