package com.wsgc.ecommerce.ui.regression.search;

import static org.junit.Assert.assertTrue;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.search.syndication.SearchResultsUrlBuilder;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchResultsPage;
import com.wsgc.evergreen.entity.api.ProductGroup;
import java.net.URI;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tag.area.SearchArea;

/**
 * Regression Tests for validating canonical tags in search results page.
 */
@Category(SearchArea.class)
public class CanonicalTagInSearchResultsRegression extends AbstractTest {

    /**
     * Verifies whether a canonical tag is available in search results page when users performs a multi-keyword search.
     * Test Case ID - RGSN-38142
     */
    @Test
    @TestRail(id = "98477")
    public void testSearchResultsPageWhenGuestUserPerformsLongTailSearchExpectCanonicalTagInPageSource() {

        // given
        ProductGroup searchItem = dataService.findProductGroup("simple-pip");
        String canonicalTagTemplate = "<link rel=\"canonical\" href=\"%s/search/results.html?words=%s\" id=\"pageCanonicalLink\">";

        // when
        String searchCriteria = searchItem.getGroupId().replace("-", " ");
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();
        String pageSource = context.getPilot().getDriver().getPageSource();
        searchCriteria = searchCriteria.replaceAll(" ", "+");
        String baseUrl = context.getBaseUrl().toString();
        if (baseUrl.contains(":") && baseUrl.contains("@")) {
            baseUrl = baseUrl.substring(0, 8) + baseUrl.substring(baseUrl.indexOf('@') + 1, baseUrl.length());
        }
        if (context.isMobileExperience()) {
        	baseUrl = baseUrl.substring(0, baseUrl.length() -2);
        }
        String expectedCanonicalTag = String.format(canonicalTagTemplate, baseUrl, searchCriteria);

        assertTrue(String.format("The canonical tag %s is not present in the page source of the search results page.", expectedCanonicalTag), 
                pageSource.contains(expectedCanonicalTag));
    }
}
