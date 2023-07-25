package com.wsgc.ecommerce.ui.regression.registry;

import static org.junit.Assert.assertEquals;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.account.AccountFlows;
import com.wsgc.ecommerce.ui.endtoend.registry.RegistryFlows;
import com.wsgc.ecommerce.ui.endtoend.registry.RegistryFlows.PrivacySettings;
import com.wsgc.ecommerce.ui.endtoend.shop.PipFlows;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.SmartLoginSection;
import com.wsgc.ecommerce.ui.pagemodel.account.AccountHomePage;
import com.wsgc.ecommerce.ui.pagemodel.account.LoginPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.CreateRegistryOverlaySection;
import com.wsgc.ecommerce.ui.pagemodel.registry.CreateRegistrySignInPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.CrossBrandRegistryListPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.RegistryLoginPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.registrylist.RegistryListItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.RegistrySelectionOverlaySection;
import com.wsgc.ecommerce.ui.pagemodel.shop.RichAddToRegistryOverlaySection;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.CustomerAccount;
import com.wsgc.evergreen.entity.api.MutableAccount;
import com.wsgc.evergreen.entity.api.ProductGroup;
import com.wsgc.evergreen.entity.api.Registry;
import com.wsgc.evergreen.impl.DefaultEvergreenContext;


/**
 * Regression Tests for validating add to registry as guest user.
 */
@Category(Registry.class)
public class AddToRegistryGuestUserRegression extends AbstractTest{
	
