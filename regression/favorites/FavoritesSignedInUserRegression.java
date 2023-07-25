package com.wsgc.ecommerce.ui.regression.favorites;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.account.AccountFlows;
import com.wsgc.ecommerce.ui.endtoend.favorites.SignInOverlayPageFlows;
import com.wsgc.ecommerce.ui.endtoend.search.syndication.SearchResultsUrlBuilder;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSectionFactory;
import com.wsgc.ecommerce.ui.pagemodel.account.LoginPage;
import com.wsgc.ecommerce.ui.pagemodel.account.SignInOverlayPageSection;
import com.wsgc.ecommerce.ui.pagemodel.favorites.FavoritesGalleryPage;
import com.wsgc.ecommerce.ui.pagemodel.favorites.FavoritesItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchResultsPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryPage;
import com.wsgc.evergreen.entity.api.CustomerAccount;
import com.wsgc.evergreen.entity.api.ProductGroup;
import com.wsgc.evergreen.impl.DefaultEvergreenContext;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tag.area.FavoritesArea;

/**
 * Regression Tests for Adding and Removing Items to Favorites as Signed In user.
 */
@Category(FavoritesArea.class)
public class FavoritesSignedInUserRegression extends AbstractTest {

    private static boolean setUpIsDone = false;

    private static int favoritesCount;

    LoginPage loginPage = context.getPage(LoginPage.class);

    CustomerAccount account = dataService.findAccount("favorites-count");
    
    /**
     * Prepares user data by getting the count of existing items added to favorites.
     */
    @Before
    public void setUp() {
        if (!setUpIsDone) {
            // when
            startPilotAt(loginPage);
            AccountFlows.signInWithAccount(context, account);
            NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
            FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
            startPilotAt(favoritesGalleryPage);
            favoritesGalleryPage.waitForFavoriteGallerySpinnerToDisappear();
            favoritesCount = navigationSection.getFavoriteCountFromGlobalHeader();
            //then
            context.getPilot().getDriver().close();
            context = DefaultEvergreenContext.getContext();
            DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();
            setUpIsDone = true;
        }
    }

    /**
     * Verifies whether a signed in user is able to see the same favorites count in Favorites Gallery and HomePage.
     * Test case ID - RGSN-38011.
     */
    @Test
    @TestRail(id = "98465")
    public void testFavoritesCountWhenSignedInUserHasItemsinFavoritesGalleryExpectFavoriteCountInHomePage() {

        //given
        startPilotAt(loginPage);
        AccountFlows.signInWithAccount(context, account);
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);

        // when
        navigationSection.goToHomePage();

