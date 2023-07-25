package com.wsgc.ecommerce.ui.regression.shop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductSubsetRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.RichAddToCartOverlaySection;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartHeaderRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.ProductGroup;
import java.util.List;
import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tag.area.ShopArea;

/**
 * Regression Tests for selecting product swatch from category page.
 */
@Category(ShopArea.class)
public class SwatchSelectionRegression extends AbstractTest {

    /**
     * Verifies whether the attributes are preselected for a Simple PIP
     * when user selects product swatch on category Page.
     * Test case ID - RGSN-38210
     */
    @Test
    @TestRail(id = "103001")
    public void testSwatchSelectionIndicatorOnCategoryPageWhenSwatchSelectedExpectSelectionCarriesOverToSimplePipAndCart() {
        // given
        final ProductGroup expectedProductGroup = dataService.findProductGroup(
                productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null, 
                "simple-pip", "attributes", "swatch-selection"); 
        int indexOfSwatch = 2;
        final int itemIndex = 0;
        final int expectedTotalItem = 1;
        final int expectedQuantity = 1;
        final String expectedSku = expectedProductGroup.getSkus().get(itemIndex);
        
        //when
        CategoryPage productCategoryPage = ShopFlows.navigateToCategoryPage(context, 
                 expectedProductGroup.getUnmappedAttribute("categoryUrl"));
        productCategoryPage.waitForCategoryPageLoad();
        CategoryItemRepeatableSection categoryItemRepeatableSection = productCategoryPage.getProductSectionByGroupId(
                expectedProductGroup.getGroupId());
        productCategoryPage.getProductSwatchSelectionSku(expectedProductGroup, indexOfSwatch);
        assertTrue("Favorites icon is not present after selection of swatch", 
                categoryItemRepeatableSection.isFavoritesIconPresentAndDisplayed());
        productCategoryPage.clickSwatchSelection(expectedProductGroup, indexOfSwatch);
        
        
        //then     
        ProductPage productPage = productCategoryPage.clickThroughProductPipImage(expectedProductGroup);
        ProductSubsetRepeatableSection section = productPage.getSimpleProductItemRepeatableSection();
        assertTrue("The price is not present on PIP", section.isRegularPricePresent() || section.isSalePricePresent());
        RichAddToCartOverlaySection richAddToCartOverlaySection =  productPage.addToCart();
        ShoppingCartPage shoppingCartPage = richAddToCartOverlaySection.goToShoppingCartPage();
        shoppingCartPage.scrollIntoCartSection();
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
	
    /**
     * Verifies whether the attributes are preselected for a Multi-Buy PIP
     * when user selects product swatch on Category Page.
     * Test case ID - RGSN-38210
     */
    @Test
    @TestRail(id = "103002")
    public void testSwatchSelectionIndicatorOnCategoryPageWhenSwatchSelectedExpectSelectionCarriesOverToMultiBuyPipAndCart() {

        // given
        final ProductGroup expectedProductGroup = dataService.findProductGroup(
                productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null, 
                "multi-buy-multi-sku-pip", "attributes", "swatch-selection");
        int indexOfSwatch = 2;
        final int itemIndex = 0;
        final int expectedTotalItem = 1;
        final int expectedQuantity = 1;
        final String expectedSku = expectedProductGroup.getSkus().get(itemIndex);
        
        // when
        CategoryPage productCategoryPage = ShopFlows.navigateToCategoryPage(context, 
                expectedProductGroup.getUnmappedAttribute("categoryUrl"));
       productCategoryPage.waitForCategoryPageLoad();
       CategoryItemRepeatableSection categoryItemRepeatableSection = productCategoryPage.getProductSectionByGroupId(
               expectedProductGroup.getGroupId());
       productCategoryPage.getProductSwatchSelectionSku(expectedProductGroup, indexOfSwatch);
       assertTrue("Favorites icon is not present after selection of swatch", 
               categoryItemRepeatableSection.isFavoritesIconPresentAndDisplayed());
       productCategoryPage.clickSwatchSelection(expectedProductGroup, indexOfSwatch);
       
       
        //then     
        ProductPage productPage = productCategoryPage.clickThroughProductPipImage(expectedProductGroup);
        RichAddToCartOverlaySection richAddToCartOverlaySection =  productPage.addToCart(); 
        ShoppingCartPage shoppingCartPage = richAddToCartOverlaySection.goToShoppingCartPage();
        shoppingCartPage.scrollIntoCartSection();
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
	
    /**
     * Verifies whether the attributes are preselected for a Guided PIP
     * when user selects product swatch on Category Page.
     * Test case ID - RGSN-38210
     */
    @Test
    @TestRail(id = "103003")
    public void testSwatchSelectionIndicatorOnCategoryPageWhenSwatchSelectedExpectSelectionCarriesOverToGuidedPipAndCart() {

        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP) 
        		|| context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP_REDESIGN));

        // given
        final ProductGroup expectedProductGroup = dataService.findProductGroup(
                productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null, 
                "guided-pip", "attributes", "swatch-selection");
        int indexOfSwatch = 2;
        final int itemIndex = 0;
        final int expectedTotalItem = 1;
        final int expectedQuantity = 1;
        final String expectedSku = expectedProductGroup.getSkus().get(itemIndex);

        // when
        CategoryPage productCategoryPage = ShopFlows.navigateToCategoryPage(context, 
                expectedProductGroup.getUnmappedAttribute("categoryUrl"));
       productCategoryPage.waitForCategoryPageLoad();
       CategoryItemRepeatableSection categoryItemRepeatableSection = productCategoryPage.getProductSectionByGroupId(
               expectedProductGroup.getGroupId());
       productCategoryPage.getProductSwatchSelectionSku(expectedProductGroup, indexOfSwatch);
       assertTrue("Favorites icon is not present after selection of swatch", 
               categoryItemRepeatableSection.isFavoritesIconPresentAndDisplayed());
       productCategoryPage.clickSwatchSelection(expectedProductGroup, indexOfSwatch);      
       
        //then     
        ProductPage productPage = productCategoryPage.clickThroughProductPipImage(expectedProductGroup);
        ProductSubsetRepeatableSection section = productPage.getGuidedProductItemRepeatableSection();
        assertTrue("The price is not present on PIP", section.isRegularPricePresent() || section.isSalePricePresent());
        RichAddToCartOverlaySection richAddToCartOverlaySection =  productPage.addToCart();
        ShoppingCartPage shoppingCartPage = richAddToCartOverlaySection.goToShoppingCartPage();
        shoppingCartPage.scrollIntoCartSection();
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
