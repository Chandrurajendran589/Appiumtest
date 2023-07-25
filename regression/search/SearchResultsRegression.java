package com.wsgc.ecommerce.ui.regression.search;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.search.syndication.SearchResultsUrlBuilder;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSectionFactory;
import com.wsgc.ecommerce.ui.pagemodel.i18n.InternationalShippingOverlay;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchResultsItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchResultsPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.ProductGroup;
import com.wsgc.evergreen.entity.api.Search;
import java.net.URI;
import java.util.List;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import tag.area.SearchArea;

/**
 * Tests is to validate different type of Searches and components of search results page.
 */
@Category(SearchArea.class)
public class SearchResultsRegression extends AbstractTest {

    /**
     * Verifies whether user is able to perform a search using keyword.
     * Test Case ID - RGSN-38153
     */
    @Test
    @TestRail(id = "98492")
    public void testSearchWhenUserPerformsSearchUsingKeywordExpectSearchResultsPage() {

        //given
        final ProductGroup searchItem = dataService.findProductGroup("favorites");
        final String searchKeyword = searchItem.getUnmappedAttribute("searchKeyword");
        String searchResultHeadingTemplate = "search results for";

        //when
        SearchResultsPage searchResultsPageNavigation = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchKeyword);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPageNavigation.waitForSearchResultsHeading();
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        SearchResultsPage searchResultsPage = navigationSection.search(searchKeyword);
        searchResultsPage.waitForSearchResultsHeading();
        String actualSearchResultTermText = searchResultsPage.getSearchResultTerm();
        String actualSearchResultHeadingText =  searchResultsPage.getSearchResultHeadingText();

