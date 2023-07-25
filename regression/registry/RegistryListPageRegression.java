package com.wsgc.ecommerce.ui.regression.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.account.AccountFlows;
import com.wsgc.ecommerce.ui.endtoend.registry.RegistryFlows;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSectionFactory;
import com.wsgc.ecommerce.ui.pagemodel.account.AccountHomePage;
import com.wsgc.ecommerce.ui.pagemodel.account.LoginPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.CrossBrandRegistryListPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.RegistryChecklistPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.registrylist.RegistryListItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.ecommerce.ui.regression.registry.syndication.RegistryListUrlBuilder;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.CustomerAccount;
import com.wsgc.evergreen.entity.api.Registry;

import tag.area.RegistryArea;

/**
 * Regression Tests for validating Registry list page.
 */
@Category(RegistryArea.class)
public class RegistryListPageRegression extends AbstractTest {

    /**
     * Prepares data fixtures & ensures feature flag is enabled.
     */
    @Before
    public void setup() {
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.REGISTRY_V2));
    }
    
    /**
     * Get the number of days between current date and the given date.
     * 
     * @param eventDay event date 
     * @param eventMonth event month 
     * @param eventYear event year
     */
    private long getNumberOfDays(String eventDay, int eventMonth , String eventYear) {
        Month eventMonthNew = Month.of(eventMonth);
        int eventYearNew = Integer.parseInt(eventYear);
        int eventDayNew = Integer.parseInt(eventDay);
        LocalDate eventDate = LocalDate.of(eventYearNew, eventMonthNew, eventDayNew);
        long numberOfDays = ChronoUnit.DAYS.between(LocalDate.now(), eventDate);
        return Math.abs(numberOfDays);
    }

    /**
     * Verifies whether a meta description tag is present in registry list page
     * when user visit registry with registrant details.
     * Test Case ID - RGSN-38270
     */
    @Test
    @TestRail(id = "102653")
    public void testRegistryListPageWhenRegistryHasOnlyRegistrantExpectMetaTagInPageSource() {

        // given
        final Registry registry = dataService.findRegistry("single-registry", "registry-list-with-registrant");  
        String metaDescriptionTemplate = "<meta name=\"description\" content=\"%s\">";
        String expectedMetaDescriptionTemplate = String.format(metaDescriptionTemplate, 
                registry.getUnmappedAttribute("metaDescription"));

        // when
        RegistryFlows.goToRegistryListPageFlow(context, registry);
        String pageSource = context.getPilot().getDriver().getPageSource();

        // then
        Assert.assertTrue(String.format("The Registrant meta tag %s is not present in page source of registry list page.", 
                expectedMetaDescriptionTemplate), pageSource.contains(expectedMetaDescriptionTemplate));  
    }

    /**
     * Verifies whether a meta description tag is present in registry list page when user 
     * visit registry with registrant & co-registrant details.
     * Test Case ID - RGSN-38270
     */
    @Test
    @TestRail(id = "102654")
    public void testRegistryListPageWhenRegistryHasCoRegistrantExpectMetaTagInPageSource() {

        // given
        final Registry registry = dataService.findRegistry("single-registry", "registry-list-with-co-registrant");  
        String metaDescriptionTemplate = "<meta name=\"description\" content=\"%s\">";
        String expectedMetaDescriptionTemplate = String.format(metaDescriptionTemplate, 
                registry.getUnmappedAttribute("metaDescription")).replaceAll("&", "&amp;");

        // when
        RegistryFlows.goToRegistryListPageFlow(context, registry);
        String pageSource = context.getPilot().getDriver().getPageSource();

        // then
        Assert.assertTrue(String.format("The Registrant & Co-Registrant meta tag %s is not present in page source of registry list page.", 
                expectedMetaDescriptionTemplate), pageSource.contains(expectedMetaDescriptionTemplate));
    }

    /**
     * Verifies if the user is able to navigate to a registry list page using the Barcode ID instead of registry ID.
     * 
     * Test Case ID - RGSN-38275, RGSN-38686
     */
    @Test
    @TestRail(id = "102655")
    public void testRegistryListPageWhenUserNavigatesToRegistryListPageWithBarcodeIdExpectRegistryListPageWithBarCodeId() {

        // given
        final Registry registry = dataService.findRegistry("single-registry", "registry-list-with-registrant");  

        // when
        RegistryFlows.goToRegistryListPageFlow(context, registry);
        CrossBrandRegistryListPage crossBrandRegistryListPage = context.getPage(CrossBrandRegistryListPage.class);

        //then
        assertTrue("Barcode is not displaying on Registry details page", crossBrandRegistryListPage.isBarcodeDisplayed());

        //when
        String defaultRegistryBarcode = crossBrandRegistryListPage.getBarcodeText();
        String newURL = RegistryListUrlBuilder.buildRegistryListUrl(context, defaultRegistryBarcode);
        context.getPilot().getDriver().get(newURL);
        String barcodeAfterRegistryNumberinURI = crossBrandRegistryListPage.getBarcodeText();

        // then
        assertTrue("Same Registry page is not loading when registry url hits with unique registry number", 
        		defaultRegistryBarcode.equals(barcodeAfterRegistryNumberinURI));
    }
    
    /**
     * Verifies that registry completion discount is not displayed for a 
     * registry with event date in future.
     * Test Case ID - RGSN-38255
     */
    @Test
    @TestRail(id = "104358")
    public void testRegistryListPageWhenRegistryHasFutureEventDateExpectNoCompletionDiscount() {
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.REGISTRY_COMPLETION_DISCOUNT));

        // given
        final Registry registry = dataService.findRegistry("registry-future-event-date");  
        final CustomerAccount account = dataService.findAccount("future-event-date-registry");  

        // when
        startPilotAt(context.getPage(LoginPage.class));
        AccountFlows.signInWithAccount(context, account);
        CrossBrandRegistryListPage registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);

        //then
        assertTrue("Registry completion discount is displayed for registry with future event date", !registryListPage.isCompletionDiscountDisplayed());
    }

    /**
     * Verifies that registry completion discount is displayed for a 
     * registry with event date in past and within the discount window.
     * Test Case ID - RGSN-38255
     */
    @Test
    @TestRail(id = "104359")
    public void testRegistryListPageWhenRegistryHasEventDateWithinDiscountWindowExpectCompletionDiscountLink() {
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.REGISTRY_COMPLETION_DISCOUNT));

        // given
        final Registry registry = dataService.findRegistry("registry-completion-discount");  
        final CustomerAccount account = dataService.findAccount("completion-discount-registry");  

        // when
        startPilotAt(context.getPage(LoginPage.class));
        AccountFlows.signInWithAccount(context, account);
        CrossBrandRegistryListPage registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);

        //then
        assertTrue("Registry completion discount is not displayed ", registryListPage.isCompletionDiscountDisplayed());
    }
    
    /**
     * Verifies that cross brands accordions in registry list page is 
     * in open state by default with products loaded.
     * Test Case ID - RGSN-38689
     */
    @Test
    @TestRail(id = "104602")
    public void testRegistryListPageWhenRegistrantHasCrossBrandProductsExpectExpandedCrossBrandAccordionWithProducts() {

        //given
        final Registry registry = dataService.findRegistry("cross-brand-product");
        final String email = registry.getRegistrant().getEmail();
        final String password = registry.getRegistrant().getPassword();
        int expectedNumberOfCrossBrandAccordion =  4;

        //when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);

        CrossBrandRegistryListPage registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);
        int actualNumberOfCrossBrandAccordion = registryListPage.getNumberOfCrossBrandAccordionPresent();

        //then
        assertEquals("All cross brand accordions are not displayed at registry list page" , 
        		expectedNumberOfCrossBrandAccordion , actualNumberOfCrossBrandAccordion);
    }
    
    /**
     * Verifies registrant is able to see registry dashboard at registry list page.
     * Test Case ID - RGSN-38686
     */
    @Test
    @TestRail(id = "104368")
    public void testRegistryDashboardWhenRegistrantVisitsRegistryListWithPastEventDateExpectRegistryDashboardWithDetails() {

        //given
        final Registry registry = dataService.findRegistry("cross-brand-product", "welcome-message");
        final String email = registry.getRegistrant().getEmail();
        final String password = registry.getRegistrant().getPassword();
        final int expectedTotalQuantity;
        final String expectedWelcomeMessage = registry.getWelcomeMessage();
        
        final String eventDay = registry.getEventDay();
        int eventMonth = Integer.parseInt(registry.getEventMonth());
        final String eventYear = registry.getEventYear();
        final String expectedDaysCountText = "Days Since Event";
        final String expectedDaysCount = String.valueOf(getNumberOfDays(eventDay, eventMonth, eventYear));
        final String expectedTitle = registry.getRegistrant().getFullName(); 
        final String expectedDate =  registry.getUnmappedAttribute("eventDate");
        
        //when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        CrossBrandRegistryListPage registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);

        List<Integer> quantityList =  registryListPage.getListOfStillNeededQuantity();
        expectedTotalQuantity = quantityList.stream().mapToInt(Integer::intValue).sum();
        int actualTotalQuantity = registryListPage.getTotalItemsFromDashboard();
        int expectedTotalPurchasedQuantity = actualTotalQuantity - expectedTotalQuantity ;
        int actualTotalPurchasedQuantity = registryListPage.getTotalPurchasedFromDashboard();
        
        //then
        assertEquals("Total items count is not matching , expected : " + expectedTotalQuantity + ", actual : " + actualTotalQuantity ,
        		expectedTotalQuantity , actualTotalQuantity);
        assertEquals("Total Purchased count is not matching , expected : " + expectedTotalPurchasedQuantity + ", actual : " + actualTotalPurchasedQuantity ,
        		expectedTotalPurchasedQuantity , actualTotalPurchasedQuantity);
        
        String actualWelcomeMessage = registryListPage.getWelcomeMessageText();
        assertTrue(String.format("Welcome message is not displayed, expected : %s and actual : %s", expectedWelcomeMessage, actualWelcomeMessage ), 
        		actualWelcomeMessage.equalsIgnoreCase(expectedWelcomeMessage));
        
        String actualTitle = registryListPage.getRegistryListPageTitle();
        String actualDaysCountText = registryListPage.getDaysCountText();
        String actualDaysCount = registryListPage.getDaysCount();
        String actualDate = registryListPage.getEventDate();

        assertTrue(String.format("Title is not matching, expected : %s and actual : %s", expectedTitle, actualTitle), 
        		expectedTitle.equalsIgnoreCase(actualTitle)); 
        assertTrue(String.format("Event date not matching, expected : %s and actual : %s", expectedDate, actualDate), 
        		actualDate.contains(expectedDate));
        assertTrue(String.format("There is a mismatch in the expected and actual Days count text, expected : %s and actual : %s", expectedDaysCountText, 
        		actualDaysCountText), actualDaysCountText.equalsIgnoreCase(expectedDaysCountText)); 
        assertTrue(String.format("There is a mismatch in the expected and actual Days count, expected : %s and actual : %s", expectedDaysCount,
        		actualDaysCount), actualDaysCount.equalsIgnoreCase(expectedDaysCount)); 
    }
    
    /**
     * Verifies registrant is able to see days count in dashboard at registry list page for registry 
     * with future event date.
     * Test Case ID - RGSN-38686
     */
    @Test
    @TestRail(id = "104367")
    public void testRegistryDashboardWhenRegistrantVisitsRegistryListWithFutureEventDateExpectRegistryDashboardWithDetails() {
    	
        //given
        final Registry registry = dataService.findRegistry("registry-with-recommendations");
        final String email = registry.getRegistrant().getEmail();
        final String password = registry.getRegistrant().getPassword();
        
        final String eventDay = registry.getEventDay();
        int eventMonth = Integer.parseInt(registry.getEventMonth());
        final String eventYear = registry.getEventYear();
        final String expectedDaysCountText = "Days Until Event";
        final String expectedDaysCount = String.valueOf(getNumberOfDays(eventDay, eventMonth, eventYear));
        		
        //when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        CrossBrandRegistryListPage registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);

        //then
        String actualDaysCountText = registryListPage.getDaysCountText();
        String actualDaysCount = registryListPage.getDaysCount();
        
        assertTrue(String.format("Days count text is mismatching, expected : %s and actual : %s", expectedDaysCountText, actualDaysCountText), 
        		actualDaysCountText.equalsIgnoreCase(expectedDaysCountText)); 
        assertTrue(String.format("There is a mismatch in the expected and actual Days count, expected : %s , actual : %s", expectedDaysCount,
        		actualDaysCount), actualDaysCount.equalsIgnoreCase(expectedDaysCount)); 
    }
    
    /**
     * Verifies that registrant able to navigate to Gift tracker page using the top navigation.
     * Test Case ID - RGSN-38687, RGSN-38273
     */
    @Test
    @TestRail(id = "104370")
    public void testPageTitleWhenRegistrantClicksOnGiftTrackerButtonExpectGiftTrackerPageTitle() {

        // given
        final Registry registry = dataService.findRegistry("cross-brand-product");
        final String email = registry.getRegistrant().getEmail();
        final String password = registry.getRegistrant().getPassword();
        final String giftTrackerPageTitle = "Gift Tracker";

        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        CrossBrandRegistryListPage crossBrandRegistryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);

        // then
        assertTrue("Top navigation tab is not displayed for registrant", crossBrandRegistryListPage.isTopNavigationTabDisplayed());

        crossBrandRegistryListPage.clickGiftTrackerTab();
        assertTrue("Gift tracker page is not displayed", context.getPilot().getDriver().getTitle().contains(giftTrackerPageTitle));
    }

    /**
     * Verifies that registrant able to navigate to Registry checklist page using the top navigation.
     * Test Case ID - RGSN-38687
     */
    @Test
    @TestRail(id = "104604")
    public void testPageTitleWhenRegistrantClicksOnRegistryCheckListButtonExpectRegistryCheckListPageTitle() {

        // given
        final Registry registry = dataService.findRegistry("cross-brand-product");
        final String email = registry.getRegistrant().getEmail();
        final String password = registry.getRegistrant().getPassword();
        final String registryChecklistPageTitle = "Registry Checklist";

        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        CrossBrandRegistryListPage crossBrandRegistryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);

        // then
        assertTrue("Top navigation tab is not displayed for registrant", crossBrandRegistryListPage.isTopNavigationTabDisplayed());

        crossBrandRegistryListPage.clickCheckListTab();
        assertTrue("Registry checklist page is not displayed", context.getPilot().getDriver().getTitle().contains(registryChecklistPageTitle));
    }

    /**
     * Verifies that registrant able to navigate to Registry list page using the top navigation.
     * Test Case ID - RGSN-38687
     */
    @Test
    @TestRail(id = "104605")
    public void testPageTitleWhenRegistrantClicksOnRegistryListButtonExpectRegistryListPageTitle() {

        // given
        final Registry registry = dataService.findRegistry("cross-brand-product");
        final String email = registry.getRegistrant().getEmail();
        final String password = registry.getRegistrant().getPassword();
        final String registryListPageTitle = "Registry";

        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        CrossBrandRegistryListPage crossBrandRegistryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);
        RegistryChecklistPage registryCheckListPage = crossBrandRegistryListPage.clickCheckListTab();
        registryCheckListPage.clickRegistryListTab();

        // then
        assertTrue("Registry list page is not displayed", context.getPilot().getDriver().getTitle().contains(registryListPageTitle));
    }    
    
    /**
     * Verifies if user is able to navigate to the product page on clicking a 
     * recommendation product from registry list page.
     * Test Case ID - RGSN-38688
     */
    @Test
    @TestRail(id = "104603")
    public void testRegistryListPageWhenUserClicksRecommendationProductExpectProductPage() {

        //given
        final Registry registry = dataService.findRegistry("registry-with-recommendations");
        final String email = registry.getRegistrant().getEmail();
        final String password = registry.getRegistrant().getPassword();
        final int expectedNumberOfCarousel = 2;
        final String EXPECTED_RECOMMENDATION_PARAM = "?cm_src=WsiRegistryList1";

        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        CrossBrandRegistryListPage registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);

        //then
        assertEquals("Two recommendation carousels are not displayed at registry list page " , expectedNumberOfCarousel, 
                registryListPage.getNumberOfRecommendationCarousel());

        int recommendationItemsAvailable = registryListPage.getNumberOfRecommendationItemsAvailable();
        int recommendationItemIndex = RandomUtils.nextInt(0, recommendationItemsAvailable - 1);
        registryListPage.clickRecentlyViewedItem(recommendationItemIndex);
        String productPageUrl = context.getPilot().getDriver().getCurrentUrl();

        assertTrue(String.format("Product page url - %s - doesn't contain Registry recommendation query parameter, %s",
                productPageUrl, EXPECTED_RECOMMENDATION_PARAM), productPageUrl.contains(EXPECTED_RECOMMENDATION_PARAM));
    }
    
    /**
     * Verifies that quantity exceeded message is displayed when user adds more than still needs quantity of products
     * to cart from registry list page.
     * Test Case ID - RGSN-38728
     */
    @Test
    @TestRail(id = "104606")
    public void testQuantityExceededMessageWhenUserAddMoreThanStillNeedsQuantityOfProductFromRegistryListExpectQuantityExceededMessage() {

        //given
        final Registry registry = dataService.findRegistry("registry-with-monogram-item");
        final String expectedSku = registry.getUnmappedAttribute("sku");
        final String expectedMessage = "This quantity exceeds the amount requested and may result in extra purchases.";
        
        //when
        CrossBrandRegistryListPage registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);        
        RegistryListItemRepeatableSection registryListItemRepeatableSection = registryListPage.getItem(expectedSku);
        String quantity = registryListItemRepeatableSection.getStillNeedsQuantityText();
        int stillNeededQuantity = Integer.parseInt(quantity);
        String expectedQuantity = String.valueOf(stillNeededQuantity + 1);
        registryListItemRepeatableSection.setQuantity(expectedQuantity);
        String actualErrorMesssage = registryListItemRepeatableSection.getQuantityExceedsMessage();
        
        //then
        assertTrue(String.format("Quantity exceeded message is not displayed, expected : %s , actual : %s", expectedMessage, actualErrorMesssage),
        		actualErrorMesssage.equalsIgnoreCase(expectedMessage));
    }

    /**
     * Verifies that registry completion discount learn more link is displayed for a 
     * registry with event date in past and within the discount window.
     * Test Case ID - RGSN-38729
     */
    @Test
    @TestRail(id = "104661")
    public void testRegistryListPageWhenRegistryHasEventDateWithinDiscountWindowExpectLearnMoreLinkInCompletionDiscount() {

        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.REGISTRY_COMPLETION_DISCOUNT));
        final Registry registry = dataService.findRegistry("registry-completion-discount");
        final CustomerAccount account = dataService.findAccount("completion-discount-registry");
        final Map<String, String> expectedDiscountTextTemplates = new HashMap<>();
        expectedDiscountTextTemplates.put("expectedHeadingText", "Your Registry Completion Discounts!");
        expectedDiscountTextTemplates.put("expectedContentText", "View your registry list from each brand to redeem your registry completion discount.");
        expectedDiscountTextTemplates.put("expectedDiscountTextWS", "10% Completion Discount at Williams Sonoma");
        expectedDiscountTextTemplates.put("expectedDiscountTextPB", "10% Completion Discount at Pottery Barn");
        expectedDiscountTextTemplates.put("expectedDiscountTextPK", "15% Completion Discount at Pottery Barn Kids");

        // when
        startPilotAt(context.getPage(LoginPage.class));
        AccountFlows.signInWithAccount(context, account);
        CrossBrandRegistryListPage registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);

        // then
        assertTrue("Learn More link in completion discount section is not displayed ", 
                registryListPage.isCompletionDiscountLearnMoreLinkDisplayed());

        // when
        registryListPage.clickLearnMoreLink();
        String learnMoreModalTextMessage = registryListPage.getLearnMoreModalTextMessage();

        //then
        for (Map.Entry<String, String> expectedDiscountTextTemplate : expectedDiscountTextTemplates.entrySet()) {
            assertTrue("Expected Discount Text \'" + expectedDiscountTextTemplate.getValue() + "\' template is not present in learn more modal",
                    learnMoreModalTextMessage.contains(expectedDiscountTextTemplate.getValue()));
        }
    }
    
    /**
     * Verify whether registrant user is able to update the Item Quantity from registry list page.
     * Test case ID - RGSN-38254
     */
    @Test
    @TestRail(id = "104540")
    public void testRegistryListPageWhenRegistrantUpdatesRequestedQuantityExpectQuantityUpdated() {
        // given
        final Registry registry = dataService.findRegistry("registry-with-item");
        final CustomerAccount account = dataService.findAccount("single-user-registry");
        final String email = account.getEmail();
        final String password = account.getPassword();
        final String sku = registry.getUnmappedAttribute("sku");
        int quantity = RandomUtils.nextInt(1, 99);
        String expectedQuantity = String.valueOf(quantity);

        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        CrossBrandRegistryListPage registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);
        RegistryListItemRepeatableSection registryListItemRepeatableSection = registryListPage.getItem(sku);      
        registryListItemRepeatableSection.setRequestedQuantity(expectedQuantity);
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        navigationSection.openMyAccountDropdownByHovering();
        navigationSection.signOut();
        
        registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);
        RegistryListItemRepeatableSection registryListItemRepeatableSectionAsGuest = registryListPage.getItem(sku);      
        String updatedQuantity = registryListItemRepeatableSectionAsGuest.getStillNeedsQuantityText();
        
        //then
        assertEquals(String.format("Quantity is not updated properly , expected : %s , actual : %s ", expectedQuantity, updatedQuantity),
        		expectedQuantity,updatedQuantity);
    }
    
    /**
     * Verify whether the user is able to see the Free Registry Consultation Services in footer
     * at registry list page with Registry Online Chat option.
     * Test Case ID - RGSN-38792
     */
    @Test
    @TestRail(id = "104588")
    public void testRegistryListPageWhenUserVisitsRegistryListPageExpectRegistryOnlineChat() {
        
        // given
        final Registry registry = dataService.findRegistry("registry-with-item");
        
        // when
        CrossBrandRegistryListPage registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);
        
        // then
        assertTrue("Free Registry Consultation Services section is not displayed in registry list page", 
                registryListPage.isFreeRegistryConsultationServicesSectionPresentAndDisplayed());
        assertTrue("Registry online chat option is not available in registry consultation services",
                registryListPage.isOnlineChatButtonPresentAndDisplayed());
    }
    
    /**
     * Verify whether the user is able to initiate the online chat by clicking chat now button.
     * Test Case ID - RGSN-38792
     */
    @Test
    @TestRail(id = "104589")
    public void testRegistryListPageWhenUserClicksChatNowButtonAndSelectsRegistryHelpOptionExpectChatIsInitiated() {
        
        // given
        Assume.assumeTrue(context.isFullSiteExperience());
        final Registry registry = dataService.findRegistry("registry-with-item");
        final String expectedResponseTextMessage = "You’ve been placed in line to speak with a live representative. "
                + "Your expected wait is unknown. Our normal business hours are 8am - 7pm Pacific";
        
        // when
        CrossBrandRegistryListPage registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);
        registryListPage.clickOnlineChatButton();

        // then
        assertTrue("The Design chat window is opened", registryListPage.isDesignChatPopupOpened());
        String originalHandle = context.getPilot().getDriver().getWindowHandle();
        registryListPage.clickPopChatButton();
        registryListPage.waitForNewWindow(10);
        for (String handle : context.getPilot().getDriver().getWindowHandles()) {
            if (!handle.equals(originalHandle)) {
                context.getPilot().getDriver().switchTo().window(handle);
                assertTrue("Registry Help Option is not listed in Container List Options at Design chat window", 
                        registryListPage.isRegistryHelpButtonPresentAndDisplayed());
                registryListPage.clickRegistryHelpButton();
                String actualResponseTextMessage = registryListPage.getResponseTextMessage();
                assertEquals("Response Text Message is not same. Expected Text - " + expectedResponseTextMessage
                        + "Actual Text - " + actualResponseTextMessage, actualResponseTextMessage, expectedResponseTextMessage);
                context.getPilot().getDriver().close();
            }
        }
        context.getPilot().getDriver().switchTo().window(originalHandle);
    }

    /**
     * Verify whether registrant user is able to see all the components of product card on registry list page.
     * Test case ID - RGSN-38793
     */
    @Test
    @TestRail(id = "104539")
    public void testRegistryListPageWhenRegistrantVisitsRegistryListPageExpectAllTheComponentsOfSavedProduct() {

        // given
        final Registry registry = dataService.findRegistry("registry-with-item");
        final CustomerAccount account = dataService.findAccount("single-user-registry");
        final String email = account.getEmail();
        final String password = account.getPassword();
        final String sku = registry.getUnmappedAttribute("sku");

        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        CrossBrandRegistryListPage registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);
        RegistryListItemRepeatableSection registryListItemRepeatableSection = registryListPage.getItem(sku);
        String itemSKu = registryListItemRepeatableSection.getItemSku();
        String regularPrice = registryListItemRepeatableSection.getProductRegularPriceText();

        // given
        assertTrue("The Product image is not available on the registry list page", registryListItemRepeatableSection.isProductImagePresent());
        assertTrue("The Product Name label is not available on the registry list page", registryListItemRepeatableSection.isProductNameLabelPresent());
        assertTrue(String.format("The Product Sku is not displayed, expected : %s , actual : %s", sku, itemSKu), itemSKu.equalsIgnoreCase(sku));

        // when
        registryListItemRepeatableSection.clickProductName();
        ProductPage productPage = context.getPage(ProductPage.class);

        // then
        assertEquals(String.format("The product price is not matched on PIP, expected : %s , actual : %s", regularPrice, productPage.getProductRegularPrice()),
                regularPrice, productPage.getProductRegularPrice());
    }
}
