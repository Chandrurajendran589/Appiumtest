package com.wsgc.ecommerce.ui.regression.shop.pip;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.Product;
import com.wsgc.ecommerce.ui.pagemodel.shop.MiniProductInformationPageOverlay;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductSubsetRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.RichAddToCartOverlaySection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartPage;
import com.wsgc.ecommerce.ui.utils.ProductGroupUtility;
import com.wsgc.evergreen.entity.api.ProductGroup;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tag.area.ShopArea;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Tests for validating  Persistent Items on Product Page.
 */
@Category(ShopArea.class)
public class PersistentProductsRegression extends AbstractTest {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Verifies whether the shopper is able to add persistent products to the cart.
     * Test ID : RGSN-38234
     */
    @Test
    @TestRail(id = "104524")
    public void testPersistentProductsWhenUserAddsPersistentProductToCartExpectProductDisplayedOnShoppingCartPage() {

        // given
        Assume.assumeTrue(context.isFullSiteExperience());
        final ProductGroup expectedProductGroup = dataService.findProductGroup("persistent-products-on-simple-pip");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        final ArrayList<Product> persistentProducts = ProductGroupUtility.getProductData(expectedProductGroup);
        final int expectedTotalItem = persistentProducts.size();
        int expectedTotalQuantity = 0;
        List<String> expectedSkus = new ArrayList<>();

        if (productPage.getQuantity().equals("1")) {
            productPage.setQuantity(0);
        }

        ProductSubsetRepeatableSection persistentItem = null;
        for (Product persistentProduct : persistentProducts) {
            int itemQuantity = persistentProduct.getQuantity();
            expectedTotalQuantity += itemQuantity;
            expectedSkus.add(persistentProduct.getSku());
            int sectionIndex = productPage.getSectionIndexForPersistentItem(persistentProduct.getName());
            persistentItem = productPage.getPersistentsByIndex(sectionIndex);

            StringTokenizer attributeTokenizer = new StringTokenizer(String.join("", persistentProduct.getAttributes()), ",");
            Map<String, String> persistentAttributeSelection = new HashMap<>();
            while (attributeTokenizer.hasMoreTokens()) {
                String attribute = attributeTokenizer.nextToken();
                if (!attribute.equals("NA")) {
                    persistentAttributeSelection.put(attribute, attribute);
                }
            }
            if (persistentAttributeSelection.size() != 0) {
                persistentItem.setAttributeSelections(persistentAttributeSelection);
            }
            persistentItem.setQuantity(itemQuantity, false);
        }

        // then
        Assume.assumeTrue("No Persistent Products Found. ", persistentItem != null);

        // when
        RichAddToCartOverlaySection rac = persistentItem.addToCart();

        // then
        assertEquals(String.format("The RAC overlay should have %s item sku.", expectedSkus.get(0).toString()), expectedSkus.get(0).toString(),
                rac.getRichAddToCartOverlayItemRepeatableSections().get(0).getSku().toString());

        // when
        ShoppingCartPage shoppingCartPage = rac.goToShoppingCartPage();
        shoppingCartPage.scrollIntoCartSection();
        List<ShoppingCartItemRepeatableSection> shoppingCartItems = shoppingCartPage.getShoppingCartItems();

        // then
        assertEquals(String.format("The shopping cart should have %s item.", expectedTotalItem), expectedTotalItem, shoppingCartItems.size());

        // when
        List<String> shoppingCartSkuList = shoppingCartPage.getSkus();

        // then
        assertThat(String.format("The shopping cart item should be '%s'", expectedSkus), shoppingCartSkuList, containsInAnyOrder(expectedSkus.toArray()));

        // when
        int totalQuantityInShoppingCart = 0;
        for (ShoppingCartItemRepeatableSection shoppingCartItem : shoppingCartItems) {
            totalQuantityInShoppingCart += Integer.parseInt(shoppingCartItem.getQuantity());
        }

        // then
        assertEquals(String.format("The shopping cart total quantity should be '%s'", expectedTotalQuantity), expectedTotalQuantity, totalQuantityInShoppingCart);
    }

