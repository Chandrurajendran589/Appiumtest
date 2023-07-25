package com.wsgc.ecommerce.ui.regression.recommendation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Assume;
import org.junit.Test;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.account.RecommendationsPage;
import com.wsgc.ecommerce.ui.pagemodel.customerservice.StoreLandingPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.ecommerce.ui.regression.stores.syndication.StoresUrlBuilder;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.ProductGroup;
import com.wsgc.evergreen.entity.api.Store;

import groovy.lang.Category;
import tag.area.ShopArea;

/**
 * Regression Tests for Validating the elements of recommendations carousel.
 * 
 */
@Category(ShopArea.class)
public class RecommendationsRegression extends AbstractTest {
	
    /**
     * Verifies if user is able to navigate to the recommendations page on clicking 
     * see more recommendations link from store landing page.
     * Test Case ID - RGSN-38037
     */
    @Test
    @TestRail(id = "104360")
    public void testStoreLandingPageWhenUserClicksSeeMoreRecommendationsExpectRecommendationsPage() {

        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.STORE_LANDING_PAGE) 
                 && context.isFullSiteExperience());

        // given
        List<ProductGroup> expectedProductGroups = dataService.findProductGroups("recently-viewed");
        final String EXPECTED_STORES_RECSTART_PARAM = "cm_sp=MyRecs-_-Carousel-_-WsiStoreDetailsRVI";
        final Store store = dataService.findStore("store-with-recently-viewed-items");

        // when
        for (ProductGroup productGroup : expectedProductGroups) {
            ShopFlows.goToPipPageFlow(context, productGroup);
        }
        
        StoreLandingPage storeLandingPage = context.getPage(StoreLandingPage.class);
        URI uri = StoresUrlBuilder.buildStoreLandingUri(store.getStoreId());
        context.getPilot().callEntryPoint(uri, StoreLandingPage.class);

        //then
        RecommendationsPage recommendationsPage = storeLandingPage.clickSeeMoreRecommendationsFromRecentlyViewedCarousel();
        String recommendationsPageUrl = recommendationsPage.getCurrentPageUrl();
        assertTrue(String.format("Recommendations page url - %s - doesn't contain RVI query parameter, %s",
        		recommendationsPageUrl, EXPECTED_STORES_RECSTART_PARAM), 
        		recommendationsPageUrl.contains(EXPECTED_STORES_RECSTART_PARAM));       
    }

    /**
     * Verifies if user is able to navigate to the product page on clicking a 
     * recently viewed product from store landing page.
     * Test Case ID - RGSN-38037
     */
    @Test
    @TestRail(id = "104361")
    public void testStoreLandingPageWhenUserClicksRecentlyViewedProductExpectProductPage() {

        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.STORE_LANDING_PAGE));

        // given
        List<ProductGroup> expectedProductGroups = dataService.findProductGroups("recently-viewed");
        final String EXPECTED_STORES_RECSTART_PARAM = "cm_src=WsiStoreDetailsRVI";
        final Store store = dataService.findStore("store-with-recently-viewed-items");

        // when
        for (ProductGroup productGroup : expectedProductGroups) {
            ShopFlows.goToPipPageFlow(context, productGroup);
        }
        
        StoreLandingPage storeLandingPage = context.getPage(StoreLandingPage.class);
        URI uri = StoresUrlBuilder.buildStoreLandingUri(store.getStoreId());
        context.getPilot().callEntryPoint(uri, StoreLandingPage.class);
        

        //then
        int recentlyViewedItemsAvailable = storeLandingPage.getNumberOfRecentlyViewedItemsAvailable();
        
        int recentlyViewedItemIndex = RandomUtils.nextInt(0, recentlyViewedItemsAvailable - 1);
        assertTrue("Product Image is not displayed in recently viewed carousel", 
        		storeLandingPage.isRecentlyViewedProductImageDisplayed(recentlyViewedItemIndex));
        assertTrue("Product Name is not displayed in recently viewed carousel", 
        		storeLandingPage.isRecentlyViewedProductNameDisplayed(recentlyViewedItemIndex));
        assertTrue("Product Price is not displayed in recently viewed carousel", 
        		storeLandingPage.isRecentlyViewedProductPriceDisplayed(recentlyViewedItemIndex));
        ProductPage productPage = storeLandingPage.clickRecentlyViewedItem(recentlyViewedItemIndex);
        String productPageUrl = productPage.getCurrentPageUrl();
        assertTrue(String.format("Product page url - %s - doesn't contain RVI query parameter, %s",
                productPageUrl, EXPECTED_STORES_RECSTART_PARAM), 
                productPageUrl.contains(EXPECTED_STORES_RECSTART_PARAM));       
    }
}
