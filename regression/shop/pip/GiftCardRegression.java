package com.wsgc.ecommerce.ui.regression.shop.pip;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartHeaderRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.ProductGroup;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tag.area.ShopArea;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Regression Tests for validating functionality of Gift Card Product Page.
 */
@Category(ShopArea.class)
public class GiftCardRegression extends AbstractTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Verifies whether the shopper is able to add a gift card to the cart with gift card message.
     * Test Case ID: RGSN-38225
     */
    @Test
    @TestRail(id = "103659")
    public void testAddToCartWhenProductIsGiftCardWithMessageExpectCartHasProductAndMessage() {

        // given
        final int itemIndex = 0;
        final int expectedTotalItem = 1;
        final int expectedQuantity = 1;
        final String expectedToFieldValue = "ToFieldValue";
        final String expectedFromFieldValue = "FromFieldValue";
        final String expectedMSG1FieldValue = "MsgOneFieldValue";
        final String expectedMSG2FieldValue = "MsgTwoFieldValue";
        final ProductGroup expectedProductGroup = dataService.findProductGroup("gift-card");
        final String expectedSku = expectedProductGroup.getSkus().get(itemIndex);

        // when
        ShoppingCartPage shoppingCartPage = ShopFlows.addGiftCardToCartSetSimplePipAttributeSelectionsEnterGiftCardMessageFlow(context, expectedProductGroup,
                String.valueOf(expectedQuantity), expectedToFieldValue, expectedFromFieldValue,
                expectedMSG1FieldValue, expectedMSG2FieldValue);
        shoppingCartPage.scrollIntoCartSection();

        // then
        List<ShoppingCartItemRepeatableSection> itemSectionList = shoppingCartPage.getShoppingCartItems();
        assertEquals(String.format("The shopping cart should have %s item.", expectedTotalItem), expectedTotalItem,
                itemSectionList.size());
        ShoppingCartItemRepeatableSection item = shoppingCartPage.getFirstShoppingCartItem();
        final String[] giftCardMessage = item.getGiftCardMessage();
        assertTrue(String.format("The shopping cart item's sku should ends with '%s'. Actual sku: '%s'", expectedSku,
                item.getSku()), item.getSku().endsWith(expectedSku));
        assertEquals(String.format("The shopping cart item's quantity should be '%s'", expectedQuantity),
                String.valueOf(expectedQuantity), item.getQuantity());
        List<ShoppingCartHeaderRepeatableSection> headerSectionList = shoppingCartPage.getSuborderHeadersOnCart();
        assertEquals(String.format("The shopping cart should have %s header.", expectedTotalItem), expectedTotalItem,
                headerSectionList.size());
        assertEquals(String.format("The To value should be: %s", String.format("to: %s", expectedToFieldValue.toLowerCase())),
                String.format("to: %s", expectedToFieldValue.toLowerCase()), giftCardMessage[0].toLowerCase());
        assertEquals(String.format("The From value should be: %s", String.format("from: %s", expectedFromFieldValue.toLowerCase())),
                String.format("from: %s", expectedFromFieldValue.toLowerCase()), giftCardMessage[1].toLowerCase());
        assertEquals(
                String.format("The Message value should be: %s",
                        String.format("message: %s %s", expectedMSG1FieldValue.toLowerCase(), expectedMSG2FieldValue.toLowerCase())),
                String.format("message: %s %s", expectedMSG1FieldValue.toLowerCase(), expectedMSG2FieldValue.toLowerCase()), giftCardMessage[2].toLowerCase());
    }

    /**
     * Verifies whether the shopper is able to see the product overview section contents on the page.
     * Test Case ID: RGSN-38225
     */
    @Test
    @TestRail(id = "104354")
    public void testProductDetailsOverviewSectionWhenUserVisitsGiftPageExpectTermsAndCheckBalanceLinksOnTheProductPage() {

        // given
        boolean testPassed = true;
        final ProductGroup productGroup = dataService.findProductGroup("gift-card");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        productPage.expandProductAccordionSection();

        // then
        if (!productPage.isTermsAndConditionsLinkDisplayed()) {
            log.error("Terms And Condition Link is not present");
            testPassed = false;
        }
        if (!productPage.isCheckYourBalanceLinkDisplayed()) {
            log.error("Check Your Balance Link is not present");
            testPassed = false;
        }
        assertTrue("Test step got failed, Please check the logs for that failed validation", testPassed);
    }

    /**
     * Verifies whether the shopper is able to navigate to products when user click on any item form Recommendation Section on
     * Gift Card Product Page
     * Test Case ID: RGSN-38225
     */
    @Test
    @TestRail(id = "104355")
    public void testRecommendationSectionWhenUserClicksOnRecommendedProductExpectProductPage() {

        // given
        final ProductGroup expectedProductGroup = dataService.findProductGroup("gift-card");
        final String EXPECTED_PIP_RECSTART_PARAM = "?cm_src=WsiPip";

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        int productRecommendationsAvailable = productPage.getTotalNumberOfProductsFromRecommendationSection();
        int productRecommendationIndex = RandomUtils.nextInt(0, productRecommendationsAvailable - 1);
        String recommendationProductUrl = productPage.getProductUrlFromRecommendationSection(productRecommendationIndex);
        productPage.selectProductFromRecommendationSection(productRecommendationIndex);
        String productPageUrl = productPage.getCurrentPageUrl();

        //then
        assertTrue(String.format("%s - is not present in the product page url - %s", EXPECTED_PIP_RECSTART_PARAM, productPageUrl),
                productPageUrl.contains(EXPECTED_PIP_RECSTART_PARAM));
    }

    /**
     * Verifies whether the shopper is able to see related Searches Section.
     * Test Case ID: RGSN-38225
     */
    @Test
    @TestRail(id = "104356")
    public void testRelatedSearchesSectionWhenUserNavigatesToGiftCardPageExpectRelatedSearchesSectionOnGiftCardPage() {

        // given
        Assume.assumeTrue(context.isFullSiteExperience());
        assumeFeatureIsSupported(FeatureFlag.RELATED_SEARCHES);
        final ProductGroup productGroup = dataService.findProductGroup("gift-card");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        int relatedSearchItemsAvailable = productPage.getTotalNumberOfRelatedSearchAvailableInRelatedSearchesSection();

        // then
        assertTrue("Related searches items do not present on GIFT PIP MFE", relatedSearchItemsAvailable > 1);
    }

    /**
     * Verifies the various elements the user sees on a Gift Card Product Page.
     * Test Case ID: RGSN-38225
     */
    @Test
    @TestRail(id = "104357")
    public void testGiftCardProductPageElementsWhenUserVisitsGiftCardPageExpectVariousElementsOfGiftCardProductPage() {

        // given
        final ProductGroup productGroup = dataService.findProductGroup("gift-card");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        boolean testPassed = true;

        // then
        if (!productPage.isHeroImageDisplayed()) {
            log.error("hero Image is not present on the gift card page");
            testPassed = false;
        }

        if (!productPage.isAlternateImageDisplayed()) {
            log.error("Alternate Image is not present on the gift card page");
            testPassed = false;
        }

       if (!productPage.isCuralateImageSectionDisplayed()) {
           log.error("Curalate Image section is not present on the gift card page");
           testPassed = false;
       }

       if (!productPage.isProductOverviewSectionDisplayed()) {
           log.error("Product Overview section is present on the gift card page");
           testPassed = false;
       }

       if (!productPage.isBreadcrumbsPresentAndDisplayed()) {
            log.error("Breadcrumbs is not present on the gift card page");
            testPassed = false;
        }
        assertTrue("Test step got failed, Please check the logs for that failed validation", testPassed);
    }
}