    /**
     * Prepares data fixtures & ensures feature flag is enabled.
     */
    @Before
    public void setUp() {
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.REGISTRY_V2));
    }

    /**
     * Verifies whether guest user is able to sign in and create new registry 
     * while adding an item to registry from product page.
     * Test Case ID - RGSN-38257
     */
    @Test
    @TestRail(id = "102570")
    public void testCreateRegistryWhenGuestUserAddingAnItemToRegistryAndSignInExpectRegistryCreatedWithProduct() {

        //given
        final Registry registry = dataService.findRegistry("create-registry");
        final ProductGroup productGroup = dataService.findProductGroup("simple-pip", "!attributes");
        MutableAccount mutableAccount = getMutableAccount("default");
        CustomerAccount account = createAccount(mutableAccount);
        final String expectedSku = productGroup.getSkus().get(0);
        final boolean useDefaultAddress = true ;

        //when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        productPage.clickAddToRegistry();
        RegistryFlows.registrySignInFlow(context, account);
        if (context.supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY)) {
            context.getPageSection(CreateRegistryOverlaySection.class).clickCreateRegistryButton();
        }
        CrossBrandRegistryListPage registryListPage = RegistryFlows.
        		createRegistryFlow(context, registry, account, PrivacySettings.PUBLIC , useDefaultAddress);

        RegistryListItemRepeatableSection registryListItemRepeatableSection = registryListPage.getItem(expectedSku);      
        String actualSku = registryListItemRepeatableSection.getItemSku();

        //then
        assertEquals("The item "+ expectedSku + " is not added to registry " , expectedSku , actualSku);
    }
    
    /**
     * Verifies whether guest user is able to see the welcome back message when user
     * sign in with the existing account to create new account from registry login page.
     * Test Case ID - RGSN-38256
     */
    @Test
    @TestRail(id = "102646")
    public void testRegistryLoginPageWhenGuestUserSignInWithExistingUserToCreateNewAccountExpectWelcomeBackMessageDisplayed() {

        //given
        final ProductGroup productGroup = dataService.findProductGroup("simple-pip", "!attributes");
        final CustomerAccount customerAccount = dataService.findAccount("single-user-registry");
        String actualMessage;
        
        //when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        productPage.clickAddToRegistry();
        if (context.supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY)) {
            SmartLoginSection smartLoginSection = context.getPageSection(SmartLoginSection.class);
            smartLoginSection.setEmailAddressAccountLookup(customerAccount.getEmail());
            smartLoginSection.clickSubmitButton();
            smartLoginSection.isKeepMeSignedInChecked();

            //then 
            String expectedMessage = "Welcome back!";
            actualMessage = smartLoginSection.getWelcomeBackMessage();
            assertEquals("Welcome Back message is not displayed", expectedMessage, actualMessage);
        } else {

            //when
            CreateRegistrySignInPage createRegistrySignInPage = context.getPage(CreateRegistrySignInPage.class);
            if (context.isFullSiteExperience()) {
                RegistryLoginPage registryLoginPage = context.getPage(RegistryLoginPage.class);
                registryLoginPage.clickCreateRegistryButton(CreateRegistrySignInPage.class);
            }
            createRegistrySignInPage.setFullName(customerAccount);
            createRegistrySignInPage.setEmailAddress(customerAccount);
            if (context.isFullSiteExperience()) {
                createRegistrySignInPage.setConfirmEmailAddress(customerAccount);
                createRegistrySignInPage.setConfirmPassword(customerAccount);
            }
            createRegistrySignInPage.setPassword(customerAccount);
            createRegistrySignInPage.clickCreateAccount();

            //then
            String expectedErrorMessage = "You already have an account with us.";
            actualMessage = createRegistrySignInPage.getErrorMessage();
            assertEquals("You alread have an account with us error message is not shown", expectedErrorMessage, actualMessage);
        }
    }

    /**
     * Verifies if a guest user is redirected to the login page after clicking
     * add to registry from a product page. The item should be added to registry
     * once the user signs in successfully.
     * Test Case ID - RGSN-38269
     */
    @Test
    @TestRail(id = "104369")
    public void testAddToRegistryWhenGuestUserAddsItemToRegistryExpectItemAddedToRegistry() {

        // given
        final Registry registry = dataService.findRegistry("single-registry", "simple-pip");
        CustomerAccount customerAccount = dataService.findAccount("add-item-to-registry", "simple-pip-registry");
        final ProductGroup productGroup = dataService.findProductGroup("simple-pip", "!attributes");
        final String itemSku = productGroup.getSkus().get(0);
        final String email = registry.getRegistrant().getEmail();
        final String password = registry.getRegistrant().getPassword();
        int expectedStillNeedQuantity = 1;
        CrossBrandRegistryListPage registryListPage;

        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);

        // then
        RegistryFlows.deleteProductsFromRegistryListFlow(registryListPage);
        context.getPilot().getDriver().quit();

        // given
        context = DefaultEvergreenContext.getContext();
        DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        productPage.clickAddToRegistry();
        RegistryFlows.registrySignIn(context, customerAccount);
        if (context.supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY)) {
            CreateRegistryOverlaySection createRegistryOverlaySection = context.getPageSection(CreateRegistryOverlaySection.class);
            registryListPage = createRegistryOverlaySection.clickViewRegistry();
        } else {
            RichAddToRegistryOverlaySection richAddToRegistryOverlaySection = context.getPageSection(RichAddToRegistryOverlaySection.class);
            registryListPage = richAddToRegistryOverlaySection.viewRegistry();
        }

        RegistryListItemRepeatableSection registryItemRepeatableSection = registryListPage.getItem(itemSku);
        String actualStillNeedQuantity = registryItemRepeatableSection.getStillNeedsQuantity();

        //then
        assertEquals("Expected still need quantity \'" + String.valueOf(expectedStillNeedQuantity)
                        + "\' does not match with actual still need quantity \'" + actualStillNeedQuantity,
                String.valueOf(expectedStillNeedQuantity), actualStillNeedQuantity);
    }

    /**
     * Verifies if a guest user is redirected to the login page after clicking
     * add to registry from a product page. Once user signs in successfully.
     * User should see multiple registries list and should be able to add item to one of them.
     * Test Case ID - RGSN-38268, RGSN-38222
     */
    @Test
    @TestRail(id = "104366")
    public void testAddToRegistryWhenGuestUserAddsItemToMultipleRegistriesExpectItemAddedToTheRegistry() {

        // given
        final Registry registry = dataService.findRegistry("multiple-registry", "simple-pip");
        CustomerAccount customerAccount = dataService.findAccount("multiple-registry", "simple-pip-registry");
        final ProductGroup productGroup = dataService.findProductGroup("simple-pip", "!attributes");
        final String itemSku = productGroup.getSkus().get(0);
        final String email = registry.getRegistrant().getEmail();
        final String password = registry.getRegistrant().getPassword();
        String registryIndex = registry.getUnmappedAttribute("registryIndex");
        String registryId = registry.getExternalId();

        int expectedStillNeedQuantity = 1;
        CrossBrandRegistryListPage registryListPage;

        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);

        // then
        RegistryFlows.deleteProductsFromRegistryListFlow(registryListPage);
        context.getPilot().getDriver().quit();

        // given
        context = DefaultEvergreenContext.getContext();
        DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        RegistrySelectionOverlaySection registrySelectionOverlaySection = productPage.addToRegistryMultipleRegistriesPresent();
        RegistryFlows.registrySignIn(context, customerAccount);

        if (context.supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY)) {
            CreateRegistryOverlaySection createRegistryOverlaySection = context.getPageSection(CreateRegistryOverlaySection.class);
            createRegistryOverlaySection.selectRegistryFromMultipleRegistryList(registryIndex);
            createRegistryOverlaySection.clickAddToRegistryButton();
            registryListPage = createRegistryOverlaySection.clickViewRegistry();
        } else {
            RichAddToRegistryOverlaySection richAddToRegistryOverlaySection = registrySelectionOverlaySection.selectRegistryAndClickContinue(registryId);
            richAddToRegistryOverlaySection.viewRegistry();
        }
        RegistryListItemRepeatableSection registryItemRepeatableSection = registryListPage.getItem(itemSku);
        String actualStillNeedQuantity = registryItemRepeatableSection.getStillNeedsQuantityText();

        //then
        assertEquals("Expected still need quantity \'" + String.valueOf(expectedStillNeedQuantity)
                        + "\' does not match with actual still need quantity \'" + actualStillNeedQuantity,
                String.valueOf(expectedStillNeedQuantity), actualStillNeedQuantity);
    }

    /**
     * Verifies if a guest user is redirected to the login page after clicking
     * add to registry from a monogrammed product page. Once user signs in successfully.
     * User should  be able to add item to the registry.
     * Test Case ID - RGSN-38027, RGSN-38730
     */
    @Test
    @TestRail(id = "104373")
    public void testAddToRegistryWhenGuestUserAddsMonogramItemToSingleRegistryExpectItemAddedToRegistry() {

        // given
        int inlineMonoStyleIndex = 0;
        final int colorIndex = 0;
        final int expectedQuantity = 1;
        String monogramText = "ABC";

        final Registry registry = dataService.findRegistry("single-registry", "simple-pip");
        CustomerAccount customerAccount = dataService.findAccount("add-item-to-registry", "simple-pip-registry");
        final ProductGroup monogrammableProduct = dataService.findProductGroup("simple-pip", "attributes", "monogrammable");
        final String itemSku = monogrammableProduct.getSkus().get(0);
        final String email = registry.getRegistrant().getEmail();
        final String password = registry.getRegistrant().getPassword();
        int expectedStillNeedQuantity = 1;
        CrossBrandRegistryListPage registryListPage;

        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);
        RegistryFlows.deleteProductsFromRegistryListFlow(registryListPage);
        context.getPilot().getDriver().quit();
        context = DefaultEvergreenContext.getContext();
        DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, monogrammableProduct);
        PipFlows.setSimplePipAttributeSelections(productPage);
        PipFlows.setMonogramDetails(productPage, inlineMonoStyleIndex, monogramText, colorIndex, expectedQuantity);
        productPage.clickAddToRegistry();
        RegistryFlows.registrySignIn(context, customerAccount);
        if (context.supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY)) {
            CreateRegistryOverlaySection createRegistryOverlaySection = context.getPageSection(CreateRegistryOverlaySection.class);
            registryListPage = createRegistryOverlaySection.clickViewRegistry();
        } else {
            RichAddToRegistryOverlaySection richAddToRegistryOverlaySection = context.getPageSection(RichAddToRegistryOverlaySection.class);
            registryListPage = richAddToRegistryOverlaySection.viewRegistry();
        }
        RegistryListItemRepeatableSection registryItemRepeatableSection = registryListPage.getItem(itemSku);
        String actualStillNeedQuantity = registryItemRepeatableSection.getStillNeedsQuantity();
        String actualPersonalizationLabelOnRegistryListPage = registryItemRepeatableSection.getPersonalizationText();

        //then
        assertEquals("Expected still need quantity \'" + String.valueOf(expectedStillNeedQuantity)
                        + "\' does not match with actual still need quantity \'" + actualStillNeedQuantity,
                String.valueOf(expectedStillNeedQuantity), actualStillNeedQuantity);
        assertEquals("Personalization text \'" + monogramText
                        + "\' does not match with actual Monogram texts on the registry list page \'" + actualPersonalizationLabelOnRegistryListPage,
                "Personalization: " + monogramText, actualPersonalizationLabelOnRegistryListPage);
    }
}
