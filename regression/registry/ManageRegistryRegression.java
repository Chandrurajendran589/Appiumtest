package com.wsgc.ecommerce.ui.regression.registry;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.account.AccountFlows;
import com.wsgc.ecommerce.ui.endtoend.registry.RegistryFlows;
import com.wsgc.ecommerce.ui.pagemodel.account.AccountHomePage;
import com.wsgc.ecommerce.ui.pagemodel.account.LoginPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.CrossBrandRegistryListPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.ManageListOfRegistriesPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.ManageRegistryListRegistryInfoSection;
import com.wsgc.ecommerce.ui.pagemodel.registry.ManageRegistryPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.RegistryLandingPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.EditRegistryOverlaySection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSectionFactory;
import com.wsgc.ecommerce.ui.pagemodel.i18n.InternationalShippingOverlay;
import com.wsgc.ecommerce.ui.pagemodel.registry.RegistryLandingPage;

import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.CustomerAccount;
import com.wsgc.evergreen.entity.api.MutableAccount;
import com.wsgc.evergreen.entity.api.Registry;
import com.wsgc.evergreen.entity.api.CustomerAccount;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tag.area.RegistryArea;

/**
 * Regression Tests for validating manage registry flows.
 */
@Category(RegistryArea.class)
public class ManageRegistryRegression  extends AbstractTest {
    
