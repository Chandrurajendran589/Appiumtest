package com.wsgc.ecommerce.ui.regression.shop.pip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.checkout.legacy.CheckoutFlows;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.RichAddToCartOverlaySection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartPage;
import com.wsgc.evergreen.entity.api.ProductGroup;

/**
 * Regression Tests for Validating the components of single MFE-PiP.
 *
 */
public class SimpleMfePipComponentsRegression extends AbstractTest {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * Verifies the various components availability in single sku pip.
     * Test Case ID: RGSN-38227
     */
    @Test
    @TestRail(id = "105253")
    public void testSingleSkuProductPageElementsWhenUserVisitsProductPageExpectVariousElementsOfSingleSkuProductPage() {

        // given
        final ProductGroup productGroup = dataService.findProductGroup("simple-pip","!attributes");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        boolean testPassed = true;

        // then
        if (!productPage.isHeroImageDisplayed()) {
            log.error("hero Image is not present on the single sku product page");
            testPassed = false;
        }

        if (!productPage.isAlternateImageDisplayed()) {
            log.error("Alternate Image is not present on the single sku product page");
            testPassed = false;
        }

        if (!productPage.isCuralateImageSectionDisplayed()) {
           log.error("Curalate Image section is not present on single sku product page");
           testPassed = false;
        }

        if (!productPage.isProductOverviewSectionDisplayed()) {
           log.error("Product Overview section is present on single sku product page");
           testPassed = false;
        }

        if (!productPage.isBreadcrumbsPresentAndDisplayed()) {
            log.error("Breadcrumbs is not present on the single sku product page");
            testPassed = false;
        }
        assertTrue("Test step got failed, Please check the logs for that failed validation", testPassed);
    }
    
    /**
     * Verifies if the user is able to update the quantity for single sku product and add to cart and recommendation carousel
     * is displayed in rich add to cart overlay.
     * .
     * Test Case ID - RGSN-38736
     */
    @Test
    @TestRail(id = "105254")
    public void testAddToCartWhenUserAddsSimplePipToCartExpectProductAddedToCart() {

        // given
        final ProductGroup productGroup = dataService.findProductGroup("simple-pip","!attributes");
        final String expectedSku = productGroup.getSkus().get(0).toString();
        final String expectedQuantity = "3";
        final int expectedTotalItem = 1;
        
        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        productPage.setQuantity(expectedQuantity);
        RichAddToCartOverlaySection richAddToCartOverlaySection = productPage.addToCart();
        if (context.isFullSiteExperience()) {
            final String actualSku = richAddToCartOverlaySection.getRichAddToCartOverlayItemRepeatableSections().get(0).getSku().toString();
            assertEquals(String.format("The RAC overlay should have %s item sku.But found %s", expectedSku, actualSku), expectedSku, actualSku);
        }

        // then
        assertTrue("Recommendation carousel is not displayed", richAddToCartOverlaySection.isRecommendationsCarouselDisplayed());
        assertTrue("Image is not displayed in recommendation carousel", richAddToCartOverlaySection.isImageDisplayedInRecommendationsCarousel());

        // when
        ShoppingCartPage shoppingCartPage = richAddToCartOverlaySection.goToShoppingCartPage();
        shoppingCartPage.scrollIntoCartSection();

        // then
        List<ShoppingCartItemRepeatableSection> itemSectionList = shoppingCartPage.getShoppingCartItems();
        assertEquals(String.format("The shopping cart should have %s item.", expectedTotalItem), expectedTotalItem,
                     itemSectionList.size());
        ShoppingCartItemRepeatableSection item = shoppingCartPage.getFirstShoppingCartItem();
        assertEquals(String.format("The shopping cart item's quantity should be '%s'", expectedQuantity),
                     String.valueOf(expectedQuantity), item.getQuantity());
    }
    
    /**
     * Verifies if the user is able to update the quantity in cart page with value less than 99.
     * Test Case ID - RGSN-38806
     */
    @Test
    @TestRail(id = "108147")
    public void testUpdateQuantityInCartWhenUserEditsQuantityExpectQuantityIsUpdated() {
    	
        // given
        final ProductGroup productGroup = dataService.findProductGroup("simple-pip", "!attributes");
        final String expectedQuantity = "99";
        final String updatedQuantity = "11";
        
        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        productPage.setQuantity(expectedQuantity);
        productPage.addToCart();
        
        ShoppingCartPage shoppingCartPage = context.getPage(ShoppingCartPage.class);
        startPilotAt(shoppingCartPage);
        shoppingCartPage.scrollIntoCartSection();

        // then
        ShoppingCartItemRepeatableSection item = shoppingCartPage.getFirstShoppingCartItem();
        assertEquals(String.format("The shopping cart item's quantity should be '%s'", expectedQuantity),
                String.valueOf(expectedQuantity), item.getQuantity());
        
        // when
        item.setQuantity(updatedQuantity);
        item.updateQuantity();
        
        // then
        assertEquals(String.format("The shopping cart item's quantity should be '%s'", updatedQuantity),
                String.valueOf(updatedQuantity), item.getQuantity());
    }
    
    /**
     * Verifies if the user is not able to edit the quantity more than 99 in cart page.
     * Test Case ID - RGSN-38806
     */
    @Test
    @TestRail(id = "108148")
    public void testUpdateQuantityInCartWhenUserEditsQuantityMoreThanTwoDigitExpectQuantityIsNotUpdated() {
    	
        // given
        final ProductGroup productGroup = dataService.findProductGroup("simple-pip", "!attributes");
        final String expectedQuantity = "1";
        final String updatedQuantity = "9900";
        
        // when
        ShoppingCartPage shoppingCartPage = CheckoutFlows.addProductToCartAndGoToShoppingCartFlow(context, productGroup);
        shoppingCartPage.scrollIntoCartSection();
        ShoppingCartItemRepeatableSection item = shoppingCartPage.getFirstShoppingCartItem();
        
        // then
        assertEquals(String.format("The shopping cart item's quantity should be '%s'", expectedQuantity),
                expectedQuantity, item.getQuantity());
        
        // when
        item.setQuantity(updatedQuantity);
        item.updateQuantity();
        
        // then
        assertEquals(String.format("The shopping cart item's quantity should be '%s'", updatedQuantity.substring(0, 2)),
                updatedQuantity.substring(0, 2), item.getQuantity());
        assertTrue("Quantity box maximum length is more than 2 digit", item.isQuantityBoxMaximumLengthIsTwoDigit());
        
        // when
        Float totalItemPriceAfterUpdatingQuantity = Float.parseFloat(item.getPrice()) * Float.parseFloat(updatedQuantity.substring(0, 2));
        String itemTotalPrice = item.getItemTotalPrice().replaceAll(",", "");
        
        // then
        assertTrue("Item total Price is not updated correctly while updating quantity of an item", 
                String.valueOf(totalItemPriceAfterUpdatingQuantity).contains(itemTotalPrice));
    }
}
