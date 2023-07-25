package com.wsgc.ecommerce.ui.regression.search;

import java.net.URI;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.search.syndication.SearchResultsUrlBuilder;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSectionFactory;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchResultsPage;
import com.wsgc.evergreen.entity.api.ProductGroup;

import tag.area.SearchArea;

/**
 * Tests is to validate Text type Ahead Search suggestions.
 */
@Category(SearchArea.class)
public class TextTypeadSuggestionRegression extends AbstractTest {

    /**
     * Verifies whether as customer is able to see the Text type ahead search suggestions from Global Search
     * and navigates to Search Results Page.
     * Test Case ID - RGSN-38153, RGSN-38201
     */
    @Test
    @TestRail(id = "98500")
    public void testSearchSuggestionsWhenUserClicksOnTextTypeAheadExpectSearchResultsPage() {

        // given
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
        navigationSection.waitForTextTypeAheadSuggestions();
        int textTypeAheadSuggestionsAvailable = navigationSection.getNumberOfTextTypeAheadSuggestionsAvailable();
        int textTypeAheadIndex = RandomUtils.nextInt(1, textTypeAheadSuggestionsAvailable);

        String expectedSearchResultsPageUrl = navigationSection.getSearchUrlFromTextTypeAheadSuggestion(textTypeAheadIndex);
        navigationSection.clickTextTypeAheadSuggestion(textTypeAheadIndex);
        String actualSearchResultsPageUrl = context.getPilot().getDriver().getCurrentUrl();

        //then
        Assert.assertEquals("Search Results URL from Text Type Ahead Suggestion \'" + expectedSearchResultsPageUrl + "\' is not matching with \'"
                + actualSearchResultsPageUrl + "\' of Search Results page URL ", expectedSearchResultsPageUrl, actualSearchResultsPageUrl);
    }
}