     /**
     * Prepares data fixtures & ensures feature flag is enabled.
     */
    @Before
    public void setUp() {
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.REGISTRY_V2));
    }
    
    /**
     * Verifies whether a signed in user is able to navigate to a registry list page via manage registry.
     * Test Case ID - RGSN-38254
     */
    @Test
    @TestRail(id = "104609")
    public void testManageRegistryWhenSignedInUserClicksViewRegistryExpectCrossBrandRegistryListPage() {

        //given
        final Registry registry = dataService.findRegistry("multiple-registry", "simple-pip");
        final String email = registry.getRegistrant().getEmail();
        final String password = registry.getRegistrant().getPassword();
        final String expectedRegistryExternalId = registry.getExternalId();

        //when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        ManageListOfRegistriesPage manageListOfRegistriesPage = context.getPage(ManageListOfRegistriesPage.class);
        startPilotAt(manageListOfRegistriesPage);
        ManageRegistryListRegistryInfoSection manageRegistryListRegistryInfoSection = manageListOfRegistriesPage.getRegistryInfoSectionByExternalId(expectedRegistryExternalId);
        CrossBrandRegistryListPage crossBrandRegistryListPage;
        if (context.isFullSiteExperience()) {
            ManageRegistryPage manageRegistryPage = manageRegistryListRegistryInfoSection.clickManageRegistryLink(ManageRegistryPage.class);
            crossBrandRegistryListPage = manageRegistryPage.clickViewRegistryListButton();
        } else {
            crossBrandRegistryListPage = manageRegistryListRegistryInfoSection.clickManageRegistryLink(CrossBrandRegistryListPage.class);
        }

        String actualRegistryExternalId = crossBrandRegistryListPage.getRegistryListExternalId();
        
        //then
        assertTrue(String.format("Expected external ID is not matching with the external ID of the actual registry list page, "
        		+ "expected : %s and actual : %s", expectedRegistryExternalId, actualRegistryExternalId), 
        		actualRegistryExternalId.equals(expectedRegistryExternalId)); 
    }

    /**
     * Verifies whether a user is able to update registrant details in registry from edit flyout overlay.
     * Test Case ID - RGSN-38258
     */
    @Test
    @TestRail(id = "108223")
    public void testUpdateRegistryWhenUserEditsRegistrantDetailsOnEditRegistryFlyoutExpectRegistrantDetailsUpdatedSuccessfully() {

        // given
        final Registry registry = dataService.findRegistry("create-registry");
        final Registry editRegistry = dataService.findRegistry("edit-registry");
        MutableAccount mutableAccount = getMutableAccount("default");
        CustomerAccount account = createAccount(mutableAccount);
        boolean useDefaultAddress = true;

        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signInWithAccount(context, account);
        RegistryLandingPage registryLandingPage = context.getPage(RegistryLandingPage.class);
        context.getPilot().startAt(registryLandingPage);
        registryLandingPage.clickCreateRegistry();
        CrossBrandRegistryListPage crossBrandRegistryListPage;
        if (context.supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY)) {
            crossBrandRegistryListPage = RegistryFlows.createRegistryFlow(context, registry, account, RegistryFlows.PrivacySettings.PUBLIC, useDefaultAddress);
        } else {
            crossBrandRegistryListPage = RegistryFlows.createRegistryFlowFromRegistryLoginPage(context, registry);
        }
        EditRegistryOverlaySection editRegistryOverlaySection = crossBrandRegistryListPage.clickEditRegistry();

        // then
        assertEquals(String.format("The registrant name is not matched on the edit registry flyout, expected : %s , actual : %s",
                        registry.getRegistrant().getFirstName() + " " + registry.getRegistrant().getLastName(), editRegistryOverlaySection.getRegistrant()),
                registry.getRegistrant().getFirstName() + " " + registry.getRegistrant().getLastName(), editRegistryOverlaySection.getRegistrant());
        assertTrue(String.format("The event type is not matched on the edit registry flyout, expected : %s , actual : %s",
                        registry.getEventType(), editRegistryOverlaySection.getEventType()),
                registry.getEventType().equalsIgnoreCase(editRegistryOverlaySection.getEventType()));

        assertEquals(String.format("The registrant name is not matched on the edit registry flyout, expected : %s , actual : %s",
                        registry.getUnmappedAttribute("eventDate"), editRegistryOverlaySection.getEventDate()),
                registry.getUnmappedAttribute("eventDate"), editRegistryOverlaySection.getEventDate());

        // when
        RegistryFlows.editRegistrantDetailsOnEditRegistry(context, editRegistry.getRegistrant().getFirstName(), editRegistry.getRegistrant().getLastName(),
                editRegistry.getEventType(), editRegistry.getEventMonth(),  editRegistry.getEventDay(), editRegistry.getEventYear());

        // then
        assertEquals(String.format("The registrant name is not matched on the edit registry flyout after the update, expected : %s , actual : %s",
                        editRegistry.getRegistrant().getFirstName() + " " + editRegistry.getRegistrant().getLastName(),
                        editRegistryOverlaySection.getRegistrant()),
                editRegistry.getRegistrant().getFirstName() + " " + editRegistry.getRegistrant().getLastName(),
                editRegistryOverlaySection.getRegistrant());
        assertTrue(String.format("The event type is not matched on the edit registry flyout after the update, expected : %s , actual : %s",
                        editRegistry.getEventType(), editRegistryOverlaySection.getEventType()),
                editRegistry.getEventType().equalsIgnoreCase(editRegistryOverlaySection.getEventType()));
        assertEquals(String.format("The registrant name is not matched on the edit registry flyout after the update, expected : %s , actual : %s",
                        editRegistry.getUnmappedAttribute("eventDate"), editRegistryOverlaySection.getEventDate()),
                editRegistry.getUnmappedAttribute("eventDate"), editRegistryOverlaySection.getEventDate());
    }

    /**
     * Verifies whether a user is able to update Shipping details in registry from edit flyout overlay.
     * Test Case ID - RGSN-38258
     */
    @Test
    @TestRail(id = "108230")
    public void testUpdateRegistryWhenUserEditsShippingDetailsOnEditRegistryFlyoutExpectShippingDetailsUpdatedSuccessfully() {

        // given
        final Registry registry = dataService.findRegistry("create-registry");
        final Registry editRegistry = dataService.findRegistry("edit-registry");
        MutableAccount mutableAccount = getMutableAccount("default");
        CustomerAccount account = createAccount(mutableAccount);
        boolean useDefaultAddress = false;

        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signInWithAccount(context, account);
        RegistryLandingPage registryLandingPage = context.getPage(RegistryLandingPage.class);
        context.getPilot().startAt(registryLandingPage);
        registryLandingPage.clickCreateRegistry();
        CrossBrandRegistryListPage crossBrandRegistryListPage;
        if (context.supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY)) {
            crossBrandRegistryListPage = RegistryFlows.createRegistryFlow(context, registry, account, RegistryFlows.PrivacySettings.PUBLIC, useDefaultAddress);
        } else {
            crossBrandRegistryListPage = RegistryFlows.createRegistry(context, registry, account);
        }
        EditRegistryOverlaySection editRegistryOverlaySection = crossBrandRegistryListPage.clickEditRegistry();

        // then
        assertEquals(String.format("The registrant name is not matched on the edit registry flyout, expected : %s , actual : %s",
                        registry.getRegistrant().getFirstName() + " " + registry.getRegistrant().getLastName(),
                        editRegistryOverlaySection.getRegistrantNameFromShippingSection()),
                registry.getRegistrant().getFirstName() + " " + registry.getRegistrant().getLastName(),
                editRegistryOverlaySection.getRegistrantNameFromShippingSection());

        assertTrue(String.format("The address line 1 is not matched on the edit registry flyout, expected : %s , actual : %s",
                        registry.getRegistrant().getAddress().getAddress1(), editRegistryOverlaySection.getAddressLine1()),
                registry.getRegistrant().getAddress().getAddress1().equalsIgnoreCase(editRegistryOverlaySection.getAddressLine1()));

        assertTrue(String.format("The city is not matched on the edit registry flyout, expected : %s , actual : %s",
                        registry.getRegistrant().getAddress().getCity(), editRegistryOverlaySection.getCity().split(",")[0]),
                registry.getRegistrant().getAddress().getCity().equalsIgnoreCase(editRegistryOverlaySection.getCity().split(",")[0]));

        assertTrue(String.format("The State code is not matched on the edit registry flyout, expected : %s , actual : %s",
                        registry.getRegistrant().getAddress().getState(), editRegistryOverlaySection.getState().split(",")[1].trim()),
                registry.getRegistrant().getAddress().getState().equalsIgnoreCase(editRegistryOverlaySection.getState().split(",")[1].trim()));

        assertTrue(String.format("The Postal code is not matched on the edit registry flyout, expected : %s , actual : %s",
                        registry.getRegistrant().getAddress().getZip(), editRegistryOverlaySection.getPostalCode().split(",")[2].trim()),
                registry.getRegistrant().getAddress().getZip().equalsIgnoreCase(editRegistryOverlaySection.getPostalCode().split(",")[2].trim()));

        assertTrue(String.format("The Phone number is not matched on the edit registry flyout, expected : %s , actual : %s",
                        registry.getRegistrant().getDaytimePhone(), editRegistryOverlaySection.getPhoneNumber().split(":")[1].trim()),
                registry.getRegistrant().getDaytimePhone().equalsIgnoreCase(editRegistryOverlaySection.getPhoneNumber().split(":")[1].trim()));

        // when
        editRegistryOverlaySection.clickEditYourShippingDetailsSection();
        RegistryFlows.editRegistryShippingDetails(context, editRegistry);

        // then
        assertEquals(String.format("The registrant name is not matched on the edit registry flyout after the update, expected : %s , actual : %s",
                        editRegistry.getRegistrant().getFirstName() + " " + editRegistry.getRegistrant().getLastName(),
                        editRegistryOverlaySection.getRegistrantNameFromShippingSection()),
                editRegistry.getRegistrant().getFirstName() + " " + editRegistry.getRegistrant().getLastName(),
                editRegistryOverlaySection.getRegistrantNameFromShippingSection());

        assertTrue(String.format("The address line 1 is not matched on the edit registry flyout after the update, expected : %s , actual : %s",
                        editRegistry.getRegistrant().getAddress().getAddress1(), editRegistryOverlaySection.getAddressLine1()),
                editRegistry.getRegistrant().getAddress().getAddress1().equalsIgnoreCase(editRegistryOverlaySection.getAddressLine1()));

        assertTrue(String.format("The city is not matched on the edit registry flyout after the update, expected : %s , actual : %s",
                        editRegistry.getRegistrant().getAddress().getCity(), editRegistryOverlaySection.getCity().split(",")[0]),
                editRegistry.getRegistrant().getAddress().getCity().equalsIgnoreCase(editRegistryOverlaySection.getCity().split(",")[0]));

        assertTrue(String.format("The State code is not matched on the edit registry flyout after the update, expected : %s , actual : %s",
                        editRegistry.getRegistrant().getAddress().getState(), editRegistryOverlaySection.getState().split(",")[1].trim()),
                editRegistry.getRegistrant().getAddress().getState().equalsIgnoreCase(editRegistryOverlaySection.getState().split(",")[1].trim()));

        assertTrue(String.format("The Postal code is not matched on the edit registry flyout after the update, expected : %s , actual : %s",
                        editRegistry.getRegistrant().getAddress().getZip(), editRegistryOverlaySection.getPostalCode().split(",")[2].trim()),
                editRegistry.getRegistrant().getAddress().getZip().equalsIgnoreCase(editRegistryOverlaySection.getPostalCode().split(",")[2].trim()));

        assertTrue(String.format("The Phone number is not matched on the edit registry flyout after the update, expected : %s , actual : %s",
                        editRegistry.getRegistrant().getDaytimePhone(), editRegistryOverlaySection.getPhoneNumber().split(":")[1].trim()),
                editRegistry.getRegistrant().getDaytimePhone().equalsIgnoreCase(editRegistryOverlaySection.getPhoneNumber().split(":")[1].trim()));
    }

    /**
     * Verifies whether a user is able to update registry visibility from public to private along with other fields in
     * optional Settings from edit flyout overlay.
     * Test Case ID - RGSN-38862
     */
    @Test
    @TestRail(id = "108301")
    public void testUpdateRegistryWhenUserEditsOptionalSettingsToPrivateOnEditRegistryFlyoutExpectOptionalSettingsUpdatedSuccessfully() {

        // given
        final Registry registry = dataService.findRegistry("create-registry");
        final Registry editRegistry = dataService.findRegistry("edit-registry");
        MutableAccount mutableAccount = getMutableAccount("default");
        CustomerAccount account = createAccount(mutableAccount);
        boolean useDefaultAddress = false;
        final String privacy = "Private";
        final String registryName = "MyRegistry";
        final String guestMessage = "Welcome to my registry";
        final String giftCardAcceptance = "Accepting";

        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signInWithAccount(context, account);
        RegistryLandingPage registryLandingPage = context.getPage(RegistryLandingPage.class);
        context.getPilot().startAt(registryLandingPage);
        registryLandingPage.clickCreateRegistry();
        CrossBrandRegistryListPage crossBrandRegistryListPage;
        if (context.supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY)) {
            crossBrandRegistryListPage = RegistryFlows.createRegistryFlow(context, registry, account, RegistryFlows.PrivacySettings.PUBLIC, useDefaultAddress);
        } else {
            crossBrandRegistryListPage = RegistryFlows.createRegistry(context, registry, account);
        }
        EditRegistryOverlaySection editRegistryOverlaySection = crossBrandRegistryListPage.clickEditRegistry();
        editRegistryOverlaySection.clickEditOptionalSettingsSection();
        RegistryFlows.editRegistryOptionalSettings(context, privacy, editRegistry, registryName, guestMessage);

        // then
        assertEquals(String.format("The registry visibility is not matched on the edit registry flyout after the update, expected : %s , actual : %s", privacy,
                        editRegistryOverlaySection.getRegistryVisibility()), privacy, editRegistryOverlaySection.getRegistryVisibility());

        assertEquals(String.format("The co-registrant name is not matched on the edit registry flyout after the update, expected : %s , actual : %s",
                        editRegistry.getCoRegistrant().getFirstName() + " " + editRegistry.getCoRegistrant().getLastName(),
                        editRegistryOverlaySection.getCoRegistrant()),
                editRegistry.getCoRegistrant().getFirstName() + " " + editRegistry.getCoRegistrant().getLastName(),
                editRegistryOverlaySection.getCoRegistrant());

        assertEquals(String.format("The co-registrant email is not matched on the edit registry flyout after the update, expected : %s , actual : %s",
                        editRegistry.getCoRegistrant().getEmail(), editRegistryOverlaySection.getCoRegistrantEmail().replace("(", "").replace(")", "")),
                editRegistry.getCoRegistrant().getEmail(), editRegistryOverlaySection.getCoRegistrantEmail().replace("(", "").replace(")", ""));

        assertEquals(String.format("The registry name is not matched on the edit registry flyout after the update, expected : %s , actual : %s",
                        registryName, editRegistryOverlaySection.getRegistryName()),
                registryName, editRegistryOverlaySection.getRegistryName());

        assertEquals(String.format("The guest message is not matched on the edit registry flyout after the update, expected : %s , actual : %s",
                        guestMessage, editRegistryOverlaySection.getGuestMessage()),
                guestMessage, editRegistryOverlaySection.getGuestMessage());

        assertEquals(String.format("The credit card acceptance is not matched on the edit registry flyout after the update, expected : %s , actual : %s",
                giftCardAcceptance, editRegistryOverlaySection.getGiftCardAcceptance()),
                giftCardAcceptance, editRegistryOverlaySection.getGiftCardAcceptance());

    }

    /**
     * Verifies whether a user is able to update registry visibility from public to protected in Optional Settings from edit flyout overlay.
     * Test Case ID - RGSN-38862
     */
    @Test
    @TestRail(id = "108302")
    public void testUpdateRegistryWhenUserEditsOptionalSettingsToProtectedOnEditRegistryFlyoutExpectOptionalSettingsUpdatedSuccessfully() {

        // given
        final Registry registry = dataService.findRegistry("create-registry");
        MutableAccount mutableAccount = getMutableAccount("default");
        CustomerAccount account = createAccount(mutableAccount);
        boolean useDefaultAddress = false;
        final String privacy = "PasswordProtected";
        final String accessCode = "MyAccessCode";

        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signInWithAccount(context, account);
        RegistryLandingPage registryLandingPage = context.getPage(RegistryLandingPage.class);
        context.getPilot().startAt(registryLandingPage);
        registryLandingPage.clickCreateRegistry();
        CrossBrandRegistryListPage crossBrandRegistryListPage;
        if (context.supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY)) {
            crossBrandRegistryListPage = RegistryFlows.createRegistryFlow(context, registry, account, RegistryFlows.PrivacySettings.PUBLIC, useDefaultAddress);
        } else {
            crossBrandRegistryListPage = RegistryFlows.createRegistry(context, registry, account);
        }
        EditRegistryOverlaySection editRegistryOverlaySection = crossBrandRegistryListPage.clickEditRegistry();
        editRegistryOverlaySection.clickEditOptionalSettingsSection();
        editRegistryOverlaySection.editRegistryOptionalSettingsToProtected(privacy, accessCode);

        // then
        assertEquals(String.format("The registry visibility is not matched on the edit registry flyout after the update, expected : %s , actual : %s",
                "Protected", editRegistryOverlaySection.getRegistryVisibility()), "Protected",
                editRegistryOverlaySection.getRegistryVisibility().split(" | ")[0].trim());
    }

    /**
     * Verifies whether a user is able to update registry visibility from private to public in Optional Settings from edit flyout overlay.
     * Test Case ID - RGSN-38862
     */
    @Test
    @TestRail(id = "108303")
    public void testUpdateRegistryWhenUserEditsOptionalSettingsToPublicOnEditRegistryFlyoutExpectOptionalSettingsUpdatedSuccessfully() {

        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY));
        final Registry registry = dataService.findRegistry("create-registry");
        MutableAccount mutableAccount = getMutableAccount("default");
        CustomerAccount account = createAccount(mutableAccount);
        boolean useDefaultAddress = false;
        final String privacy = "Public";

        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signInWithAccount(context, account);
        RegistryLandingPage registryLandingPage = context.getPage(RegistryLandingPage.class);
        context.getPilot().startAt(registryLandingPage);
        registryLandingPage.clickCreateRegistry();
        CrossBrandRegistryListPage crossBrandRegistryListPage = RegistryFlows.createRegistryFlow(context, registry, account,
                RegistryFlows.PrivacySettings.PRIVATE, useDefaultAddress);
        EditRegistryOverlaySection editRegistryOverlaySection = crossBrandRegistryListPage.clickEditRegistry();
        editRegistryOverlaySection.clickEditOptionalSettingsSection();
        editRegistryOverlaySection.editRegistryOptionalSettingsToPublic(privacy);

        // then
        assertEquals(String.format("The registry visibility is not matched on the edit registry flyout after the update, expected : %s , actual : %s",
                        privacy, editRegistryOverlaySection.getRegistryVisibility()), privacy,
                editRegistryOverlaySection.getRegistryVisibility().split(" | ")[0].trim());
    }
    
    /**
     * Verify whether the international users are allowed to access registry functionalities.
     * Test case ID - RGSN-38795
     *
     */
    @Test
    @TestRail(id = "108300")
    public void testRegistryPageAccessWhenInternationalUserSigninExpectErrorMessageDisplay() {

        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY));
        final CustomerAccount account = dataService.findAccount("multiple-registry");
        final String email = account.getEmail();
        final String password = account.getPassword();

        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        navigationSection.goToInternationalShippingOverlay();
        InternationalShippingOverlay internationalShippingOverlay = context.getPageSection(InternationalShippingOverlay.class);
        int countryOptionsAvailable = internationalShippingOverlay.getNonUsDollarInternationalShippingCountries();
        int countryNameIndex = RandomUtils.nextInt(1, countryOptionsAvailable);
        internationalShippingOverlay.selectNonUsDollarCurrencyInternationalCountry(countryNameIndex);
        internationalShippingOverlay.clickUpdateCurrencyAndCountryButton();

        RegistryLandingPage registryLandingPage = context.getPage(RegistryLandingPage.class);
        navigationSection.goToRegistryLandingPage();
        registryLandingPage.clickManageMyRegistryButton();

        // then
        ManageListOfRegistriesPage manageListOfRegistriesPage = context.getPage(ManageListOfRegistriesPage.class);
        assertTrue("Registry International shipping message should display", manageListOfRegistriesPage.registryInternationalShippingMessageDisplayed());
    }
}
