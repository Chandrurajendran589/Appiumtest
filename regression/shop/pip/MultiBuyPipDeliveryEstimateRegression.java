package com.wsgc.ecommerce.ui.regression.shop.pip;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.wsgc.ecommerce.ui.endtoend.TestRail;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.shop.PipFlows;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.shop.Product;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductSubsetRepeatableSection;
import com.wsgc.ecommerce.ui.utils.ProductGroupUtility;
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
public class MultiBuyPipDeliveryEstimateRegression extends AbstractTest {
    
    ProductPage productPage;
    ArrayList<Product> productData;
    List<ProductSubsetRepeatableSection> subsetSectionList;
    ProductSubsetRepeatableSection section;
    
    /**
     * Prepares data fixtures & ensures feature flag is enabled.
     */
    @Before
    public void setUp() {
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.MULTI_BUY_PIP));
        // given
        final ProductGroup expectedProductGroup = dataService
                .findProductGroup(productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null,
                                  "multi-buy-multi-sku-pip");

        //when
        productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        subsetSectionList = productPage.getSubsetsOnPIP();
        section = subsetSectionList.get(0);
        Map<String, List<Object>> sectionDataList = ProductGroupUtility
                .getMappedSectionAttributes(productPage.getProductGroup().getUnmappedAttributes());
        List<Object> sectionData = sectionDataList.get(section.getSubsetName().trim().toLowerCase());
        Map<String, String> sectionAttributes = (LinkedHashMap) sectionData.get(ProductGroupUtility.MULTIBUY_UNMAPPED_DATA_ATTRIBUTE_INDEX);
        section.setAttributeSelections(sectionAttributes);
    }

    /**
     * Verifies whether a user is presented with an error message when the delivery 
     * estimate zip code is submitted invalid numerals.
     * Test Case ID - RGSN-38226
     */
    @Test
    @TestRail(id = "104630")
    public void testDeliveryEstimateSectionWhenUserSubmitsInvalidZipCodeExpectNoDeliveryEstimateMessage() {
        
        //given
        final String invalidZipCode = "00000";
        final String expectedErrorMessageText = "This is not a valid ZIP code - please try again.";

        // when
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
    @TestRail(id = "104632")
    public void testDeliveryEstimateSectionWhenUserSubmitsInvalidZipCodeExpectErrorMessage() {
        
        //given
        final String invalidZipCode = "a#2g*";
        final String expectedErrorMessageText = "The zip code you have entered contains invalid characters.";

        // when
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
    @TestRail(id = "104628")
    public void testDeliveryEstimateSectionWhenUserSubmitsValidZipCodeExpectDeliveryEstimateMessage() {

        //given
        CustomerAccount account = dataService.findAccount("add-address");
        final String validZipCode = account.getShippingAddresses().get(0).getZip();
        final String expectedDeliveryEstimateTextPattern = "Arrives\\s[A-Z]{1}[a-z]{2}\\.\\s[0-9]"
                 + "{1,2}\\s-\\s[A-Z]{0,1}[a-z]{0,12}\\.{0,1}\\s{0,1}[0-9]{1,2}";

        // when
        PipFlows.setZipCodeForDeliveryEstimate(section, validZipCode);
        String actualDeliveryEstimateText = PipFlows.setZipCodeForDeliveryEstimate(section, validZipCode);
        
        //then
        assertTrue(String.format("Delivery Estimate Message is not valid - %s", actualDeliveryEstimateText), 
                actualDeliveryEstimateText.matches(expectedDeliveryEstimateTextPattern));     
    }
}
