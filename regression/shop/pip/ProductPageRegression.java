package com.wsgc.ecommerce.ui.regression.shop.pip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wsgc.ecommerce.ui.pagemodel.shop.MoreItemsLikeThisOverlay;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.openqa.selenium.Dimension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.shop.PipFlows;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSectionFactory;
import com.wsgc.ecommerce.ui.pagemodel.i18n.InternationalShippingOverlay;
import com.wsgc.ecommerce.ui.pagemodel.shop.AllStateProtectionPlanOverlaySection;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.RichAddToCartOverlaySection;
import com.wsgc.ecommerce.ui.pagemodel.shop.TurnToQuestionAndAnswerSection;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.CustomerAccount;
import com.wsgc.evergreen.entity.api.MutableAccount;
import com.wsgc.ecommerce.ui.pagemodel.shop.RecentlyViewedOverlay;
import com.wsgc.evergreen.entity.api.ProductGroup;

import groovy.lang.Category;
import tag.area.ShopArea;

/**
 * Regression Tests for Validating the elements on product page.
 * 
 */
@Category(ShopArea.class)
public class ProductPageRegression extends AbstractTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Verifies whether a user is taken to the category page when they click on the category bread crumbs.
     * Test Case ID - RGSN-38236
     */
    @Test
    @TestRail(id = "98423")
    public void testProductPageWhenUserClicksBreadCrumbsExpectCategoryPage() {

        // given
        final ProductGroup expectedProductGroup = dataService
                .findProductGroup(productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null,
                                  "simple-pip", "!attributes", "!monogrammable");

        String categoryPageUrl = expectedProductGroup.getUnmappedAttribute("categoryUrl");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        String expectedCategoryPageUrl = productPage.getCategoryUrlFromBreadCrumb(categoryPageUrl);
        if (context.isMobileExperience()) {
            expectedCategoryPageUrl = expectedCategoryPageUrl.replace("/m/", "/");
        }
        if (expectedCategoryPageUrl.contains(":") && expectedCategoryPageUrl.contains("@")) {
            expectedCategoryPageUrl = expectedCategoryPageUrl.substring(0, 8) + expectedCategoryPageUrl.substring(expectedCategoryPageUrl.indexOf('@') + 1, expectedCategoryPageUrl.length());
        }
        productPage.clickCategoryBreadCrumb(categoryPageUrl);
        String currentPageUrl = context.getPilot().getDriver().getCurrentUrl();
        if (currentPageUrl.contains(":") && currentPageUrl.contains("@")) {
            currentPageUrl = currentPageUrl.substring(0, 8) + currentPageUrl.substring(currentPageUrl.indexOf('@') + 1, currentPageUrl.length());
        }

        //then
        Assert.assertEquals("Category page URL from breadcrumb '" + expectedCategoryPageUrl + "' is not matching with '"
                + currentPageUrl + "' of Category page URL ", expectedCategoryPageUrl, currentPageUrl);
    }
    
    /**
     * Verifies whether a user is able to see an error message on a gift card product page 
     * while performing an international shipping.
     * Test Case ID - RGSN-38220, RGSN-38225
     */
    @Test
    @TestRail(id = "98424")
    public void testAddGiftCardToCartWhenUserTriesToPerformInternationalShippingExpectErrorMessage() {

        // given
        Assume.assumeTrue(context.isFullSiteExperience());
        final String INTERNATIONAL_ERROR_MESSAGE_TEMPLATE = "Gift Cards are not available for international customers.";
        final ProductGroup expectedproductGroup = dataService.findProductGroup("gift-card");

        // when
        ShopFlows.goToPipPageFlow(context, expectedproductGroup);
        NavigationSection navigationSection = context.getPageSection(NavigationSection.class);
        InternationalShippingOverlay internationalShippingOverlay = navigationSection.goToInternationalShippingOverlay();
        int countryOptionsAvailable = internationalShippingOverlay.getInternationalShippingCountries();
        int countryNameIndex = RandomUtils.nextInt(0, countryOptionsAvailable - 1);
        internationalShippingOverlay.selectCountryByIndex(countryNameIndex);
        internationalShippingOverlay.clickUpdateCurrencyAndCountryButton();

        // then
        assertTrue(String.format("%s error message is not displayed", INTERNATIONAL_ERROR_MESSAGE_TEMPLATE),
                context.getPage(ProductPage.class).getInternationalErrorMessage().contains(INTERNATIONAL_ERROR_MESSAGE_TEMPLATE));
    }

    /**
     * Verifies whether 'Also in the collection' section is displayed in the Product Page.
     * Test Case ID - RGSN-38231
     */
    @Test
    @TestRail(id = "98426")
    public void testNavigationWhenClicksAProductInAlsoInTheCollectionSectionExpectProductPage() {

        // given
        final ProductGroup expectedProductGroup = dataService.findProductGroup("also-in-the-collection");
        final String EXPECTED_PIP_RECSTART_PARAM = "?cm_src=rel";

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        int productRecommendationsAvailable = productPage.getTotalNumberOfProductsFromAlsoInTheCollectionSection();
        int productRecommendationIndex = RandomUtils.nextInt(0, productRecommendationsAvailable - 1);
        String recommendationProductUrl = productPage.getProductUrlFromAlsoInTheCollectionSection(productRecommendationIndex);
        productPage.selectProductFromAlsoInTheCollectionSection(productRecommendationIndex);
        String productPageUrl = productPage.getCurrentPageUrl();


        //then
        Assert.assertEquals("Product page URL from Also in the Collection Section '" + recommendationProductUrl 
                + "' is not matching with '" + productPageUrl + "' of Category page URL ",
                recommendationProductUrl, productPageUrl);
        assertTrue(String.format("%s - is not present in the product page url - %s", EXPECTED_PIP_RECSTART_PARAM, productPageUrl),
                productPageUrl.contains(EXPECTED_PIP_RECSTART_PARAM));
    }

    /**
     * Verifies if the user is navigated to a product page on clicking an item from the recently viewed carousel in the product page.
     * Test Case ID - RGSN-38228
     */
    @Test
    @TestRail(id = "103015")
    public void testRecentlyViewedCarouselWhenUserClicksRecentlyViewedProductExpectProductPage() {

        // given
        final String EXPECTED_PIP_RECSTART_PARAM = "?cm_src=WsiPipRvi";
        List<ProductGroup> findProductGroups = dataService.findProductGroups("recently-viewed");

        // when
        for (ProductGroup productGroup : findProductGroups) {
            ShopFlows.goToPipPageFlow(context, productGroup);
        }
        ProductPage productPage = context.getPage(ProductPage.class);
        int recentlyViewedItemsAvailable = productPage.getTotalNumberOfProductsFromRecentlyViewedSection();
        int recentlyViewedIndex = RandomUtils.nextInt(0, recentlyViewedItemsAvailable - 1);
        productPage.selectProductFromRecentlyViewedSection(recentlyViewedIndex);
        String productPageUrl = productPage.getCurrentPageUrl();

        //then
        assertTrue(String.format("%s - is not present in the product page url - %s", EXPECTED_PIP_RECSTART_PARAM, productPageUrl),
                productPageUrl.contains(EXPECTED_PIP_RECSTART_PARAM));
    }

    /**
     * Verifies if the user is navigated to a product page on clicking an item from the recently viewed carousel in global header section.
     * Test Case ID - RGSN-38733
     */
    @Test
    @TestRail(id = "105250")
    public void testRecentlyViewedWhenUserClicksRecentlyViewedOnGlobalHeaderExpectProductPage() {

        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.RECENTLY_VIEWED_CAROUSEL_GLOBAL_HEADER));
        final String expectedPipRecStartParam = "?cm_src=RVI-Carousel";
        List<ProductGroup> findProductGroups = dataService.findProductGroups("recently-viewed");

        // when
        for (int i = 0; i < 2; i++) {
            ShopFlows.goToPipPageFlow(context, findProductGroups.get(i));
        }
        ProductPage productPage = context.getPage(ProductPage.class);
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        RecentlyViewedOverlay recentlyViewedOverlay = navigationSection.clickRecentlyViewedLink();

        // then
        assertTrue("Recently Viewed Heading is not present on Recently Viewed Flyout", recentlyViewedOverlay.isRecentlyViewedLabelPresent());

        // when
        int recentlyViewedItemsAvailable = recentlyViewedOverlay.getTotalNumberOfProductsFromRecentlyViewedSection();
        int recentlyViewedIndex = RandomUtils.nextInt(0, recentlyViewedItemsAvailable - 1);
        recentlyViewedOverlay.selectProductFromRecentlyViewedSection(recentlyViewedIndex);
        String productPageUrl = productPage.getCurrentPageUrl();

        // then
        assertTrue(String.format("%s - is not present in the product page url - %s", expectedPipRecStartParam, productPageUrl),
                productPageUrl.contains(expectedPipRecStartParam));
    }

    /**
     * Verifies if the user is navigated to More Items Like This Overlay on clicking See More Like link from the recently viewed carousel on desktop
     * experience.
     * Test Case ID - RGSN-38733
     */
    @Test
    @TestRail(id = "105251")
    public void testSeeMoreLikeWhenUserClicksSeeMoreLikeLinkOnRecentlyViewedDesktopOverlayExpectMoreItemsLikeThisOverlay() {

        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.RECENTLY_VIEWED_CAROUSEL_GLOBAL_HEADER) && context.isFullSiteExperience());
        List<ProductGroup> findProductGroups = dataService.findProductGroups("recently-viewed");

        // when
        for (int i = 0; i < 2; i++) {
            ShopFlows.goToPipPageFlow(context, findProductGroups.get(i));
        }
        String expectedProductPageURL = context.getPilot().getDriver().getCurrentUrl();
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        RecentlyViewedOverlay recentlyViewedOverlay = navigationSection.clickRecentlyViewedLink();
        int recentlyViewedItemsAvailable = recentlyViewedOverlay.getTotalNumberOfProductsFromRecentlyViewedSection();
        int recentlyViewedIndex = RandomUtils.nextInt(0, recentlyViewedItemsAvailable - 1);
        MoreItemsLikeThisOverlay moreItemsLikeThisOverlay = recentlyViewedOverlay.clickSeeMoreLikeThisLink(recentlyViewedIndex);

        // then
        assertTrue("More Items Like This label is not displayed", moreItemsLikeThisOverlay.isMoreItemsLikeThisLabelPresent());
        assertTrue("More Items Like This Products are not available",
                moreItemsLikeThisOverlay.getTotalNumberOfProductsAvailableOnMoreItemsLikeThisOverlay() > 1);

        // when
        moreItemsLikeThisOverlay.clickCloseButton();
        String actualProductPageURL = context.getPilot().getDriver().getCurrentUrl();

        // then
        assertTrue("User is not on the same product page after closing the overlay", expectedProductPageURL.equals(actualProductPageURL));
    }

    /**
     * Verifies if the user is navigated to More Items Like This Overlay on clicking See More Like link from the recently viewed carousel on mobile
     * experience.
     * Test Case ID - RGSN-38733
     */
    @Test
    @TestRail(id = "105252")
    public void testSeeMoreLikeWhenUserClicksSeeMoreLikeLinkOnRecentlyViewedMobileOverlayExpectMoreItemsLikeThisOverlay() {

        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.RECENTLY_VIEWED_CAROUSEL_GLOBAL_HEADER) && context.isMobileExperience());
        List<ProductGroup> findProductGroups = dataService.findProductGroups("recently-viewed");

        // when
        for (int i = 0; i < 2; i++) {
            ShopFlows.goToPipPageFlow(context, findProductGroups.get(i));
        }
        String expectedProductPageURL = context.getPilot().getDriver().getCurrentUrl();
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        RecentlyViewedOverlay recentlyViewedOverlay = navigationSection.clickRecentlyViewedLink();
        int recentlyViewedItemsAvailable = recentlyViewedOverlay.getTotalNumberOfProductsFromRecentlyViewedSection();
        int recentlyViewedIndex = RandomUtils.nextInt(0, recentlyViewedItemsAvailable - 1);
        MoreItemsLikeThisOverlay moreItemsLikeThisOverlay = recentlyViewedOverlay.clickSeeMoreLikeThisLink(recentlyViewedIndex);

        // then
        assertTrue("More Items Like This label is not displayed", moreItemsLikeThisOverlay.isMoreItemsLikeThisLabelPresent());
        assertTrue("More Items Like This Products are not available",
                moreItemsLikeThisOverlay.getTotalNumberOfProductsAvailableOnMoreItemsLikeThisOverlay() > 1);

        // when
        moreItemsLikeThisOverlay.clickBackButton();

        // then
        assertTrue("Recently Viewed Heading is not present on Recently Viewed Flyout", recentlyViewedOverlay.isRecentlyViewedLabelPresent());

        // when
        recentlyViewedOverlay.clickCloseButton();
        String actualProductPageURL = context.getPilot().getDriver().getCurrentUrl();

        // then
        assertTrue("User is not on the same product page after closing the overlay", expectedProductPageURL.equals(actualProductPageURL));
    }

    /**
     * Verifies if user is able to submit a valid question at product page.
     * Test Case ID - RGSN-38224
     */
    @Test
    @TestRail(id = "104376")
    public void testQuestionsAndAnswersWhenUserSubmitsNewQuestionInProductPageExpectQuestionAdded() {

        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.TURN_TO_QUESTION_AND_ANSWER));

        //given
        final ProductGroup expectedProductGroup = dataService.findProductGroup("questions");
        MutableAccount mutableAccount = getMutableAccount("default");
        CustomerAccount account = accountCreator.getRandomEmailAccount(mutableAccount);
        String expectedQuestion = "Test automation ?";
        
        //when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        assertTrue("Have a question section is not displayed ", productPage.isTurnToQaWidgetDisplayed());
        
        PipFlows.submitTurnToQuestionForm(context, account, expectedQuestion);
        TurnToQuestionAndAnswerSection turnToQuestionAndAnswerSection = context.getPageSection(TurnToQuestionAndAnswerSection.class);

        if (productPage.isPipMfePresent()) {
            String newQuestionId = turnToQuestionAndAnswerSection.getNewQuestionId();
            String actualQuestionAdded = turnToQuestionAndAnswerSection.getQuestion(newQuestionId);
            //then
            assertTrue("Question is not added ", actualQuestionAdded.equalsIgnoreCase(expectedQuestion));
        } 
        assertTrue("Confirmation message is not displayed ", turnToQuestionAndAnswerSection.isConfirmationDisplayed());
    }
    
    /**
     * Verifies if user is unable to submit a invalid question at product page.
     * Test Case ID - RGSN-38224
     */
    @Test
    @TestRail(id = "104379")
    public void testQuestionsAndAnswersWhenUserSubmitsInvalidQuestionInProductPageExpectErrorMessage() {

        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.TURN_TO_QUESTION_AND_ANSWER));

        //given
        final ProductGroup expectedProductGroup = dataService.findProductGroup("questions");
        String expectedQuestion = "Test automation";
        String expectedMessage = "Just checking: Is this a question? (We didn't see a question mark...)";
        
        //when
        ShopFlows.goToPipPageFlow(context, expectedProductGroup);   
        TurnToQuestionAndAnswerSection turnToQuestionAndAnswerSection = context.getPageSection(TurnToQuestionAndAnswerSection.class);
        turnToQuestionAndAnswerSection.setQuestion(expectedQuestion);
        turnToQuestionAndAnswerSection.clickSubmitNewQuestion();
        turnToQuestionAndAnswerSection.clickSubmitQuestionButton();
        String errorMessage = turnToQuestionAndAnswerSection.getErrorMessageTextInQuestionAndAnswerSection();
        
        //then
        assertTrue("Error message is not displayed for invalid question ", errorMessage.equalsIgnoreCase(expectedMessage));
    }
    
    /**
     * Verifies if user is able to see existing questions asked and suggestions are displayed if user
     * enter keyword related to existing questions.
     * Test Case ID - RGSN-38224
     */
    @Test
    @TestRail(id = "104378")
    public void testQuestionsAndAnswersWhenUserSubmitsExistingQuestionExpectExistingQuestions() {

        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.TURN_TO_QUESTION_AND_ANSWER));

        // given
        final ProductGroup expectedProductGroup = dataService.findProductGroup("questions");
        String expectedQuestion = expectedProductGroup.getUnmappedAttribute("questionText");
        
        // when
        ShopFlows.goToPipPageFlow(context, expectedProductGroup);  
        TurnToQuestionAndAnswerSection turnToQuestionAndAnswerSection = context.getPageSection(TurnToQuestionAndAnswerSection.class);

        // then
        assertTrue("Existing questions are not displayed", turnToQuestionAndAnswerSection.isExistingQuestionDisplayed());
        
        turnToQuestionAndAnswerSection.setQuestion(expectedQuestion);
        assertTrue("Question suggestions are not displayed", turnToQuestionAndAnswerSection.isQuestionSuggestionDisplayed());
    }
    
    /**
     * Verifies if user is able to navigate to virtual choice page by clicking on make a free design 
     * appointment button at Product page.
     * Test Case ID - RGSN-38769
     */
    @Test
    @TestRail(id = "104384")
    public void testDesignPageNavigationWhenUserClicksDesignAppointmentButtonFromProductPageExpectVirtualChoicePage() {
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.DESIGN_APPOINTMENT_LINK));

        // given
        final ProductGroup expectedProductGroup = dataService.findProductGroup("order-free-swatch-flyout");
        final String expectedUrlTemplate = "virtual-choice";
        
        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        productPage.clickRequestAnAppointment();
        List<String> windows = new ArrayList<String>(context.getPilot().getDriver().getWindowHandles());
        context.getPilot().getDriver().switchTo().window(windows.get(1));
        String currentUrl = context.getPilot().getDriver().getCurrentUrl();
        
        // then
        assertTrue("Virtual Choice Page is not displayed.", currentUrl.contains(expectedUrlTemplate));
    }
    
    /**
     * Verifies if the user is able to see the you may also add Protection Plan section with learn more link in
     * Product Information Page.
     * Test Case ID - RGSN-38736
     */
    @Test
    public void testYouMayAlsoAddSectionWhenUserSelectsAttributesExpectYouMayAlsoAddSectionWithLearnMoreLink() {

        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.PROTECTION_PLAN));
        final ProductGroup expectedProductGroup = dataService.findProductGroup("all-state-protection-plan");
        final String expectedProtectionPlanText = "3-year Allstate Protection Plan for";
        final String expectedLearnMoreText = "Learn more";

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        PipFlows.setSimplePipAttributeSelections(productPage);

        // then
        assertTrue("You may also add all state protection plan section is not displayed", 
                productPage.isYouMayAlsoAddSectionPresentAndDisplayed());
        assertTrue("Learn More link is not present in you may also add protection plan section",
                productPage.isAllStateProtectionPlanLearnMoreLinkPresentAndDisplayed());
        String actualProtectionPlanText = productPage.getAllStateProtectionPlanInfoText();
        assertTrue("AllState Protection Plan info text is not same. Expected - \'" + expectedProtectionPlanText + "\'" + 
                "Actual - \'" + actualProtectionPlanText + "\'", actualProtectionPlanText.contains(expectedProtectionPlanText));
        String actualLearnmoreText = productPage.getProtectionPlanLearnMoreText();
        assertTrue("Learn More text is not same. Expected Text - \'" + expectedLearnMoreText + "\'" 
                + "Actual Text - \'" + actualLearnmoreText + "\'", actualLearnmoreText.equals(expectedLearnMoreText));      
    }

    /**
     * Verifies if the user is able to see the protection plan logo, add protection and no thanks button
     * in AllState Protection Plan Modal.
     * Test Case ID - RGSN-38726
     */
    @Test
    public void testAllStateProtectionPlanModalWhenUserClicksLearnMoreLinkExpectProtectionPlanLogoAndAddProtectionAndNoThanksButton() {

        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.PROTECTION_PLAN));
        final ProductGroup expectedProductGroup = dataService.findProductGroup("all-state-protection-plan");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        PipFlows.setSimplePipAttributeSelections(productPage);
        AllStateProtectionPlanOverlaySection allStateProtectionPlanOverlaySection = productPage.clickAllStateProtectionPlanLearnMoreLink();

        // then
        assertTrue("Add Protection Button is not displayed in AllState Protection Plan Modal", 
                allStateProtectionPlanOverlaySection.isAddProtectionButtonPresentAndDisplayed());
        assertTrue("No Thanks Button is not displayed in AllState Protection Plan Modal",
                allStateProtectionPlanOverlaySection.isNoThanksButtonPresentAndDisplayed());
        assertTrue("AllState Protection Plan logo is not present in modal",
                allStateProtectionPlanOverlaySection.isAllStateProtectionPlanLogoPresentAndDisplayed());
    }

    /**
     * Verifies if the user is able to see remove plan button in AllState Protection Plan Modal
     * when protection plan is added.
     * Test Case ID - RGSN-38736
     */
    @Test
    public void testAllStateProtectionPlanModalWhenUserSelectsProtectionPlanExpectRemovePlanButtonInModal() {

        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.PROTECTION_PLAN));
        final ProductGroup expectedProductGroup = dataService.findProductGroup("all-state-protection-plan");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        PipFlows.setSimplePipAttributeSelections(productPage);
        AllStateProtectionPlanOverlaySection allStateProtectionPlanOverlaySection = productPage.clickAllStateProtectionPlanLearnMoreLink();
        allStateProtectionPlanOverlaySection.clickAddProtectionOnTheWidget(ProductPage.class);

        // then
        assertTrue("AllState Proctection Plan checkbox is not selected", productPage.isAllStateProtectionPlanCheckboxSelected());
        productPage.clickAllStateProtectionPlanLearnMoreLink();
        assertTrue("Remove Plan Button is not displayed in AllState Protection Plan Modal", 
                allStateProtectionPlanOverlaySection.isRemovePlanButtonPresentAndDisplayed());
    }

    /**
     * Verifies if the user is able to see the same estimated charges for protection plan in product
     * page and protection plan modal when quantity is more than 1.
     * Test Case ID - RGSN-38736
     */
    @Test
    public void testAllStateProtectionPlanModalWhenUserSelectsProtectionPlanExpectEstimatedChargesIsSameInProductPageAndProtectionPlanModal() {

        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.PROTECTION_PLAN));
        final ProductGroup expectedProductGroup = dataService.findProductGroup("all-state-protection-plan");
        final int expectedQuantity = 2;

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        PipFlows.setSimplePipAttributeSelections(productPage);
        String estimatedProtectionPlanAmount = productPage.getAllStateProtectionPlanInfoText().replaceAll("[^0-9.]", "").substring(1);
        Float estimatedProtectionPlanAmountAfterIncreasingQuanity = Float.parseFloat(estimatedProtectionPlanAmount) * expectedQuantity;
        PipFlows.setSimplePipQuantity(productPage, String.valueOf(expectedQuantity));
        Float finalEstimatedProtectionPlanCharges = Float.parseFloat(productPage.getAllStateProtectionPlanInfoText().replaceAll("[^0-9.]", "").substring(1));

        // then
        assertEquals("Estimated protection plan charge after increasing quantity is not correct.",
                estimatedProtectionPlanAmountAfterIncreasingQuanity, finalEstimatedProtectionPlanCharges);

        // when
        productPage.selectAllStateProtectionPlanCheckbox();
        String expectedAllStateProtectionPlanInfoText = productPage.getAllStateProtectionPlanInfoText();
        AllStateProtectionPlanOverlaySection allStateProtectionPlanOverlaySection = productPage.clickAllStateProtectionPlanLearnMoreLink();
        String actualAllStateProtectionPlanInfoText = allStateProtectionPlanOverlaySection.getAllStateProtectionPlanInfoText();

        // then
        assertEquals("AllState Protection Plan estimated charges and info text is not matching in Product page and protection plan modal.",
                actualAllStateProtectionPlanInfoText, expectedAllStateProtectionPlanInfoText);
    }

    /**
     * Verifies if the user is able to close the protection plan modal by clicking on close icon.
     * Test Case ID - RGSN-38736
     */
    @Test
    public void testAllStateProtectionPlanModalWhenUserClickCloseIconInModalExpectProtectionPlanModalIsClosed() {

        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.PROTECTION_PLAN));
        final ProductGroup expectedProductGroup = dataService.findProductGroup("all-state-protection-plan");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        PipFlows.setSimplePipAttributeSelections(productPage);
        AllStateProtectionPlanOverlaySection allStateProtectionPlanOverlaySection = productPage.clickAllStateProtectionPlanLearnMoreLink();

        // then
        assertTrue("AllState Protection Plan modal is not displayed", 
                allStateProtectionPlanOverlaySection.isProtectionPlanModalDisplayed());
        allStateProtectionPlanOverlaySection.clickCloseIcon();
        assertTrue("AllState Protection Plan modal is not closed", 
                !allStateProtectionPlanOverlaySection.isProtectionPlanModalDisplayed());
    }

    /**
     * Verifies if the user is able to see the add to cart disclaimer text for protection plan is displayed in
     * rich add to cart overlay.
     * Test Case ID - RGSN-38736
     */
    @Test
    public void testRichAddToCartOverlayWhenUserAddsItemWithProtectionPlanExpectProtectionPlanText() {

        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.PROTECTION_PLAN) && context.isFullSiteExperience());
        final ProductGroup expectedProductGroup = dataService.findProductGroup("all-state-protection-plan");
        final String expectedDisclaimerText = "Subtotal does not include shipping & processing, protection plan, gift wrap, discount or tax";

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        PipFlows.setSimplePipAttributeSelections(productPage);
        productPage.selectAllStateProtectionPlanCheckbox();
        RichAddToCartOverlaySection richAddToCartOverlaySection = productPage.addToCart();

        // then
        assertEquals("Add to cart disclaimer text is not having protection plan label",
               expectedDisclaimerText, richAddToCartOverlaySection.getDisclaimerText());
    }
    
    /**
     * Verifies if user is able to see price range at multi buy pip  with lower limit as lowest of all sub 
     * products and higher limit as highest of all sub products.
     * Test Case ID - RGSN-38734
     */
    @Test
    @TestRail(id = "104608")
    public void testProductPriceRangeWhenUserVisitsMultiBuyPipExpectPriceRangeWithLowestAndHighestOfAllSubsetProducts() {

        //given
        final ProductGroup expectedProductGroup = dataService.findProductGroup("multi-buy-multi-sku-pip");

        //when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        List<Double> actualPriceRange = productPage.getPriceRange();
        List<Double> subsetPriceList = productPage.getSubsetPrices();
        Collections.sort(subsetPriceList);

        double highestPrice = subsetPriceList.get(subsetPriceList.size() - 1);
        double lowestPrice = subsetPriceList.get(0);

        //then
        assertTrue("Lowest price is not displayed as lower limit of price range." + " Expected :"+ lowestPrice + " actual :" + actualPriceRange.get(0), 
                actualPriceRange.get(0).equals(lowestPrice));
        assertTrue("Highest price is not displayed as higher limit of price range." + " Expected :"+ highestPrice + " actual :" + actualPriceRange.get(1),
                actualPriceRange.get(1).equals(highestPrice));
    }
    
    /**
     * Verifies that the credit card rewards message is not displayed on Donation PIPs.
     *
     * Test Case ID - RGSN-38735
     */
    @Test
    public void testDonationPipWhenUserVisitsDonationPipExpectCreditCardRewardsMessageNotDisplayed() {

        // given
        final ProductGroup expectedProductGroup = dataService.findProductGroup("donation-pip");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);

        // then
        assertFalse("Credit card offers widget should not display", productPage.isCreditCardOffersWidgetDisplayed());
    }

    /**
     * Verifies that on a resized browser window with table resolution user able to see the 
     * components of product page.
     * 
     * Test Case ID - RGSN-38229
     */
    @Test
    @TestRail(id = "104528")
    public void testProductPageWhenUserResizeBrowserWindowInTabletSizeExpectProductPageComponentsDisplayed() {

        Assume.assumeTrue(context.isFullSiteExperience());

        // given
        final ProductGroup expectedProductGroup = dataService.findProductGroup("simple-pip", "!attributes");
        Dimension dimension = new Dimension(990, 763);
        
        // when
        
        context.getPilot().getDriver().manage().window().setSize(dimension);
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        boolean testPassed = true;

        // then
        if (!productPage.isHeroImagePresentAndDisplayed()) {
            log.error("hero Image is not present on the product page");
            testPassed = false;
        }

        if (!productPage.isAlternateImageDisplayed()) {
            log.error("Alternate Image is not present on the product page");
            testPassed = false;
        }
        
        if (!productPage.isFullfillmentWidgetDisplayed()) {
            log.error("Fullfillement widget is not present on the product page");
            testPassed = false;
        }

        if (!productPage.isAddToCartButtonPresent()) {
            log.error("Add to cart button is not present on the product page");
            testPassed = false;
        }
        
        if (!productPage.isProductOverviewSectionDisplayed()) {
            log.error("Product Overview section is present on the product page");
            testPassed = false;
        }

        if (!productPage.isRecommendationCarouselDisplayed()) {
            log.error("Recommendation carousel is not present on the product page");
            testPassed = false;
        }
        assertTrue("Test step got failed, Please check the logs for that failed validation", testPassed);
    }
    
    /**
     * Verifies that on a resized browser window with mobile resolution user able to see the 
     * components of product page.
     * 
     * Test Case ID - RGSN-38229
     */
    @Test
    @TestRail(id = "104529")
    public void testProductPageWhenUserResizeBrowserWindowInMobileSizeExpectProductPageComponentsDisplayed() {

        Assume.assumeTrue(context.isFullSiteExperience());

        // given
        final ProductGroup expectedProductGroup = dataService.findProductGroup("simple-pip", "!attributes");
        Dimension dimension = new Dimension(700, 763);
        
        // when
        context.getPilot().getDriver().manage().window().setSize(dimension);
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        boolean testPassed = true;

        // then
        if (!productPage.isHeroImagePresentAndDisplayed()) {
            log.error("hero Image is not present on the product page");
            testPassed = false;
        }

        if (!productPage.isAlternateImageDisplayed()) {
            log.error("Alternate Image is not present on the product page");
            testPassed = false;
        }
        
        if (!productPage.isFullfillmentWidgetDisplayed()) {
            log.error("Fullfillement widget is not present on the product page");
            testPassed = false;
        }

        if (!productPage.isAddToCartButtonPresent()) {
            log.error("Add to cart button is not present on the product page");
            testPassed = false;
        }
        
        if (!productPage.isProductOverviewSectionDisplayed()) {
            log.error("Product Overview section is present on the product page");
            testPassed = false;
        }

        if (!productPage.isRecommendationCarouselDisplayed()) {
            log.error("Recommendation carousel is not present on the product page");
            testPassed = false;
        }
        assertTrue("Test step got failed, Please check the logs for that failed validation", testPassed);
    }

    /**
     * Verifies whether product price is converted to selected international country's
     * currency in product page.
     * Test Case ID - RGSN-38182
     */
    @Test
    @TestRail(id = "105261")
    public void testProductPageWhenUserSelectsInternationalCountryExpectCurrencyConversion() {

        // given
        Assume.assumeTrue(context.isFullSiteExperience());
        final ProductGroup productGroup = dataService.findProductGroup("simple-pip", "!attributes");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        NavigationSection navigationSection = context.getPageSection(NavigationSection.class);
        InternationalShippingOverlay internationalShippingOverlay = navigationSection.goToInternationalShippingOverlay();
        int countryOptionsAvailable = internationalShippingOverlay.getNonUsDollarInternationalShippingCountries();
        int countryNameIndex = RandomUtils.nextInt(1, countryOptionsAvailable);
        internationalShippingOverlay.selectNonUsDollarCurrencyInternationalCountry(countryNameIndex);
        String expectedCurrencyCode = internationalShippingOverlay.getCurrencyCodeFromInternationalShippingOverlay(countryNameIndex);
        internationalShippingOverlay.clickUpdateCurrencyAndCountryButton();
        String actualCurrencyCode = productPage.getInternationalCurrencyCode();

        // then
        Assert.assertEquals("Expected currency code \'" + expectedCurrencyCode + "\'" + "is not matching with actual currency code \'"
                + actualCurrencyCode + "\'", expectedCurrencyCode, actualCurrencyCode);
    }

    /**
     * Verifies whether product price is converted to selected international country's
     * currency in product page.
     * Test Case ID - RGSN-38182
     */
    @Test
    @TestRail(id = "105262")
    public void testAddToCartOverlayWhenUserSelectsInternationalCountryExpectCurrencyConversion() {

        // given
        Assume.assumeTrue(context.isFullSiteExperience());
        final ProductGroup productGroup = dataService.findProductGroup("simple-pip", "!attributes");
        final int expectedQuantity = 1;

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        NavigationSection navigationSection = context.getPageSection(NavigationSection.class);
        InternationalShippingOverlay internationalShippingOverlay = navigationSection.goToInternationalShippingOverlay();
        int countryOptionsAvailable = internationalShippingOverlay.getNonUsDollarInternationalShippingCountries();
        int countryNameIndex = RandomUtils.nextInt(1, countryOptionsAvailable);
        internationalShippingOverlay.selectNonUsDollarCurrencyInternationalCountry(countryNameIndex);
        String expectedCurrencyCode = internationalShippingOverlay.getCurrencyCodeFromInternationalShippingOverlay(countryNameIndex);
        internationalShippingOverlay.clickUpdateCurrencyAndCountryButton();
        productPage.setQuantity(expectedQuantity);
        RichAddToCartOverlaySection rac = productPage.addToCart();
        String actualCurrencyCode = rac.getInternationalCurrencyCode();

        // then
        Assert.assertEquals("Expected currency code \'" + expectedCurrencyCode + "\'" + "is not matching with acutal currency code \'"
                + actualCurrencyCode + "\'", expectedCurrencyCode, actualCurrencyCode);
    }

    /**
     * Verifies whether product price is converted to selected international country's
     * currency in Also in the collection section.
     * Test Case ID - RGSN-38220
     */
    @Test
    @TestRail(id = "105265")
    public void testAlsoInThisCollectionWhenUserSelectsInternationalCountryExpectCurrencyConversion() {

        // given
        Assume.assumeTrue(context.isFullSiteExperience());
        final ProductGroup productGroup = dataService.findProductGroup("also-in-the-collection");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        NavigationSection navigationSection = context.getPageSection(NavigationSection.class);
        InternationalShippingOverlay internationalShippingOverlay = navigationSection.goToInternationalShippingOverlay();
        int countryOptionsAvailable = internationalShippingOverlay.getNonUsDollarInternationalShippingCountries();
        int countryNameIndex = RandomUtils.nextInt(1, countryOptionsAvailable);
        internationalShippingOverlay.selectNonUsDollarCurrencyInternationalCountry(countryNameIndex);
        String expectedCurrencyCode = internationalShippingOverlay.getCurrencyCodeFromInternationalShippingOverlay(countryNameIndex);
        internationalShippingOverlay.clickUpdateCurrencyAndCountryButton();
        String actualCurrencyCode = productPage.getInternationalCurrencyCodeFromAlsoInThisCollectionSection();

        // then
        Assert.assertEquals("Expected currency code \'" + expectedCurrencyCode + "\'" + "is not matching with acutal currency code \'"
                + actualCurrencyCode + "\'", expectedCurrencyCode, actualCurrencyCode);
    }

    /**
     * Verifies whether product price is converted to selected international country's
     * currency in Product Recommendation section.
     * Test Case ID - RGSN-38220
     */
    @Test
    @TestRail(id = "105266")
    public void testPipRecommendationWhenUserSelectsInternationalCountryExpectCurrencyConversion() {

        //given
        Assume.assumeTrue(context.isFullSiteExperience());
        final ProductGroup productGroup = dataService.findProductGroup("also-in-the-collection");

        //when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        NavigationSection navigationSection = context.getPageSection(NavigationSection.class);
        InternationalShippingOverlay internationalShippingOverlay = navigationSection.goToInternationalShippingOverlay();
        int countryOptionsAvailable = internationalShippingOverlay.getNonUsDollarInternationalShippingCountries();
        int countryNameIndex = RandomUtils.nextInt(1, countryOptionsAvailable);
        internationalShippingOverlay.selectNonUsDollarCurrencyInternationalCountry(countryNameIndex);
        String expectedCurrencyCode = internationalShippingOverlay.getCurrencyCodeFromInternationalShippingOverlay(countryNameIndex);
        internationalShippingOverlay.clickUpdateCurrencyAndCountryButton();
        String actualCurrencyCode = productPage.getInternationalCurrencyCodeFromPipRecommendation();

        //then
        Assert.assertEquals("Expected currency code \'" + expectedCurrencyCode + "\'" + "is not matching with acutal currency code \'"
                + actualCurrencyCode + "\'", expectedCurrencyCode, actualCurrencyCode);
    }
}