        //then
        assertEquals("The Favorites count in Favorites Gallery does not match with the Favorites count in Home Page", favoritesCount, navigationSection.getFavoriteCountFromGlobalHeader());
    }

    /**
     * Verifies whether a signed in user is able to see the same favorites count in Favorites Gallery and Product Page.
     * Test case ID - RGSN-38011
     */
    @Test
    @TestRail(id = "98466")
    public void testFavoritesCountWhenSignedInUserHasItemsinFavoritesGalleryExpectFavoriteCountInProductPage() {

        // given
        final ProductGroup expectedProductGroup = dataService.findProductGroup("favorites");
        startPilotAt(loginPage);
        AccountFlows.signInWithAccount(context, account);

        // when
        ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        assertEquals("The Favorites count in Favorites Gallery does not match with the Favorites count in Product Page", favoritesCount, navigationSection.getFavoriteCountFromGlobalHeader());
    }

    /**
     * Verifies whether a signed in user is able to see the same favorites count in Favorites Gallery and Shopping Cart Page.
     * Test case ID - RGSN-38011
     */
    @Test
    @TestRail(id = "98467")
    public void testFavoritesCountWhenSignedInUserHasItemsinFavoritesGalleryExpectFavoriteCountInShoppingCartPage() {

        //given
        startPilotAt(loginPage);
        AccountFlows.signInWithAccount(context, account);
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);

        //when
        navigationSection.goToShoppingCartPage();

        //then
        assertEquals("The Favorites count in Favorites Gallery does not match with the Favorites count in Shopping Cart Page", favoritesCount, navigationSection.getFavoriteCountFromGlobalHeader());
    }

    /**
     * Verifies whether a signed in user is able to see the same favorites count in Favorites Gallery and Search Results Page.
     * Test case ID - RGSN-38011
     */
    @Test
    @TestRail(id = "98468")
    public void testFavoritesCountWhenSignedInUserHasItemsinFavoritesGalleryExpectFavoriteCountInSearchResultsPage() {

        // given
        final ProductGroup searchItem = dataService.findProductGroup("favorites");
        String searchCriteria = searchItem.getGroupId().replace("-", " ");
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);


        // when
        startPilotAt(loginPage);
        AccountFlows.signInWithAccount(context, account);
        SearchResultsPage searchResultsPage = navigationSection.search(searchCriteria);
        searchResultsPage.waitForSearchResultsHeading();
        assertEquals("The Favorites count in Favorites Gallery does not match with the Favorites count in Search Results Page", favoritesCount, navigationSection.getFavoriteCountFromGlobalHeader());
    }

    /**
     * Verifies whether a signed in user is able to see the same favorites count in Favorites Gallery and Shop Page.
     * Test case ID - RGSN-38011
     */
    @Test
    @TestRail(id = "98469")
    public void testFavoritesCountWhenSignedInUserHasItemsinFavoritesGalleryExpectFavoriteCountInShopPage() {

        // given
        final ProductGroup expectedProductGroup = dataService.findProductGroup(productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null);        
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);

        // when
        startPilotAt(loginPage);
        AccountFlows.signInWithAccount(context, account);
        ShopFlows.navigateToCategoryPage(context, expectedProductGroup.getUnmappedAttribute("categoryUrl"));
        assertEquals("The Favorites count in Favorites Gallery does not match with the Favorites count in Shop Page", favoritesCount, navigationSection.getFavoriteCountFromGlobalHeader());
    }
    
    /**
     * Verifies whether a user is able to sign in from Sign in flyout in Favorites Gallery.
     * Test case ID - RGSN-38014, RGSN-38678
     */
    @Test
    @TestRail(id = "98429")
    public void testFavoritesGalleryWhenCustomerSignsInExpectFavoritesGalleryWithItems() {

        // given
        String productName;
        CustomerAccount account = dataService.findAccount("favorites-available", "account-exist");
        List<Entry<String, String>> productNames = account.getUnmappedAttributes().entrySet().stream().filter(entry -> entry.getKey().contains("productName"))
                .sorted(Map.Entry.comparingByKey()).collect(Collectors.toList());
        List<Entry<String, String>> groupIds = account.getUnmappedAttributes().entrySet().stream().filter(entry -> entry.getKey().contains("groupId"))
                .sorted(Map.Entry.comparingByKey()).collect(Collectors.toList());
        Iterator<Entry<String, String>> productNamesIterator = productNames.iterator();

        // when
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        navigationSection.goToHomePage();
        FavoritesGalleryPage favoritesGalleryPage = navigationSection.goToFavoritesGallery();
        SignInOverlayPageFlows.signInOnFavoritesGallery(context, false, account);
        //then
        favoritesGalleryPage.waitForFavoriteGallerySpinnerToDisappear();
        assertTrue("Favorites Gallery is not populated with items.", favoritesGalleryPage.isFavoriteGalleryPopulatedWithItems());
        if (navigationSection.isFavoritesIconIntheHeaderDisplayed()) {
            assertEquals(String.valueOf(favoritesGalleryPage.getfavoriteCount()), String.valueOf(favoritesGalleryPage.getFavoriteCountFromGlobalHeader()));
        }
        assertEquals(String.valueOf(productNames.size()), String.valueOf(favoritesGalleryPage.getfavoriteCount()));
        
        for (Entry<String, String> groupId : groupIds) {
            productName = productNamesIterator.next().getValue();
            FavoritesItemRepeatableSection favoritesItemRepeatableSection = favoritesGalleryPage.getProductSectionByGroupId(groupId.getValue());
            if (favoritesGalleryPage.isGridViewToggleButtonPresent()) {
                favoritesGalleryPage.clickGridViewToggleButton();
            } 
            assertTrue(productName.equalsIgnoreCase(favoritesItemRepeatableSection.getProductName()));
        }
    }
    
    /**
     * Verifies whether a user is able to view an error message when they try to sign in using invalid email from 
     * Sign in flyout in Favorites Gallery.
     * Test case ID - RGSN-38014
     */
    @Test
    @TestRail(id = "98430")
    public void testFavoritesGalleryWhenUserLogsInFromSignInFlyOutWithInvalidEmailExpectErrorMessage() {

        // given
        CustomerAccount account = dataService.findAccount("favorites-count");
        String expectedErrorMessageText = "Please enter a valid email address.";
        String invalidEmail = "inV@l%dcom";

        // when
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);
        SignInOverlayPageSection signInOverlayPageSection = favoritesGalleryPage.goToSignInOverlay(true);
        SignInOverlayPageFlows.signInViaOverlay(signInOverlayPageSection, invalidEmail, account.getPassword());
        String actualErrorMessageText = signInOverlayPageSection.getSignInErrorMessageText();

        //then
        assertEquals(String.format("The Invalid Email Error Message is incorrect, Expected - %s, Actual - %s", expectedErrorMessageText,  actualErrorMessageText), expectedErrorMessageText, actualErrorMessageText);
    }
    
    /**
     * Verifies whether the Customer is able to Favorite items on the site and 
     * then logs out to see no item is retained in search results page.
     * Test case ID - RGSN-38150
     */
    @Test
    @TestRail(id = "98442")
    public void testFavoriteCountAsSignedInUserWhenUserLogoutFromSearchResultsPageExpectNoFavoriteItemRetained() {

        // given
        CustomerAccount account = dataService.findAccount("favorites-available");
        List<Entry<String, String>> productNames = account.getUnmappedAttributes().entrySet().stream().filter(entry -> entry.getKey().contains("productName"))
                .sorted(Map.Entry.comparingByKey()).collect(Collectors.toList());
        List<Entry<String, String>> groupIds = account.getUnmappedAttributes().entrySet().stream().filter(entry -> entry.getKey().contains("groupId"))
                .sorted(Map.Entry.comparingByKey()).collect(Collectors.toList());
        String searchCriteria = groupIds.get(0).toString().replace("-", " ");
        int favoriteCount = productNames.size();

        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        startPilotAt(loginPage);
        AccountFlows.signInWithAccount(context, account);

        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        favoriteCount = navigationSection.getFavoriteCountFromGlobalHeader();
        Assert.assertEquals("Favorite Count from global header is not matching", favoriteCount, navigationSection.getFavoriteCountFromGlobalHeader());
        navigationSection.openMyAccountDropdownByHovering();
        navigationSection.signOut();
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        //then
        if (navigationSection.isFavoritesIconIntheHeaderDisplayed()) {
        	assertEquals("Favorite Count is retained even after user signs out.", 0, navigationSection.getFavoriteCountFromGlobalHeader());
        }           
    }

    /**
     * Verifies whether the Customer is able to Favorite items on the site and 
     * then logs out to see no item is retained in Category page.
     * Test case ID - RGSN-38187
     */
    @Test
    @TestRail(id = "98432")
    public void testFavoriteCountAsSignedInUserWhenUserLogoutFromCategoryPageExpectNoFavoriteItemRetained() {

        //given
        CustomerAccount account = dataService.findAccount("favorites-available");
        final ProductGroup expectedProductGroup = dataService
    	          .findProductGroup(productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null,
    		                          "favorites");
        String categoryUrl = expectedProductGroup.getUnmappedAttribute("categoryUrl");
        List<Entry<String, String>> productNames = account.getUnmappedAttributes().entrySet().stream().filter(entry -> entry.getKey().contains("productName"))
    					.sorted(Map.Entry.comparingByKey()).collect(Collectors.toList());
        int favoriteCount = productNames.size();

        //when
        LoginPage loginPage = context.getPage(LoginPage.class);
        startPilotAt(loginPage);
        AccountFlows.signInWithAccount(context, account);
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, categoryUrl);   
        categoryPage.waitForCategoryPageLoad();
        favoriteCount = navigationSection.getFavoriteCountFromGlobalHeader();
        Assert.assertEquals("Favorite Count from global header is not matching", favoriteCount, navigationSection.getFavoriteCountFromGlobalHeader());
        navigationSection.openMyAccountDropdownByHovering();
        navigationSection.signOut();

        //then
        assertEquals("Favorite Count is retained even after user signs out.", 0, navigationSection.getFavoriteCountFromGlobalHeader());
    }
}
