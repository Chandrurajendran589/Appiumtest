package com.wsgc.ecommerce.ui.regression.stores;

import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.search.syndication.SearchResultsUrlBuilder;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSectionFactory;
import com.wsgc.ecommerce.ui.pagemodel.customerservice.StoreLandingPage;
import com.wsgc.ecommerce.ui.pagemodel.customerservice.StoreListItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.customerservice.StoreLocatorPage;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchResultsPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryPage;
import com.wsgc.ecommerce.ui.pagemodel.store.MyStoreDetailsPage;
import com.wsgc.ecommerce.ui.pagemodel.store.MyStoreOverlay;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.ProductGroup;
import com.wsgc.evergreen.entity.api.Store;

import groovy.lang.Category;
import tag.area.ShopArea;

/**
 * Regression Tests for Validating the elements on store landing page.
 * 
 */
@Category(ShopArea.class)
public class StoreLandingPageRegression extends AbstractTest {

    /**
     * Verifies whether a user is taken to the store landing page when they click on an 
     * outlet store from the store locator page.
     * Test Case ID - RGSN-38031
     */
    @Test
    @TestRail(id = "98428")
    public void testStoreLocatorWhenUserClicksOnStoreExpectStoreLandingPage() {

        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.STORE_LOCATOR));

        // when
        StoreLocatorPage storeLocatorPage = context.getPage(StoreLocatorPage.class);
        context.getPilot().startAt(storeLocatorPage);
        List<StoreListItemRepeatableSection> storeListItems = storeLocatorPage.getStoreListItems();
        int numberOfStoreListItems = storeListItems.size();
        int storeIndex = RandomUtils.nextInt(1, numberOfStoreListItems);
        String expectedStoreTitle = storeListItems.get(storeIndex).getStoreTitle().toLowerCase();
        StoreLandingPage storeLandingPage =  storeListItems.get(storeIndex).clickStoreTitle();
        String actualStoreTitle = storeLandingPage.getStoreNameDisplayed().toLowerCase();

        //then
        Assert.assertTrue("Expected Store Title '" + expectedStoreTitle + "' is not present in Actual Store Title'"
                + actualStoreTitle + "' of the Store Landing Page", actualStoreTitle.contains(expectedStoreTitle));
    }
    
    /**
     * Verify the selected My Store hyperlink with new format navigated from shop page.
     *
     * Test Case ID: RGSN-38032.
     */
    @Test
    public void testMyStoreHyperlinkFormatWhenStoreOverlayOpensFromShopPageExpectMyStoreHyperlinkWithNewFormat() {
    	
        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.MY_STORE_GLOBAL_NAV));
        final ProductGroup expectedProductGroup = dataService.findProductGroup("simple-pip","attributes","swatch-selection");
        Store store = dataService.findStore("store-search");
        String postalCode = store.getAddress().getUnmappedAttribute("zip");
        String storeURL = store.getStoreId();

        // when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, expectedProductGroup.getUnmappedAttribute("categoryUrl"));
        categoryPage.waitForCategoryPageLoad();
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        navigationSection.clickMyStoreFromHeader();
        MyStoreOverlay mystoreoverlay = StoreFlows.chooseStore(context, postalCode);
        navigationSection.clickMyStoreFromHeader();
        String actualHyperlink = mystoreoverlay.getHrefValueOfStoreUrl();

        // then
        assertTrue("The Store locator is not having valid hyperlink", actualHyperlink.contains(storeURL));

        // when
        mystoreoverlay.clickMyStoreNameHyperlink();
        String currentURL = context.getPilot().getDriver().getCurrentUrl();

        // then
        assertTrue("Store Landing page is not displaying by click on store name", currentURL.contains(storeURL));
    }
    
    /**
     * Verify the selected My Store hyperlink with new format navigated from Search page.
     *
     * Test Case ID: RGSN-38032
     */
    @Test
    public void testMyStoreHyperlinkFormatWhenStoreOverlayOpensFromSearchPageExpectMyStoreHyperlinkWithNewFormat() {

        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.MY_STORE_GLOBAL_NAV));
        ProductGroup searchItem = dataService.findProductGroup("simple-pip");
        String searchCriteria = searchItem.getGroupId().replace("-", " ");
        Store store = dataService.findStore("store-search");
        String postalCode = store.getAddress().getUnmappedAttribute("zip");
        String storeURL = store.getStoreId();

        // when
        SearchResultsPage searchResultsPage = context.getPage(SearchResultsPage.class);
        String searchResultsQueryParameter = SearchResultsUrlBuilder.constructSearchResultsQueryParameter(searchCriteria);
        URI uri = SearchResultsUrlBuilder.buildSearchResultsUri(searchResultsQueryParameter);
        context.getPilot().callEntryPoint(uri, SearchResultsPage.class);
        searchResultsPage.waitForSearchResultsHeading();
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        navigationSection.clickMyStoreFromHeader();
        MyStoreOverlay mystoreoverlay = StoreFlows.chooseStore(context, postalCode);
        navigationSection.clickMyStoreFromHeader();
        String newHyperlink = mystoreoverlay.getHrefValueOfStoreUrl();

        // then
        assertTrue("The Store locator is not having valid hyperlink", newHyperlink.contains(storeURL));

        // when
        mystoreoverlay.clickMyStoreNameHyperlink();
        String currentURL = context.getPilot().getDriver().getCurrentUrl();

        // then
        assertTrue("Store Landing page is not displaying by clickon store name", currentURL.contains(storeURL));
    }

    /**
     * Verify the Store name, Mystore label, GiveUsFeedback and Print buttons are present on My Store Landing page.
     *
     * Test Case ID - RGSN-38030
     */
    @Test
    public void testStoreLandingPageComponentsWhenUserOnStoreLandingPageExpectStoreNameMystoreLableGiveUsFeedbackLinksPresent() {

        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.MY_STORE_GLOBAL_NAV));
        final ProductGroup expectedProductGroup = dataService.findProductGroup("simple-pip","attributes","swatch-selection");
        Store store = dataService.findStore("store-search");
        String postalCode = store.getAddress().getUnmappedAttribute("zip");
        String withInmiles = store.getUnmappedAttribute("withInMiles");
        String storeName = store.getUnmappedAttribute("storeName");

        // when
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        MyStoreDetailsPage myStoreDetailPage = context.getPageSection(MyStoreDetailsPage.class);
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, expectedProductGroup.getUnmappedAttribute("categoryUrl"));
        categoryPage.waitForCategoryPageLoad();
        navigationSection.clickMyStoreFromHeader();
        MyStoreOverlay mystoreoverlay = StoreFlows.chooseStore(context, postalCode);
        StoreLocatorPage storeLocatorPage = context.getPage(StoreLocatorPage.class);
        context.getPilot().startAt(storeLocatorPage);
        storeLocatorPage.enterLocation(postalCode);
        storeLocatorPage.selectWithInMiles(withInmiles);
        storeLocatorPage.clickFindStoreSearchButton();
        storeLocatorPage.clickStoreDetailsEventsButton();

        // then
        String actualStoreNameDisplayed = myStoreDetailPage.getStoreNameHeadingText();
        assertTrue("The Store name is displaying incorrect", actualStoreNameDisplayed.contains(storeName));
        assertTrue("My store lable is not displaying on MyStore landing page", myStoreDetailPage.isMyStoreLableDisplayed());
        assertTrue("GiveusFeedBack link is not displaying on MyStore landing page", myStoreDetailPage.isGiveUsFeedbackLinkDisplayed());
    }
    
    /**
     * Verify recommendations carousel is present on My Store Landing page.
     *
     * Test Case ID - RGSN-38030
     */
    @Test
    public void testRecommendationsWhenUserIsOnStoreLandingPageExpectRecommendationsCaroselPresent() {

        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.MY_STORE_GLOBAL_NAV));
        Store store = dataService.findStore("store-search");
        String postalCode = store.getAddress().getUnmappedAttribute("zip");
        String withInmiles = store.getUnmappedAttribute("withInMiles");

        // when
        StoreLocatorPage storeLocatorPage = context.getPage(StoreLocatorPage.class);
        MyStoreDetailsPage myStoreDetailPage = context.getPageSection(MyStoreDetailsPage.class);
        context.getPilot().startAt(storeLocatorPage);
        storeLocatorPage.enterLocation(postalCode);
        storeLocatorPage.selectWithInMiles(withInmiles);
        storeLocatorPage.clickFindStoreSearchButton();
        storeLocatorPage.clickStoreDetailsEventsButton();

        // then
        assertTrue("Recommendation carousel is not displaying on Store Landing page.", myStoreDetailPage.isRecommendationsCarouselDisplayed());
    }

    /**
     * Verify address text hyperlink navigation on store landing page.
     *
     * Test Case ID - RGSN-38030
     */
    @Test
    public void testAddressLinkNavigationWhenUserIsOnStoreLandingPageExpectStoreMapPresent() {

        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.MY_STORE_GLOBAL_NAV));
        Store store = dataService.findStore("store-search");
        String postalCode = store.getAddress().getUnmappedAttribute("zip");
        String withInmiles = store.getUnmappedAttribute("withInMiles");
        String expectedBingMapURL = "https://www.bing.com/maps";

        // when
        StoreLocatorPage storeLocatorPage = context.getPage(StoreLocatorPage.class);
        MyStoreDetailsPage myStoreDetailPage = context.getPageSection(MyStoreDetailsPage.class);
        context.getPilot().startAt(storeLocatorPage);
        storeLocatorPage.enterLocation(postalCode);
        storeLocatorPage.selectWithInMiles(withInmiles);
        storeLocatorPage.clickFindStoreSearchButton();
        storeLocatorPage.clickStoreDetailsEventsButton();
        myStoreDetailPage.clickStoreAddressLink();

        // then
        String actualBingMapURL = myStoreDetailPage.getBingMapURL();
        assertTrue("User is not navigated to the expected Bing map URL", actualBingMapURL.contains(expectedBingMapURL));
    }
    
    /**
     * Verify whether the store details page url is in new format.
     *
     * Test Case ID - RGSN-38726
     */
    @Test
    public void testStoreDetailsPageWhenUserOnStoreLandingPageExpectUrlWithNewFormatStoreDescriptionAndReturnToStoreResultsComponentsDisplay() {

        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.MY_STORE_GLOBAL_NAV));
        Store store = dataService.findStore("store-search");
        String postalCode = store.getAddress().getUnmappedAttribute("zip");
        String withInmiles = store.getUnmappedAttribute("withInMiles");
        String storeURL = store.getStoreId();

        // when
        StoreLocatorPage storeLocatorPage = context.getPage(StoreLocatorPage.class);
        MyStoreDetailsPage myStoreDetailPage = context.getPageSection(MyStoreDetailsPage.class);
        context.getPilot().startAt(storeLocatorPage);
        storeLocatorPage.enterLocation(postalCode);
        storeLocatorPage.selectWithInMiles(withInmiles);
        storeLocatorPage.clickFindStoreSearchButton();
        storeLocatorPage.clickStoreDetailsEventsButton();
        String actualHyperlink = context.getPilot().getDriver().getCurrentUrl();

        // then
        assertTrue("The Store locator is not having valid hyperlink", actualHyperlink.contains(storeURL));
        assertTrue("The Store locator page should have Return to Store Results breadcrumb", myStoreDetailPage.isReturnToStoreResultsBreadcrumbDisplayed());
        assertTrue("The Store locator page should have StoreDescription", myStoreDetailPage.isStoreDescriptionDisplayed());
    }

    /**
     * Verify whether the print button is displaying for store details page.
     *
     * Test Case ID - RGSN-38726
     */
    @Test
    public void testStoreDetailsPageWhenUserOnStoreLandingPageExpectPrintButtondisplay() {

        // given
        Assume.assumeTrue(context.isFullSiteExperience() && context.getTargetSite().supportsFeature(FeatureFlag.MY_STORE_GLOBAL_NAV));
        Store store = dataService.findStore("store-search");
        String postalCode = store.getAddress().getUnmappedAttribute("zip");
        String withInmiles = store.getUnmappedAttribute("withInMiles");

        // when
        StoreLocatorPage storeLocatorPage = context.getPage(StoreLocatorPage.class);
        MyStoreDetailsPage myStoreDetailPage = context.getPageSection(MyStoreDetailsPage.class);
        context.getPilot().startAt(storeLocatorPage);
        storeLocatorPage.enterLocation(postalCode);
        storeLocatorPage.selectWithInMiles(withInmiles);
        storeLocatorPage.clickFindStoreSearchButton();
        storeLocatorPage.clickStoreDetailsEventsButton();

        // then
        assertTrue("Print Button is not displaying on MyStore landing page", myStoreDetailPage.isPrintButtonDisplayed());
    }
}
