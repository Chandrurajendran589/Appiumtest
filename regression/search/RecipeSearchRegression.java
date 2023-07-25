package com.wsgc.ecommerce.ui.regression.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.JavascriptExecutor;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.search.syndication.SearchResultsUrlBuilder;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.search.RecipeSearchResultsPageSection;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchResultsItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchResultsPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.RecipeGroup;

import tag.area.SearchArea;

/**
 * Regression Tests that validates recipe search in the site.
 */
@Category(SearchArea.class)
public class RecipeSearchRegression extends AbstractTest {

    /**
     * Ensures feature flag is enabled.
     */
    @Before
    public void setUp() {
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.RECIPE_PAGES));
    }

    /**
     * Verifies whether the user is able see the search results for products and recipe at search results and 
     * count displayed at the search results header is sum of products count and recipes count. 
     * Test Case ID - RGSN-38152
     */
    @Test
    @TestRail(id = "98470")
    public void testSearchResultsWhenUserPerformsKeywordSearchExpectSearchResultsForProductsAndRecipes() {

        // given
        final RecipeGroup recipeGroup = dataService.findRecipeGroup("recipe-search");
        final String searchCriteria = recipeGroup.getUnmappedAttribute("productAndRecipeKeyword");
        final String productTabName = "Products";
        final String recipeTabName = "Recipes";

        // when
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();

        int productsCount = searchResultsPage.getCountFromSearchResultsTab(productTabName);
        int recipesCount = searchResultsPage.getCountFromSearchResultsTab(recipeTabName);

        // then
        assertEquals("Search results count is not sum of products count and recipes count ", 
                searchResultsPage.getProductCountFromSearchResultsHeader() , productsCount + recipesCount);
        searchResultsPage.clickRecipesTab();
        assertTrue("Recipe tab is not selected ", searchResultsPage.getActiveTabText().toLowerCase().contains(recipeTabName.toLowerCase()));
   }

    /**
     * Verifies whether the user is able see only the products tab if the search
     * results has only products. 
     * Test Case ID - RGSN-38152
     */
    @Test
    @TestRail(id = "98471")
    public void testSearchResultsWhenUserPerformsProductOnlyKeywordSearchExpectSearchResultsForProducts() {

        // given
        final RecipeGroup recipeGroup = dataService.findRecipeGroup("recipe-search");
        final String searchCriteria = recipeGroup.getUnmappedAttribute("productOnlyKeyword");
        final String productTabName = "Products";

        // when
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();

        // then
        assertTrue("Products tab is not displayed ", searchResultsPage.getActiveTabText().toLowerCase().contains(productTabName.toLowerCase()));
        assertTrue("Recipes tab is displayed for products only search ", !searchResultsPage.isRecipesTabDisplayed());
    }

    /**
     * Verifies whether the user is able see only the recipe tab if the search
     * results has recipes only. 
     * Test Case ID - RGSN-38152
     */
    @Test
    @TestRail(id = "98472")
    public void testSearchResultsWhenUserPerformsRecipeOnlyKeywordSearchExpectSearchResultsForRecipes() {

        // given
        final RecipeGroup recipeGroup = dataService.findRecipeGroup("recipe-search");
        final String searchCriteria = recipeGroup.getUnmappedAttribute("recipeOnlyKeyword");
        final String productTabName = "Products";
        final String recipeTabName = "Recipes";

        // when
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();

        // then
        assertTrue("Recipe tab is not displayed ", searchResultsPage.getActiveTabText().toLowerCase().contains(recipeTabName.toLowerCase()));
        assertEquals("Products are displayed for recipe only search ", searchResultsPage.getCountFromSearchResultsTab(productTabName), 0);
	}
    
    /**
     * Verifies whether the user is able filter facets see in recipe search results page. 
     * Test Case ID - RGSN-38146
     */
    @Test
    @TestRail(id = "102658")
    public void testRecipesSearchResultsWhenAllProductsAreLoadedAfterFacetRefinementExpectProductCount() {

        // given
        final RecipeGroup recipeGroup = dataService.findRecipeGroup("recipe");
        final String searchCriteria = recipeGroup.getUnmappedAttribute("searchKeyword");
        final String recipeTabName = "Recipes";
        int facetGroup = 1;
        int facetValue = 1;

        // when
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();
        RecipeSearchResultsPageSection recipeSearchResultsPageSection = searchResultsPage.clickRecipesTab();
        String numberOfProductsForTheGivenFacetRefinement = recipeSearchResultsPageSection.
                getNumberOfProductsForGivenFacetRefinementInRecipePage(facetGroup, facetValue);
        recipeSearchResultsPageSection.resetFacetValueForSelection();
        recipeSearchResultsPageSection.clickFacetRefinement(facetGroup, facetValue);
        searchResultsPage.waitForSearchResultsHeading();
        int recipeCountFromHeader = searchResultsPage.getCountFromSearchResultsTab(recipeTabName);
        
        // then
        Assert.assertEquals("Recipe count from header '" + recipeCountFromHeader + "' is not matching with '" + numberOfProductsForTheGivenFacetRefinement 
                + "' the product count after selecting facet value ", recipeCountFromHeader, Integer.parseInt(numberOfProductsForTheGivenFacetRefinement));
    }
    
    /**
     * Verifies whether the user is able to see feedback link in recipe search results page. 
     * Test Case ID - RGSN-38146
     */
    @Test
    @TestRail(id = "102659")
    public void testFeedbackLinkInRecipesSearchResultsWhenUserClicksOnLinkExpectFeedbackPopupDisplayed() {

        // given
        Assume.assumeTrue(context.isFullSiteExperience());
        final RecipeGroup recipeGroup = dataService.findRecipeGroup("recipe");
        final String searchCriteria = recipeGroup.getUnmappedAttribute("searchKeyword");
        
        // when
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();
        RecipeSearchResultsPageSection recipeSearchResultsPageSection = searchResultsPage.clickRecipesTab();
        assertTrue("Feedback Link is not shown in recipe search result page", recipeSearchResultsPageSection.isFeedbackLinkPresentAndDisplayed());
        recipeSearchResultsPageSection.clickFeedbackLink();
        
        // then
        String originalHandle = context.getPilot().getDriver().getWindowHandle();
        for (String handle : context.getPilot().getDriver().getWindowHandles()) {
            if (!handle.equals(originalHandle)) {
                context.getPilot().getDriver().switchTo().window(handle);
                assertTrue("Feedback form is not displayed", context.getPageSection(NavigationSection.class).isFeedbackFormDisplayed());
                context.getPilot().getDriver().close();
            }
        }
        context.getPilot().getDriver().switchTo().window(originalHandle);
    }
    
    /**
     * Verifies whether the user is able to navigate to recipe page from recipe search results page. 
     * Test Case ID - RGSN-38146
     */
    @Test
    @TestRail(id = "102660")
    public void testRecipePageWhenUserSelectRecipeFromRecipeSearchPageExpectRecipePage() {

        // given
        final RecipeGroup recipeGroup = dataService.findRecipeGroup("recipe");
        final String searchCriteria = recipeGroup.getUnmappedAttribute("searchKeyword");

        // when
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();
        searchResultsPage.clickRecipesTab();
        SearchResultsItemRepeatableSection recipeSection = searchResultsPage.getRecipeSectionByGroupId(recipeGroup.getRecipeId());
        recipeSection.goToRecipePage(recipeGroup.getRecipeId());
        String currentPageUrl = context.getPilot().getDriver().getCurrentUrl();
        
        // then
        assertTrue("Recipe Page is not displayed", currentPageUrl.contains(recipeGroup.getRecipeId()));
    }
    
    /**
     * Verifies whether the user is able to see sticky sort and filter buttons while 
     * scrolling down in recipe search results page. 
     * Test Case ID - RGSN-38146
     */
    @Test
    @TestRail(id = "102661")
    public void testRecipesSearchResultsWhenUserScrollDownExpectStickySortAndFilterOptions() {

        // given
        Assume.assumeTrue(context.isMobileExperience());
        final RecipeGroup recipeGroup = dataService.findRecipeGroup("recipe");
        final String searchCriteria = recipeGroup.getUnmappedAttribute("searchKeyword");
        JavascriptExecutor jse = (JavascriptExecutor) context.getPilot().getDriver();
        
        // when
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();
        RecipeSearchResultsPageSection recipeSearchResultsPageSection = searchResultsPage.clickRecipesTab();
        searchResultsPage.waitForSearchResultsHeading();
        searchResultsPage.scrollToBottom(jse);
        
        // then
        assertTrue("Sticky Sort and Filter buttons is not displayed while scrolling down in recipe search page",
                recipeSearchResultsPageSection.isStickySortAndFilterButtonIsPresentAndDisplayed());
    }
}
