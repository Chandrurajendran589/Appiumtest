package com.wsgc.ecommerce.ui.regression.shop;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.shop.PipFlows;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.ProductGroup;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tag.area.ShopArea;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Regression tests for Inline Monogramming.
 */
@Category(ShopArea.class)
public class InlineMonogrammingRegression extends AbstractTest {

    /**
     * Verifies whether an error message is displayed when the user enters no characters in the monogram text.
     * Test Case ID: RGSN-38244
     */
    @Test
    @TestRail(id = "102999")
    public void testInlineMonoPZWhenUserEntersNoMonogramTextExpectErrorMessage() {

        // given
        final int expectedQuantity = 2;
        final String expectedErrorMessage = "Please enter text to personalize your item";
        final ProductGroup monogrammableProduct = dataService.findProductGroup("simple-pip", "attributes", "monogrammable");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, monogrammableProduct);
        PipFlows.setSimplePipAttributeSelections(productPage);
        productPage.clickInlineAddMonogram();
        int monogramFontsAvailable = productPage.getTotalNumberOfMonogrammingStylesAvailable();
        int inlineMonoStyleIndex = RandomUtils.nextInt(0, monogramFontsAvailable - 1);
        productPage.clickStyle(inlineMonoStyleIndex);
        productPage.setQuantity(expectedQuantity);
        productPage.clickAddToCart();
        String actualErrorMessage = productPage.getInlineMonogramErrorMessage();

