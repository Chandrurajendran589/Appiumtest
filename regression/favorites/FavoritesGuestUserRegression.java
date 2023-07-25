package com.wsgc.ecommerce.ui.regression.favorites;

import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Assume;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.account.AccountFlows;
import com.wsgc.ecommerce.ui.endtoend.search.syndication.SearchResultsUrlBuilder;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSectionFactory;
import com.wsgc.ecommerce.ui.pagemodel.account.AccountHomePage;
import com.wsgc.ecommerce.ui.pagemodel.account.LoginPage;
import com.wsgc.ecommerce.ui.pagemodel.account.PasswordRequestConfirmationPage;
import com.wsgc.ecommerce.ui.pagemodel.account.SignInOverlayPageSection;
import com.wsgc.ecommerce.ui.pagemodel.favorites.FavoritesGalleryPage;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchResultsItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchResultsPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.CustomerAccount;
import com.wsgc.evergreen.entity.api.MutableAccount;
import com.wsgc.evergreen.entity.api.ProductGroup;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import tag.area.FavoritesArea;

/**
 * Regression Tests for Adding and Removing Items to Favorites as Guest user.
 */
@Category(FavoritesArea.class)
public class FavoritesGuestUserRegression extends AbstractTest {
    
    /**
     * Verifies whether a guest user is able to navigate to forgot password page.
     * Test case ID - RGSN-38014, RGSN-38016
     */
    @Test
    @TestRail(id = "98457")
    public void testFavoritesGalleryWhenGuestUserClicksOnForgetPasswordFromSignInFlyOutExpectForgetPasswordPage() {

        // given
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);

