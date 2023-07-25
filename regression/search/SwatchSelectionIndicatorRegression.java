package com.wsgc.ecommerce.ui.regression.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.search.syndication.SearchResultsUrlBuilder;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchResultsItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchResultsPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductSubsetRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.RichAddToCartOverlaySection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartHeaderRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.ProductGroup;
import java.net.URI;
import java.util.List;
import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tag.area.SearchArea;

/**
 * Regression Tests for selecting product swatch from search results page.
 */
@Category(SearchArea.class)
public class SwatchSelectionIndicatorRegression extends AbstractTest {

    /**
     * Verifies whether the attributes are preselected for a Simple PIP
     * when user selects product swatch on Search Result Page.
     * Test case ID - RGSN-38148, RGSN-38154
     */
    @Test
    @TestRail(id = "102778")
    public void testSwatchSelectionIndicatorOnSearchPageWhenSwatchSelectedExpectSelectionCarriesOverToSimplePipAndCart() {
        // given
        final ProductGroup searchItem = dataService.findProductGroup("simple-pip", "attributes", "swatch-selection"); 
        int indexOfSwatch = 2;
        final int itemIndex = 0;
        final int expectedTotalItem = 1;
        final int expectedQuantity = 1;
        final String expectedSku = searchItem.getSkus().get(itemIndex);
        
        //when
        String searchCriteria = searchItem.getGroupId().replace("-", " ");
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();
        SearchResultsItemRepeatableSection productInSearchResults = searchResultsPage.getProductSectionByGroupId(
                    searchItem.getGroupId());
        productInSearchResults.getProductSwatchSelection(searchItem, indexOfSwatch);
        productInSearchResults.clickSwatchSelection(searchItem, indexOfSwatch); 
        
        
        //then     
        ProductPage productPage = productInSearchResults.goToProductInformationPage(searchItem);
        ProductSubsetRepeatableSection section = productPage.getSimpleProductItemRepeatableSection();
        assertTrue("The price is not present on PIP", section.isRegularPricePresent() || section.isSalePricePresent());
        RichAddToCartOverlaySection richAddToCartOverlaySection =  productPage.addToCart();
        ShoppingCartPage shoppingCartPage = richAddToCartOverlaySection.goToShoppingCartPage();
        shoppingCartPage.scrollIntoCartSection();
        List<ShoppingCartItemRepeatableSection> itemSectionList = shoppingCartPage.getShoppingCartItems();
        assertEquals(String.format("The shopping cart should have %s item.", expectedTotalItem), expectedTotalItem,
                     itemSectionList.size());
        ShoppingCartItemRepeatableSection item = shoppingCartPage.getFirstShoppingCartItem();
        assertTrue(String.format("The shopping cart item's sku should ends with '%s'. Actual sku: '%s'", expectedSku,
                                 item.getSku()), item.getSku().endsWith(expectedSku));
        assertEquals(String.format("The shopping cart item's quantity should be '%s'", expectedQuantity),
                     String.valueOf(expectedQuantity), item.getQuantity());
        List<ShoppingCartHeaderRepeatableSection> headerSectionList = shoppingCartPage.getSuborderHeadersOnCart();
        assertEquals(String.format("The shopping cart should have %s header.", expectedTotalItem), expectedTotalItem,
                     headerSectionList.size());
	}
	
