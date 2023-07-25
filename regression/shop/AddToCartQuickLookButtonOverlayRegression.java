package com.wsgc.ecommerce.ui.regression.shop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.account.AccountFlows;
import com.wsgc.ecommerce.ui.endtoend.registry.RegistryFlows;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.account.AccountHomePage;
import com.wsgc.ecommerce.ui.pagemodel.account.LoginPage;
import com.wsgc.ecommerce.ui.pagemodel.i18n.InternationalShippingOverlay;
import com.wsgc.ecommerce.ui.pagemodel.registry.CrossBrandRegistryListPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.QuicklookOverlay;
import com.wsgc.ecommerce.ui.pagemodel.shop.RichAddToCartQuicklookOverlay;
import com.wsgc.ecommerce.ui.pagemodel.shop.RichAddToRegistryOverlaySection;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartHeaderRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.ProductGroup;
import com.wsgc.evergreen.entity.api.Registry;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;
import org.junit.*;
import org.junit.experimental.categories.Category;
import tag.area.ShopArea;

/**
 * Regression Tests for validating add to cart button in category pages.
 */
@Category(ShopArea.class)
public class AddToCartQuickLookButtonOverlayRegression extends AbstractTest {

    /**
     * Prepares data fixtures & ensures feature flag is enabled.
     */
    @Before
    public void setUp() {
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.ADD_TO_CART_ON_SHOP_AND_SEARCH)
                && context.isFullSiteExperience());
    }

    /**
     * Verifies if favorite confirmation pop up is displayed when favorite icon is
     * clicked on an item from add to cart button overlay.
     * Test Case ID - RGSN-38179
     */
    @Test
    @TestRail(id = "98549")
    public void testAddToCartQuickLookButtonOverlayWhenUserAddsItemToFavoritesExpectAddedToFavoritesPopup() {

        // given
        final ProductGroup quicklookItem = dataService.findProductGroup(
                  productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null, "simple-pip", "attributes", "add-to-cart-button");

        // when
        QuicklookOverlay quicklookOverlay = ShopFlows.goToAddToCartQuicklookButtonFlow(context, quicklookItem);

        //then
        assertTrue("Item was not added to favorites from Quicklook", quicklookItem.getName().equalsIgnoreCase(quicklookOverlay.favoriteProductOnQuickLookOverlay()));
    }
    
    /**
     * Verifies if user is redirected to the Product Page when view full details is clicked from add to cart button overlay.
     * Test Case ID - RGSN-38179
     */
    @Test
    @TestRail(id = "98602")
    public void testAddToCartQuickLookButtonOverlayWhenUserClicksViewFullDetailsExpectProductPage() {

        // given
        final ProductGroup quicklookItem = dataService.findProductGroup(
                productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null, "simple-pip", "attributes", "add-to-cart-button");

        // when
        QuicklookOverlay quicklookOverlay = ShopFlows.goToAddToCartQuicklookButtonFlow(context, quicklookItem);
        String productUrlFromQuicklook = quicklookOverlay.getProductUrlFromQuickLook();
        if (productUrlFromQuicklook.contains(":") && productUrlFromQuicklook.contains("@")) {
        	productUrlFromQuicklook = productUrlFromQuicklook.substring(0, 8) + productUrlFromQuicklook.substring(productUrlFromQuicklook.indexOf('@') + 1, productUrlFromQuicklook.length());
        }
        quicklookOverlay.goToProductPageFromQuickLookOverlay();
        String productPageUrl = context.getPilot().getDriver().getCurrentUrl();
        if (productPageUrl.contains(":") && productPageUrl.contains("@")) {
        	productPageUrl = productPageUrl.substring(0, 8) + productPageUrl.substring(productPageUrl.indexOf('@') + 1, productPageUrl.length());
        }

        //then
        assertTrue("Product URL from Quicklook \'" + productUrlFromQuicklook + "\' is not matching with \'"
                + productPageUrl + "\' of PIP page URL ", productPageUrl.contains(productUrlFromQuicklook));
    }
    
    /**
     * Verifies if affirm overlay is displayed when learn more link is clicked from add to cart button overlay.
     * Test Case ID - RGSN-38179
     */
    @Ignore /*Currently affirm message is not displayed in the VM Boxes. Skipping this test until the issue is resolved.*/
    @Test
    @TestRail(id = "99800")
    public void testAddToCartQuickLookButtonOverlayWhenUserClicksLearnMoreExpectAffirmOverlay() {

        // given
        final ProductGroup quicklookItem = dataService.findProductGroup(
                productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null, "simple-pip", "attributes", "add-to-cart-button");

        // when
        QuicklookOverlay quicklookOverlay = ShopFlows.goToAddToCartQuicklookButtonFlow(context, quicklookItem);
        quicklookOverlay.clickLearnMoreOnAffirmPayment();

        //then
        assertTrue("Affirm Modal overlay is not displayed after clicking on learn more.", quicklookOverlay.isAffirmModalDisplayed());
    }
    
    /**
     * Verifies if product summary is displayed in add to cart button overlay.
     * Test Case ID - RGSN-38179
     */
    @Test
    @TestRail(id = "99802")
	public void testAddToCartQuickLookButtonOverlayWhenUserClicksSummaryExpectProductSummary() {

        // given
        final ProductGroup quicklookItem = dataService.findProductGroup(
                productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null, "simple-pip", "attributes", "add-to-cart-button");

        // when
        QuicklookOverlay quicklookOverlay = ShopFlows.goToAddToCartQuicklookButtonFlow(context, quicklookItem);
        quicklookOverlay.clickProductSummary();

        //then
        assertTrue("Product Summary is not displayed in the summary tab.", quicklookOverlay.getProductSummaryFromQuickLook() != null);
    }
    
    /**
     * Verifies whether a customer is able to add an item to the cart from
     * the QuickLook Overlay surfaced from the Category page.
     * Test Case ID - RGSN-38179, RGSN-38209
     */
    @Test
    @TestRail(id = "99804")
    public void testAddToCartQuickLookButtonOverlayWhenUserAddsItemToCartExpectCartHasProduct() {
    	
        // given
        final ProductGroup quicklookItem = dataService.findProductGroup(
                productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null, 
                "simple-pip", "attributes", "add-to-cart-button", "!made-to-order");
        final int itemIndex = 0;
        final int expectedTotalItem = 1;
        final String expectedQuantity = "3";
        final String productSku = quicklookItem.getSkus().get(itemIndex);

        // when
        QuicklookOverlay quicklookOverlay = ShopFlows.goToAddToCartQuicklookButtonFlow(context, quicklookItem);
        quicklookOverlay.setProductAttributeSelections(quicklookItem);
        quicklookOverlay.setQuantity(expectedQuantity);
        RichAddToCartQuicklookOverlay rac = quicklookOverlay.addToCart();
        
        //then
        assertTrue("The product recommendation section is not displayed in the Rich Add To Cart Overlay", rac.isProductRecommendationsDisplayed());

        //when
        ShoppingCartPage shoppingCartPage = rac.checkout();
        shoppingCartPage.scrollIntoCartSection();

        // then
        List<ShoppingCartItemRepeatableSection> itemSectionList = shoppingCartPage.getShoppingCartItems();
        assertEquals(String.format("The shopping cart should have %s item.", expectedTotalItem), expectedTotalItem,
                     itemSectionList.size());
        ShoppingCartItemRepeatableSection item = shoppingCartPage.getFirstShoppingCartItem();
        assertTrue(String.format("The shopping cart item's sku should ends with '%s'. Actual sku: '%s'", productSku,
                                 item.getSku()),
                   item.getSku().endsWith(productSku));
        assertEquals(String.format("The shopping cart item's quantity should be '%s'", expectedQuantity),
                     String.valueOf(expectedQuantity), item.getQuantity());

        List<ShoppingCartHeaderRepeatableSection> headerSectionList = shoppingCartPage.getSuborderHeadersOnCart();       
        assertEquals(String.format("The shopping cart should have %s header.", expectedTotalItem), expectedTotalItem,
                     headerSectionList.size());
    }
    
    /**
     * Verifies whether a customer is able to add an item to registry from
     * the QuickLook Overlay surfaced from the Category page.
     * Test Case ID - RGSN-38179
     */
    @Test
    @TestRail(id = "99807")
    public void testAddToCartQuickLookButtonOverlayWhenUserAddsItemToRegistryExpectRegistryHasProduct() {

        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.REGISTRY_V2));
        final Registry registry = dataService.findRegistry("single-registry", "add-to-cart-button");
        final String email = registry.getRegistrant().getEmail();
        final String password = registry.getRegistrant().getPassword();
        final ProductGroup quicklookItem = dataService.findProductGroup(
                productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null, "simple-pip", "attributes", "add-to-cart-button");
        int expectedTotalItem;
        final String expectedQuantity = "1";

        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        CrossBrandRegistryListPage registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);
        expectedTotalItem = registryListPage.getTotalItemsFromDashboard() + 1;
        QuicklookOverlay quicklookOverlay = ShopFlows.goToAddToCartQuicklookButtonFlow(context, quicklookItem);
        quicklookOverlay.setProductAttributeSelections(quicklookItem);
        quicklookOverlay.setQuantity(expectedQuantity);
        RichAddToRegistryOverlaySection rar = quicklookOverlay.addToRegistryOneRegistryPresent();
        rar.waitForOverlayToDisplay();
        
        //then
        assertTrue("The product recommendation section is not displayed in the Rich Add To Registry Overlay", rar.isProductRecommendationsDisplayed());
        
        //when
        registryListPage = rar.viewRegistry();
        int actualTotalItem = registryListPage.getTotalItemsFromDashboard();
        
        //then
        assertEquals("Expected total items count \'" + expectedTotalItem + "\' does not match with actual total items count \'" + actualTotalItem, expectedTotalItem, actualTotalItem);
    }
    
    /**
     * Verify whether user is able to add a made to order item via add to cart button overlay.
     * Test Case ID - RGSN-38178
     */
    @Test
    @TestRail(id = "99810")
    public void testAddToCartButtonOverlayWhenUserAddsMadeToOrderItemToCartExpectItemAddedToCart() {

        //given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.MADE_TO_ORDER_IN_ADD_TO_CART_OVERLAY));
        final ProductGroup quicklookItem = dataService.findProductGroup(productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null,
                "simple-pip", "attributes","made-to-order", "add-to-cart-button");
        final int itemIndex = 0;
        final String expectedItemId = quicklookItem.getSkus().get(itemIndex);
        final int expectedTotalItem = 1;
        final String expectedQuantity = "1";

        //when
        QuicklookOverlay quicklookOverlay = ShopFlows.goToAddToCartQuicklookButtonFlow(context, quicklookItem);
        quicklookOverlay.setProductAttributeSelections(quicklookItem);
        RichAddToCartQuicklookOverlay rac = quicklookOverlay.addToCart();
        quicklookOverlay.checkForMadeToOrderOverlayAndAccept();
        ShoppingCartPage shoppingCartPage = rac.checkout();
        shoppingCartPage.scrollIntoCartSection();

        //then
        List<ShoppingCartItemRepeatableSection> itemSectionList = shoppingCartPage.getShoppingCartItems();
        assertEquals(String.format("The shopping cart should have %s item.", expectedTotalItem), expectedTotalItem,
                itemSectionList.size());
        ShoppingCartItemRepeatableSection item = shoppingCartPage.getFirstShoppingCartItem();
        assertTrue(String.format("The shopping cart item's sku should ends with '%s'. Actual sku: '%s'", expectedItemId,
                item.getSku()), item.getSku().endsWith(expectedItemId));
        assertEquals(String.format("The shopping cart item's quantity should be '%s'", expectedQuantity),
                String.valueOf(expectedQuantity), item.getQuantity());
        List<ShoppingCartHeaderRepeatableSection> headerSectionList = shoppingCartPage.getSuborderHeadersOnCart();
        assertEquals(String.format("The shopping cart should have %s header.", expectedTotalItem), expectedTotalItem,
                headerSectionList.size());
    }

    /**
     * Verifies whether product price is converted to selected international country's
     * currency in quick look overlay.
     * Test Case ID - RGSN-38182
     */
    @Test
    @TestRail(id = "105264")
    public void testQuicklookOverlayWhenUserSelectsInternationalCountryAtCategoryPageExpectCurrencyConversion() {

        // given
        final ProductGroup quicklookItem = dataService.findProductGroup("simple-pip", "attributes", "add-to-cart-button");
        String categoryUrl = quicklookItem.getUnmappedAttribute("categoryUrl");

        // when
        ShopFlows.navigateToCategoryPage(context, categoryUrl);
        NavigationSection navigationSection = context.getPageSection(NavigationSection.class);
        InternationalShippingOverlay internationalShippingOverlay = navigationSection.goToInternationalShippingOverlay();
        int countryOptionsAvailable = internationalShippingOverlay.getNonUsDollarInternationalShippingCountries();
        int countryNameIndex = RandomUtils.nextInt(1, countryOptionsAvailable);
        internationalShippingOverlay.selectNonUsDollarCurrencyInternationalCountry(countryNameIndex);
        String expectedCurrencyCode = internationalShippingOverlay.getCurrencyCodeFromInternationalShippingOverlay(countryNameIndex);
        internationalShippingOverlay.clickUpdateCurrencyAndCountryButton();
        CategoryPage categoryPage = context.getPage(CategoryPage.class);
        categoryPage.waitForCategoryPageLoad();
        QuicklookOverlay quicklookOverlay = ShopFlows.addToCartQuicklookButtonFlow(context, categoryPage, quicklookItem);
        String actualCurrencyCode = quicklookOverlay.getProductCurrencyCode();

        // then
        Assert.assertEquals("Expected currency code \'" + expectedCurrencyCode + "\'" + "is not matching with actual currency code \'"
                + actualCurrencyCode + "\'", expectedCurrencyCode, actualCurrencyCode);
    }
}
