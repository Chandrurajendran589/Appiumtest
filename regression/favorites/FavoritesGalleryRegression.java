package com.wsgc.ecommerce.ui.regression.favorites;

import java.net.URI;
import java.util.List;

import com.wsgc.ecommerce.ui.endtoend.shop.PipFlows;
import com.wsgc.ecommerce.ui.pagemodel.shop.RichAddToCartOverlaySection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartPage;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assume;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.account.AccountFlows;
import com.wsgc.ecommerce.ui.endtoend.favorites.SignInOverlayPageFlows;
import com.wsgc.ecommerce.ui.endtoend.search.syndication.SearchResultsUrlBuilder;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSectionFactory;
import com.wsgc.ecommerce.ui.pagemodel.account.AccountHomePage;
import com.wsgc.ecommerce.ui.pagemodel.account.LoginPage;
import com.wsgc.ecommerce.ui.pagemodel.designservice.DesignServiceAppointmentsPage;
import com.wsgc.ecommerce.ui.pagemodel.favorites.FavoritesGalleryPage;
import com.wsgc.ecommerce.ui.pagemodel.favorites.FavoritesItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchResultsItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchResultsPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryPage;
import com.wsgc.ecommerce.ui.regression.favorites.syndication.FavoritesApiServices;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.CustomerAccount;
import com.wsgc.evergreen.entity.api.MutableAccount;
import com.wsgc.evergreen.entity.api.ProductGroup;
import com.wsgc.evergreen.impl.DefaultEvergreenContext;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import tag.area.FavoritesArea;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Regression Tests for Validating the elements on Favorites gallery page.
 * 
 */
@Category(FavoritesArea.class)
public class FavoritesGalleryRegression extends AbstractTest {

    /**
     * Verifies whether Design Crew link is redirected correctly from favorites gallery page for a guest user.
     * Test Case ID - RGSN-38017, RGSN-38018, RGSN-38678
     */
    @Test
    @TestRail(id = "98439")
    public void testFavoritesGalleryWhenGuestUserClicksDesignCrewLinkExpectDesignCrewAppointmentPage() {

        Assume.assumeTrue(context.supportsFeature(FeatureFlag.DESIGN_CREW_APPOINTMENTS));

        // given
        final ProductGroup expectedProductGroup = dataService.findProductGroup("favorites");

        // when
        CategoryPage productCategoryPage = ShopFlows.navigateToCategoryPage(context, expectedProductGroup.getUnmappedAttribute("categoryUrl"));
        productCategoryPage.waitForCategoryPageLoad();
        CategoryItemRepeatableSection productInShopList = productCategoryPage.getProductSectionByGroupId(expectedProductGroup.getGroupId());
        assertTrue(expectedProductGroup.getName().equalsIgnoreCase(productInShopList.favoriteProductOnCategory()));
        FavoritesGalleryPage favoriteGalleryPage = context.getPage(FavoritesGalleryPage.class);
        context.getPilot().startAt(favoriteGalleryPage);
        DesignServiceAppointmentsPage designServiceAppointmentsPage = favoriteGalleryPage.goToDesignServiceAppointmentsPage();
        
        //then
        assertTrue("Design Services Appointments Page header is not displayed", designServiceAppointmentsPage.isDesignServiceAppointmentsHeaderDisplayed());
    }

