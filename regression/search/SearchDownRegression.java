package com.wsgc.ecommerce.ui.regression.search;

import static org.junit.Assert.assertTrue;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchDownPage;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tag.area.SearchArea;

/**
 * Regression Tests for validating when search results page is down.
 */
@Category(SearchArea.class)
public class SearchDownRegression extends AbstractTest {

    /**
     * Verifies whether a robots meta tag is available in search results page when search page is down.
     * Test Case ID - RGSN-38151
     */
    @Test
    @TestRail(id = "98484")
    public void testSearchResultsPageWhenGuestUserPerformsSearchDuringSearchDownExpectRobotsTagInPageSource() {

        // given
        String expectedRobotsTag = "<meta name=\"robots\" content=\"noindex,follow\">";

        // when
        SearchDownPage searchDownPage = context.getPage(SearchDownPage.class);
        startPilotAt(searchDownPage);
        String pageSource = context.getPilot().getDriver().getPageSource();

        assertTrue(String.format("The robots meta tag %s is not present in the page source of the search down page.", expectedRobotsTag), 
                pageSource.contains(expectedRobotsTag));
    }
}