        // then
        assertTrue(String.format("The expected error message %s is not matching with the actual error message %s",
                expectedErrorMessage, actualErrorMessage), actualErrorMessage.contains(expectedErrorMessage));
    }

    /**
     * Verifies whether an error message is displayed when the user enters special characters in the monogram.
     * Test Case ID: RGSN-38244
     */
    @Test
    @TestRail(id = "103000")
    public void testInlineMonoPZWhenUserEntersSpecialCharactersForMonogramExpectErrorMessage() {

        // given
        final int expectedQuantity = 2;
        final String invalidMonogramText = "#%&$*@";
        final String expectedErrorMessage = "Please enter valid characters:";
        final ProductGroup monogrammableProduct = dataService.findProductGroup("simple-pip", "attributes", "monogrammable");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, monogrammableProduct);
        PipFlows.setSimplePipAttributeSelections(productPage);
        productPage.clickInlineAddMonogram();
        int monogramFontsAvailable = productPage.getTotalNumberOfMonogrammingStylesAvailable();
        int inlineMonoStyleIndex = RandomUtils.nextInt(0, monogramFontsAvailable - 1);
        productPage.clickStyle(inlineMonoStyleIndex);
        productPage.setQuantity(expectedQuantity);
        productPage.typeInlineMonoPzText(invalidMonogramText);
        productPage.clickAddToCart();

        // then
        String actualErrorMessage = productPage.getInlineMonogramErrorMessage();
        assertTrue(String.format("The expected error message %s is not matching with the actual error message %s",
                expectedErrorMessage, actualErrorMessage), actualErrorMessage.contains(expectedErrorMessage));
    }
    
    /**
     * Verifies whether an error message is displayed when the user enters no characters in the monogram text
     * field and clicks add to registry button.
     * Test Case ID - RGSN-38730
     */
    @Test
    @TestRail(id = "104656")
    public void testInlineMonoPZWhenUserEntersNoMonogramTextAndClickAddToRegistryExpectErrorMessage() {

        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.REGISTRY_V2));
        final int expectedQuantity = 2;
        final String expectedErrorMessage = "Please enter text to personalize your item";
        final ProductGroup monogrammableProduct = dataService.findProductGroup("simple-pip", "attributes", "monogrammable");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, monogrammableProduct);
        PipFlows.setSimplePipAttributeSelections(productPage);
        productPage.clickInlineAddMonogram();
        int monogramFontsAvailable = productPage.getTotalNumberOfMonogrammingStylesAvailable();
        int inlineMonoStyleIndex = RandomUtils.nextInt(0, monogramFontsAvailable - 1);
        productPage.clickStyle(inlineMonoStyleIndex);
        productPage.setQuantity(expectedQuantity);
        productPage.clickAddToRegistry();
        String actualErrorMessage = productPage.getInlineMonogramErrorMessage();

        // then
        assertTrue(String.format("The expected error message %s is not matching with the actual error message %s",
                expectedErrorMessage, actualErrorMessage), actualErrorMessage.contains(expectedErrorMessage));
    }

    /**
     * Verifies whether an error message is displayed when the user enters special characters in the monogram text
     * field and clicks add to registry button.
     * Test Case ID - RGSN-38730
     */
    @Test
    @TestRail(id = "104657")
    public void testInlineMonoPZWhenUserEntersSpecialCharactersAndClickAddToRegistryExpectErrorMessage() {

        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.REGISTRY_V2));
        final int expectedQuantity = 2;
        final String invalidMonogramText = "#%&$*@";
        final String expectedErrorMessage = "Please enter valid characters:";
        final ProductGroup monogrammableProduct = dataService.findProductGroup("simple-pip", "attributes", "monogrammable");
        final int inlineMonoStyleIndex;
        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, monogrammableProduct);
        PipFlows.setSimplePipAttributeSelections(productPage);
        productPage.clickInlineAddMonogram();
        int monogramFontsAvailable = productPage.getTotalNumberOfMonogrammingStylesAvailable();
        inlineMonoStyleIndex = RandomUtils.nextInt(0, monogramFontsAvailable - 1);
        productPage.clickStyle(inlineMonoStyleIndex);
        productPage.setQuantity(expectedQuantity);
        productPage.typeInlineMonoPzText(invalidMonogramText);
        productPage.clickAddToRegistry();

        // then
        String actualErrorMessage = productPage.getInlineMonogramErrorMessage();
        assertTrue(String.format("The expected error message %s is not matching with the actual error message %s",
                expectedErrorMessage, actualErrorMessage), actualErrorMessage.contains(expectedErrorMessage));
    }
    
    /**
     * Verifies whether the default copy message is displayed when user tries to add quantity 
     * more than one from monogram personalization.
     * Test Case ID - RGSN-38730
     */
    @Test
    @TestRail(id = "104658")
    public void testInlineMonoPzWhenUserAddsMonogramItemWithQuantityMoreThanOneExpectDefaultCopyMessage() {

        // given
        final int inlineMonoStyleIndex = 0;
        final int colorIndex = 0;
        final int expectedQuantity = 3;
        final String monogramText = "ABC";
        final String expectedDefaultCopy = "Additional quantities will display the same personalization. Unique personalization must be added to cart separately";
        final ProductGroup monogrammableProduct = dataService.findProductGroup("simple-pip", "attributes", "monogrammable");
        
        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, monogrammableProduct);
        PipFlows.setSimplePipAttributeSelections(productPage);
        PipFlows.setMonogramDetails(productPage, inlineMonoStyleIndex, monogramText, colorIndex, expectedQuantity);
        String actualDefaultCopy = productPage.getDefaultCopytext();
        
        // then
        assertEquals("Default copy message is not shown for more than 1 quantity in monogram product page.", expectedDefaultCopy, actualDefaultCopy);       
    }

    /**
     * To Verify the in-line mono/PZ component displays when the user checks ( opt-in) the
     * checkbox for a simple PIP.
     * Test Case ID - RGSN-38731
     */
    @Test
    @TestRail(id = "104611")
    public void testInlineMonoPZComponentWhenUserVisitsMonogramEnabledProductThanExpectMonoPZComponents() {

        // given
        int inlineMonoStyleIndex = 0;
        final int colorIndex = 0;
        final int expectedQuantity = 1;
        String monogramText = "ABC";

        final ProductGroup monogrammableProduct = dataService.findProductGroup("simple-pip", "attributes", "monogrammable");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, monogrammableProduct);
        PipFlows.setSimplePipAttributeSelections(productPage);
        PipFlows.setMonogramDetails(productPage, inlineMonoStyleIndex, monogramText, colorIndex, expectedQuantity);

        // then
        assertTrue("The InLine MonoPZ Style component doesn't Present", productPage.isInLineMonoPZStyleDisplayed());
        assertTrue("The InLine MonoPZ Enter Text component doesn't Present", productPage.isInLineMonoPZEnterTextDisplayed());
    }
}
