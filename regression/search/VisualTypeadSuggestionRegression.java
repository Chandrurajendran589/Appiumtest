package com.wsgc.ecommerce.ui.regression.search;

import com.wsgc.ecommerce.ui.endtoend.search.syndication.SearchResultsUrlBuilder;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchResultsPage;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSectionFactory;
import com.wsgc.evergreen.entity.api.ProductGroup;

import tag.area.SearchArea;

import java.net.URI;

/**
 * Tests is to validate visual type ahead Search suggestions.
 */
@Category(SearchArea.class)
public class VisualTypeadSuggestionRegression extends AbstractTest {
	
    /**
     * Verifies whether as customer is able to see the visual type ahead search suggestions from Global Search
     * and navigates to Product Page.
     * Test Case ID - RGSN-38153, RGSN-38201 
     */
    @Test
    @TestRail(id = "98501")
    public void testSearchSuggestionsWhenUserClicksOnVisualTypeAheadExpectProductPage() {
        
        // given
        Assume.assumeTrue(context.isFullSiteExperience());
        final ProductGroup searchItem = dataService.findProductGroup("type-ahead-suggestions");
        final String searchKeyword = searchItem.getUnmappedAttribute("searchKeyword");
        String searchCriteria = searchKeyword.substring(0,3);
        
        //when
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchKeyword);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();

        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        navigationSection.searchSuggestions(searchCriteria);
        navigationSection.waitForVisualTypeAheadSuggestions();
        int visualTypeAheadSuggestionsAvailable = navigationSection.getNumberOfVisualTypeAheadSuggestionsAvailable();
        int visualTypeAheadIndex = RandomUtils.nextInt(1, visualTypeAheadSuggestionsAvailable);

        String expectedProductPageUrl = navigationSection.getProductUrlFromVisualTypeAheadSuggestion(visualTypeAheadIndex);
        navigationSection.clickVisualTypeAheadSuggestion(visualTypeAheadIndex);
        String actualProductPageUrl = context.getPilot().getDriver().getCurrentUrl().replace("%20","+");

        //then
        Assert.assertEquals("Product URL from Visual Type Ahead Suggestion \'" + expectedProductPageUrl + "\' is not matching with \'"
                + actualProductPageUrl + "\' of PIP page URL ", expectedProductPageUrl, actualProductPageUrl);
    }
}