        // when
        SignInOverlayPageSection signInOverlayPageSection = favoritesGalleryPage.goToSignInOverlay(true);
        PasswordRequestConfirmationPage passwordRequestConfirmationPage = signInOverlayPageSection.clickForgetPasswordOnOverlay();
        String currentPageURL = context.getPilot().getDriver().getCurrentUrl();
        String passwordRequestConfirmationPageUrl = String.format("%s?path=account", passwordRequestConfirmationPage.getExpectedUrlPath());
        //then
        assertTrue(String.format("The Forgot Password Page Url is incorrect, Expected - %s, Actual - %s", currentPageURL,  passwordRequestConfirmationPageUrl), currentPageURL.endsWith(passwordRequestConfirmationPageUrl));
    }

    /**
     * Verifies whether a guest user is able to navigate to Create Account page.
     * Test case ID - RGSN-38014
     */
    @Test
    @TestRail(id = "98459")
    public void testFavoritesGalleryWhenGuestUserClicksOnCreateAccountFromSignInFlyOutExpectCreateAccountPage() {

        // given
    	String expectedPageURL;
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);

        // when
        SignInOverlayPageSection signInOverlayPageSection = favoritesGalleryPage.goToSignInOverlay(true);
        LoginPage loginPage = signInOverlayPageSection.clickCreateAccountOnOverlay();
        String currentPageURL = context.getPilot().getDriver().getCurrentUrl();
        if (context.supportsFeature(FeatureFlag.SMART_LOGIN_ACCOUNT)) {
        	expectedPageURL = String.format("%s?target=%sfavorites", loginPage.getExpectedUrlPath(), "%2F");
        } else {
        	expectedPageURL = String.format("%s?target=%sfavorites", loginPage.getExpectedUrlPath(), "/");
        }
        
        //then
        assertTrue(String.format("The Create Account Page Url is incorrect, Expected - %s, Actual - %s", currentPageURL,  expectedPageURL), currentPageURL.endsWith(expectedPageURL));
    }
    
    /**
     * Verifies whether the favorites count in header gets updated when a guest user signs in after adding an item to favorites 
     * from search results pages.
     * Test case ID - RGSN-38150
     */
    @Test
    @TestRail(id = "98463")
    public void testAddToFavoritesFromSearchResultsPageWhenAGuestUserSignsInExpectFavoritesCountInHeader() {
	
        // given
        ProductGroup productGroup = dataService.findProductGroup("favorites");
        String searchCriteria = productGroup.getGroupId().replace("-", " ");
        MutableAccount mutableAccount = getMutableAccount("default");
        CustomerAccount newAccount = accountCreator.getRandomEmailAccount(mutableAccount);

        // when
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();
        SearchResultsItemRepeatableSection productInSearchResults = searchResultsPage.getProductSectionByGroupId(productGroup.getGroupId());
        assertTrue(String.format("The item was not added to favorites - %s", productGroup.getName()), productGroup.getName().equalsIgnoreCase(productInSearchResults.favoriteProductOnSearchResults()));

        // then
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        Assert.assertEquals("The Favorites count in header does not match the products added to favorites", 1, navigationSection.getFavoriteCountFromGlobalHeader());
        AccountFlows.goToLoginPageFlow(context);
        AccountFlows.createAccount(context, newAccount, AccountHomePage.class);
        Assert.assertEquals("The Favorites count is not updated in Search Results Page", 1, navigationSection.getFavoriteCountFromGlobalHeader());
    }
    
    /**
     * Verifies whether the favorites count in header gets updated when a guest user signs in after adding an item to favorites 
     * from Category page.
     * Test Case ID - RGSN-38187
     */
    @Test
    @TestRail(id = "98464")
    public void testAddToFavoritesFromCategoryPageWhenAGuestUserSignsInExpectFavoritesCountInHeader() {

        //given
        final ProductGroup productGroup = dataService.findProductGroup("favorites");
        MutableAccount mutableAccount = getMutableAccount("default");
        CustomerAccount newAccount = accountCreator.getRandomEmailAccount(mutableAccount);
        String categoryUrl = productGroup.getUnmappedAttribute("categoryUrl");

        //when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, categoryUrl);         
        CategoryItemRepeatableSection categoryItemRepeatableSection = categoryPage.getProductSectionByGroupId(productGroup.getGroupId());
        assertTrue(String.format("The item was not added to favorites - %s", productGroup.getName()), 
                productGroup.getName().equalsIgnoreCase(categoryItemRepeatableSection.favoriteProductOnCategory()));

        //then
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        Assert.assertEquals("The Favorites count in header does not match the products added to favorites", 1, navigationSection.getFavoriteCountFromGlobalHeader());
        AccountFlows.goToLoginPageFlow(context);
        AccountFlows.createAccount(context, newAccount, AccountHomePage.class);
        ShopFlows.navigateToCategoryPage(context, categoryUrl); 
        Assert.assertEquals("The Favorites count is not updated in Category Page", 1, navigationSection.getFavoriteCountFromGlobalHeader());
    }
    
    /**
     * Verifies whether user is able to navigate to home page via bread crumbs
     * in an empty favorites gallery page.
     * Test Case ID - RGSN-38016
     */
    @Test
    @TestRail(id = "102476")
    public void testEmptyFavoritesGalleryWhenUserClicksHomeInBreadcrumbsExpectHomePage() {

        //given
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);
        
        //when
        String expectedHomePageUrl = favoritesGalleryPage.getHomePageUrlInBreadcrumbs();
        favoritesGalleryPage.goToHomePageViaBreadcrumbs();
        String actualHomePageUrl = context.getPilot().getDriver().getCurrentUrl();

        //then
        Assert.assertTrue(String.format("Expected Home page url - %s does not match actual home page url  - %s", 
        		expectedHomePageUrl, actualHomePageUrl), actualHomePageUrl.contains(expectedHomePageUrl));
    }
    
    /**
     * Verifies whether user is able to view the Favorites items count 
     * in an empty favorites gallery page.
     * Test Case ID - RGSN-38016
     */
    @Test
    @TestRail(id = "102477")
    public void testFavoriteItemsLabelWhenUserNavigatesToEmptyFavoritesGalleryExpectFavoritesCount() {
    	
        Assume.assumeFalse(context.getTargetSite().getBrand().getCode().equalsIgnoreCase("pb"));

        //given
        final String expectedFavoritesCountTextTemplate = "0 Items";
        final String expectedFavoritesCountTemplate = "( 0 )";
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);
        
        //when
        String actualFavoritesCountText = favoritesGalleryPage.getFavoritesCountText();

        //then
        Assert.assertTrue(String.format("Expected Favorites count texts - %s or %s does not match actual favorites count text - %s", 
                expectedFavoritesCountTextTemplate, expectedFavoritesCountTemplate, actualFavoritesCountText), 
                actualFavoritesCountText.equalsIgnoreCase(expectedFavoritesCountTextTemplate) 
                || actualFavoritesCountText.equalsIgnoreCase(expectedFavoritesCountTemplate));
    }
    
    /**
     * Verifies whether user is able to view the description
     * in an empty favorites gallery page.
     * Test Case ID - RGSN-38016
     */
    @Test
    @TestRail(id = "102478")
    public void testFavoritesGalleryWhenUserNavigatesToEmptyFavoritesGalleryExpectEmptyGalleryDescription() {

        //given
        final HashMap<String, String> expectedEmptyGalleryTitleTemplates = new HashMap<>();
        expectedEmptyGalleryTitleTemplates.put("WS", "See it, love it, save it.");
        expectedEmptyGalleryTitleTemplates.put("PB", "Find Your Favorites");
        expectedEmptyGalleryTitleTemplates.put("PT", "Lots of room for the things you love.");
        expectedEmptyGalleryTitleTemplates.put("PK", "Lots of room for the things you love.");
        expectedEmptyGalleryTitleTemplates.put("WE", "Lots of room for the things you love.");
        expectedEmptyGalleryTitleTemplates.put("MG", "Find Your Favorites");
        
        final HashMap<String, String> expectedEmptyGalleryDescriptionTemplates = new HashMap<>();
        expectedEmptyGalleryDescriptionTemplates.put("WS", "Keep track of your favorite items and "
                + "inspiration by selecting the heart icon.");
        expectedEmptyGalleryDescriptionTemplates.put("PB", "See something you like? Keep track of "
                + "your favorite items and inspiration by selecting the heart icon.");
        expectedEmptyGalleryDescriptionTemplates.put("PT", "Explore our products and add your favorites anywhere you see a heart.");
        expectedEmptyGalleryDescriptionTemplates.put("PK", "Explore our products and add your favorites anywhere you see a heart.");
        expectedEmptyGalleryDescriptionTemplates.put("WE", "Explore our products and add your favorites anywhere you see a heart.");
        expectedEmptyGalleryDescriptionTemplates.put("MG", "See it, love it, save it. Keep track of "
                + "your favorite items and inspiration by selecting the heart icon.");
        
        //when
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);
        String expectedEmptyGalleryTitle = expectedEmptyGalleryTitleTemplates.get(context.getBrand().getCode()).toLowerCase();
        String expectedEmptyGalleryDescription = expectedEmptyGalleryDescriptionTemplates.get(context.getBrand().getCode()).toLowerCase();
        String actualEmptyGalleryTitle = favoritesGalleryPage.getEmptyFavoritesGalleryTitle().toLowerCase();
        String actualEmptyGalleryDescription = favoritesGalleryPage.getEmptyFavoritesGalleryDescription().toLowerCase();
        if (context.getTargetEnvironment().getName().contains("-")) {
        	expectedEmptyGalleryTitle = expectedEmptyGalleryTitle.replaceAll("favorite", "favourite");
        	expectedEmptyGalleryDescription = expectedEmptyGalleryDescription.replaceAll("favorite", "favourite");
        }

        //then
        Assert.assertEquals(String.format("Expected empty gallery title - %s does not match actual empty gallery title  - %s", 
        		expectedEmptyGalleryTitle, actualEmptyGalleryTitle), expectedEmptyGalleryTitle, actualEmptyGalleryTitle);
        Assert.assertEquals(String.format("Expected empty gallery description - %s does not match actual empty gallery description  - %s", 
        		expectedEmptyGalleryDescription, actualEmptyGalleryDescription), expectedEmptyGalleryDescription, actualEmptyGalleryDescription);
    }
    
    /**
     * Verifies whether user is able to view the start shopping button
     * in an empty favorites gallery page.
     * Test Case ID - RGSN-38016
     */
    @Test
    @TestRail(id = "102479")
    public void testEmptyFavoritesGalleryWhenUserClicksStartShoppingExpectHomePage() {
    	
        Assume.assumeFalse(context.getTargetSite().getBrand().getCode().equalsIgnoreCase("pb"));

        //given
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);

        //when
        String expectedHomePageUrl = favoritesGalleryPage.getUrlInStartShoppingButton();
        favoritesGalleryPage.clickStartShopping();
        String actualHomePageUrl = context.getPilot().getDriver().getCurrentUrl();

        //then
        Assert.assertTrue(String.format("Expected Home page url - %s does not match actual home page url  - %s", 
        		expectedHomePageUrl, actualHomePageUrl), actualHomePageUrl.contains(expectedHomePageUrl));
    }
}
