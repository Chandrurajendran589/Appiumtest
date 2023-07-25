package com.wsgc.ecommerce.ui.regression.favorites;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.account.AccountFlows;
import com.wsgc.ecommerce.ui.endtoend.search.syndication.SearchResultsUrlBuilder;
import com.wsgc.ecommerce.ui.endtoend.shop.PipFlows;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.account.LoginPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.GuidedPipFlows;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchResultsItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchResultsPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryPage;
import com.wsgc.ecommerce.ui.regression.favorites.syndication.FavoritesApiServices;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.CustomerAccount;
import com.wsgc.evergreen.entity.api.ProductGroup;
import com.wsgc.evergreen.impl.DefaultEvergreenContext;
import java.net.URI;
import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tag.area.FavoritesArea;

/**
 * Verifies whether the Group level Favorite is persistent between Shop to PIP 
 * page for Signed in user.
 */
@Category(FavoritesArea.class)
public class SignedInUserFavoritesPersistenceAtGroupLevelRegression extends AbstractTest {

    /**
     * Verifies whether the Group level Favorite is persistent between Shop to
     * Simple PIP page for a signed-in user.
     * Test case ID - RGSN-38021, RGSN-38680
     */
    @Test
    @TestRail(id = "98449")
    public void testSimplePipPageWhenSignedInUserFavoriteAnItemViaShopPageExpectGroupLevelFavorites() {

        //given
        final ProductGroup expectedProductGroup = dataService.findProductGroup(
                 productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null, "simple-pip", "attributes");
        final String expectedQuantity = "1";
        CustomerAccount account = dataService.findAccount("group-favorites");

        //when
        FavoritesApiServices.setUp(context);
        FavoritesApiServices.deleteFavoritesList(account);
        context.getPilot().getDriver().close();
        context = DefaultEvergreenContext.getContext();
        DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();
        startPilotAt(context.getPage(LoginPage.class));
        AccountFlows.signInWithAccount(context, account);
        CategoryPage productCategoryPage = ShopFlows.navigateToCategoryPage(context, expectedProductGroup.getUnmappedAttribute("categoryUrl"));
        CategoryItemRepeatableSection productInShopList = productCategoryPage.getProductSectionByGroupId(expectedProductGroup.getGroupId());
        assertTrue(String.format("%s - is not added to favorites", 
                expectedProductGroup.getName()), expectedProductGroup.getName().equalsIgnoreCase(productInShopList.favoriteProductOnCategory()));

        //then
        ProductPage productPage = productCategoryPage.clickProductOnCategoryPage(expectedProductGroup);
        assertTrue("The item is not added to favorites at Group Level in PIP Page", productPage.isItemAddedToFavorites());
        PipFlows.setSimplePipQuantityAndAttributes(productPage, expectedQuantity);
        assertFalse("The item is added to favorites at SKU Level in PIP Page", productPage.isItemAddedToFavorites());
    }

