package com.wsgc.ecommerce.ui.regression.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.account.AccountFlows;
import com.wsgc.ecommerce.ui.endtoend.registry.RegistryFlows;
import com.wsgc.ecommerce.ui.endtoend.registry.RegistryFlows.PrivacySettings;
import com.wsgc.ecommerce.ui.pagemodel.account.AccountHomePage;
import com.wsgc.ecommerce.ui.pagemodel.account.LoginPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.CreateRegistryOverlaySection;
import com.wsgc.ecommerce.ui.pagemodel.registry.CreateRegistryPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.CreateRegistrySignInPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.CrossBrandRegistryListPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.RegistryLandingPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.RegistryLoginPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.RegistryProfileFormSection;
import com.wsgc.ecommerce.ui.pagemodel.registry.find.FindRegistryPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.find.FindRegistryResultsPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.find.RegistryProtectedOverlaySection;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.CustomerAccount;
import com.wsgc.evergreen.entity.api.MutableAccount;
import com.wsgc.evergreen.entity.api.Registry;
import com.wsgc.evergreen.impl.DefaultEvergreenContext;

import tag.area.RegistryArea;

/**
 * Regression Tests for validating create registry flows.
 */
@Category(RegistryArea.class)
public class CreateRegistryRegression  extends AbstractTest {
    
	private final Logger log = LoggerFactory.getLogger(getClass());
	
