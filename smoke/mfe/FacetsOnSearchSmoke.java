package com.wsgc.ecommerce.ui.smoke.mfe;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.search.syndication.SearchResultsUrlBuilder;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchResultsPage;
import com.wsgc.evergreen.entity.api.ProductGroup;

import java.net.URI;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tag.area.SearchArea;

/**
 * Smoke Tests for Facets on Search page.
 */
@Category(SearchArea.class)
public class FacetsOnSearchSmoke extends AbstractTest {

    /**
     * Verifies whether the products are filtered accordingly on search page, when user selected single facet.
     */
    @Test
    public void testSearchFacetsWhenProductsAreFilteredWithSingleFacetExpectedFilteredSearchResults() {
        // given
        final ProductGroup searchItem = dataService.findProductGroup("search-results");
        int facetGroup = 3;
        int facetValue = 1;

        // when
        String searchCriteria = searchItem.getGroupId().replace("-", " ");
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();

        //then
        Assert.assertTrue("User specified Facet Group: " + facetGroup +" or " + "Facet Value: " + facetValue + ""
                + " is not present", searchResultsPage.isFacetsDisplayed(facetGroup, facetValue));
        int numberOfProductsForTheGivenFacetRefinement = searchResultsPage.getNumberOfProductsForGivenFacetRefinement(facetGroup, facetValue);
        searchResultsPage.clickFacetRefinement(facetGroup, facetValue);
        /*
        We are refreshing the page after facets selection, to avoid race condition errors.
         */
        context.getPilot().refresh();
        int numberOfProductsAfterSelectingTheGivenFacetRefinement = searchResultsPage.getNumberOfProductsLoaded();
        Assert.assertEquals("Incorrect number of products were filtered upon facet selection", numberOfProductsForTheGivenFacetRefinement,
                numberOfProductsAfterSelectingTheGivenFacetRefinement);
    }
}
