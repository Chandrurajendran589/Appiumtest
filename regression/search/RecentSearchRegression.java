package com.wsgc.ecommerce.ui.regression.search;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.search.syndication.SearchResultsUrlBuilder;
import com.wsgc.ecommerce.ui.pagemodel.HomePage;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSectionFactory;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchResultsPage;
import com.wsgc.evergreen.entity.api.ProductGroup;
import groovy.lang.Category;
import java.net.URI;
import java.util.List;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import tag.area.SearchArea;

/**
 * Regression Tests for validating recent search.
 */
@Category(SearchArea.class)
public class RecentSearchRegression extends AbstractTest {

    /**
     * Verifies whether user able to see up to three recent search suggestions.
     * Test case ID - RGSN-38140
     */
    @Test
    @TestRail(id = "98481")
    public void testRecentSearchWhenUserPerformsSearchViaSearchKeywordExpectRecentSearchSuggestions() {
         //given
         Assume.assumeTrue(context.isFullSiteExperience());
         List<ProductGroup> productGroups = dataService.findProductGroups(productGroup -> productGroup.getUnmappedAttribute("searchKeyword") != null); 
         String searchCriteria = productGroups.get(0).getUnmappedAttribute("searchKeyword");
         int expectedRecentSearchDisplayed = 3;
         int recentSearchIndex = 0;

         //when
         String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
         URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
         context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
         NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
         for (ProductGroup productGroup : productGroups) {
             searchCriteria = productGroup.getUnmappedAttribute("searchKeyword");
             SearchResultsPage searchResultsPage = navigationSection.search(searchCriteria);
             searchResultsPage.waitForSearchResultsHeading();
         }
         navigationSection.clickSearchInput();
         int actualRecentSearchDisplayed =  navigationSection.getRecentSearchText().size();
         List<String> recentSearchSuggestionsText = navigationSection.getRecentSearchText();

         //then
         Assert.assertEquals("Expected number of Recent Search '" + expectedRecentSearchDisplayed + "' that should be displayed is not matching with \'"
                    + actualRecentSearchDisplayed + "\' of Recent Search displayed", expectedRecentSearchDisplayed, actualRecentSearchDisplayed);
         for (int index = productGroups.size() - 1; index >= productGroups.size() - expectedRecentSearchDisplayed; index--) {
              ProductGroup productGroup = productGroups.get(index);
              searchCriteria = productGroup.getUnmappedAttribute("searchKeyword");
              Assert.assertEquals("Expected Recent Search Text'" + searchCriteria + "' is not matching with \'"
                        + recentSearchSuggestionsText.get(recentSearchIndex) + "\' the Recent Search displayed", searchCriteria, recentSearchSuggestionsText.get(recentSearchIndex));
              recentSearchIndex++;
         }
     }

     /**
      * Verifies whether one recent search suggestion is displayed when the user performs search with the same keyword twice.
      * Test case ID - RGSN-38140
      */ 
    @Test
    @TestRail(id = "98482")
    public void testRecentSearchWhenUserPerformsSearchWithSameKeywordTwiceExpectOneRecentSearchSuggestion() {

         //given
         Assume.assumeTrue(context.isFullSiteExperience());
         ProductGroup productGroup = dataService.findProductGroup(expectedProductGroup -> expectedProductGroup.getUnmappedAttribute("searchKeyword") != null);
         String searchCriteria = productGroup.getUnmappedAttribute("searchKeyword");
         int recentSuggestionIndex = 1;

         //when
         NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
         startPilotAt(context.getPage(HomePage.class));
         SearchResultsPage searchResultsPage = navigationSection.search(searchCriteria);
         searchResultsPage.waitForSearchResultsHeading();
         navigationSection.clickSearchInput();
         int totalRecentSearchDisplayed =  navigationSection.getRecentSearchText().size();

         //then
         Assert.assertEquals("Expected number of Recent Search '" + recentSuggestionIndex + "' that should be displayed is not matching with \'"
                    + totalRecentSearchDisplayed + "\' of Recent Search displayed", recentSuggestionIndex, totalRecentSearchDisplayed);
    }

    /**
     * Verifies whether the user is able to navigation to a search results page on click a recent search suggestion.
     * Test case ID - RGSN-38140, RGSN-38201
     */
    @Test
    @TestRail(id = "98480")
    public void testRecentSearchWhenUserClicksRecentSearchSuggestionExpectSearchResultsPage() {
        //given
        Assume.assumeTrue(context.isFullSiteExperience());
        ProductGroup productGroup = dataService.findProductGroup(expectedProductGroup -> expectedProductGroup.getUnmappedAttribute("searchKeyword") != null);
        String searchCriteria = productGroup.getUnmappedAttribute("searchKeyword");
        int recentSuggestionIndex = 1;

        //when
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        startPilotAt(context.getPage(HomePage.class));
        SearchResultsPage searchResultsPage = navigationSection.search(searchCriteria);
        searchResultsPage.waitForSearchResultsHeading();
        navigationSection.clickSearchInput();
        String expectedSearchResultsPageUrl = navigationSection.getSearchUrlFromRecentSuggestion(recentSuggestionIndex);
        searchResultsPage = navigationSection.clickRecentSuggestion(recentSuggestionIndex);
        searchResultsPage.waitForSearchResultsHeading();
        String actualSearchResultsPageUrl = context.getPilot().getDriver().getCurrentUrl();

        //then
        Assert.assertEquals("Search Results URL from Recent Suggestion \'" + expectedSearchResultsPageUrl + "\' is not matching with \'"
                   + actualSearchResultsPageUrl + "\' of Search Results page URL ", expectedSearchResultsPageUrl, actualSearchResultsPageUrl);
    }
}