    /**
     * Verifies whether Design Crew link is redirected correctly from favorites gallery page for a signed in user.
     * Test Case ID - RGSN-38017, RGSN-38018, RGSN-38678
     */
    @Test
    @TestRail(id = "98452")
    public void testFavoritesGalleryWhenSignedInUserClicksDesignCrewLinkExpectDesignCrewAppointmentPage() {

        Assume.assumeTrue(context.supportsFeature(FeatureFlag.DESIGN_CREW_APPOINTMENTS));

        // given
        final ProductGroup expectedProductGroup = dataService.findProductGroup("favorites");
        MutableAccount mutableAccount = getMutableAccount("default");
        CustomerAccount account = accountCreator.getRandomEmailAccount(mutableAccount);
        
        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.createAccount(context, account, AccountHomePage.class);
        CategoryPage productCategoryPage = ShopFlows.navigateToCategoryPage(context, expectedProductGroup.getUnmappedAttribute("categoryUrl"));
        productCategoryPage.waitForCategoryPageLoad();
        CategoryItemRepeatableSection productInShopList = productCategoryPage.getProductSectionByGroupId(expectedProductGroup.getGroupId());
        assertTrue(expectedProductGroup.getName().equalsIgnoreCase(productInShopList.favoriteProductOnCategory()));
        FavoritesGalleryPage favoriteGalleryPage = context.getPage(FavoritesGalleryPage.class);
        context.getPilot().startAt(favoriteGalleryPage);
        DesignServiceAppointmentsPage designServiceAppointmentsPage = favoriteGalleryPage.goToDesignServiceAppointmentsPage();

        //then
        assertTrue("Design Services Appointments Page header is not displayed", designServiceAppointmentsPage.isDesignServiceAppointmentsHeaderDisplayed());
    }

    /**
     * Verifies whether Room Planner link is redirected correctly from favorites gallery page for a guest user.
     * Test Case ID - RGSN-38017, RGSN-38018, RGSN-38678
     */	
    @Test
    @TestRail(id = "98453")
    public void testFavoritesGalleryWhenGuestUserClickRoomPlannerLinkExpectRoomPlannerPage() {

        Assume.assumeTrue(context.supportsFeature(FeatureFlag.ROOM_PLANNER) && context.isFullSiteExperience());

        //given
        final ProductGroup searchItem = dataService.findProductGroup("favorites");
        final String EXPECTED_ROOM_PLANNER_PAGE_URL = "https://designcrew-roomplanner";
       
        // when
        String searchCriteria = searchItem.getGroupId().replace("-", " ");
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();
        SearchResultsItemRepeatableSection productInSearchResults = searchResultsPage.getProductSectionByGroupId(searchItem.getGroupId());
        assertTrue(searchItem.getName().equalsIgnoreCase(productInSearchResults.favoriteProductOnSearchResults()));
        FavoritesGalleryPage favoriteGalleryPage = context.getPage(FavoritesGalleryPage.class);
        context.getPilot().startAt(favoriteGalleryPage);
        favoriteGalleryPage.goToRoomPlannerPage();
        String actualRoomPlannerPageUrl = context.getPilot().getDriver().getCurrentUrl();

        //then
        assertTrue("User should be navigated to Room Planner Page", actualRoomPlannerPageUrl.startsWith(EXPECTED_ROOM_PLANNER_PAGE_URL));
    }
	
    /**
     * Verifies whether Room Planner link is redirected correctly from favorites gallery page for a signed in user.
     * Test Case ID - RGSN-38017, RGSN-38018, RGSN-38678
     */	
    @Test
    @TestRail(id = "98454")
    public void testFavoritesGalleryWhenSignedInUserClickRoomPlannerLinkExpectRoomPlannerPage() {

        Assume.assumeTrue(context.supportsFeature(FeatureFlag.ROOM_PLANNER) && context.isFullSiteExperience());

        //given
        final ProductGroup searchItem = dataService.findProductGroup("favorites");
        final String EXPECTED_ROOM_PLANNER_PAGE_URL = "https://designcrew-roomplanner";
        MutableAccount mutableAccount = getMutableAccount("default");
        CustomerAccount account = accountCreator.getRandomEmailAccount(mutableAccount);

        // when
        AccountFlows.goToLoginPageFlow(context);
        AccountFlows.createAccount(context, account, AccountHomePage.class);
        String searchCriteria = searchItem.getGroupId().replace("-", " ");
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();
        SearchResultsItemRepeatableSection productInSearchResults = searchResultsPage.getProductSectionByGroupId(searchItem.getGroupId());
        assertTrue(searchItem.getName().equalsIgnoreCase(productInSearchResults.favoriteProductOnSearchResults()));
        FavoritesGalleryPage favoriteGalleryPage = context.getPage(FavoritesGalleryPage.class);
        context.getPilot().startAt(favoriteGalleryPage);
        favoriteGalleryPage.goToRoomPlannerPage();
        String actualRoomPlannerPageUrl = context.getPilot().getDriver().getCurrentUrl();

        //then
        assertTrue("User should be navigated to Room Planner Page", actualRoomPlannerPageUrl.startsWith(EXPECTED_ROOM_PLANNER_PAGE_URL));
    }

