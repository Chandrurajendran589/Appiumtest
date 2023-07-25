package com.wsgc.ecommerce.ui.smoke.mfe;

import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.pagemodel.HomePage;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSectionFactory;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchResultsPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.evergreen.entity.api.ProductGroup;

import tag.area.SearchArea;

/**
 * Smoke Tests for Navigating to Search Result page from MFE Page
 */
@Category(SearchArea.class)
public class SearchForAnItemSmoke extends AbstractTest {

    /**
     * Verifies when as customer searches using a SKU from search results page and they 
     * land on the Product Information Page.
     */
    @Test
    public void testSearchWhenUsingSkuToSearchForAnItemExpectProductPage() {
        // given
        final int itemIndex = 0;
        final ProductGroup searchItem = dataService.findProductGroup("simple-pip", "!attributes", "!monogrammable", "search-results");
        final String productSku = searchItem.getSkus().get(itemIndex);
        final String groupId = searchItem.getGroupId();
        final String EXPECTED_PRODUCT_PAGE_TEMPLATE_URL[] = {"products/%s/?sku=%s&words=%s", "products/%s/?words=%s&sku=%s"};
        final String[] expectedProductPageUrls = new String[EXPECTED_PRODUCT_PAGE_TEMPLATE_URL.length];
        int index = 0;

        // when
        String searchCriteria = searchItem.getGroupId().replace("-", " ");
        startPilotAt(context.getPage(HomePage.class));
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        SearchResultsPage searchResultsPage = navigationSection.search(searchCriteria);
        searchResultsPage.waitForSearchResultsHeading();
        assertTrue("Search Results page is not displayed", searchResultsPage.isSearchResultsSectionPresent());
        ProductPage productPage = navigationSection.searchWithSku(productSku);
        productPage.setProductGroup(searchItem);
        for (String productPageUrl : EXPECTED_PRODUCT_PAGE_TEMPLATE_URL) {
        	expectedProductPageUrls[index] = String.format(productPageUrl, groupId, productSku, productSku);
        	index++;
        }
        String productPageURL = productPage.getCurrentPageUrl();
        productPageURL = productPageURL.substring(productPageURL.indexOf("products/"), productPageURL.length());
       
        index = 0;
        for (String expectedProductPageUrl : expectedProductPageUrls) {
        	if (productPageURL.equalsIgnoreCase(expectedProductPageUrl)) {
        		break;
        	}
        	index++;
        }
        
        Assert.assertTrue("The expected product page for the sku is not displayed", productPageURL.equalsIgnoreCase(expectedProductPageUrls[index]));
       
    }
    
    /**
     * Verifies when as customer searches using a keyword from search results page and they 
     * land on the Search Results Page.
     */
    @Test
    public void testSearchWhenUsingSearchKeywordToSearchForAnItemExpectSearchResultsPage() {
        // given
        final ProductGroup searchItem = dataService.findProductGroup("simple-pip", "!attributes", "!monogrammable", "search-results");

        // when
        String searchCriteria = searchItem.getGroupId().replace("-", " ");
        startPilotAt(context.getPage(HomePage.class));
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        SearchResultsPage searchResultsPage = navigationSection.search(searchCriteria);
        searchResultsPage.waitForSearchResultsHeading();
        assertTrue("Search Results page is not displayed", searchResultsPage.isSearchResultsSectionPresent());
        searchResultsPage = navigationSection.search(searchCriteria);
        searchResultsPage.waitForSearchResultsHeading();
        assertTrue("Search Results page is not displayed", searchResultsPage.isSearchResultsSectionPresent());
    }
}
