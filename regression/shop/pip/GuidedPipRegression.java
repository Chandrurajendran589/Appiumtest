package com.wsgc.ecommerce.ui.regression.shop.pip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.registry.GuidedPipFlows;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.ProductGroup;
import groovy.lang.Category;
import tag.area.ShopArea;

/**
 * Regression Tests for validating Guided PIPs.
 */
@Category(ShopArea.class)
public class GuidedPipRegression extends AbstractTest {

    private final Logger log = LoggerFactory.getLogger(getClass());
	
    /**
     * Prepares data fixtures & ensures feature flag is enabled.
     */
    @Before
    public void setUp() {
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP) 
                || context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP_REDESIGN));
    }

    /**
     * Verifies whether hero image is displayed in a guided pip.
     * Test Case ID - RGSN-38245
     */
    @Test
    @TestRail(id = "103004")
    public void testGuidedPipElementsWhenUserVisistsGuidedPipExpectHeroImage() {
	
        // given
        ProductGroup productGroup = dataService.findProductGroup("guided-pip-favorite");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
    
        //then
        assertTrue("Hero Image is not displayed ", productPage.isHeroImagePresent());
	}
	
    /**
     * Verifies whether alternate image is displayed in a guided pip.
     * Test Case ID - RGSN-38245
     */
    @Test
    @TestRail(id = "103005")
    public void testGuidedPipElementsWhenUserVisistsGuidedPipExpectAlternateImage() {
        Assume.assumeTrue(context.isFullSiteExperience());
        // given
        ProductGroup productGroup = dataService.findProductGroup("guided-pip-favorite");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);

        //then
        assertTrue("Alternate Image is not displayed ", productPage.isAlternateImagePresent());
	}
	
    /**
     * Verifies whether print icon is displayed in a guided pip.
     * Test Case ID - RGSN-38245
     */
    @Test
    @TestRail(id = "103006")
    public void testGuidedPipElementsWhenUserVisistsGuidedPipExpectPrintIcon() {
        Assume.assumeTrue(context.isFullSiteExperience());
        // given
        ProductGroup productGroup = dataService.findProductGroup("guided-pip-favorite");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);

        //then
        assertTrue("Print icon is not displayed ", productPage.isPrintIconPresent());
	}

    /**
     * Verifies whether add to registry button is displayed in a guided pip.
     * Test Case ID - RGSN-38245
     */
    @Test
    @TestRail(id = "103007")
    public void testGuidedPipElementsWhenUserVisistsGuidedPipExpectAddToRegistryButton() {
        Assume.assumeTrue(context.isFullSiteExperience() &&  context.supportsFeature(FeatureFlag.REGISTRY_V2));
        // given
        ProductGroup productGroup = dataService.findProductGroup("guided-pip-favorite");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);

        //then
        assertTrue("Add to registry button is not displayed ", productPage.isAddToRegistryButtonPresent());
	}

    /**
     * Verifies whether user able to see an error message on clicking add to cart button 
     * without selecting any attributes.
     * Test Case ID - RGSN-38249
     */
    @Test
    @TestRail(id = "103008")
    public void testGuidedPipWhenUserClicksOnAddToCartWithoutSelectingAttributesExpectErrorMessage() {

        //given
        final ProductGroup expectedProductGroup = dataService
                     .findProductGroup(productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null,
                     "guided-pip", "attributes", "swatch-selection");
        String expectedErrorMessage = "Please make your selection";

        //when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        productPage.clickAddToCart();
        String actualErrorMessage = productPage.getItemSelectionErrorMessage();
        
        //then
        assertTrue(String.format("The expected error message %s is not matching with the actual error message %s",
                expectedErrorMessage, actualErrorMessage), actualErrorMessage.contains(expectedErrorMessage));
    }
 
    /**
     * Verifies whether user able to see an error message on clicking add to registry 
     * without selecting any attributes.
     * Test Case ID - RGSN-38249
     */
    @Test
    @TestRail(id = "103009")
    public void testGuidedPipWhenUserClicksOnAddToRegistryWithoutSelectingAttributesExpectErrorMessage() {

        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.REGISTRY_V2));

        //given
        final ProductGroup expectedProductGroup = dataService
                     .findProductGroup(productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null,
                     "guided-pip", "attributes", "swatch-selection");
        String expectedErrorMessage = "Please make your selection";

        //when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        productPage.clickAddToRegistry();
        String actualErrorMessage = productPage.getItemSelectionErrorMessage();
        
        //then
        assertTrue(String.format("The expected error message %s is not matching with the actual error message %s",
                expectedErrorMessage, actualErrorMessage), actualErrorMessage.contains(expectedErrorMessage));
    }
    
    /**
     * Verifies whether user able to see an error message on clicking add to cart button 
     * on setting quantity to zero.
     * Test Case ID - RGSN-38249
     */
    @Test
    @TestRail(id = "103010")
    public void testGuidedPipWhenUserClicksOnAddToCartWithQuantitySetToZeroExpectErrorMessage() {

        //given
        final ProductGroup expectedProductGroup = dataService
                     .findProductGroup(productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null,
                     "guided-pip", "attributes", "swatch-selection");
        String expectedErrorMessage = "Please enter a quantity between 1 and 99";
        final String expectedQuantity = "0";

        //when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        GuidedPipFlows.setGuidedPipQuantityAndAttributes(context, String.valueOf(expectedQuantity));
        productPage.clickAddToCart();
        String actualErrorMessage = productPage.getItemSelectionErrorMessage();
        
        //then
        assertTrue(String.format("The expected error message %s is not matching with the actual error message %s",
                expectedErrorMessage, actualErrorMessage), actualErrorMessage.contains(expectedErrorMessage));
    }
    
    /**
     * Verifies whether user able to see an error message on clicking add to registry
     * on setting quantity to zero.
     * Test Case ID - RGSN-38249
     */
    @Test
    @TestRail(id = "103011")
    public void testGuidedPipWhenUserClicksOnAddToRegistryWithQuantitySetToZeroExpectErrorMessage() {

        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.REGISTRY_V2));

        //given
        final ProductGroup expectedProductGroup = dataService
                     .findProductGroup(productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null,
                     "guided-pip", "attributes", "swatch-selection");
        String expectedErrorMessage = "Please enter a quantity between 1 and 99";
        final String expectedQuantity = "0";

        //when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        GuidedPipFlows.setGuidedPipQuantityAndAttributes(context, String.valueOf(expectedQuantity));
        productPage.clickAddToRegistry();
        String actualErrorMessage = productPage.getItemSelectionErrorMessage();
        
        //then
        assertTrue(String.format("The expected error message %s is not matching with the actual error message %s",
                expectedErrorMessage, actualErrorMessage), actualErrorMessage.contains(expectedErrorMessage));
    }
    
    /**
     * Verifies if the user is able to see the select option text above the accordion in
     * Guided pip page.
     * Test Case ID - RGSN-38246
     */
    @Test
    @TestRail(id = "106714")
    public void testGuidedPipWhenUserVisitsProductPageExpectSelectOptionTextAboveAccordion() {
    	
        // given
        final ProductGroup expectedProductGroup = dataService.findProductGroup("guided-pip", "attributes", "swatch-selection");
        String expectedStepSelectionText;
        final Map<String, String> expectedStepSelectionTextTemplate = new HashMap<>();
        expectedStepSelectionTextTemplate.put("WE", "Select Options 1 and %s");
        expectedStepSelectionTextTemplate.put("PK", "Select Options 1 and %s");
        expectedStepSelectionTextTemplate.put("WS", "Select Options 1 to %s");
        expectedStepSelectionTextTemplate.put("PT", "Select Options 1 to %s");
        expectedStepSelectionTextTemplate.put("PB", "Select Options 1 and %s");
        
        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        int stepContainerCount = productPage.getGuidedPipStepContainer().size();
        if (stepContainerCount == 1) {
            expectedStepSelectionText = "Select Option";
        } else {
            expectedStepSelectionText = String.format(expectedStepSelectionTextTemplate.
                    get(context.getBrand().getCode()), productPage.getGuidedPipStepContainer().size());
        }
        String actualStepSelectionText = productPage.getStepSelectionTextMessage();
        
        // then
        assertTrue(String.format("The expected select option text above the accordion %s is not matching with "
                + "actual text message %s", expectedStepSelectionText, actualStepSelectionText),
                actualStepSelectionText.equalsIgnoreCase(expectedStepSelectionText));
    }
    
    /**
     * Verifies if the user is able to see the color wheel palette icon in guided pip page.
     * Test Case ID - RGSN-38346
     */
    @Test
    @TestRail(id = "106715")
    public void testGuidedPipWhenUserVisitsProductPageExpectColorWheelIcon() {
    	
        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.COLOR_WHEEL_PALETTE));
        final ProductGroup expectedProductGroup = dataService.findProductGroup("guided-pip", "attributes", "swatch-selection");
        
        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        productPage.clickSelectFabricAndColorAccordion();
        
        // then
        assertTrue("Color wheel palette icon is not displayed in fabric accordion header",
                productPage.isColorWheelPaletteIconPresentAndDisplayed());
    }
    
    /**
     * Verifies if the user is able to see order free swatch flyout is opened while clicking on order swatch button in guided pip.
     * Test Case ID - RGSN-38246
     */
    @Test
    @TestRail(id = "106716")
    public void testGuidedPipWhenUserClickOrderSwatchButtonFromSwatchContainerExpectedOrderFreeSwatchFlyoutOpened() {
        
        // given
        Assume.assumeTrue(context.isFullSiteExperience());
        ProductGroup expectedProductGroup = dataService.findProductGroup("guided-pip", "attributes", "swatch-selection");
        final String expectedQuantity = "1";
        
        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        GuidedPipFlows.setGuidedPipQuantityAndAttributes(context, String.valueOf(expectedQuantity));
        productPage.clickSelectedSwatch();
        productPage.clickOrderSwatchButtonOnSwatchDetails();
        
        // then
        if (productPage.isPipMfePresent()) {
            assertTrue("Order Free swatch FlyOut is not displayed", productPage.isOrderFreeSwatchFlyoutDisplayed());
        } else {
            assertTrue("Order Free swatch Overaly is not displayed", productPage.isRequestSwatchOverlayDisplayed());
        }
    }
    
    /**
     * Verifies if the user is able to select filter from fabric and color accordion and filtered swatches are
     * displayed in guided pip page.
     * Test Case ID - RGSN-38246
     */
    @Test
    @TestRail(id = "106742")
    public void testGuidedPipWhenUserSelectFilterFromFabricAndColorAccordionExpectFilteredSwatchesDisplayed() {
        
        // given
        ProductGroup expectedProductGroup = dataService.findProductGroup("guided-pip", "attributes", "swatch-selection");
        String numberOfSwatchesForSelectedFacetValue = null;
        
        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        productPage.clickSelectFabricAndColorAccordion();
        if (context.isMobileExperience()) {
            productPage.clickFilterButton();
        }
        WebElement swatchFilter = productPage.clickSwatchFilterButton(0);
        WebElement swatchFilterItem = productPage.clickSwatchFilterItem(swatchFilter, 1);
        numberOfSwatchesForSelectedFacetValue =  productPage.getCountFromSelectedFacetValue(swatchFilterItem);
        if (context.isMobileExperience()) {
            productPage.clickApplyButtonFromFilterDrawer();
        }
        int numberOfSwatchProductsAfterFiltering = productPage.getTotalNumberOfSwatchProducts();

        // then
        assertEquals("Incorrect number of swatches are filtered upon facet selection", 
                numberOfSwatchesForSelectedFacetValue, String.valueOf(numberOfSwatchProductsAfterFiltering));
    }
    
    /**
     * Verifies if the user is not able to see the swatch copy and product summary section is not displayed
     * in guided pip page without selecting attributes.
     * Test Case ID - RGSN-38247
     */
    @Test
    @TestRail(id = "106819")
    public void testGuidedPipWhenUserVisitGuidedPipWithoutSelectingAttributesExpectNoSwatchCopyBlockAndProductSummarySectionDisplayed() {
	    
        // given
        ProductGroup expectedProductGroup = dataService.findProductGroup("guided-pip", "attributes", "swatch-selection");
        
        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
       
        // then
        assertTrue("Swatch detailed copy section is displayed before selecting attributes",
                !productPage.isSwatchDetailCopySectionPresentAndDisplayed());
        assertTrue("Product summary section is displayed before selecting attributes",
                !productPage.isProductSelectionSummarySectionPresentAndDisplayed());
    }
   
    /**
     * Verifies if the user is able to see the swatch copy and product summary section is displayed
     * in guided pip page after selecting attributes.
     * Test Case ID - RGSN-38247
     */
    @Test
    @TestRail(id = "106820")
    public void testGuidedPipWhenUserVisitSelectsAttributesExpectProductSummarySectionAndSwatchCopyDisplayed() {
	     
        // given
        ProductGroup expectedProductGroup = dataService.findProductGroup("guided-pip", "attributes", "swatch-selection");
        final String expectedQuantity = "1";
        
        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        GuidedPipFlows.setGuidedPipQuantityAndAttributes(context, String.valueOf(expectedQuantity));
       
        // then
        assertTrue("Product summary section is not displayed after selecting attributes",
                productPage.isProductSelectionSummarySectionPresentAndDisplayed());
        
        // when
        productPage.clickSelectedSwatch();
        
        // then
        assertTrue("Swatch detailed copy section not is displayed after selecting attributes",
                productPage.isSwatchDetailCopySectionPresentAndDisplayed());
    }
    
    /**
     * Verifies if the user is able to clear the swatch filter selected in guided pip.
     * Test Case ID - RGSN-38246
     */
    @Test
    @TestRail(id = "106743")
    public void testGuidedPipWhenUserClicksClickClearAllButtonExpectFacetsSelectedAreCleared() {
    	
        // given
        ProductGroup expectedProductGroup = dataService.findProductGroup("guided-pip", "attributes", "swatch-selection");
        
        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        productPage.clickSelectFabricAndColorAccordion();
        if (context.isMobileExperience()) {
            productPage.clickFilterButton();
        }
        WebElement swatchFilter = productPage.clickSwatchFilterButton(0);
        productPage.clickSwatchFilterItem(swatchFilter, 1);
        if (context.isMobileExperience()) {
            productPage.clickApplyButtonFromFilterDrawer();
        }
        
        // then
        assertTrue("Clear all button is not displayed", productPage.isClearAllButtonDisplayed());
        
        // when
        productPage.clickClearAllButton();
        
        // then
        assertTrue("Clear all button is displayed", !productPage.isClearAllButtonDisplayed());
    }

    /**
     * Verifies if the user is able to see the selected attribute name in Step selector header.
     * Test Case ID - RGSN-38247
     */
    @Test
    @TestRail(id = "106821")
    public void testGuidedPipWhenUserSelectsAttributeExpectAttributeNameInStepSelectorHeader() {
    	
        // given
        ProductGroup expectedProductGroup = dataService.findProductGroup("guided-pip", "attributes", "swatch-selection");
        final String expectedQuantity = "1";
        Map<String, String> expectedAttributeValue = expectedProductGroup.getAttributes();
        
        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        GuidedPipFlows.setGuidedPipQuantityAndAttributes(context, String.valueOf(expectedQuantity));

        // then
        List<WebElement> getGuidedPipStepContainerAttributeValues = productPage.getGuidedPipStepContainerAttributeValue();
        for (Map.Entry<String,String> hinokiData : expectedAttributeValue.entrySet()){
            for (WebElement getGuidedPipStepContainerAttributeValue : getGuidedPipStepContainerAttributeValues) {
                boolean testPassed = true;
                if (!getGuidedPipStepContainerAttributeValue.getText().equalsIgnoreCase(hinokiData.getValue())) {
                    log.error("Selected Guided pip attribute is not displayed in accordion header.");
                    testPassed = false;
                }
                if (testPassed) {
                    assertTrue("Test step got failed, Please check the log for failed validation", testPassed);
                }
            }
        }
    }
    
    /**
     * Verifies if the user is able to see the selected attribute name in product summary section
     * at guided pip.
     * Test Case ID - RGSN-38247
     */
    @Test
    @TestRail(id = "106825")
    public void testGuidedPipWhenUserSelectsAttributeExpectAttributeNameInProductSummarySection() {
    	
        // given
        ProductGroup expectedProductGroup = dataService.findProductGroup("guided-pip", "attributes", "swatch-selection");
        final String expectedQuantity = "1";
        Map<String, String> expectedAttributeValue = expectedProductGroup.getAttributes();
        
        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        GuidedPipFlows.setGuidedPipQuantityAndAttributes(context, String.valueOf(expectedQuantity));

        // then
        List<WebElement> productSummarySecitonContainerAttributeValues = productPage.guidedPipProductSummarySecitonContainerAttributeValue();
        for (Map.Entry<String,String> hinokiData : expectedAttributeValue.entrySet()){
            for (WebElement productSummarySecitonContainerAttributeValue : productSummarySecitonContainerAttributeValues) {
                boolean testPassed = true;
                if (!productSummarySecitonContainerAttributeValue.getText().equalsIgnoreCase(hinokiData.getValue())) {
                    log.error("Selected Guided pip attribute is not displayed in product summary section.");
                    testPassed = false;
                }
                if (testPassed) {
                    assertTrue("Test step got failed, Please check the log for failed validation", testPassed);
                }
            }
        }
    }
    
    /**
     * Verifies if the user is not able to see the product summary section and the selected attributes are removed
     * after clicking on clear all options link in guided pip.
     * Test Case ID - RGSN-38247
     */
    @Test
    @TestRail(id = "106828")
    public void testGuidedPipWhenUserClicksClearAllOptionsLinkExpectNoAttributesSelectedAndProductSummarySectionDisplayed() {
    	
        // given
        ProductGroup expectedProductGroup = dataService.findProductGroup("guided-pip", "attributes", "swatch-selection");
        final String expectedQuantity = "1";
        
        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        GuidedPipFlows.setGuidedPipQuantityAndAttributes(context, String.valueOf(expectedQuantity));
        productPage.clickClearAllOptions();
        
        // then
        assertTrue("Attributes are selected after clearing the selections",
                !productPage.isAttributesSelectedInGuidedPip());
        assertTrue("Product summary section is displayed after selecting attributes",
                !productPage.isProductSelectionSummarySectionPresentAndDisplayed());
    }
    
    /**
     * Verifies if the user is able to make a new attribute selection in guided pip page.
     * Test Case ID - RGSN-38247
     */
    @Test
    @TestRail(id = "106829")
    public void testGuidedPipWhenUserSelectsNewAttributeSectionExpectProductSkuForTheSelectedAttributes() {
    	
        // given
        ProductGroup expectedProductGroup = dataService.findProductGroup("guided-pip", "attributes", "swatch-selection");
        final String expectedQuantity = "1";
        String expectedSkuAfterNewAttributeSelectionMade = expectedProductGroup.getSkus().get(0);
        
        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup, 
                expectedProductGroup.getSkus().get(1));
        GuidedPipFlows.setGuidedPipQuantityAndAttributes(context, String.valueOf(expectedQuantity));
        
        // then
        Assert.assertTrue(String.format("SKU of the product selected \'" + productPage.getProductSkuFromPIP() 
                + "\' is not matching with the expected SKU \'" + expectedSkuAfterNewAttributeSelectionMade + "\'"), 
                expectedSkuAfterNewAttributeSelectionMade.equalsIgnoreCase(productPage.getProductSkuFromPIP()));
        assertTrue("Product summary section is displayed after selecting attributes",
                productPage.isProductSelectionSummarySectionPresentAndDisplayed());
    }
    
    /**
     * Verifies if the user is able to see the swatch selected in the delivery view is selected
     * in fabric view at swatch container.
     * Test Case ID - RGSN-38251
     */
    @Test
    @TestRail(id = "106830")
    public void testGuidedPipWhenUserSelectsSwatchInDeliveryViewExpectSameSwatchSelectedInFabricView() {
    	
        // given
        ProductGroup expectedProductGroup = dataService.findProductGroup("guided-pip", "attributes", "swatch-selection");
        final String expectedQuantity = "1";
    	
        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        GuidedPipFlows.setGuidedPipQuantityAndAttributes(context, String.valueOf(expectedQuantity));
        
        // then
        assertTrue("User is not in delivery view at swatch container", productPage.isDeliveryToggleEnabled());
        
        // when
        String swatchSelectedInDeliveryView = productPage.getSelectedSwatchFabricTextFromSwatchContainer();
        productPage.clickFabricViewToggle();
        
        // then
        assertTrue("User is not in fabric view at swatch container", productPage.isFabricToggleEnabled());
        
        // When
        String swatchSelectedInFabricView = productPage.getSelectedSwatchFabricTextFromSwatchContainer();
        
        // then
        assertTrue(String.format("Swatch selected in delivery view is not selected in fabric view. Swatch selected in delivery view - %s"
                + "Swatch selected in fabric view - %s", swatchSelectedInDeliveryView, swatchSelectedInFabricView),
                swatchSelectedInDeliveryView.equalsIgnoreCase(swatchSelectedInFabricView));
    }
    
    /**
     * Verifies that first accordion in guided pip is opened and all the remaining accordions are closed.
     * Test Case ID - RGSN-38251
     */
    @Test
    @TestRail(id = "106831")
    public void testGuidedPipWhenUserVisitsPipPageExpectOnlyFirstAccordionIsOpened() {
    	
        // given
        ProductGroup expectedProductGroup = dataService.findProductGroup("guided-pip", "attributes", "swatch-selection");
        
        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        
        // then
        List<WebElement> guidedPipStepContainers = productPage.getGuidedPipStepContainer();
        for(int i = 0; i < guidedPipStepContainers.size(); i++){
           if (i == 0) {
               assertTrue("First Accordion in guided pip is not opened", 
                       guidedPipStepContainers.get(i).getAttribute("data-accordion-active").trim().equals("true"));
           } else if (i != 0) {
               assertTrue("All other accordions expect first accordion is opened", 
                       guidedPipStepContainers.get(i).getAttribute("data-accordion-active").trim().equals("false"));
            }
        }
    }

    /**
     * Verifies if the user is able to select multiple facet values in a Facet Group on guided pip.
     * Test Case ID - RGSN-38817
     */
    @Test
    @TestRail(id = "107943")
    public void testGuidedPipWhenUserSelectsMultipleValuesFromAFacetGroupExpectFacetsGotFiltered() {

        // given
        ProductGroup expectedProductGroup = dataService.findProductGroup("multiple-facets-selection");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        productPage.clickSelectFabricAndColorAccordion();
        if (context.isMobileExperience()) {
            productPage.clickFilterButton();
        }
        int noOfFacetsToBeSelected = 2;
        WebElement swatchFilter = productPage.clickSwatchFilterButton(0);
        productPage.selectMultipleFacetsFromTheFacetGroup(swatchFilter, noOfFacetsToBeSelected);
        if (context.isMobileExperience()) {
            productPage.clickApplyButtonFromFilterDrawer();
        }

        // then
        assertEquals("The selected facets count is not matched", noOfFacetsToBeSelected, productPage.getNumberOfFacetsSelected());

        // when
        if (context.isMobileExperience()) {
            productPage.clickFilterButton();
        }
        productPage.clickSwatchFilterItem(swatchFilter, 3);
        if (context.isMobileExperience()) {
            productPage.clickApplyButtonFromFilterDrawer();
        }

        // then
        assertEquals("The selected facets count is not matched after adding a few more facets",
                noOfFacetsToBeSelected + 1, productPage.getNumberOfFacetsSelected());
        assertTrue("Clear all button is not displayed", productPage.isClearAllButtonDisplayed());

        // when
        productPage.clickClearAllButton();

        // then
        assertTrue("Clear all button is displayed", !productPage.isClearAllButtonDisplayed());
    }

    /**
     * Verifies if the user is able to select multiple values in multiple facet groups on guided pip.
     * Test Case ID - RGSN-38817
     */
    @Test
    @TestRail(id = "107944")
    public void testGuidedPipWhenUserSelectsMultipleFacetsExpectSelectedFacetsDisplayed() {

        // given
        ProductGroup expectedProductGroup = dataService.findProductGroup("multiple-facets-selection");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        productPage.clickSelectFabricAndColorAccordion();
        if (context.isMobileExperience()) {
            productPage.clickFilterButton();
        }

        int countOfColorFacetsToBeSelected = 1;
        WebElement firstSwatchFilter = productPage.clickSwatchFilterButton(0);
        productPage.selectMultipleFacetsFromTheFacetGroup(firstSwatchFilter, countOfColorFacetsToBeSelected);
        if (context.isMobileExperience()) {
            productPage.clickApplyButtonFromFilterDrawer();
        }

        // then
        assertEquals("The selected facets count is not matched", countOfColorFacetsToBeSelected, productPage.getNumberOfFacetsSelected());

        // when
        if (context.isMobileExperience()) {
            productPage.clickFilterButton();
        }

        int countOfFabricFacetsToBeSelected = 1;
        WebElement secondSwatchFilter = productPage.clickSwatchFilterButton(1);
        productPage.selectMultipleFacetsFromTheFacetGroup(secondSwatchFilter, countOfFabricFacetsToBeSelected);
        if (context.isMobileExperience()) {
            productPage.clickApplyButtonFromFilterDrawer();
        }

        // then
        assertEquals("The selected facets count is not matched after adding few fabric facets",
                countOfColorFacetsToBeSelected + countOfFabricFacetsToBeSelected, productPage.getNumberOfFacetsSelected());
    }
}
