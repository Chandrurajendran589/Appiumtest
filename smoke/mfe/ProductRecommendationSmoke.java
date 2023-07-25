package com.wsgc.ecommerce.ui.smoke.mfe;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.evergreen.entity.api.ProductGroup;

/**
 * Smoke Tests for validating WSI recommendations in site.
 */
public class ProductRecommendationSmoke extends AbstractTest {

    /**
     * This test is intended to verify that WSI recommendation products 
     * are loaded in Recommendation Carousel in a PIP. This test also checks to see 
     * if the user is able to click on a recommendation product 
     * from PIP and if the user is taken to the PIP of the respective recommendation product.
     */
    @Test
    public void testRecommendationProductWhenNavigatedFromPipExpectRecommendationCarouselPresent() {
        //given
        final ProductGroup expectedProductGroup = dataService.findProductGroup("simple-pip-mfe", "!attributes");
        final String EXPECTED_RECOMMENDATION_WSI_PARAM = "?cm_src=Wsi";
        int indexOfProductRecommendationToSelect = 1;
        List<WebElement> productRecommendationUrls;
        String productRecommendationUrl;

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        productRecommendationUrls = productPage.getProductRecommendationUrls();
        productRecommendationUrl = productRecommendationUrls.get(0).getAttribute("href");
        
        assertTrue(String.format("Product url %s doesn't contain WSI recs query parameter, %s", productRecommendationUrl, EXPECTED_RECOMMENDATION_WSI_PARAM), 
        		productRecommendationUrl.contains(EXPECTED_RECOMMENDATION_WSI_PARAM));
        productPage.selectProductRecommendation(indexOfProductRecommendationToSelect);
        String actualRecommendationUrl = context.getPilot().getDriver().getCurrentUrl();
        assertTrue("Product url doesn't contain WSI recs query parameter", actualRecommendationUrl.contains(EXPECTED_RECOMMENDATION_WSI_PARAM));
    }
}