    /**
     * Verifies whether the Group level Favorite is persistent between Shop to
     * Guided PIP page for a signed-in user.
     * Test case ID - RGSN-38021, RGSN-38680
     */
    @Test
    @TestRail(id = "98450")
    public void testGuidedPipPageWhenSignedInUserFavoriteAnItemViaShopPageExpectGroupLevelFavorites() {

        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP) || context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP_REDESIGN));

        //given
        final ProductGroup expectedProductGroup = dataService.findProductGroup(
                 productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null, "guided-pip", "attributes");
        CustomerAccount account = dataService.findAccount("group-favorites");

        //when
        FavoritesApiServices.setUp(context);
        FavoritesApiServices.deleteFavoritesList(account);
        context.getPilot().getDriver().close();
        context = DefaultEvergreenContext.getContext();
        DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();
        startPilotAt(context.getPage(LoginPage.class));
        AccountFlows.signInWithAccount(context, account);
        CategoryPage productCategoryPage = ShopFlows.navigateToCategoryPage(context, expectedProductGroup.getUnmappedAttribute("categoryUrl"));
        CategoryItemRepeatableSection productInShopList = productCategoryPage.getProductSectionByGroupId(expectedProductGroup.getGroupId());
        assertTrue(String.format("%s - is not added to favorites", 
                expectedProductGroup.getName()), expectedProductGroup.getName().equalsIgnoreCase(productInShopList.favoriteProductOnCategory()));

        // then
        ProductPage productPage = productCategoryPage.clickProductOnCategoryPage(expectedProductGroup);
        assertTrue("The item is not added to favorites at Group Level in PIP Page", productPage.isItemAddedToFavorites());
        GuidedPipFlows.setGuidedPipAttributeSelections(context);
        assertFalse("The item is added to favorites at SKU Level in PIP Page", productPage.isItemAddedToFavorites());
    }
    
    /**
     * Verifies whether the Group level Favorite is persistent between Search Result Page 
     * to Simple PIP page for a signed-in user.
     * Test case ID - RGSN-38147, RGSN-38680
     */
    @Test
    @TestRail(id = "98451")
    public void testSimplePipPageWhenSignedInUserFavoriteAnItemViaSearchResultsPageExpectGroupLevelFavorites() {

        // given
        final ProductGroup productGroup = dataService.findProductGroup("simple-pip", "attributes");
        final String expectedQuantity = "1";
        CustomerAccount account = dataService.findAccount("group-favorites");

        // when	
        FavoritesApiServices.setUp(context);
        FavoritesApiServices.deleteFavoritesList(account);
        context.getPilot().getDriver().close();
        context = DefaultEvergreenContext.getContext();
        DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();
        startPilotAt(context.getPage(LoginPage.class));
        AccountFlows.signInWithAccount(context, account);
        String searchCriteria = productGroup.getGroupId().replace("-", " ");
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();
        SearchResultsItemRepeatableSection productInSearchResults = searchResultsPage.getProductSectionByGroupId(productGroup.getGroupId());
        assertTrue(String.format("The item was not added to favorites - %s", 
                productGroup.getName()), productGroup.getName().equalsIgnoreCase(productInSearchResults.favoriteProductOnSearchResults()));

        // then
        ProductPage productPage = productInSearchResults.goToProductInformationPage(productGroup);
        assertTrue("The item is not added to favorites at Group Level in PIP Page", productPage.isItemAddedToFavorites());
        PipFlows.setSimplePipQuantityAndAttributes(productPage, expectedQuantity);
        assertFalse("The item is added to favorites at SKU Level in PIP Page", productPage.isItemAddedToFavorites());
    }

    /**
     * Verifies whether the Group level Favorite is persistent between Search Result Page  
     * to Guided PIP page for a signed-in user.
     * Test case ID - RGSN-38147, RGSN-38680
     */
    @Test
    @TestRail(id = "98458")
    public void testGuidedPipPageWhenSignedInUserFavoriteAnItemViaSearchResultsPageExpectGroupLevelFavorites() {

        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP) 
                || context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP_REDESIGN));

        // given
        final ProductGroup productGroup = dataService.findProductGroup("guided-pip", "attributes");
        CustomerAccount account = dataService.findAccount("group-favorites");

        // when
        FavoritesApiServices.setUp(context);
        FavoritesApiServices.deleteFavoritesList(account);
        context.getPilot().getDriver().close();
        context = DefaultEvergreenContext.getContext();
        DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();
        startPilotAt(context.getPage(LoginPage.class));
        AccountFlows.signInWithAccount(context, account);
        String searchCriteria = productGroup.getGroupId().replace("-", " ");
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();
        SearchResultsItemRepeatableSection productInSearchResults = searchResultsPage.getProductSectionByGroupId(productGroup.getGroupId());
        assertTrue(String.format("The item was not added to favorites - %s", 
                productGroup.getName()), productGroup.getName().equalsIgnoreCase(productInSearchResults.favoriteProductOnSearchResults()));

        // then
        ProductPage productPage = productInSearchResults.goToProductInformationPage(productGroup);
        assertTrue("The item is not added to favorites at Group Level in PIP Page", productPage.isItemAddedToFavorites());
        GuidedPipFlows.setGuidedPipAttributeSelections(context);
        assertFalse("The item is added to favorites at SKU Level in PIP Page", productPage.isItemAddedToFavorites());
    }
}