    /**
     * Verifies whether the Design chat banner is displaying on Favorite gallery.
     * Test Case ID - RGSN-38025
     */
    @Test
    @TestRail(id = "98455")
    public void testFavoritesGalleryWhenUserClicksOnDesignChatIconExpectDesignChatWindow() {
        //when
        Assume.assumeTrue(context.supportsFeature(FeatureFlag.DESIGN_CHAT));
        FavoritesGalleryPage favoriteGalleryPage = context.getPage(FavoritesGalleryPage.class);
        context.getPilot().startAt(favoriteGalleryPage);
        favoriteGalleryPage.waitForFavoriteGallerySpinnerToDisappear();

        //then
        assertTrue("The Design chat icon is not displayed", favoriteGalleryPage.isDesignChatIconDisplayed());
        favoriteGalleryPage.clickOnDesignChatIcon();	
        assertTrue("The Design chat window is opened", favoriteGalleryPage.isDesignChatOpened());
        favoriteGalleryPage.clickOnDesignChatCloseIcon();		 
    }

    /**
     * Verifies whether the scroll up icon is displayed and the page is getting scrolled 
     * up when clicking the scroll up icon on Favorite gallery.
     * Test Case ID - RGSN-38025
     */
    @Test
    @TestRail(id = "98456")
    public void testFavoritesGalleryWhenScrollUpIconIsClickedExpectScrollToHeader() {
        //given
        Assume.assumeTrue(context.isFullSiteExperience());

        //when
        FavoritesGalleryPage favoriteGalleryPage = context.getPage(FavoritesGalleryPage.class);
        context.getPilot().startAt(favoriteGalleryPage);

        //then
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        navigationSection.scrollToFooter();
        assertTrue("The Scroll Up button is displayed", favoriteGalleryPage.isScrollUpIconDisplayed());
        favoriteGalleryPage.clickOnScrollUpIcon();
        assertFalse("The Scroll Up button is disappeared", favoriteGalleryPage.isScrollUpIconDisplayed());
        assertTrue("The Header section is displayed", navigationSection.isHeaderSectionDisplayed());
    }
    
    /**
     * Verifies if user is able to navigate to the product page on clicking a 
     * recently viewed product from favorites gallery.
     * Test Case ID - RGSN-38013
     */
    @Test
    @TestRail(id = "102475")
    public void testFavoritesGalleryWhenUserClicksRecentlyViewedProductExpectProductPage() {

        // given
        List<ProductGroup> expectedProductGroups = dataService.findProductGroups("recently-viewed");
        final String EXPECTED_FAVORITES_RECSTART_PARAM = "cm_src=WsiFavoritesRvi";
        final ProductGroup product = dataService.findProductGroup("simple-pip", "attributes");

        // when
        for (ProductGroup productGroup : expectedProductGroups) {
            ShopFlows.goToPipPageFlow(context, productGroup);
        }
        
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, product);
        productPage.favoriteProductOnPip();
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        context.getPilot().startAt(favoritesGalleryPage);
        favoritesGalleryPage.waitForFavoriteGallerySpinnerToDisappear();

