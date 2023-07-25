package com.wsgc.ecommerce.ui.regression.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.account.AccountFlows;
import com.wsgc.ecommerce.ui.endtoend.registry.RegistryFlows;
import com.wsgc.ecommerce.ui.endtoend.search.SearchFlows;
import com.wsgc.ecommerce.ui.pagemodel.account.AccountHomePage;
import com.wsgc.ecommerce.ui.pagemodel.account.LoginPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.CrossBrandRegistryListPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.QuicklookOverlay;
import com.wsgc.ecommerce.ui.pagemodel.shop.RichAddToCartQuicklookOverlay;
import com.wsgc.ecommerce.ui.pagemodel.shop.RichAddToRegistryOverlaySection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartHeaderRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.ProductGroup;
import com.wsgc.evergreen.entity.api.Registry;
import java.util.List;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tag.area.SearchArea;

/**
 * Regression Tests for validating add to cart button in search pages.
 */
@Category(SearchArea.class)
public class AddToCartQuickLookRegression extends AbstractTest {

    /**
     * Prepares data fixtures & ensures feature flag is enabled.
     */
    @Before
    public void setUp() {
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.ADD_TO_CART_ON_SHOP_AND_SEARCH)
                && context.isFullSiteExperience());
    }
    
    /**
     * Verify if User is able to view alternate image on Quicklook Overlay. 
     * Test Case ID: RGSN-38039
     */
    @Test
    @TestRail(id = "98476")
    public void testAddToCartQuickLookButtonOverlayWhenUserOpensQuicklookExpectAlternateImageDisplayed() {
        // given
        final ProductGroup quicklookItem = dataService.findProductGroup("simple-pip", "attributes", "add-to-cart-button");

        // when
        QuicklookOverlay quicklookOverlay = SearchFlows.goToAddToCartQuickLookInSearchResultsFlow(context, quicklookItem);
        assertTrue("Alt image is not displayed", quicklookOverlay.isAlternateImagePresentAndDisplayed());
    }

    /**
     * Verifies if favorite confirmation pop up is displayed when favorite icon is
     * clicked on an item from add to cart button overlay.
     * Test Case ID - RGSN-38039
     */
    @Test
    @TestRail(id = "98462")
    public void testAddToCartQuickLookButtonOverlayWhenUserAddsItemToFavoritesExpectAddedToFavoritesPopup() {

        // given
        final ProductGroup quicklookItem = dataService.findProductGroup("simple-pip", "attributes", "add-to-cart-button");

        // when
        QuicklookOverlay quicklookOverlay = SearchFlows.goToAddToCartQuickLookInSearchResultsFlow(context, quicklookItem);

        //then
        assertTrue("Item was not added to favorites from Quicklook", quicklookItem.getName().equalsIgnoreCase(quicklookOverlay.favoriteProductOnQuickLookOverlay()));
    }
    
    /**
     * Verifies if user is redirected to the Product Page when view full details is clicked from add to cart button overlay.
     * Test Case ID - RGSN-38039
     */
    @Test
    @TestRail(id = "98473")
    public void testAddToCartQuickLookButtonOverlayWhenUserClicksViewFullDetailsExpectProductPage() {

        // given
        final ProductGroup quicklookItem = dataService.findProductGroup("simple-pip", "attributes", "add-to-cart-button");

        // when
        QuicklookOverlay quicklookOverlay = SearchFlows.goToAddToCartQuickLookInSearchResultsFlow(context, quicklookItem);
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
     * Verifies whether a customer is able to add an item to the cart from
     * the QuickLook Overlay surfaced from the search page.
     * Test Case ID - RGSN-38039
     */
    @Test
    @TestRail(id = "98474")
    public void testAddToCartQuickLookButtonOverlayWhenUserAddsItemToCartExpectCartHasProduct() {
    	
        // given
        final ProductGroup quicklookItem = dataService.findProductGroup("simple-pip", "attributes", 
                     "add-to-cart-button", "!made-to-order");
        final int itemIndex = 0;
        final int expectedTotalItem = 1;
        final String expectedQuantity = "3";
        final String productSku = quicklookItem.getSkus().get(itemIndex);

        // when
        QuicklookOverlay quicklookOverlay = SearchFlows.goToAddToCartQuickLookInSearchResultsFlow(context, quicklookItem);
        quicklookOverlay.setProductAttributeSelections(quicklookItem);
        quicklookOverlay.setQuantity(expectedQuantity);
        RichAddToCartQuicklookOverlay rac = quicklookOverlay.addToCart();

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
     * the QuickLook Overlay surfaced from the search page.
     * Test Case ID - RGSN-38039
     */
    @Test
    @TestRail(id = "98475")
    public void testAddToCartQuickLookButtonOverlayWhenUserAddsItemToRegistryExpectRegistryHasProduct() {

        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.REGISTRY_V2));
        final Registry registry = dataService.findRegistry("single-registry", "add-to-cart-button");
        final String email = registry.getRegistrant().getEmail();
        final String password = registry.getRegistrant().getPassword();
        final ProductGroup quicklookItem = dataService.findProductGroup("simple-pip", "attributes", "add-to-cart-button");
        int expectedTotalItem;
        final String expectedQuantity = "1";

        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        CrossBrandRegistryListPage registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);
        expectedTotalItem = registryListPage.getTotalItemsFromDashboard() + 1;
        QuicklookOverlay quicklookOverlay = SearchFlows.goToAddToCartQuickLookInSearchResultsFlow(context, quicklookItem);
        quicklookOverlay.setProductAttributeSelections(quicklookItem);
        quicklookOverlay.setQuantity(expectedQuantity);
        RichAddToRegistryOverlaySection rar = quicklookOverlay.addToRegistryOneRegistryPresent();
        
        //when
        registryListPage = rar.viewRegistry();
        int actualTotalItem = registryListPage.getTotalItemsFromDashboard();
        
        //then
        assertEquals("Expected total items count \'" + expectedTotalItem + "\' does not match with actual total items count \'" + actualTotalItem, expectedTotalItem, actualTotalItem);
    }
}
