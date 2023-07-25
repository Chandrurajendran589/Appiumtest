package com.wsgc.ecommerce.ui.regression.shop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.shop.PipFlows;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.shop.Product;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.RichAddToCartOverlaySection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartHeaderRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartPage;
import com.wsgc.ecommerce.ui.utils.ProductGroupUtility;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.ProductGroup;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tag.area.ShopArea;

/**
 * Regression Tests for validating choose options button in shop pages.
 */
@Category(ShopArea.class)
public class AddProductViaChooseOptionsRegression extends AbstractTest {

    /**
     * Ensures feature flag is enabled.
     */
    @Before
    public void setUp() {
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.CHOOSE_OPTIONS_ON_SHOP_AND_SEARCH)
                && context.isFullSiteExperience());
    }
    
    /**
     * Verifies whether a customer is able to add an item to the cart from 
     * the Product page via choose options in category page. 
     * Test Case ID - RGSN-38209 
     */
    @Test
    @TestRail(id = "99801")
    public void testAddMultiBuyToCartWhenUserNavigateToProductPageViaChooseOptionsInCategoryPageExpectCartHasProduct() {

        assumeFeatureIsSupported(FeatureFlag.MULTI_BUY_PIP);
        // given
        final ProductGroup expectedProductGroup = dataService.findProductGroup(
                 productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null, "multi-buy-multi-sku-pip");
        final ArrayList<Product> productData = ProductGroupUtility.getProductData(expectedProductGroup);

        // when
        ProductPage productPage = ShopFlows.goToPipPageViaChooseOptions(context, expectedProductGroup);
        PipFlows.setMultiBuyPipQuantityAndAttributes(productPage, null);

        RichAddToCartOverlaySection rac = productPage.addToCart();
        ShoppingCartPage shoppingCartPage = rac.goToShoppingCartPage();
        shoppingCartPage.scrollIntoCartSection();

        // then
        int totalQuantityInShoppingCart = 0;
        int expectedTotalQuantity = 0;
        for (Product product : productData) {
            expectedTotalQuantity += product.getQuantity();
            List<String> shoppingPageSkuList = shoppingCartPage.getSkus();
            int shoppingCartItemIndex = shoppingCartPage.getShoppingCartItemIndexMatchingProductSku(product, shoppingPageSkuList);
            ShoppingCartItemRepeatableSection shoppingCartItem = shoppingCartPage.getShoppingCartItemByIndex(shoppingCartItemIndex);
            totalQuantityInShoppingCart += Integer.parseInt(shoppingCartItem.getQuantity());
            assertTrue(String.format("The shopping cart item's sku should end with '%s'. Actual sku: '%s'", product.getSku(),
                                     shoppingCartItem.getSku()), shoppingCartItem.getSku().endsWith(product.getSku()));
            assertTrue(String.format("The shopping cart item's quantity should be '%s'", product.getQuantity(),
                                     shoppingCartItem.getQuantity()), shoppingCartItem.getQuantity().equals(String.valueOf(product.getQuantity())));
        }
        
        final int expectedTotalItems = productData.size();
        // then
        List<ShoppingCartItemRepeatableSection> itemSectionList = shoppingCartPage.getShoppingCartItems();

        assertEquals(String.format("The shopping cart should have %s items.", expectedTotalItems), expectedTotalItems,
                     itemSectionList.size());

        assertEquals(String.format("The shopping carts total item quantity should be '%s'", expectedTotalQuantity),
                     expectedTotalQuantity, totalQuantityInShoppingCart);

        List<ShoppingCartHeaderRepeatableSection> headerSectionList = shoppingCartPage.getSuborderHeadersOnCart();
        assertEquals(String.format("The shopping cart should have %s header.", 1), 1,
                     headerSectionList.size());
    }
}