        //then
        int recentlyViewedItemsAvailable = favoritesGalleryPage.getNumberOfRecentlyViewedItemsAvailable();
        int recentlyViewedItemIndex = RandomUtils.nextInt(0, recentlyViewedItemsAvailable - 1);
        assertTrue("Product Image is not displayed in recently viewed carousel", 
                favoritesGalleryPage.isRecentlyViewedProductImageDisplayed(recentlyViewedItemIndex));
        assertTrue("Product Name is not displayed in recently viewed carousel", 
                favoritesGalleryPage.isRecentlyViewedProductNameDisplayed(recentlyViewedItemIndex));
        assertTrue("Product Price is not displayed in recently viewed carousel", 
                favoritesGalleryPage.isRecentlyViewedProductPriceDisplayed(recentlyViewedItemIndex));
        productPage = favoritesGalleryPage.clickRecentlyViewedItem(recentlyViewedItemIndex);
        String productPageUrl = productPage.getCurrentPageUrl();
        assertTrue(String.format("Product page url - %s - doesn't contain RVI query parameter, %s",
                productPageUrl, EXPECTED_FAVORITES_RECSTART_PARAM), 
                productPageUrl.contains(EXPECTED_FAVORITES_RECSTART_PARAM));       
    }
    
    /**
     * Verifies whether a guest user is able to remove items from favorites from favorites gallery.
     * Test Case ID - RGSN-38679
     */
    @Test
    public void testFavoritesGalleryWhenGuestUserRemovedAnItemFromFavoritesExpectEmptyFavoritesGallery() {

        // given
        final ProductGroup productGroup = dataService.findProductGroup("favorites");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        productPage.favoriteProductOnPip();
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);
        assertTrue("Favorites Gallery is not populated with items.", favoritesGalleryPage.isFavoriteGalleryPopulatedWithItems());
        FavoritesItemRepeatableSection favoritesItemRepeatableSection = favoritesGalleryPage
                .getProductSectionByGroupId(productGroup.getGroupId());
        favoritesItemRepeatableSection.clickFavoriteIcon();

        // then
        assertTrue("Favorites Gallery is not empty", favoritesGalleryPage.isGalleryEmpty());

    }

    /**
     * Verifies whether a signed in user is able to remove items from favorites from favorites gallery.
     * Test Case ID - RGSN-38679
     */
    @Test
    public void testFavoritesGalleryWhenSignedInUserRemovedAnItemFromFavoritesExpectEmptyFavoritesGallery() {

        // given
        CustomerAccount account = dataService.findAccount("unfavorite-from-gallery");
        final String groupId = account.getUnmappedAttribute("groupId");

        // when
        FavoritesApiServices.setUp(context);
        FavoritesApiServices.deleteFavoritesList(account);
        context.getPilot().getDriver().close();
        context = DefaultEvergreenContext.getContext();
        DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();
        FavoritesApiServices.createFavoritesList(account);
        context.getPilot().getDriver().close();
        context = DefaultEvergreenContext.getContext();
        DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);
        SignInOverlayPageFlows.signInOnFavoritesGallery(context, false, account);
        assertTrue("Favorites Gallery is not populated with items.", favoritesGalleryPage.isFavoriteGalleryPopulatedWithItems());
        FavoritesItemRepeatableSection favoritesItemRepeatableSection = favoritesGalleryPage
                .getProductSectionByGroupId(groupId);
        favoritesItemRepeatableSection.clickFavoriteIcon();

        // then
        assertTrue("Favorites Gallery is not empty", favoritesGalleryPage.isGalleryEmpty());

    }

    /**
     * Verifies whether a guest user is able to navigate to look book page from favorites gallery.
     * Test Case ID - RGSN-38678
     */
    @Test
    public void testFavoritesGalleryWhenGuestUserClicksShopLookbookExpectLookBookPage() {

        Assume.assumeTrue(context.supportsFeature(FeatureFlag.SPRING_LOOK_BOOK));

        // given
        final ProductGroup productGroup = dataService.findProductGroup("favorites");
        final String LOOKBOOK_URI_TEMPLATE = "/pages/lookbook/";

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        productPage.favoriteProductOnPip();
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);
        assertTrue("Favorites Gallery is not populated with items.", favoritesGalleryPage.isFavoriteGalleryPopulatedWithItems());
        favoritesGalleryPage.goToLookbookPage();
        String currentUrl = context.getPilot().getDriver().getCurrentUrl();

        // then
        assertTrue("Spring Lookbook Page is not displayed", currentUrl.contains(LOOKBOOK_URI_TEMPLATE));
    }

    /**
     * Verifies if user clicks on View Details on favorite product from favorites gallery is navigated to corresponding product
     * page.
     * Test Case ID - RGSN-38773
     */
    @Test
    @TestRail(id = "104542")
    public void testFavoritesGalleryWhenGuestUserClicksViewDetailsExpectProductPage() {

        // given
        final ProductGroup expectedProductGroup = dataService.findProductGroup("simple-pip", "attributes");
        String expectedSku = expectedProductGroup.getSkus().toString().replaceAll("\\[", "");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        productPage.favoriteProductOnPip();

        // then
        assertTrue("The Product is not added to the favorites", productPage.isItemAddedToFavorites());

        // when
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);
        favoritesGalleryPage.isFavoriteGalleryPopulatedWithItems();
        FavoritesItemRepeatableSection favoritesItemRepeatableSection = favoritesGalleryPage.getProductSectionByGroupId(expectedProductGroup.getGroupId());
        favoritesItemRepeatableSection.viewDetailsButtonLocator();

        // then
        assertTrue("User is not navigated to correct product page", context.getPilot().getDriver().getCurrentUrl().contains(expectedProductGroup.getGroupId()));
    }

    /**
     * Verifies if favorite product added to cart from favorites gallery is displayed on shopping cart page.
     * Test Case ID - RGSN-38773
     */
    @Test
    @TestRail(id = "104541")
    public void testFavoritesGalleryWhenGuestUserClicksAddToCartExpectItemAddedToCart() {

        // given
        final int expectedTotalItem = 1;
        final int expectedQuantity = 1;
        final ProductGroup expectedProductGroup = dataService.findProductGroup("simple-pip", "!attributes");
        String expectedSku = expectedProductGroup.getSkus().toString().replaceAll("\\[", "");
        expectedSku = expectedSku.replaceAll("\\]", "");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        productPage.favoriteProductOnPip();

        // then
        assertTrue("The Product is not added to the favorites", productPage.isItemAddedToFavorites());

        // when
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);
        favoritesGalleryPage.isFavoriteGalleryPopulatedWithItems();
        FavoritesItemRepeatableSection favoritesItemRepeatableSection = favoritesGalleryPage.getProductSectionByGroupId(expectedProductGroup.getGroupId());
        RichAddToCartOverlaySection richAddToCartOverlaySection = favoritesItemRepeatableSection.clickAddToCart();

        // then
        if (context.isFullSiteExperience()) {
            assertTrue("The Checkout button is not displayed on RAC overlay", richAddToCartOverlaySection.isCheckoutButtonDisplayed());
            assertTrue("The Continue Shopping button is not displayed on RAC overlay", richAddToCartOverlaySection.isContinueShoppingButtonDisplayed());
        }

        // when
        ShoppingCartPage shoppingCartPage = richAddToCartOverlaySection.goToShoppingCartPage();
        shoppingCartPage.scrollIntoCartSection();
        List<ShoppingCartItemRepeatableSection> itemSectionList = shoppingCartPage.getShoppingCartItems();

        // then
        assertEquals(String.format("The shopping cart should have %s item.", expectedTotalItem), expectedTotalItem,
                itemSectionList.size());

        // when
        ShoppingCartItemRepeatableSection item = shoppingCartPage.getFirstShoppingCartItem();

        // then
        assertTrue(String.format("The shopping cart item's sku should ends with '%s'. Actual sku: '%s'", expectedSku,
                item.getSku()), item.getSku().endsWith(expectedSku));
        assertEquals(String.format("The shopping cart item's quantity should be '%s'", expectedQuantity),
                String.valueOf(expectedQuantity), item.getQuantity());
    }
}