    /**
     * Prepares data fixtures & ensures feature flag is enabled.
     */
    @Before
    public void setUp() {
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.REGISTRY_V2));
    }
    
    /**
     * Sets the Brand to be tested.
     *
     * @param brand The concept code for the Brand to be set
     */
    private void setBrand(String brand) {
        System.setProperty("brand", brand);
    }
    
    /**
     * Verifies whether a signed in user is able to create registry from registry
     * landing page and accessible over crossbrands.
     * Test Case ID - RGSN-38266, RGSN-38789
     */
    @Test
    @TestRail(id = "104374")
    public void testCreateRegistryWhenSignedInUserCreatesRegistryFromRegistryLandingPageExpectCrossBrandRegistryCreated() {

        //given
        final Registry registry = dataService.findRegistry("create-registry");
        MutableAccount mutableAccount = getMutableAccount("default");
        CustomerAccount account = createAccount(mutableAccount);
        boolean useDefaultAddress = true;
        int numberOfCrossBrandAccordion =  4;
        int expectedTotalItem = 0;
        String initialBrand = System.getProperty("brand");
        String[] brands = { "PB", "PK", "WE", "WS"};

        //when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signInWithAccount(context, account);
        RegistryLandingPage registryLandingPage = context.getPage(RegistryLandingPage.class);
        context.getPilot().startAt(registryLandingPage);
        registryLandingPage.clickCreateRegistry();
        CrossBrandRegistryListPage crossBrandRegistryListPage ;
        if (context.supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY)) {
            crossBrandRegistryListPage = RegistryFlows.createRegistryFlow(context, registry, account, PrivacySettings.PUBLIC , useDefaultAddress);            
        } else {
            crossBrandRegistryListPage = RegistryFlows.createRegistryFlowFromRegistryLoginPage(context, registry);
        }
        int actualAccordion = crossBrandRegistryListPage.getNumberOfCrossBrandAccordions();

        //then
        assertEquals("All cross brand accordions are not displayed at registry list page" , numberOfCrossBrandAccordion , actualAccordion);
        assertEquals("Registry is not empty", expectedTotalItem , crossBrandRegistryListPage.getTotalItemsFromDashboard());

        for (String brand : brands) {
            if (!brand.equalsIgnoreCase(initialBrand)) {
                try {
                    setBrand(brand);
                    context.getPilot().getDriver().close();
                    context = DefaultEvergreenContext.getContext();
                    DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();
                                        
                    AccountFlows.goToLoginPageFlow(context);
                    AccountFlows.signIn(context, account.getEmail(), account.getPassword(), AccountHomePage.class);
                    CrossBrandRegistryListPage registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);
                    int actualAccordionInCrossBrand = registryListPage.getNumberOfCrossBrandAccordions();

                    //then
                    assertEquals("All cross brand accordions are not displayed at registry list page" , numberOfCrossBrandAccordion ,
                    		actualAccordionInCrossBrand);
                    assertEquals("Registry is not empty", expectedTotalItem , registryListPage.getTotalItemsFromDashboard());
                } finally {
                    setBrand(initialBrand);
                }
            }
        }
    }

    /**
     * Verifies that privacy settings is set to Public by default when user creates
     * a registry.
     * Test Case ID - RGSN-38263
     */
    @Test
    @TestRail(id = "104375")
    public void testDefaultPrivacySettingsWhenUserIsCreatingRegistryExpectPrivacySettingsIsPublicByDefault() {
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY));

        //given
        final Registry registry = dataService.findRegistry("create-registry");
        final CustomerAccount account = dataService.findAccount("single-user-registry");
        final String expectedPrivacySettings = "Public";

        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signInWithAccount(context, account);
        
        RegistryLandingPage registryLandingPage = context.getPage(RegistryLandingPage.class);
        context.getPilot().startAt(registryLandingPage);
        registryLandingPage.clickCreateRegistry();
        CreateRegistryOverlaySection createRegistryOverlay = context.getPageSection(CreateRegistryOverlaySection.class);
        //Filling Event details in create registry overlay
        createRegistryOverlay.setFirstName(registry.getRegistrant().getFirstName());
        createRegistryOverlay.setLastName(registry.getRegistrant().getLastName());
        createRegistryOverlay.setEventType(registry.getEventType());
        createRegistryOverlay.setEventDate(registry.getEventMonth() + registry.getEventDay() + registry.getEventYear());
        createRegistryOverlay.clickNextButton();
        
        //Filling the Shipping address in create registry overlay
        createRegistryOverlay.setFirstName(registry.getRegistrant().getFirstName());
        createRegistryOverlay.setLastName(registry.getRegistrant().getLastName());
        createRegistryOverlay.setAddress(registry.getRegistrant().getAddress().getAddress1());
        createRegistryOverlay.setCity(registry.getRegistrant().getAddress().getCity());
        createRegistryOverlay.setState(registry.getRegistrant().getAddress().getState());
        createRegistryOverlay.setZipCode(registry.getRegistrant().getAddress().getZip());
        createRegistryOverlay.setPhoneNumber(registry.getRegistrant().getDaytimePhone());
        createRegistryOverlay.clickNextButton();
        String actualPrivacySettings = createRegistryOverlay.getPrivacySettings();

        //then
        assertTrue("Privacy settings is not Public by default , found : " + actualPrivacySettings, 
        		actualPrivacySettings.equalsIgnoreCase(expectedPrivacySettings));
    }
    
    /**
     * Verifies if the user is able to see the error messages when submits the form without filling registrant details
     * in create registry overlay section.
     * Test Case ID - RGSN-38789
     */
    @Test
    @TestRail(id = "106708")
    public void testCreateRegistryOverlaySectionWhenUserClicksNextButtonWithoutFillingRegistrantDetailsExpectErrorMessage() {
        
        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY));
        MutableAccount mutableAccount = getMutableAccount("default");
        CustomerAccount newAccount = accountCreator.getRandomEmailAccount(mutableAccount);
        final String expectedFirstNameErrorMessage = "Please enter a first name";
        final String expectedLastNameErrorMessage = "Please enter a last name";
        final String expectedEventTypeErrorMessage = "Please select an event type";
        final String expectedEventDateErrorMessage = "Please enter an event date";
        
        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.createAccount(context, newAccount, AccountHomePage.class);
        RegistryLandingPage registryLandingPage = context.getPage(RegistryLandingPage.class);
        context.getPilot().startAt(registryLandingPage);
        registryLandingPage.clickCreateRegistry();
        CreateRegistryOverlaySection createRegistryOverlay = context.getPageSection(CreateRegistryOverlaySection.class);
        createRegistryOverlay.clearFirstNameField();
        createRegistryOverlay.clearLastNameField();
        createRegistryOverlay.clickNextButton();
         
        boolean testPassed = true;
        // then
        if (!createRegistryOverlay.getFirstNameErrorMessage().equals(expectedFirstNameErrorMessage)) {
            log.error("First Name error text message is not same");
            testPassed = false;
        }
        if (!createRegistryOverlay.getLastNameErrorMessage().equals(expectedLastNameErrorMessage)) {
            log.error("Last Name error text message is not same");
            testPassed = false;
        }
        if (!createRegistryOverlay.getEventTypeErrorMessage().equals(expectedEventTypeErrorMessage)) {
            log.error("Event Type error text message is not same");
            testPassed = false;
        }
        if (!createRegistryOverlay.getEventDateErrorMessage().equals(expectedEventDateErrorMessage)) {
            log.error("Event Date error text message is not same");
            testPassed = false;
        }
        assertTrue("Test step got failed, Please check the logs for that failed validation", testPassed);
    }
    
    /**
     * Verifies if the user is able to see the error messages when submits the form without filling address details
     * in create registry overlay section.
     * Test Case ID - RGSN-38789
     */
    @Test
    @TestRail(id = "106709")
    public void testCreateRegistryOverlaySectionWhenUserClicksNextButtonWithoutAddressDetailsExpectErrorMessage() {
        
        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY));
        final Registry registry = dataService.findRegistry("create-registry");
        MutableAccount mutableAccount = getMutableAccount("default");
        CustomerAccount newAccount = accountCreator.getRandomEmailAccount(mutableAccount);
        final String expectedFirstNameErrorMessage = "Please enter a first name";
        final String expectedLastNameErrorMessage = "Please enter a last name";
        final String expectedAddressErrorMessage = "Please enter an address";
        final String expectedCityErrorMessage = "Please enter a city";
        final String expectedStateErrorMessage = "Please select a state";
        final String expectedZipCodeErrorMessage = "Please enter a zipcode";
        final String expectedPhoneNumberErrorMessage = "Please enter a phone number";
        
        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.createAccount(context, newAccount, AccountHomePage.class);
        RegistryLandingPage registryLandingPage = context.getPage(RegistryLandingPage.class);
        context.getPilot().startAt(registryLandingPage);
        registryLandingPage.clickCreateRegistry();
        CreateRegistryOverlaySection createRegistryOverlay = context.getPageSection(CreateRegistryOverlaySection.class);
        createRegistryOverlay.setEventType(registry.getEventType());
        createRegistryOverlay.setEventDate(registry.getEventMonth() + registry.getEventDay() + registry.getEventYear());
        createRegistryOverlay.clickNextButton();
        createRegistryOverlay.clearFirstNameField();
        createRegistryOverlay.clearLastNameField();
        createRegistryOverlay.clickNextButton();
        
        boolean testPassed = true;
        // then
        if (!createRegistryOverlay.getFirstNameErrorMessage().equals(expectedFirstNameErrorMessage)) {
            log.error("First Name error text message is not same");
            testPassed = false;
        }
        if (!createRegistryOverlay.getLastNameErrorMessage().equals(expectedLastNameErrorMessage)) {
            log.error("Last Name error text message is not same");
            testPassed = false;
        }
        if (!createRegistryOverlay.getAddressErrorMessage().equals(expectedAddressErrorMessage)) {
            log.error("Address error text message is not same");
            testPassed = false;
        }
        if (!createRegistryOverlay.getCityErrorMessage().equals(expectedCityErrorMessage)) {
            log.error("City error text message is not same");
            testPassed = false;
        }
        if (!createRegistryOverlay.getStateErrorMessage().equals(expectedStateErrorMessage)) {
            log.error("State error text message is not same");
            testPassed = false;
        }
        if (!createRegistryOverlay.getZipCodeErrorMessage().equals(expectedZipCodeErrorMessage)) {
            log.error("Zipcode error text message is not same");
            testPassed = false;
        }
        if (!createRegistryOverlay.getPhoneNumberErrorMessage().equals(expectedPhoneNumberErrorMessage)) {
            log.error("Phone Numbe rerror text message is not same");
            testPassed = false;
        }
        assertTrue("Test step got failed, Please check the logs for that failed validation", testPassed);
    }
    
    /**
     * Verifies if the user is able to see the Address Verification Service error when user enters 
     * incorrect address details in create registry overlay section.
     * Test Case ID - RGSN-38789
     */
    @Test
    @TestRail(id = "106710")
    public void testCreateRegistryOverlaySectionWhenUserEntersIncorrectAddressExpectAddressVerificationServiceError() {
        
        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY));
        final Registry registry = dataService.findRegistry("create-registry");
        MutableAccount mutableAccount = getMutableAccount("default");
        CustomerAccount newAccount = accountCreator.getRandomEmailAccount(mutableAccount);
        final String expectedAddressVerificationServiceErrorMessage = "The street you entered may not be accurate. Please make "
                + "corrections if needed or use the address as entered";
        
        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.createAccount(context, newAccount, AccountHomePage.class);
        RegistryLandingPage registryLandingPage = context.getPage(RegistryLandingPage.class);
        context.getPilot().startAt(registryLandingPage);
        registryLandingPage.clickCreateRegistry();
        CreateRegistryOverlaySection createRegistryOverlay = context.getPageSection(CreateRegistryOverlaySection.class);
        createRegistryOverlay.setFirstName(registry.getRegistrant().getFirstName());
        createRegistryOverlay.setLastName(registry.getRegistrant().getLastName());
        createRegistryOverlay.setEventType(registry.getEventType());
        createRegistryOverlay.setEventDate(registry.getEventMonth() + registry.getEventDay() + registry.getEventYear());
        createRegistryOverlay.clickNextButton();
        createRegistryOverlay.setAddress(registry.getRegistrant().getAddress().getAddress1().substring(1, 5));
        createRegistryOverlay.setCity(registry.getRegistrant().getAddress().getCity());
        createRegistryOverlay.setState(registry.getRegistrant().getAddress().getState());
        createRegistryOverlay.setZipCode(registry.getRegistrant().getAddress().getZip());
        createRegistryOverlay.setPhoneNumber(registry.getRegistrant().getDaytimePhone());
        createRegistryOverlay.clickNextButton();
        
        boolean testPassed = true;
        // then
        if (!createRegistryOverlay.isAddressVerificationServiceErrorDisplayed()) {
            log.error("Address Verification Service error is not shown for Invalid Address");
            testPassed = false;
        }
        if (!createRegistryOverlay.getAddressVerificationServiceErrorMessage().equals(expectedAddressVerificationServiceErrorMessage)) {
            log.error("Address Verification Service error message is not same");
            testPassed = false;
        }
        if (!createRegistryOverlay.isUseAsEnteredButtonPresentAndDisplayed()) {
            log.error("Use as Entered button is not displayed");
            testPassed = false;
        }
        assertTrue("Test step got failed, Please check the logs for that failed validation", testPassed);
    }
    
    /**
     * Verifies if the user is able to see the Address Verification Service error when user enters 
     * incorrect address details in create registry page
     * Test Case ID - RGSN-38789
     */
    @Test
    @TestRail(id = "106712")
    public void testCreateRegistryPageWhenUserEntersIncorrectAddressExpectAddressVerificationServiceError() {
        
        // given
        Assume.assumeTrue(!context.getTargetSite().supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY));
        final Registry registry = dataService.findRegistry("create-registry");
        MutableAccount mutableAccount = getMutableAccount("default");
        CustomerAccount newAccount = accountCreator.getRandomEmailAccount(mutableAccount);
        final String expectedAddressVerificationServiceErrorMessage = "The street you entered may not be accurate. Please make "
                + "corrections if needed, or use the address as entered.";
        
        // when
        RegistryLandingPage registryLandingPage = context.getPage(RegistryLandingPage.class);
        context.getPilot().startAt(registryLandingPage);
        registryLandingPage.clickCreateRegistry();
        CreateRegistryPage createRegistryPage = RegistryFlows.createAccount(context, newAccount, CreateRegistryPage.class);
        createRegistryPage.submitCreateRegistryWithInvalidAddress(registry, CreateRegistryPage.class);
        
        boolean testPassed = true;
        // then
        if (!createRegistryPage.isAddressVerificationServiceErrorDisplayed()) {
            log.error("Address Verification Service error is not shown for Invalid Address");
            testPassed = false;
        }
        if (!createRegistryPage.getAddressVerificationServiceErrorMessage().equals(expectedAddressVerificationServiceErrorMessage)) {
            log.error("Address Verification Service error message is not same");
            testPassed = false;
        }
        if (!createRegistryPage.isUseAsEnteredCheckboxPresentAndDisplayed()) {
            log.error("Use as Entered is not displayed");
            testPassed = false;
        }
        assertTrue("Test step got failed, Please check the logs for that failed validation", testPassed);
    }
    
    /**
     * Verifies if the user is able to see the error messages when submits the form without filling
     * create registry form in create registry page.
     * Test Case ID - RGSN-38789
     */
    @Test
    @TestRail(id = "106713")
    public void testCreateRegistryPageWhenUserClicksContinueButtonWithoutFillingCreateRegistryFormExpectErrorMessage() {
        
        // given
        Assume.assumeTrue(!context.getTargetSite().supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY));
        final Registry registry = dataService.findRegistry("create-registry");
        MutableAccount mutableAccount = getMutableAccount("default");
        CustomerAccount newAccount = accountCreator.getRandomEmailAccount(mutableAccount);
        final String expectedRegistryNameErrorMessage = "Please enter the registry name.";
        final String expectedEventTypeErrorMessage = "Please select the event type.";
        final String expectedEventDateErrorMessage = "Please enter the event date.";
        final String expectedFirstNameErrorMessage = "Please enter a first name.";
        final String expectedLastNameErrorMessage = "Please enter a last name.";
        final String expectedAddressErrorMessage = "Please enter an address.";
        final String expectedCityErrorMessage = "Please enter a city.";
        final String expectedStateErrorMessage = "Please select the state.";
        final String expectedZipCodeErrorMessage = "Please enter a zip code.";
        final String expectedPhoneNumberErrorMessage = "Please enter a day phone number.";
        
        // when
        RegistryLandingPage registryLandingPage = context.getPage(RegistryLandingPage.class);
        context.getPilot().startAt(registryLandingPage);
        registryLandingPage.clickCreateRegistry();
        CreateRegistryPage createRegistryPage = RegistryFlows.createAccount(context, newAccount, CreateRegistryPage.class);
        
        // then
        if (context.isFullSiteExperience()) {
            createRegistryPage.clickCreateRegistryButton();
            boolean testPassed = true;
            if (!createRegistryPage.getRegistryNameErrorMessage().equals(expectedRegistryNameErrorMessage)) {
                log.error("Regsitry Name error text message is not same");
                testPassed = false;
            }
            if (!createRegistryPage.getEventTypeErrorMessage().equals(expectedEventTypeErrorMessage)) {
                log.error("Event Type error text message is not same");
                testPassed = false;
            }
            if (!createRegistryPage.getEventDateErrorMessage().equals(expectedEventDateErrorMessage)) {
                log.error("Event Date error text message is not same");
                testPassed = false;
            } 
            if (!createRegistryPage.getFirstNameErrorMessage().equals(expectedFirstNameErrorMessage)) {
                log.error("First Name error text message is not same");
                testPassed = false;
            }
            if (!createRegistryPage.getLastNameErrorMessage().equals(expectedLastNameErrorMessage)) {
                log.error("Last Name error text message is not same");
                testPassed = false;
            }
            if (!createRegistryPage.getAddressErrorMessage().equals(expectedAddressErrorMessage)) {
                log.error("Address error text message is not same");
                testPassed = false;
            }
            if (!createRegistryPage.getCityErrorMessage().equals(expectedCityErrorMessage)) {
                log.error("City error text message is not same");
                testPassed = false;
            }
            if (!createRegistryPage.getStateErrorMessage().equals(expectedStateErrorMessage)) {
                log.error("State error text message is not same");
                testPassed = false;
            }
            if (!createRegistryPage.getZipCodeErrorMessage().equals(expectedZipCodeErrorMessage)) {
                log.error("Zipcode error text message is not same");
                testPassed = false;
            }
            if (!createRegistryPage.getPhoneNumberErrorMessage().equals(expectedPhoneNumberErrorMessage)) {
                log.error("Phone Numbe rerror text message is not same");
                testPassed = false;
            }
            assertTrue("Test step got failed, Please check the logs for that failed validation", testPassed);
        } else {
            createRegistryPage.clickCreateRegistryButton();
            boolean testPassed = true;
            if (!createRegistryPage.getRegistryNameErrorMessage().equals(expectedRegistryNameErrorMessage)) {
                log.error("Regsitry Name error text message is not same");
                testPassed = false;
            }
            if (!createRegistryPage.getEventTypeErrorMessage().equals(expectedEventTypeErrorMessage)) {
                log.error("Event Type error text message is not same");
                testPassed = false;
            }
            if (!createRegistryPage.getEventDateErrorMessage().equals(expectedEventDateErrorMessage)) {
                log.error("Event Date error text message is not same");
                testPassed = false;
            }
            
            // when
            RegistryProfileFormSection registryProfileFormSection = context.getPageSection(RegistryProfileFormSection.class);
            registryProfileFormSection.populateBasicInfo(registry);
            createRegistryPage.clickCreateRegistryButton();
            createRegistryPage.clickCreateRegistryButton();
            
            // then
            if (!createRegistryPage.getFirstNameErrorMessage().equals(expectedFirstNameErrorMessage)) {
                log.error("First Name error text message is not same");
                testPassed = false;
            }
            if (!createRegistryPage.getLastNameErrorMessage().equals(expectedLastNameErrorMessage)) {
                log.error("Last Name error text message is not same");
                testPassed = false;
            }
            if (!createRegistryPage.getAddressErrorMessage().equals(expectedAddressErrorMessage)) {
                log.error("Address error text message is not same");
                testPassed = false;
            }
            if (!createRegistryPage.getCityErrorMessage().equals(expectedCityErrorMessage)) {
                log.error("City error text message is not same");
                testPassed = false;
            }
            if (!createRegistryPage.getStateErrorMessage().equals(expectedStateErrorMessage)) {
                log.error("State error text message is not same");
                testPassed = false;
            }
            if (!createRegistryPage.getZipCodeErrorMessage().equals(expectedZipCodeErrorMessage)) {
                log.error("Zipcode error text message is not same");
                testPassed = false;
            }
            if (!createRegistryPage.getPhoneNumberErrorMessage().equals(expectedPhoneNumberErrorMessage)) {
                log.error("Phone Numbe rerror text message is not same");
                testPassed = false;
            }
            assertTrue("Test step got failed, Please check the logs for that failed validation", testPassed);
        }
    }

    /**
     * Verify whether the user can create protected registry.
     * 
     * Test Case ID - RGSN-38261.
     */
    @Test
    @TestRail(id = "105256")
    public void testCreateRegistryWhenUserSelectProtectedForPrivacySettingsExpectProtectedRegisrtyCreated() {

        //given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY));
        final Registry registry = dataService.findRegistry("create-registry");
        MutableAccount mutableAccount = getMutableAccount("default");
        CustomerAccount newAccount = accountCreator.getRandomEmailAccount(mutableAccount);
        final Boolean defaultAddress = false;

        //when
        RegistryLandingPage registryLandingPage = context.getPage(RegistryLandingPage.class);
        context.getPilot().startAt(registryLandingPage);
        registryLandingPage.clickCreateRegistry();
        if (context.supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY)) {
            RegistryFlows.createAccount(context, newAccount, RegistryLandingPage.class);
        } else {
            RegistryFlows.createAccount(context, newAccount, CreateRegistryPage.class);
        }
        CrossBrandRegistryListPage registryListPage = RegistryFlows.createRegistryFlow(context, registry, newAccount, PrivacySettings.PROTECTED, defaultAddress);

        //then
        assertTrue("Protected regirsty should be Created", registryLandingPage.isRegistryFullnameDisplayed());
    }

    /**
     * Verify whether the user can see the validation message for invalid access code for protected registry.
     * 
     * Test Case ID - RGSN-38261.
     * 
     */
    @Test
    @TestRail(id = "105258")
    public void testProtectRegistryAccessWhenGGEnteredInvalidAccessCodeExpectValidationMessageDisplayed(){

        //given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY));
        final Registry findRegistry = dataService.findRegistry("protected-registry");
        String invalidGuestAccessCode = "test12";
        final String invalidAccessCodeValidationMessage = "The password you entered is incorrect. Please try again.";

        //when
        RegistryLandingPage registryLandingPage = context.getPage(RegistryLandingPage.class);
        context.getPilot().startAt(registryLandingPage);
        FindRegistryPage findRegistryPage = context.getPage(FindRegistryPage.class);
        startPilotAt(findRegistryPage);
        findRegistryPage.setFirtsName(findRegistry.getUnmappedAttribute("firstName"));
        findRegistryPage.setLastName(findRegistry.getUnmappedAttribute("lastName"));
        findRegistryPage.clickFindRegistryButton();
        findRegistryPage.clickProtectedRegistryName();
        RegistryProtectedOverlaySection registryProtectedOverlaySection = context.getPageSection(RegistryProtectedOverlaySection.class);

        //then
        assertTrue("Guest access code field is not displayed", registryProtectedOverlaySection.isGuestAccessCodeFieldDisplayed());

        //when
        registryProtectedOverlaySection.setGuestAccessCode(invalidGuestAccessCode);
        registryProtectedOverlaySection.clickContinueButton();
        String errorMessage = registryProtectedOverlaySection.getInputErrorMessage();

        //then
        assertTrue("Invalid access code Error message should be display", invalidAccessCodeValidationMessage.equals(errorMessage));
    }

    /**
     * Verify whether the user can see the protected registry with a valid access code.
     * 
     * Test Case ID - RGSN-38261.
     * 
     */
    @Test
    @TestRail(id = "105257")
    public void testProtectedRegistryAccessWhenGGEnteredValidAccessCodeExpectProtectedRegistryOpened() {

        //given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY));
        final Registry findRegistry = dataService.findRegistry("protected-registry");
        String validGuestAccessCode = findRegistry.getGuestAccessCode();
        String firstName = findRegistry.getUnmappedAttribute("firstName");
        String lastName = findRegistry.getUnmappedAttribute("lastName");
        String RegistryFullname = firstName + " " + lastName;

        //when
        RegistryLandingPage registryLandingPage = context.getPage(RegistryLandingPage.class);
        context.getPilot().startAt(registryLandingPage);
        FindRegistryPage findRegistryPage = context.getPage(FindRegistryPage.class);
        FindRegistryResultsPage findRegistryResultsPage = context.getPage(FindRegistryResultsPage.class);
        startPilotAt(findRegistryPage);
        findRegistryPage.setFirtsName(firstName);
        findRegistryPage.setLastName(lastName);
        findRegistryPage.clickFindRegistryButton();
        findRegistryResultsPage.clickProtectedRegistryName();
        RegistryProtectedOverlaySection registryProtectedOverlaySection = context.getPageSection(RegistryProtectedOverlaySection.class);
        registryProtectedOverlaySection.setGuestAccessCode(validGuestAccessCode);
        registryProtectedOverlaySection.clickContinueButton();

        //then
        assertTrue("The selected Protected Registry should be display", RegistryFullname.equals(registryLandingPage.getRegistrantFullname()));
    }
    
    /**
     * Verify whether the user can create private registry.
     * 
     * Test Case ID - RGSN-38262.
     * 
     */
    @Test
    @TestRail(id = "105259")
    public void testCreateRegistryWhenUserSelectPrivateForPrivacySettingsExpectPrivateRegisrtyCreated() {

        //given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY));
        final Registry registry = dataService.findRegistry("create-registry");
        MutableAccount mutableAccount = getMutableAccount("default");
        CustomerAccount newAccount = accountCreator.getRandomEmailAccount(mutableAccount);
        final Boolean defaultAddress = false;

        //when
        RegistryLandingPage registryLandingPage = context.getPage(RegistryLandingPage.class);
        context.getPilot().startAt(registryLandingPage);
        registryLandingPage.clickCreateRegistry();
        if (context.supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY)) {
            RegistryFlows.createAccount(context, newAccount, RegistryLandingPage.class);
        } else {
            RegistryLoginPage registryLoginPage = context.getPage(RegistryLoginPage.class);
            registryLoginPage.clickCreateRegistryButton(CreateRegistrySignInPage.class);
            RegistryFlows.createAccount(context, newAccount, CreateRegistryPage.class);
        }
        CrossBrandRegistryListPage registryListPage = RegistryFlows.createRegistryFlow(context, registry, newAccount, PrivacySettings.PRIVATE, defaultAddress);
        
        //then
        assertTrue("Protected regirsty should be created successfully", registryLandingPage.isRegistryFullnameDisplayed());
    }
    
    /**
     * Verify the user should not see the private registry in Search results.
     * 
     * Test Case ID - RGSN-38262.
     * 
     */
    @Test
    @TestRail(id = "105260")
    public void testPrivateRegistryAccessWhenGGSearchFromFindRegistryExpectSearchResultsIsEmpty() {

        //given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY));
        final Registry findRegistry = dataService.findRegistry("private-registry");
        String firstName = findRegistry.getUnmappedAttribute("firstName");
        String lastName = findRegistry.getUnmappedAttribute("lastName");

        //when
        RegistryLandingPage registryLandingPage = context.getPage(RegistryLandingPage.class);
        context.getPilot().startAt(registryLandingPage);
        FindRegistryPage findRegistryPage = context.getPage(FindRegistryPage.class);
        startPilotAt(findRegistryPage);
        findRegistryPage.setFirtsName(firstName);
        findRegistryPage.setLastName(lastName);
        findRegistryPage.clickFindRegistryButton();

        //then
        assertTrue("System should be display the validation message for Private Registry Search.", findRegistryPage.isValidationMessageDisplayed());
    }

    /**
     * Verify whether the event type dropdown should not have the Ring Ceremony, Retirement Celebration for PB brand.
     * Test Case ID - RGSN-38794
     * 
     */
    @Test
    @TestRail(id = "108321")
    public void testCreateRegistryWhenUserClickOnEventTypeExpectRingCeremonyAndRetirementCelebrationOptionsSholdNotDisplay() {

        //given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY));
        final Registry registry = dataService.findRegistry("create-registry");
        final CustomerAccount account = dataService.findAccount("single-user-registry");
        final String eventType1 = "Ring Ceremony";
        final String eventType2 = "Retirement Celebration";

        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signInWithAccount(context, account);
        RegistryLandingPage registryLandingPage = context.getPage(RegistryLandingPage.class);
        context.getPilot().startAt(registryLandingPage);
        registryLandingPage.clickCreateRegistry();
        CreateRegistryOverlaySection createRegistryOverlay = context.getPageSection(CreateRegistryOverlaySection.class);

        // then
        assertFalse(eventType1 + "should not display in the event type dropdown", createRegistryOverlay.getAllEventTypeOptions().contains(eventType1));
        assertFalse(eventType2 + "should not display in the event type dropdown", createRegistryOverlay.getAllEventTypeOptions().contains(eventType2));
    }
}