    /**
     * Verifies whether the attributes are preselected for a Multi-Buy PIP
     * when user selects product swatch on Search Result Page.
     * Test case ID - RGSN-38148, RGSN-38154
     */
    @Test
    @TestRail(id = "102779")
    public void testSwatchSelectionIndicatorOnSearchPageWhenSwatchSelectedExpectSelectionCarriesOverToMultiBuyPipAndCart() {

        // given
        final ProductGroup searchItem = dataService.findProductGroup("multi-buy-multi-sku-pip", 
                    "attributes", "swatch-selection");
        int indexOfSwatch = 2;
        final int itemIndex = 0;
        final int expectedTotalItem = 1;
        final int expectedQuantity = 1;
        final String expectedSku = searchItem.getSkus().get(itemIndex);
        
        // when
        String searchCriteria = searchItem.getGroupId().replace("-", " ");
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();
        SearchResultsItemRepeatableSection productInSearchResults = searchResultsPage.getProductSectionByGroupId(
        		searchItem.getGroupId());
        productInSearchResults.getProductSwatchSelection(searchItem, indexOfSwatch);
        productInSearchResults.clickSwatchSelection(searchItem, indexOfSwatch);
        
        //then     
        ProductPage productPage = productInSearchResults.goToProductInformationPage(searchItem);
        RichAddToCartOverlaySection richAddToCartOverlaySection =  productPage.addToCart(); 
        ShoppingCartPage shoppingCartPage = richAddToCartOverlaySection.goToShoppingCartPage();
        shoppingCartPage.scrollIntoCartSection();
        List<ShoppingCartItemRepeatableSection> itemSectionList = shoppingCartPage.getShoppingCartItems();
        assertEquals(String.format("The shopping cart should have %s item.", expectedTotalItem), expectedTotalItem,
                     itemSectionList.size());
        ShoppingCartItemRepeatableSection item = shoppingCartPage.getFirstShoppingCartItem();
        assertTrue(String.format("The shopping cart item's sku should ends with '%s'. Actual sku: '%s'", expectedSku,
                                 item.getSku()), item.getSku().endsWith(expectedSku));
        assertEquals(String.format("The shopping cart item's quantity should be '%s'", expectedQuantity),
                     String.valueOf(expectedQuantity), item.getQuantity());
        List<ShoppingCartHeaderRepeatableSection> headerSectionList = shoppingCartPage.getSuborderHeadersOnCart();
        assertEquals(String.format("The shopping cart should have %s header.", expectedTotalItem), expectedTotalItem,
                     headerSectionList.size());
	}
	
    /**
     * Verifies whether the attributes are preselected for a Guided PIP
     * when user selects product swatch on Search Result Page.
     * Test case ID - RGSN-38148, RGSN-38154
     */
    @Test
    @TestRail(id = "102780")
    public void testSwatchSelectionIndicatorOnSearchPageWhenSwatchSelectedExpectSelectionCarriesOverToGuidedPipAndCart() {

        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP) 
        		|| context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP_REDESIGN));

        // given
        final ProductGroup searchItem = dataService.findProductGroup("guided-pip", "attributes", "swatch-selection");
        int indexOfSwatch = 2;
        final int itemIndex = 0;
        final int expectedTotalItem = 1;
        final int expectedQuantity = 1;
        final String expectedSku = searchItem.getSkus().get(itemIndex);

        // when
        String searchCriteria = searchItem.getGroupId().replace("-", " ");
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();
        SearchResultsItemRepeatableSection productInSearchResults = searchResultsPage.getProductSectionByGroupId(
                   searchItem.getGroupId());
        productInSearchResults.getProductSwatchSelection(searchItem, indexOfSwatch);
        productInSearchResults.clickSwatchSelection(searchItem, indexOfSwatch);     

        //then
        ProductPage productPage = productInSearchResults.goToProductInformationPage(searchItem);
        ProductSubsetRepeatableSection section = productPage.getGuidedProductItemRepeatableSection();
        assertTrue("The price is not present on PIP", section.isRegularPricePresent() || section.isSalePricePresent());
        RichAddToCartOverlaySection richAddToCartOverlaySection =  productPage.addToCart();
        ShoppingCartPage shoppingCartPage = richAddToCartOverlaySection.goToShoppingCartPage();
        shoppingCartPage.scrollIntoCartSection();
        List<ShoppingCartItemRepeatableSection> itemSectionList = shoppingCartPage.getShoppingCartItems();
        assertEquals(String.format("The shopping cart should have %s item.", expectedTotalItem), expectedTotalItem,
                     itemSectionList.size());
        ShoppingCartItemRepeatableSection item = shoppingCartPage.getFirstShoppingCartItem();
        assertTrue(String.format("The shopping cart item's sku should ends with '%s'. Actual sku: '%s'", expectedSku,
                                 item.getSku()), item.getSku().endsWith(expectedSku));
        assertEquals(String.format("The shopping cart item's quantity should be '%s'", expectedQuantity),
                     String.valueOf(expectedQuantity), item.getQuantity());
        List<ShoppingCartHeaderRepeatableSection> headerSectionList = shoppingCartPage.getSuborderHeadersOnCart();
        assertEquals(String.format("The shopping cart should have %s header.", expectedTotalItem), expectedTotalItem,
                     headerSectionList.size());
    }
}
