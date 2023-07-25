package com.wsgc.ecommerce.ui.regression.search;

import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.search.syndication.SearchResultsUrlBuilder;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchResultsPage;
import com.wsgc.evergreen.entity.api.ProductGroup;

import tag.area.SearchArea;

/**
 * Regression Tests for Facets on search page.
 */
@Category(SearchArea.class)
public class FacetsOnSearchRegression extends AbstractTest {

    /**
     * Verifies whether the product count is displayed and remains unchanged after all products are
     * loaded at search results page. 
     * Test Case ID - RGSN-38166
     */
    @Test
    @TestRail(id = "98478")
    public void testSearchResultsWhenAllProductsAreLoadedExpectProductCountAtSearchResultsHeaderUnchanged() {
        // given
        final ProductGroup searchItem = dataService.findProductGroup(
				productGroup -> productGroup.getUnmappedAttribute("searchKeyword") != null, "simple-pip", "!attributes",
				"search-results");
        // when
        String searchCriteria = searchItem.getUnmappedAttribute("searchKeyword");
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();

        int productCountFromHeaderBeforeScrolling = searchResultsPage.getProductCountFromSearchResultsHeader();
        int numberOfProductsAfterScrolling = searchResultsPage.getNumberOfProductsLoaded();
        
        int productCountFromHeaderAfterScrolling = searchResultsPage.getProductCountFromSearchResultsHeader();

        Assert.assertEquals("Incorrect number of products displayed at search results header ",
                numberOfProductsAfterScrolling, productCountFromHeaderBeforeScrolling); 
        
        Assert.assertEquals("Incorrect number of products displayed at search results header after scrolling to bottom",
                productCountFromHeaderAfterScrolling, productCountFromHeaderBeforeScrolling); 
	}

    /**
     * Verifies whether the search results product count is displayed and updated when user applies
     * facets refinement. 
     * Test Case ID - RGSN-38166
     */
    @Test
    @TestRail(id = "98479")
    public void testSearchResultsWhenAllProductsAreLoadedAfterFacetRefinementExpectProductCountAtSearchResultsHeaderUpdated() {
        // given
        final ProductGroup searchItem = dataService.findProductGroup(
				productGroup -> productGroup.getUnmappedAttribute("searchKeyword") != null, "simple-pip", "!attributes",
				"search-results");

        int facetGroup = 2;
        int facetValue = 1;

        // when
        String searchCriteria = searchItem.getUnmappedAttribute("searchKeyword");
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();

        int numberOfProductsForTheGivenFacetRefinement = searchResultsPage.getNumberOfProductsForGivenFacetRefinement(facetGroup, facetValue);
        searchResultsPage.clickFacetRefinement(facetGroup, facetValue);
        /*
        We are refreshing the page after facets selection, to avoid race condition errors.
         */
        context.getPilot().refresh();

        // then
        int productCoundFromHeader = searchResultsPage.getProductCountFromSearchResultsHeader();
        Assert.assertEquals("Incorrect number of products are displayed at search results header upon facet refinement",
                productCoundFromHeader, numberOfProductsForTheGivenFacetRefinement);
	}
    
    /**
     * Verifies whether the search results product count is displayed and updated when user selects
     * multiple facets refinement. 
     * Test Case ID - RGSN-38163
     */
    @Test
    @TestRail(id = "104663")
    public void testFacetPersistenceWhenUserSelectsMultipleFacetsAndNavigateToProductPageAndBackToSearchResultsExpectFilteredResults() {

        // given
        final ProductGroup searchItem = dataService.findProductGroup("type-ahead-suggestions");
        int facetGroup = 2;
        int facetValue = 1;
        
        // when
        String searchCriteria = searchItem.getUnmappedAttribute("searchKeyword");
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();
        searchResultsPage.clickFacetRefinement(facetGroup, facetValue);
        searchResultsPage.resetFacetValueForSelection();
        int numberOfProductsForTheGivenFacetRefinement = searchResultsPage.getNumberOfProductsForGivenFacetRefinement(facetGroup + 2, facetValue);
        searchResultsPage.clickFacetRefinement(facetGroup + 2, facetValue);
        int numberOfProductsAfterSelectingTheGivenFacetRefinement = searchResultsPage.getNumberOfProductsLoaded();
        
        // then
        Assert.assertEquals("Incorrect number of products were filtered upon facet selection", 
                numberOfProductsForTheGivenFacetRefinement, numberOfProductsAfterSelectingTheGivenFacetRefinement);
        
        // when
        searchResultsPage.clickOnFirstProduct();
        context.getPilot().getDriver().navigate().back();
        
        // then
        int numberOfProductsAfterNavigatingToPreviousPage = searchResultsPage.getNumberOfProductsLoaded();
        Assert.assertEquals("Incorrect number of products were filtered while navigating back from product page",
                numberOfProductsForTheGivenFacetRefinement, numberOfProductsAfterNavigatingToPreviousPage);
	}
    
    /**
     * Verifies whether the multiple facets selected are cleared after clicking clear all.
     * Test case ID - RGSN-38163
     */
    @Test
    @TestRail(id = "104664")
    public void testFacetsOnSearchResultsWhenUserClearsMultipleFacetsSelectionsExpectFacetsSelectedAreCleared() {
	
        //given
        final ProductGroup searchItem = dataService.findProductGroup("type-ahead-suggestions");
        int facetValue = 1;
        int facetGroup = 2;

        //when
        String searchCriteria = searchItem.getUnmappedAttribute("searchKeyword");
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();
        
        if (context.isFullSiteExperience()) {
            if (searchResultsPage.isClearAllSelectionPresentAndDisplayed()) {
                searchResultsPage.clickOnClearAllSelection();
            }
        } else {
            searchResultsPage.clickFilterButton();
            if (searchResultsPage.isClearAllSelectionPresentAndDisplayed()) {
                searchResultsPage.clickOnClearAllSelection();
            }
            searchResultsPage.clickFacetDoneButton();
        }
        
        searchResultsPage.clickFacetRefinement(facetGroup, facetValue);
        searchResultsPage.resetFacetValueForSelection();
        searchResultsPage.clickFacetRefinement(facetGroup + 1, facetValue);
        
        //then
        assertTrue("Clear All link is not displayed after facets is selected", searchResultsPage.isClearAllSelectionPresentAndDisplayed());
        if (context.isFullSiteExperience()) {
            assertTrue("Bubble button is not shown after facet selection", searchResultsPage.isBubbleButtonPresentAndDisplayed());
        }

        searchResultsPage.clickOnClearAllSelection();

        assertTrue("Clear All link is displayed after clearing facets", !searchResultsPage.isClearAllSelectionPresentAndDisplayed());
        if (context.isMobileExperience()) {
            searchResultsPage.clickFacetDoneButton();
        }
        for (facetGroup = 2; facetGroup <= 3; facetGroup++) {
            searchResultsPage.resetFacetValueForSelection();
            assertTrue("Facet value is selected", !searchResultsPage.isCheckboxSelectedInFacet(facetGroup, facetValue));
        }
    }
}
