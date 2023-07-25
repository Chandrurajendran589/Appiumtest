package com.wsgc.ecommerce.ui.regression.favorites;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.HomePage;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSectionFactory;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchResultsItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchResultsPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryPage;
import com.wsgc.evergreen.entity.api.ProductGroup;
import com.wsgc.evergreen.impl.DefaultEvergreenContext;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.Cookie;
import tag.area.FavoritesArea;

/**
 * Regression Tests for validating persistence of item removed from Favorites as Guest user.
 */
@Category(FavoritesArea.class)
public class RemoveFavoritesPersistenceGuestUserRegression extends AbstractTest {
    
    private static Set<Cookie> siteCookies;
    
    private NavigationSection navigationSection;
    
    private String productName;
    
    private List<ProductGroup> productGroups = dataService.findProductGroups("favorites");
	
    /**
     * Prepares user data by getting adding items to favorites.
     */
    public void addItemsToFavorites() {
        //given
        String searchCriteria;
        SearchResultsPage searchResultsPage;
        SearchResultsItemRepeatableSection productInSearchResults;
        navigationSection = NavigationSectionFactory.getNavigationSection(context);
            
        // when
        for (ProductGroup productGroup : productGroups) {
            startPilotAt(context.getPage(HomePage.class));
            searchCriteria = productGroup.getUnmappedAttribute("searchKeyword");
            searchResultsPage = navigationSection.search(searchCriteria);
            searchResultsPage.waitForSearchResultsHeading();
            productInSearchResults = searchResultsPage.getProductSectionByGroupId(productGroup.getGroupId());
            productName = productGroup.getName();
            //then
            assertTrue(String.format("The item was not added to favorites - %s", productName), 
                    productName.equalsIgnoreCase(productInSearchResults.favoriteProductOnSearchResults()));
            }
    }

    /**
     * Verifies whether a guest user is able to remove items from favorites via the Search Results Page 
     * and items are persisted when browser is re-launched.
     * Test Case ID - RGSN-38019, RGSN-38020
     */
    @Test
    @TestRail(id = "98445")
    public void testRemoveFromFavoritesWhenGuestUserViaSearchResultPageExpectFavoriteCountInHeader() {

        // given
        ProductGroup productGroup = productGroups.get(1);
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);

        // when
        addItemsToFavorites();
        String searchCriteria = productGroup.getUnmappedAttribute("searchKeyword");
        SearchResultsPage searchResultsPage = navigationSection.search(searchCriteria);
        searchResultsPage.waitForSearchResultsHeading();
        int favoritesCount = navigationSection.getFavoriteCountFromGlobalHeader();
        SearchResultsItemRepeatableSection productInSearchResults = searchResultsPage.getProductSectionByGroupId(productGroup.getGroupId());
        productName = productGroup.getName();
        favoritesCount = favoritesCount - 1;
        assertTrue(String.format("The item was not removed from favorites - %s", productName), productName.equalsIgnoreCase(productInSearchResults.favoriteProductOnSearchResults()));
        assertEquals("The Favorites count in header does not match after the product got removed from favorites", favoritesCount, navigationSection.getFavoriteCountFromGlobalHeader());

        String searchPageUrl = context.getPilot().getDriver().getCurrentUrl();

        // Close Browser Session and revisit search result page
        siteCookies = context.getPilot().getDriver().manage().getCookies();
        context.getPilot().getDriver().close();
        context = DefaultEvergreenContext.getContext();
        DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();
        for (Cookie cookie : siteCookies) {
             context.getPilot().getDriver().manage().addCookie(cookie);
        }

        context.getPilot().getDriver().get(searchPageUrl);
        searchResultsPage = context.getPage(SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();
        navigationSection = NavigationSectionFactory.getNavigationSection(context);
        assertEquals("The Favorites count in header does not match after relaunching the browser", 
                favoritesCount, navigationSection.getFavoriteCountFromGlobalHeader());
    }
    
    /**
     * Verifies whether a guest user is able to remove items from favorites via the Shop Page 
     * and items are persisted when browser is re-launched.
     * Test Case ID - RGSN-38019, RGSN-38022, RGSN-38020
     */
    @Test
    @TestRail(id = "98446")
    public void testRemoveFromFavoritesWhenGuestUserViaShopPageExpectFavoriteCountInHeader() {

        // given
        ProductGroup productGroup = productGroups.get(1);
        CategoryPage productCategoryPage; 
        CategoryItemRepeatableSection productInShopList;

        // when
        addItemsToFavorites();
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        productCategoryPage = ShopFlows.navigateToCategoryPage(context, productGroup.getUnmappedAttribute("categoryUrl"));
        productCategoryPage.waitForCategoryPageLoad();
        productInShopList = productCategoryPage.getProductSectionByGroupId(productGroup.getGroupId());

        int favoritesCount = navigationSection.getFavoriteCountFromGlobalHeader();
        
        productName = productGroup.getName();
        favoritesCount = favoritesCount - 1;
        assertTrue(String.format("The item was not removed from favorites - %s", productName), 
                productName.equalsIgnoreCase(productInShopList.favoriteProductOnCategory()));
        assertEquals("The Favorites count in header does not match after the product got removed from favorites", 
                favoritesCount, navigationSection.getFavoriteCountFromGlobalHeader());
        
        String shopPageUrl = context.getPilot().getDriver().getCurrentUrl();

        // Close Browser Session and revisit search result page
        siteCookies = context.getPilot().getDriver().manage().getCookies();
        context.getPilot().getDriver().close();
        context = DefaultEvergreenContext.getContext();
        DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();
        for (Cookie cookie : siteCookies) {
             context.getPilot().getDriver().manage().addCookie(cookie);
        }

        context.getPilot().getDriver().get(shopPageUrl);
        productCategoryPage = context.getPage(CategoryPage.class);
        productCategoryPage.waitForCategoryPageLoad();
        navigationSection = NavigationSectionFactory.getNavigationSection(context);
        assertEquals("The Favorites count in header does not match after relaunching the browser", 
                favoritesCount, navigationSection.getFavoriteCountFromGlobalHeader());
    }
}