        //then
        Assert.assertTrue("Search Result For label doesn't exists ", actualSearchResultHeadingText.toLowerCase().contains(searchResultHeadingTemplate));
        Assert.assertEquals("Expected Search Keyword \'" + searchKeyword + "\' is not matching with \'"
                + actualSearchResultTermText + "\' keyword of the Search Results Page ", searchKeyword.toLowerCase(), actualSearchResultTermText.toLowerCase());

    }
    
    /**
     * Verifies user able to search product with sku in search field in search results page.
     * Test Case ID - RGSN-38162.
     */
    @Test
    @TestRail(id = "98485")
    public void testSearchWhenUserPerformsSkuSearchFromSearchResultsPageExpectProductPage() {
        //given
        final ProductGroup searchItem = dataService.findProductGroup("simple-pip", "!attributes");
        final String productSku = searchItem.getSkus().get(0);
        final String productGroupId = searchItem.getGroupId();

        //when
        String searchKeyword = productGroupId.replace("-", " ");
        SearchResultsPage searchResultsPageNavigation = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchKeyword);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPageNavigation.waitForSearchResultsHeading();
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        navigationSection.searchWithSku(productSku);
        String productPageUrl = context.getPilot().getDriver().getCurrentUrl();

        //then
        assertTrue("The expected product page for the sku is not displayed.", productPageUrl.contains(productGroupId));
    }
    
    /**
     * Verifies search field is not displayed by default in global header on mobile sites.
     * Test case ID - RGSN-38165
     */
    @Test
    @TestRail(id = "98494")
    public void testSearchFieldWhenNavigatingToSearchResultPageExpectNoSearchBarDisplayed() {

        // given
        Assume.assumeTrue((context.isMobileExperience()) && !context.getTargetSite().supportsFeature(FeatureFlag.PERSISTENT_SEARCH_FIELD_MOBILE));
        final ProductGroup searchItem = dataService.findProductGroup("favorites");
        final String searchKeyword = searchItem.getUnmappedAttribute("searchKeyword");

        // when
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchKeyword);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();

        // then
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        assertFalse("Search Field is displayed", navigationSection.isSearchFieldPresentAndDisplayed());
    }

    /**
     * Verifies grey shader overlay is not displayed while refreshing the current page
     * for mobile sites.
     * Test case ID - RGSN-38165
     */
    @Test
    @TestRail(id = "98495")
    public void testSearchFieldWhenFocusIsOutOfSearchFieldExpectedNoGreyAreaInSearchResults() {

        // given
        Assume.assumeTrue(context.isMobileExperience());
        final ProductGroup searchItem = dataService.findProductGroup("favorites");
        final String searchKeyword = searchItem.getUnmappedAttribute("searchKeyword");

        // when
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchKeyword);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();

        // then
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        navigationSection.clickSearchIconFromGlobalHeader();
        assertTrue("Search Field is not displayed", navigationSection.isSearchFieldPresentAndDisplayed());
        assertTrue("Dark Grey out overlay is not displayed", navigationSection.isGreyedOutShaderAreaPresentAndDisplayed());
        context.getPilot().refresh();
        if (!context.getTargetSite().supportsFeature(FeatureFlag.PERSISTENT_SEARCH_FIELD_MOBILE)) {
            assertFalse("Search Field is displayed", navigationSection.isSearchFieldPresentAndDisplayed());
        }
        assertFalse("Dark Grey out overlay is displayed", navigationSection.isGreyedOutShaderAreaPresentAndDisplayed());
        
    }
    
    /**
     * Verifies whether a user is taken to the top of the search page on clicking the back to top icon.
     * Test Case ID - RGSN-38165
     */
    @Test
    @TestRail(id = "98496")
    public void testSearchPageWhenUserClickBackToTopIconExpectGlobalHeader() {

        // given
        Assume.assumeTrue(context.isMobileExperience());
        final ProductGroup expectedProductGroup = dataService.findProductGroup("favorites");
        final String searchKeyword = expectedProductGroup.getUnmappedAttribute("searchKeyword");

        // when
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchKeyword);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();
        searchResultsPage.getNumberOfProductsLoaded();
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);

        //then
        Actions builder = new Actions(context.getPilot().getDriver());
        builder.sendKeys(Keys.PAGE_UP).build().perform();
        assertTrue("Sticky Header should be displayed.", navigationSection.isStickyGlobalHeaderDisplayed());
        searchResultsPage.clickScrollToTop();
        assertFalse("Sticky Global Header is displayed instead of regular global header.", navigationSection.isStickyGlobalHeaderDisplayed());
    }
    
    /**
     * Verifies whether a user is able to search for items using a misspelled search.
     * Test Case ID - RGSN-38145
     */
    @Test
    @TestRail(id = "98497")
    public void testSearchWhenUserPerformsSearchUsingMisspelledKeywordExpectSearchResultsPage() {
        // given
        final ProductGroup searchItem = dataService.findProductGroup("misspelled-search");
        final String productName = searchItem.getName();
        final String searchCriteria = searchItem.getUnmappedAttribute("misspelledKeyword");
        String SPELL_CORRECTED_MESSAGE_TEMPLATE = "\"%s\" was spell-corrected to \"%s\"";
        String expectedAlternateSearchMessage = "Did you mean";

        // when
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();
        String searchResultsHeadingTerm = searchResultsPage.getSearchTerm().toLowerCase();
        String spellCorrectedSearchTerm = searchResultsPage.getSpellCorrectedSearchTerm().toLowerCase().replaceAll("\"", "").trim();
        String expectedSpellCorrectedMessage = String.format(SPELL_CORRECTED_MESSAGE_TEMPLATE, searchCriteria.toLowerCase(), spellCorrectedSearchTerm.toLowerCase());
        String actualSpellCorrectedMessage = searchResultsPage.getSpellCorrectedSearchMessage().toLowerCase();
        String actualAlternateSearchMessage = searchResultsPage.getAlternateSearchMessage().replaceAll("\"", "").trim();
        
        //then
        Assert.assertEquals("Search Results Heading term \'" + searchResultsHeadingTerm + "\' is not matching with Spell Corrected Search Term \'"
                + spellCorrectedSearchTerm + "\'", searchResultsHeadingTerm, spellCorrectedSearchTerm);
        Assert.assertEquals("Expected Spell Corrected Search Message \'" + expectedSpellCorrectedMessage + "\' is not matching with Actual Spell Corrected Search Message \'"
                + actualSpellCorrectedMessage + "\'", expectedSpellCorrectedMessage, actualSpellCorrectedMessage);
        Assert.assertTrue("Expected Alternate Search Message \'" + expectedAlternateSearchMessage + "\' is not present in Actual Alternate Search Message \'"
                + actualAlternateSearchMessage + "\'", actualAlternateSearchMessage.contains(expectedAlternateSearchMessage)); 
        Assert.assertTrue(productName + " is not found in the search Resuts Page", searchResultsPage.getProductSectionByGroupId(searchItem.getGroupId()) != null);      
    }
    
    /**
     * Verifies whether search results are displayed when alternate search term is clicked.
     * Test Case ID - RGSN-38145
     */
    @Test
    @TestRail(id = "98498")
    public void testSearchWhenUserClicksAlternateSearchTermExpectSearchResultsPage() {
        // given
        final ProductGroup searchItem = dataService.findProductGroup("misspelled-search");
        final String searchCriteria = searchItem.getUnmappedAttribute("misspelledKeyword");


        // when
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();

        List<String> alternateSearchTerms = searchResultsPage.getAlternateSearchTerms();
        int alternateSearchIndex = RandomUtils.nextInt(0, alternateSearchTerms.size() - 1);
        String expectedAlternateSearchTerm = alternateSearchTerms.get(alternateSearchIndex).toLowerCase().replaceAll("\"", "").trim();
        searchResultsPage.clickAlternateSearchTerm(alternateSearchIndex);
        searchResultsPage.waitForSearchResultsHeading();
        String searchResultsHeadingTerm = searchResultsPage.getSearchTerm().toLowerCase();
        
        //then
        Assert.assertEquals("Search Results Heading term \'" + searchResultsHeadingTerm + "\' is not matching with Alternate Search Term Clicked\'"
                + expectedAlternateSearchTerm + "\'", searchResultsHeadingTerm, expectedAlternateSearchTerm);
        
    }

    /**
     * Verified whether the primary key is displayed in Product Page url while user selects facet and navigate to
     * Product Page from Search results page.
     * 
     * Test Case ID RGSN-38139. 
     */
    @Test
    @TestRail(id = "98499")
    public void testPrimaryKeyLogicWhenUserSelectsFacetsAndNavigatesToProductPageExpectPrimaryKeyInUrl()  {
        // given
        final ProductGroup searchItem = dataService.findProductGroup(
                productGroup -> productGroup.getUnmappedAttribute("searchKeyword") != null, "simple-pip", "!attributes",
                "search-results");
        int facetGroup = 2;
        int facetValue = 1;
        final String PRIMARY_KEY_TEMPLATE = "pkey=s~%s~%s";
        
        // when
        String searchCriteria = searchItem.getUnmappedAttribute("searchKeyword");
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder
				.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();
        searchResultsPage.clickFacetRefinement(facetGroup, facetValue);
        /*
        We are refreshing the page after facets selection, to avoid race condition errors.
         */
        context.getPilot().refresh();
        int productCount = searchResultsPage.getProductCountFromSearchResultsHeader();
        String primaryKey =  String.format(PRIMARY_KEY_TEMPLATE , searchCriteria.replace(" ", "%20"), productCount);
        searchResultsPage.clickOnFirstProduct();
        String productPageUrl = context.getPilot().getDriver().getCurrentUrl().replace("%7E", "~").replace("+", "%20");

        //then
        assertTrue("Product page url : " + productPageUrl + " does not contain primary key: " + primaryKey , (productPageUrl.contains(primaryKey)));
	}
    
    /**
     * Verifies whether the user is able see a sub-text message when related products
     * from other brands are not displayed for a junk search.
     * Test Case ID - RGSN-38155
     */
    @Test
    @TestRail(id = "98603")
    public void testSearchResultsWhenUserPerformsJunkTextSearchExpectRelatedProductsWithSubTextMessage() {

        //given
        final ProductGroup expectedProductGroup = dataService.findProductGroup(
                productGroup -> productGroup.getUnmappedAttribute("searchKeyword") != null);
        final Search searchItem = dataService.findSearch("junk-search", "with-related-products");
        final String subTextMessageTemplate = "Check the spelling, try a more general term, use "
                + "fewer words or view results from our family of brands."
                + "You can also enter the product catalog item number.";

        //when
        String searchCriteria = expectedProductGroup.getUnmappedAttribute("searchKeyword");
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        searchCriteria = searchItem.getJunkSearchKeyword();
        searchResultsPage = navigationSection.search(searchCriteria);
        searchResultsPage.waitForBlankSearchResultMessage();
        String actualSubTextMessage = searchResultsPage.getBlankSearchResultMessage();
        actualSubTextMessage = actualSubTextMessage.replace(". ", ".").trim();
        actualSubTextMessage = actualSubTextMessage.replace("\n", "").trim();
        
        //then
        Assert.assertEquals("Expected Sub-Text Message \'" + subTextMessageTemplate + "\' is not matching with actual\'"
                + actualSubTextMessage + "\'", subTextMessageTemplate, actualSubTextMessage);
        Assert.assertTrue("Related Cross Brand products section is not displayed in the Search Results Page", 
                searchResultsPage.isRelatedCrossBrandProductSectionPresent());      
     }
    
    /**
     * Verifies whether the user is able see a sub-text message when related products
     * from other brands are displayed for a junk search.
     * Test Case ID - RGSN-38155
     */
    @Test
    @TestRail(id = "98605")
    public void testSearchResultsWhenUserPerformsJunkTextSearchExpectSubTextMessage() {

        //given
        final ProductGroup expectedProductGroup = dataService.findProductGroup(
                productGroup -> productGroup.getUnmappedAttribute("searchKeyword") != null);
        final String searchItem = "abc876";
        final String subTextMessageFirstTemplate = "Check the spelling, try a more general term, or use "
                + "fewer words."
                + "You can also enter the product catalog item number.";
        
        final String subTextMessageSecondTemplate = "Try using broader search words, fewer keywords, or"
                + " double-checking your spelling."
                + "You can also enter the product catalog item number.";

        //when
        String searchCriteria = expectedProductGroup.getUnmappedAttribute("searchKeyword");
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        searchResultsPage = navigationSection.search(searchItem);
        searchResultsPage.waitForBlankSearchResultMessage();
        String actualSubTextMessage = searchResultsPage.getBlankSearchResultMessage();
        actualSubTextMessage = actualSubTextMessage.replace(". ", ".").trim();
        actualSubTextMessage = actualSubTextMessage.replace("\n", "").trim();
        
        //then
        Assert.assertTrue("Actual Sub-Text Message \'" + actualSubTextMessage + "\' is not matching with either\'"
                + subTextMessageFirstTemplate + "\' or \'" + subTextMessageSecondTemplate 
                + "\'", actualSubTextMessage.equals(subTextMessageFirstTemplate) 
                || actualSubTextMessage.equals(subTextMessageSecondTemplate));
     }
    
    /**
     * Verifies if user is able to see sale price for an item in search results page.
     * Test case ID - RGSN-38157
     */
    @Test
    @TestRail(id = "102776")
    public void testProductPriceWhenNavigatingToSearchResultPageExpectItemWithSalePrice() {

        // given
        final ProductGroup searchItem = dataService.findProductGroup("sale-price", "search-results");
        String searchCriteria = searchItem.getGroupId().replace("-", " ");
        final String salePriceLabelText = "sale";
        final String clearancePriceLabelText = "clearance";
        final String nowPriceLabelText = "now";
        boolean isSalePrice = false;

        // when
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();
        SearchResultsItemRepeatableSection productInSearchResults = searchResultsPage
                .getProductSectionByGroupId(searchItem.getGroupId());
        
        //then
        Assert.assertTrue("Sale Price is not displayed for the item in the Search Results Page", 
                productInSearchResults.isSalePricePresent()); 
        String salePriceLabel = productInSearchResults.getSalePriceLabel();
        assertTrue("Sale/Clearance price label is not displayed for the item", salePriceLabel.equalsIgnoreCase(salePriceLabelText)
                || salePriceLabel.equalsIgnoreCase(clearancePriceLabelText) || salePriceLabel.equalsIgnoreCase(nowPriceLabelText));
        List<String> salePrices = productInSearchResults.getSalePrice();
        if (salePrices.size() > 1) {
            assertTrue("Sale/Clearance price range should only contain the lower and highest item price and not more", 
                    salePrices.size() == 2);
            for (String salePrice : salePrices) {
                if (salePrice.endsWith(".99") || salePrice.endsWith(".97")) {
                    isSalePrice = true;
                    break;
                }
            }
            assertTrue("Sale/Clearance price does not end with .99 & .97 ", isSalePrice);
        } else {
            assertTrue("Sale/Clearance price does not end with .99 & .97 ", 
                    salePrices.get(0).endsWith(".99") || salePrices.get(0).endsWith(".97"));
        }
    }
    
    /**
     * Verifies if user is able to see regular price for an item in search results page.
     * Test case ID - RGSN-38157
     */
    @Test
    @TestRail(id = "102777")
    public void testProductPriceWhenNavigatingToSearchResultPageExpectItemWithRegularPrice() {

        // given
        final ProductGroup searchItem = dataService.findProductGroup("sale-price", "search-results");
        String searchCriteria = searchItem.getGroupId().replace("-", " ");

        // when
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();
        SearchResultsItemRepeatableSection productInSearchResults = searchResultsPage
                .getProductSectionByGroupId(searchItem.getGroupId());
        
        //then
        Assert.assertTrue("Regular Price is not displayed for the item in the Search Results Page", 
                productInSearchResults.isRegularPricePresent()); 
        List<String> regularPrices = productInSearchResults.getRegularPrice();
        if (regularPrices.size() > 1) {
            assertTrue("Regular price range should only contain the lower and highest item price and not more", 
                    regularPrices.size() == 2);
            assertTrue("The lower and highest item price is the same in the regular price range", 
                    !regularPrices.get(0).equalsIgnoreCase(regularPrices.get(1)));
            for (String regularPrice : regularPrices) {
                assertTrue("Regular price ends with .99 & .97 ", 
                        !(regularPrice.endsWith(".99")) || !(regularPrice.endsWith(".97")));
            }
        } else {
            assertTrue("Regular price ends with .99 & .97 ", 
                    !(regularPrices.get(0).endsWith(".99")) || !(regularPrices.get(0).endsWith(".97")));
        }
    }
    
    /**
     * Verifies whether the Prices are converted for International users on Search result page.
     * Test Case ID - RGSN-38161
     */
    @Test
    @TestRail(id = "104599")
    public void testPricesConvertedWhenUserWantsToInternationalShipExpectPricesConverted() {

        // given
        final ProductGroup searchItem = dataService.findProductGroup("favorites");
        final String searchKeyword = searchItem.getUnmappedAttribute("searchKeyword");
        int countryIndex = 3;

        // when
        SearchResultsPage searchResultsPageNavigation = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchKeyword);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPageNavigation.waitForSearchResultsHeading();
        searchResultsPageNavigation.clickShipTo();
        InternationalShippingOverlay internationalShippingOverlay = context.getPageSection(InternationalShippingOverlay.class);
        internationalShippingOverlay.selectNonUsDollarCurrencyInternationalCountry(countryIndex);
        String expectedCurrecyType = internationalShippingOverlay.getCurrencyType(countryIndex);
        internationalShippingOverlay.clickUpdateCurrencyAndCountryButton();
        searchResultsPageNavigation.waitForSearchResultsHeading();

        // then
        assertTrue("The Currency code should be updated based on Country selected", 
        		expectedCurrecyType.equalsIgnoreCase(searchResultsPageNavigation.getCurrencyType()));
    }
}
