package com.wsgc.ecommerce.ui.regression.shop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.ProductGroup;
import java.util.List;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tag.area.ShopArea;

/**
 * Regression Tests for Facets on Shop page.
 */
@Category(ShopArea.class)
public class FacetsOnShopRegression extends AbstractTest {

    /**
     * Verifies whether the facets buttons displayed are the same as first facet group values.
     * Test Case ID - RGSN-38206
     */
    @Test
    @TestRail(id = "100702")
    public void testFacetsButtonsWhenMultipleFacetsAreAvailableExpectFirstFacetGroupValuesAsFacetButtons() {
        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.FACET_BUTTONS)
                && context.isMobileExperience());
        final ProductGroup expectedProductGroup = dataService.findProductGroup(productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null);

        String productCategoryUrl = expectedProductGroup.getUnmappedAttribute("categoryUrl");

        // when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, productCategoryUrl);
        List<String> facetButtonsText = categoryPage.getAllFacetButtonsText();
        List<String> facetValuesText = categoryPage.getAllFacetValuesText(1);
        
        //then
        Assert.assertTrue(String.format("Facet buttons texts - %s does not match Facet Value texts - %s", facetButtonsText, facetValuesText), 
                facetButtonsText.equals(facetValuesText));
    }
    
    /**
     * Verifies whether the facets selections are displayed, when user selects single facet button.
     * Test Case ID - RGSN-38206
     */
    @Test
    @TestRail(id = "98483")
    public void testFacetsButtonsWhenProductsAreFilteredWithSingleFacetButtonExpectFacetSelections() {
        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.FACET_BUTTONS)
                && context.isMobileExperience());
        final ProductGroup expectedProductGroup = dataService.findProductGroup(productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null);

        String productCategoryUrl = expectedProductGroup.getUnmappedAttribute("categoryUrl");

        // when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, productCategoryUrl);
        int totalNumberOfFacetButtons = categoryPage.getTotalNumberOfFacetButtonsDisplayed();
        int facetButtonValue = RandomUtils.nextInt(1, totalNumberOfFacetButtons);
        String selectedFacetButton = categoryPage.clickSingleFacetButton(facetButtonValue);
        categoryPage.clickFilterButton();
        categoryPage.waitForFacetDrawer();
        List<String> facetSelection = categoryPage.getAllFacetSelectionsText();
        
        //then
        Assert.assertTrue(String.format("Facet buttons texts - %s does not match Facet Selection texts - %s", selectedFacetButton.toLowerCase(), facetSelection.get(0)), 
                selectedFacetButton.toLowerCase().equals(facetSelection.get(0)));
    }

    /**
     * Verifies whether the products are filtered accordingly on shop page, when user selects single facet button.
     * Test Case ID - RGSN-38206
     */
    @Test
    @TestRail(id = "99782")
    public void testFacetsButtonsWhenProductsAreFilteredWithSingleFacetExpectFilteredResults() {
        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.FACET_BUTTONS)
                && context.isMobileExperience());
        final ProductGroup expectedProductGroup = dataService
                .findProductGroup(productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null);

        String productCategoryUrl = expectedProductGroup.getUnmappedAttribute("categoryUrl");

        // when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, productCategoryUrl);
        categoryPage.waitForCategoryPageLoad();
        Assert.assertTrue("Facet buttons are not displayed for Mobile ", categoryPage.isFacetButtonsDisplayed());
        int totalNumberOfFacetButtons = categoryPage.getTotalNumberOfFacetButtonsDisplayed();
        int facetButtonValue = RandomUtils.nextInt(1, totalNumberOfFacetButtons);
        // click on the facet button.
        String selectedFacetButton = categoryPage.clickSingleFacetButton(facetButtonValue);
        // Get number of products for selected refinement
        int numberOfProductsForTheSelectedFacetButton = categoryPage.getNumberOfProductsForSelectedFacetButton(selectedFacetButton.toLowerCase());
        // Get number of products loaded.
        int numberOfProductsLoadedAfterSelectingFacetButton = categoryPage.getNumberOfProductsLoaded();
        
        // then
        Assert.assertEquals("Incorrect number of products were filtered upon facet selection", numberOfProductsForTheSelectedFacetButton,
                numberOfProductsLoadedAfterSelectingFacetButton);
    }
    
    /**
     * Verifies whether no follow tag is generated when the products are filtered accordingly on shop page.
     * Test Case ID - RGSN-38188
     */
    @Test
    @TestRail(id = "99783")
    public void testFacetsWhenProductsAreFilteredWithSingleFacetExpectNoFollowTag() {

        // given
        final ProductGroup expectedProductGroup = dataService
                .findProductGroup(productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null);
        String categoryUrl = expectedProductGroup.getUnmappedAttribute("categoryUrl");
        String expectedRelValueForSelectedFacet = "";
        String expectedRelValueForUnSelectedFacet = "nofollow";
        int facetGroup = 2;
        int facetValue = 1;

        // when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, categoryUrl);
        categoryPage.waitForCategoryPageLoad();
        Assert.assertTrue("User specified Facet Group: " + facetGroup + " or " + "Facet Value: " + facetValue + ""
                + " is not present", categoryPage.isFacetsDisplayed(facetGroup, facetValue));
        categoryPage.clickFacetRefinement(facetGroup, facetValue);

        // then
        String actualRelValueForSelectedFacet = categoryPage.getRelAttributeOfGivenFacetRefinement(facetGroup, facetValue);
        assertEquals("Expected Rel Value \'" + expectedRelValueForSelectedFacet + "\' is not matching with \'"
                + actualRelValueForSelectedFacet + "\' of the actual value ", expectedRelValueForSelectedFacet, actualRelValueForSelectedFacet);

        categoryPage.resetFacetValueForSelection();
        String actualRelValueForUnSelectedFacet = categoryPage.getRelAttributeOfGivenFacetRefinement(facetGroup, facetValue + 1);
        assertEquals("Expected Rel Value \'" + expectedRelValueForUnSelectedFacet + "\' is not matching with \'"
                + actualRelValueForUnSelectedFacet + "\' of the actual value ", expectedRelValueForUnSelectedFacet, actualRelValueForUnSelectedFacet);
    }
    
    /**
     * Verifies whether the facets are cleared on shop page, when user clicks clear all and facet selections are 
     * persisted when user navigate back using browser back button.
     * Test Case ID - RGSN-38219, RGSN-38177, RGSN-38706
     */
    @Test
    @TestRail(id = "99784")
    public void testFacetsWhenUserClearsFacetsSelectionsAndNavigatesToThePreviousPageExpectFilteredResults() {
        // given
        final ProductGroup expectedProductGroup = dataService.findProductGroup(
                productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null);
        String productCategoryUrl = expectedProductGroup.getUnmappedAttribute("categoryUrl");
        int facetGroup = 2;
        int facetValue = 1;

        // when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, productCategoryUrl);
        categoryPage.waitForCategoryPageLoad();
        String categoryPageUrlBeforeFacetSelection = categoryPage.getCurrentPageUrl();
        int numberOfProductsBeforeSelectingTheGivenFacetRefinement = categoryPage.getNumberOfProductsLoaded();
        categoryPage.clickFacetRefinement(facetGroup, facetValue);
        categoryPage.resetFacetValueForSelection();
        String hrefOfChosenFacets = categoryPage.getHrefAttributeOfGivenFacetRefinement(facetGroup + 1, facetValue);
        int numberOfProductsForTheGivenFacetRefinement = categoryPage.getNumberOfProductsForGivenFacetRefinement(facetGroup + 1, facetValue);
        categoryPage.clickFacetRefinement(facetGroup + 1, facetValue);  
        int numberOfProductsAfterSelectingTheGivenFacetRefinement = categoryPage.getNumberOfProductsLoaded();
        String categoryPageUrlAfterFacetSelection = categoryPage.getCurrentPageUrl();
        
        Assert.assertEquals("Incorrect number of products were filtered upon facet selection", numberOfProductsForTheGivenFacetRefinement,
                numberOfProductsAfterSelectingTheGivenFacetRefinement);

        Assert.assertEquals("Expected Href Value of Facet Refinements \'" + hrefOfChosenFacets + "\' is not matching with Category Page Url \'"
                + categoryPageUrlAfterFacetSelection + "\'", hrefOfChosenFacets, categoryPageUrlAfterFacetSelection); 

        categoryPage.clickOnClearAllSelection();
        int numberOfProductsAfterClearingFacetRefinement = categoryPage.getNumberOfProductsLoaded();
        String categoryPageUrlAfterClearingFacetSelection = categoryPage.getCurrentPageUrl();
        
        
        Assert.assertEquals("Incorrect number of products were displayed after clearing facet selection", numberOfProductsBeforeSelectingTheGivenFacetRefinement,
        		numberOfProductsAfterClearingFacetRefinement);

        Assert.assertEquals("Expected Category Page Url \'" + categoryPageUrlBeforeFacetSelection + "\' is matching with actual Category Page Url \'"
                + categoryPageUrlAfterClearingFacetSelection + "\'", categoryPageUrlBeforeFacetSelection, categoryPageUrlAfterClearingFacetSelection); 

        context.getPilot().getDriver().navigate().back();
        categoryPageUrlAfterFacetSelection = categoryPage.getCurrentPageUrl();

        //then
        Assert.assertEquals("Incorrect number of products were filtered upon facet selection", numberOfProductsForTheGivenFacetRefinement,
                numberOfProductsAfterSelectingTheGivenFacetRefinement);
        Assert.assertEquals("Expected Href Value of Facet Refinements \'" + hrefOfChosenFacets + "\' is not matching with Category Page Url \'"
                + categoryPageUrlAfterFacetSelection + "\'", hrefOfChosenFacets, categoryPageUrlAfterFacetSelection); 
    }
    
    /**
     * Verifies whether the multiple facets selected from horizontal facets are cleared after clicking clear all.
     * Test case ID - RGSN-38693, RGSN-38173
     */
    @Test
    @TestRail(id = "104653")
    public void testHorizontalFacetsWhenUserClearsMultipleFacetsSelectionsExpectFacetsSelectedAreCleared() {
	
        //given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.HORIZONTAL_FACET)
                && context.isFullSiteExperience());
        final ProductGroup expectedProductGroup = dataService.findProductGroup(
                productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null);
        String categoryUrl = expectedProductGroup.getUnmappedAttribute("categoryUrl");
        int facetValue = 1;

        //when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, categoryUrl);
        int numberOfProductsBeforeSelectingTheGivenFacetRefinement = categoryPage.getNumberOfProductsLoaded();

        /*
         Selects facet value from two different facet group.
         */
        for (int facetGroup = 2; facetGroup <= 3; facetGroup++) {
            categoryPage.clickFacetRefinement(facetGroup, facetValue);
            categoryPage.resetFacetValueForSelection();
        }
        categoryPage.clickOnClearAllSelection();

        //then
        int numberOfProductsAfterClearingFacetRefinement = categoryPage.getNumberOfProductsLoaded();
        Assert.assertEquals("Incorrect number of products were displayed after clearing facet selection", 
                numberOfProductsBeforeSelectingTheGivenFacetRefinement, numberOfProductsAfterClearingFacetRefinement);
    }

    /**
     * Verifies whether user is able to see the selected facet from horizontal facet is 
     * available and selected inside facet flyout.
     * Test case ID - RGSN-38693
     */
    @Test
    @TestRail(id = "104654")
    public void testFacetPersistenceWhenUserSelectsFacetFromHorizontalFacetExpectSameFaceAvailableInFlyout() {
    	//given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.HORIZONTAL_FACET)
                && context.isFullSiteExperience());
        final ProductGroup expectedProductGroup = dataService.findProductGroup("also-in-the-collection");
        String categoryUrl = expectedProductGroup.getUnmappedAttribute("categoryUrl");
        int facetGroup = 3;
        int facetValue = 1;
        
        //when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, categoryUrl);
        categoryPage.clickFacetRefinement(facetGroup, facetValue);
        categoryPage.resetFacetValueForSelection();
        categoryPage.clickSortAndMoreFiltersButton();
        
        //then
        assertTrue("Facet value is not persistent inside shop facets flyout", 
                categoryPage.isCheckboxSelectedInFacetFlyout(facetGroup + 1, facetValue));
    }
    
    /** 
     * Verifies whether the selected facet text is shown in bubble button and 
     * bubble selection button is displayed when the facet is selected from horizontal facet.
     * Test case ID - RGSN-38173
     */
    @Test
    @TestRail(id = "104655")
    public void testHorizontalFacetWhenUserSelectsFacetFromHorizontalFacetExpectFacetValueInBubbleButton() {
        //given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.HORIZONTAL_FACET)
                && context.isFullSiteExperience());
        final ProductGroup expectedProductGroup = dataService.findProductGroup(productGroup -> productGroup
                .getUnmappedAttribute("categoryUrl") != null, "also-in-the-collection");
        String categoryUrl = expectedProductGroup.getUnmappedAttribute("categoryUrl");
        int facetGroup = 3;
        int facetValue = 1;

        //when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, categoryUrl);
        categoryPage.waitForCategoryPageLoad();
        categoryPage.clickFacetRefinement(facetGroup, facetValue);
   
        //then
        assertTrue("Bubble button is not shown after facet selection", categoryPage.isBubbleButtonPresentAndDisplayed());
        
        //when
        String facetValueText = categoryPage.getTextOfGivenFacetRefinement(facetGroup, facetValue);
        String bubbleButtonText = categoryPage.getTextFromBubbleButton();
        
        //then
        Assert.assertEquals("Expected facet text that should be displayed inside bubble button \'" + facetValueText + 
                "\' but it contains \'" + bubbleButtonText + "\'", bubbleButtonText, facetValueText);
    }
    
    /**
     * Verifies whether the SEO meta description based on filter selection on category page.
     * Test Case ID - RGSN-38706
     */
    @Test
    @TestRail(id = "104383")
    public void testMetaDescriptionWhenProductsAreFilteredWithSingleFacetButtonExpectMetaTagsInPageSource() {
        
        // given
        final ProductGroup expectedProductGroup = dataService.findProductGroup(productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null, 
                                                  "meta-description");
        String productCategoryUrl = expectedProductGroup.getUnmappedAttribute("categoryUrl");
        int facetGroup = 2;
        int facetValue = 1;
        
        final String metaDescriptionTemplate = "<meta name=\"description\" content=";
        final String endTagTemplate = "><";
        final String CONTENT_ATTRIBUTE_TEMPLATE = "content=";
        final String EMPTY_STRING_TEMPLATE = "\"\"";
        final String UNDEFINED_STRING_TEMPLATE = "\"undefined\"";
        String canonicalTagTemplate = "<link rel=\"canonical\" href=\"%s\" id=\"pageCanonicalLink\">";
        final String robotsTagTemplate = "<meta name=\"robots\" content=";

        //when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, productCategoryUrl);
        categoryPage.waitForCategoryPageLoad();
        categoryPage.clickFacetRefinement(facetGroup, facetValue);
        context.getPilot().refresh();
        String categoryPageUrl = categoryPage.getCurrentPageUrl();
        if (categoryPageUrl.contains(":") && categoryPageUrl.contains("@")) {
        	categoryPageUrl = categoryPageUrl.substring(0, 8) + categoryPageUrl.substring(categoryPageUrl.indexOf('@') + 1, categoryPageUrl.length());
        }
        String pageSource = context.getPilot().getDriver().getPageSource();
        String expectedCanonicalTag = String.format(canonicalTagTemplate, categoryPageUrl);
        String metaDescription = pageSource.substring(pageSource.indexOf(metaDescriptionTemplate));
        metaDescription = metaDescription.substring(metaDescription.indexOf(CONTENT_ATTRIBUTE_TEMPLATE), 
                metaDescription.indexOf(endTagTemplate));
        metaDescription = metaDescription.substring(metaDescription.indexOf("=") + 1);
        String expectedRobotsTag = pageSource.substring(pageSource.indexOf(robotsTagTemplate));
        expectedRobotsTag = expectedRobotsTag.substring(expectedRobotsTag.indexOf(CONTENT_ATTRIBUTE_TEMPLATE), 
        		expectedRobotsTag.indexOf(endTagTemplate));
        expectedRobotsTag = expectedRobotsTag.substring(expectedRobotsTag.indexOf("=") + 1);

        //then
        Assert.assertTrue("The meta description is not present in page source of category page.", !metaDescription.equals(EMPTY_STRING_TEMPLATE) 
                || !metaDescription.equalsIgnoreCase(UNDEFINED_STRING_TEMPLATE));
        Assert.assertTrue(String.format("The expected canonical tag: %s , is not present in page source of category page.",
                expectedCanonicalTag), pageSource.contains(expectedCanonicalTag));
        Assert.assertTrue("The robots meta tag is not present in page source of category page.", !expectedRobotsTag.equals(EMPTY_STRING_TEMPLATE) 
                || !expectedRobotsTag.equalsIgnoreCase(UNDEFINED_STRING_TEMPLATE));
    }

    /**
     * Verifies that the products should be filtered based on the color of choice and
     * The product list should be the same count as mentioned in the facet.
     * Test case ID - RGSN-38818
     */
    @Test
    @TestRail(id = "105893")
    public void testColorFacetOnCategoryPageWhenColorFacetSelectedExpectTheSameProductCounts() {

        // given
        final ProductGroup expectedProductGroup = dataService.findProductGroup(
                productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null,
                "color-facet");
        final String facetGroupType = "Color";
        int facetValue = 1;

        // when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, expectedProductGroup.getUnmappedAttribute("categoryUrl"));
        categoryPage.waitForCategoryPageLoad();
        int indexOfColorFacetGroup = categoryPage.getFacetGroupIndex(facetGroupType);
        int numberOfProductsForTheGivenFacetRefinement = categoryPage.getNumberOfProductsForGivenFacetRefinement(indexOfColorFacetGroup + 1, facetValue);

        // then
         assertTrue("For the color group facets the facet count is not displayed", numberOfProductsForTheGivenFacetRefinement >= 0);

        // when
        categoryPage.clickFacetRefinement(indexOfColorFacetGroup + 1, facetValue);

        // then
        if (context.isFullSiteExperience()) {
            assertTrue("The clear all selection link doesn't present", categoryPage.isClearAllSelectionLinkPresent());
        }
        assertEquals(String.format("The value of selected color facet from facet group is : '%s' .The products count after color facet selected is '%s'. ",
                numberOfProductsForTheGivenFacetRefinement, categoryPage.getProductCountFromCategoryPage()), numberOfProductsForTheGivenFacetRefinement,
                categoryPage.getProductCountFromCategoryPage());
    }

    /**
     * Verifies that The attributes on the product page should be pre-selected based on the color of choice on the category page.
     * Test case ID - RGSN-38818
     */
    @Test
    @TestRail(id = "105894")
    public void testColorFacetWhenNavigatedToProductPageAfterColorFacetSelectedExpectTheAttributesArePreSelectedOnProductPage() {

        // given
        final ProductGroup expectedProductGroup = dataService.findProductGroup(
                productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null,
                "color-facet");
        final String facetGroupType = "Color";
        int facetValue = 1;

        // when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context,
                expectedProductGroup.getUnmappedAttribute("categoryUrl"));
        categoryPage.waitForCategoryPageLoad();
        int indexOfColorFacetGroup = categoryPage.getFacetGroupIndex(facetGroupType);
        categoryPage.clickFacetRefinement(indexOfColorFacetGroup + 1, facetValue);
        ProductPage productPage = categoryPage.getProductCategoryItems().get(0).clickProductImage();

        // then
        assertTrue("The Attributes are not pre-selected on the product page", productPage.isSimplePipAttributesPresentAndSelected(expectedProductGroup));
    }
}