    /**
     * Verifies whether the shopper is able to see and navigate through Persistent Product section on mobile site.
     * Test ID : RGSN-38234
     */
    @Test
    @TestRail(id = "104525")
    public void testPersistentProductsCarouselWhenClickOnPersistentProductCarouselExpectProductPage() {

        // given
        Assume.assumeTrue(context.isMobileExperience());
        final ProductGroup expectedProductGroup = dataService.findProductGroup("persistent-products-on-simple-pip");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        ProductSubsetRepeatableSection productSubsetRepeatableSection = productPage.scrollToPersistentProductsSection();
        int persistentProductsCount = productSubsetRepeatableSection.getPersistentProductsCount();
        int persistentProductIndex = RandomUtils.nextInt(0, persistentProductsCount - 1);
        String expectedPersistentProductUrl = productSubsetRepeatableSection.getPersistentProductUrl(persistentProductIndex);
        productSubsetRepeatableSection.clickPersistentProduct(persistentProductIndex);
        String actualProductPageUrl = context.getPilot().getDriver().getCurrentUrl();

        //then
        Assert.assertEquals("Product Page URL from Persistent Product \'" + expectedPersistentProductUrl + "\' is not matching with \'"
                + actualProductPageUrl + "\' of Product Page URL ", expectedPersistentProductUrl, actualProductPageUrl);
    }

    /**
     * Verifies whether the shopper is able to see various section of Mini PIP overlay.
     * Test ID : RGSN-38234
     */
    @Test
    @TestRail(id = "104526")
    public void testMiniPIPOverlayOnPersistentProductsWhenClicksViewDetailsLinkExpectMiniPIPOverlayWithTheProperContents() {

        // given
        Assume.assumeTrue(context.isFullSiteExperience());
        final ProductGroup expectedProductGroup = dataService.findProductGroup("persistent-products-on-simple-pip");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        final ArrayList<Product> persistentProducts = ProductGroupUtility.getProductData(expectedProductGroup);
        List<String> expectedSkus = new ArrayList<>();
        ProductSubsetRepeatableSection persistentItem = null;
        expectedSkus.add(persistentProducts.get(0).getSku());
        int sectionIndex = productPage.getSectionIndexForPersistentItem(persistentProducts.get(0).getName());
        persistentItem = productPage.getPersistentsByIndex(sectionIndex);
        MiniProductInformationPageOverlay miniProductInformationPageOverlay = persistentItem.clickViewDetailsLink();
        boolean testPassed = true;

        // then
        if (!miniProductInformationPageOverlay.isHeroThumbnailImagePresent()) {
            testPassed = false;
            log.error("The Hero Image is not displayed in the Mini PIP Overlay");
        }

        if (!miniProductInformationPageOverlay.isAltImageCarouselPresent()) {
            testPassed = false;
            log.error("The Alt Image is not displayed in the Mini PIP Overlay");
        }

        if (!miniProductInformationPageOverlay.isProductOverviewTabPresent()) {
            testPassed = false;
            log.error("The Product Overview tab is not displayed in the Mini PIP Overlay");
        }

        if (!miniProductInformationPageOverlay.isShippingTabPresent()) {
            testPassed = false;
            log.error("The Shipping tab is not displayed in the Mini PIP Overlay");
        }
        assertTrue("Test steps got failed, Please check ERROR logs in console", testPassed);
    }

    /**
     * Verifies whether the shopper is able to see various section of Persistent Products.
     * Test ID : RGSN-38234
     */
    @Test
    @TestRail(id = "104527")
    public void testPersistentProductSectionContentsWhenNavigateToPIPExpectPersistentProductSection() {

        // given
        Assume.assumeTrue(context.isFullSiteExperience());
        final ProductGroup expectedProductGroup = dataService.findProductGroup("persistent-products-on-simple-pip");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        final ArrayList<Product> persistentProducts = ProductGroupUtility.getProductData(expectedProductGroup);
        List<String> expectedSkus = new ArrayList<>();
        ProductSubsetRepeatableSection persistentItem = null;
        expectedSkus.add(persistentProducts.get(0).getSku());
        int sectionIndex = productPage.getSectionIndexForPersistentItem(persistentProducts.get(0).getName());
        persistentItem = productPage.getPersistentsByIndex(sectionIndex);
        boolean testPassed = true;

        // then
        if (!persistentItem.isHeroImagePresentOnPersistentProducts()) {
            testPassed = false;
            log.error("The Hero Image is not displayed in the Persistent Product section");
        }

        if (!persistentItem.isPricePresentOnPersistentProducts()) {
            testPassed = false;
            log.error("The Price is not displayed in the Persistent Product section");
        }

        if (!persistentItem.isAffirmLinkPresentOnPersistentProducts()) {
            testPassed = false;
            log.error("The Affirm link is not displayed in the Persistent Product section");
        }
        assertTrue("Test steps got failed, Please check ERROR logs in console", testPassed);
    }
}
