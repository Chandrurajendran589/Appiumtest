package com.wsgc.ecommerce.ui.regression.favorites;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.search.syndication.SearchResultsUrlBuilder;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSectionFactory;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchResultsItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchResultsPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryPage;
import com.wsgc.evergreen.entity.api.ProductGroup;
import com.wsgc.evergreen.impl.DefaultEvergreenContext;
import java.net.URI;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.Cookie;
import tag.area.FavoritesArea;

/**
 * Regression Tests for validating persistence of item added to Favorites as Guest user.
 */
@Category(FavoritesArea.class)
public class AddFavoritesPersistenceGuestUserRegression extends AbstractTest {

    /**
     * Verifies whether a guest user is able to add items to favorites via the Search Results Page 
     * and items are persisted when browser is re-launched.
     * Test Case ID - RGSN-38019, RGSN-38020, RGSN-38230
     */
    @Test
    @TestRail(id = "98431")
    public void testAddToFavoritesWhenGuestUserViaSearchResultPageExpectFavoriteCountInHeader() {

        // given
        List<ProductGroup> productGroups = dataService.findProductGroups("favorites");
        SearchResultsPage searchResultsPage;
        SearchResultsItemRepeatableSection productInSearchResults;
        NavigationSection navigationSection;
        String productName;

        // when
        navigationSection = NavigationSectionFactory.getNavigationSection(context);
        for (ProductGroup productGroup : productGroups) {
             String searchCriteria = productGroup.getUnmappedAttribute("searchKeyword");
             searchResultsPage = context.getPage(SearchResultsPage.class);
             String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
             URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
             context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
             searchResultsPage.waitForSearchResultsHeading();
             productInSearchResults = searchResultsPage.getProductSectionByGroupId(productGroup.getGroupId());
             productName = productGroup.getName();
             assertTrue(String.format("The item was not added to favorites - %s", productName), 
                     productName.equalsIgnoreCase(productInSearchResults.favoriteProductOnSearchResults()));
        }

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
        searchResultsPage = context.getPage(SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();
        navigationSection = NavigationSectionFactory.getNavigationSection(context);
        assertEquals("The Favorites count in header does not match the products added to favorites", 
                productGroups.size(), navigationSection.getFavoriteCountFromGlobalHeader());
    }
    
    /**
     * Verifies whether a guest user is able to add items to favorites via the Shop Page 
     * and items are persisted when browser is re-launched.
     * Test Case ID - RGSN-38019, RGSN-38022, RGSN-38020
     */
    @Test
    @TestRail(id = "98434")
    public void testAddToFavoritesWhenGuestUserViaShopPageExpectFavoriteCountInHeader() {

        // given
        List<ProductGroup> productGroups = dataService.findProductGroups("favorites");
        CategoryPage productCategoryPage; 
        CategoryItemRepeatableSection productInShopList;

        // when
        for (ProductGroup productGroup : productGroups) {
             productCategoryPage = ShopFlows.navigateToCategoryPage(context, productGroup.getUnmappedAttribute("categoryUrl"));
             productInShopList = productCategoryPage.getProductSectionByGroupId(productGroup.getGroupId());
             assertTrue(String.format("The item was not added to favorites - %s", 
                     productGroup.getName()), productGroup.getName().equalsIgnoreCase(productInShopList.favoriteProductOnCategory()));
        }
        
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        
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
        productCategoryPage = context.getPage(CategoryPage.class);
        productCategoryPage.waitForCategoryPageLoad();
        navigationSection = NavigationSectionFactory.getNavigationSection(context);
        assertEquals("The Favorites count in header does not match the products added to favorites", 
                productGroups.size(), navigationSection.getFavoriteCountFromGlobalHeader());
    }
}
