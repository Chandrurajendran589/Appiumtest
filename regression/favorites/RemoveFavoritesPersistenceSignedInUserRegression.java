package com.wsgc.ecommerce.ui.regression.favorites;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.account.AccountFlows;
import com.wsgc.ecommerce.ui.endtoend.search.syndication.SearchResultsUrlBuilder;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSectionFactory;
import com.wsgc.ecommerce.ui.pagemodel.account.LoginPage;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchResultsItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchResultsPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryPage;
import com.wsgc.ecommerce.ui.regression.favorites.syndication.FavoritesApiServices;
import com.wsgc.evergreen.entity.api.CustomerAccount;
import com.wsgc.evergreen.impl.DefaultEvergreenContext;
import java.net.URI;
import java.util.Set;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.Cookie;
import tag.area.FavoritesArea;

/**
 * Regression Tests for validating persistence of item removed from Favorites as Signed in user.
 */
@Category(FavoritesArea.class)
public class RemoveFavoritesPersistenceSignedInUserRegression extends AbstractTest {
    
    /**
     * Verifies whether a signed in user is able to remove items from favorites via the Search Results Page 
     * and items are persisted when browser is re-launched.
     * Test Case ID - RGSN-38019, RGSN-38020
     */
    @Test
    @TestRail(id = "98447")
    public void testRemoveFromFavoritesWhenSignInUserViaSearchResultPageExpectFavoriteCountInHeader() {

        // given
        CustomerAccount account = dataService.findAccount("favorites-remove-persistance", "search-results");
        String groupId = account.getUnmappedAttribute("groupId");
        String productName = account.getUnmappedAttribute("productName");
        int favoritesCount;

        // when
        FavoritesApiServices.setUp(context);
        FavoritesApiServices.addItemToFavoritesList(account);
        context.getPilot().getDriver().close();
        context = DefaultEvergreenContext.getContext();
        DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signInWithAccount(context, account);
        String searchCriteria = groupId.replace("-", " ");
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        favoritesCount = navigationSection.getFavoriteCountFromGlobalHeader();
        SearchResultsItemRepeatableSection productInSearchResults = searchResultsPage.getProductSectionByGroupId(groupId);
        assertTrue(String.format("The item was not removed from favorites - %s", productName), 
                productName.equalsIgnoreCase(productInSearchResults.favoriteProductOnSearchResults()));
        favoritesCount = favoritesCount - 1;
        assertEquals("The Favorites count in header does not match after the product got removed from favorites", 
                favoritesCount, navigationSection.getFavoriteCountFromGlobalHeader());
        
        
        String searchPageUrl = context.getPilot().getDriver().getCurrentUrl();

        // Close Browser Session and revisit search result page
        Set<Cookie> siteCookies = context.getPilot().getDriver().manage().getCookies();
        context.getPilot().getDriver().close();
        context = DefaultEvergreenContext.getContext();
        DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();
        for (Cookie cookie : siteCookies) {
             context.getPilot().getDriver().manage().addCookie(cookie);
        }

        context.getPilot().getDriver().get(searchPageUrl);
        navigationSection = NavigationSectionFactory.getNavigationSection(context);
        searchResultsPage.waitForSearchResultsHeading();

        //then
        assertEquals("The Favorites count in header does not match after relaunching the browser", 
                favoritesCount, navigationSection.getFavoriteCountFromGlobalHeader());
    }
    
    /**
     * Verifies whether a signed in user is able to remove items from favorites via the Shop Page 
     * and items are persisted when browser is re-launched.
     * Test Case ID - RGSN-38019, RGSN-38023, RGSN-38020
     */
    @Test
    @TestRail(id = "98448")
    public void testRemoveFromFavoritesWhenSignedUserViaShopPageExpectFavoriteCountInHeader() {

        //given
        CustomerAccount account = dataService.findAccount("favorites-remove-persistance", "category-page");
        String groupId = account.getUnmappedAttribute("groupId");
        String productName = account.getUnmappedAttribute("productName");
        int favoritesCount;

        //when
        FavoritesApiServices.setUp(context);
        FavoritesApiServices.addItemToFavoritesList(account);
        context.getPilot().getDriver().close();
        context = DefaultEvergreenContext.getContext();
        DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signInWithAccount(context, account);
        CategoryPage productCategoryPage = ShopFlows.navigateToCategoryPage(context, account.getUnmappedAttribute("categoryUrl"));
        favoritesCount = navigationSection.getFavoriteCountFromGlobalHeader();
        CategoryItemRepeatableSection productInShopList = productCategoryPage.getProductSectionByGroupId(groupId);      
        assertTrue(String.format("The item was not removed from favorites - %s", productName), 
                productName.equalsIgnoreCase(productInShopList.favoriteProductOnCategory()));
        favoritesCount = favoritesCount - 1;
        assertEquals("The Favorites count in header does not match after the product got removed from favorites", 
                favoritesCount, navigationSection.getFavoriteCountFromGlobalHeader());


        String shopPageUrl = context.getPilot().getDriver().getCurrentUrl();

        // Close Browser Session and revisit search result page
        Set<Cookie> siteCookies = context.getPilot().getDriver().manage().getCookies();
        context.getPilot().getDriver().close();
        context = DefaultEvergreenContext.getContext();
        DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();
        for (Cookie cookie : siteCookies) {
             context.getPilot().getDriver().manage().addCookie(cookie);
        }

        context.getPilot().getDriver().get(shopPageUrl);
        navigationSection = NavigationSectionFactory.getNavigationSection(context);
        productCategoryPage.waitForCategoryPageLoad();
        
        //then
        assertEquals("The Favorites count in header does not match after relaunching the browser", 
                favoritesCount, navigationSection.getFavoriteCountFromGlobalHeader());
    }
}
