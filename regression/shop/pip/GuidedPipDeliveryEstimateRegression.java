package com.wsgc.ecommerce.ui.regression.shop.pip;

import static org.junit.Assert.assertTrue;

import com.wsgc.ecommerce.ui.endtoend.TestRail;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.shop.PipFlows;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.registry.GuidedPipFlows;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductSubsetRepeatableSection;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.CustomerAccount;
import com.wsgc.evergreen.entity.api.ProductGroup;

import groovy.lang.Category;
import tag.area.ShopArea;

/**
 * Regression Tests for Validating the delivery estimate on Simple PIP page.
 * 
 */
@Category(ShopArea.class)
public class GuidedPipDeliveryEstimateRegression extends AbstractTest {
    
    ProductPage productPage;
    
    /**
     * Prepares data fixtures & ensures feature flag is enabled.
     */
    @Before
    public void setUp() {
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP) 
                || context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP_REDESIGN));
        // given
        final ProductGroup expectedProductGroup = dataService
                .findProductGroup(productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null,
                                  "guided-pip", "order-free-swatch-flyout");
        final String expectedQuantity = "1";

        //when
        productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        GuidedPipFlows.setGuidedPipQuantityAndAttributes(context, String.valueOf(expectedQuantity));
    }

    /**
     * Verifies whether a user is presented with an error message when the delivery 
     * estimate zip code is submitted invalid numerals.
     * Test Case ID - RGSN-38226
     */
    @Test
    @TestRail(id = "104625")
    public void testDeliveryEstimateSectionWhenUserSubmitsInvalidZipCodeExpectNoDeliveryEstimateMessage() {
        
        //given
        final String invalidZipCode = "00000";
        final String expectedErrorMessageText = "This is not a valid ZIP code - please try again.";

        // when
        ProductSubsetRepeatableSection section = productPage.getGuidedProductItemRepeatableSection();
        PipFlows.setZipCodeForDeliveryEstimate(section, invalidZipCode);
        String actualErrorMessageText = section.getZipCodeErrorText().trim();
        
        //then
        Assert.assertEquals("Expected Error Message text '" + expectedErrorMessageText + "' is not matching with '"
                + actualErrorMessageText + "' - actual Error Message text ", expectedErrorMessageText, actualErrorMessageText);
        
    }
    
    /**
     * Verifies whether a user is presented with an error message when the delivery 
     * estimate zip code is submitted invalid.
     * Test Case ID - RGSN-38226
     */
    @Test
    @TestRail(id = "104626")
    public void testDeliveryEstimateSectionWhenUserSubmitsInvalidZipCodeExpectErrorMessage() {
        
        //given
        final String invalidZipCode = "a#2g*";
        final String expectedErrorMessageText = "The zip code you have entered contains invalid characters.";

        // when
        ProductSubsetRepeatableSection section = productPage.getGuidedProductItemRepeatableSection();
        PipFlows.setZipCodeForDeliveryEstimate(section, invalidZipCode);
        String actualErrorMessageText = section.getZipCodeErrorText().trim();
        
        //then
        Assert.assertEquals("Expected Error Message text '" + expectedErrorMessageText + "' is not matching with '"
                + actualErrorMessageText + "' - actual Error Message text ", expectedErrorMessageText, actualErrorMessageText);
        
    }
    
    /**
     * Verifies whether a user is presented with a delivery estimate when the delivery 
     * estimate zip code submitted is valid.
     * Test Case ID - RGSN-38226
     */
    @Test
    @TestRail(id = "104627")
    public void testDeliveryEstimateSectionWhenUserSubmitsValidZipCodeExpectDeliveryEstimateMessage() {

        //given
        CustomerAccount account = dataService.findAccount("add-address");
        final String validZipCode = account.getShippingAddresses().get(0).getZip();
        final String expectedDeliveryEstimateTextPattern = "Arrives\\s[A-Z]{1}[a-z]{2}\\.\\s[0-9]"
                 + "{1,2}\\s-\\s[A-Z]{0,1}[a-z]{0,12}\\.{0,1}\\s{0,1}[0-9]{1,2}";

        // when
        ProductSubsetRepeatableSection section = productPage.getGuidedProductItemRepeatableSection();
        String actualDeliveryEstimateText = PipFlows.setZipCodeForDeliveryEstimate(section, validZipCode);
        
        //then
        assertTrue(String.format("Delivery Estimate Message is not valid - %s", actualDeliveryEstimateText), 
                actualDeliveryEstimateText.matches(expectedDeliveryEstimateTextPattern));     
    }
}
