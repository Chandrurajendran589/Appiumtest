package com.wsgc.ecommerce.ui.regression.registry;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.Cookie;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.account.AccountFlows;
import com.wsgc.ecommerce.ui.endtoend.registry.RegistryFlows;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSectionFactory;
import com.wsgc.ecommerce.ui.pagemodel.account.AccountHomePage;
import com.wsgc.ecommerce.ui.pagemodel.account.LoginPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.CreateRegistryOverlaySection;
import com.wsgc.ecommerce.ui.pagemodel.registry.ManageListOfRegistriesPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.find.FindRegistryResultsPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.CustomerAccount;
import com.wsgc.evergreen.entity.api.ProductGroup;
import com.wsgc.evergreen.entity.api.Registry;
import com.wsgc.evergreen.entity.impl.FindRegistryRequest;
import com.wsgc.evergreen.impl.DefaultEvergreenContext;

import tag.area.RegistryArea;

@Category(RegistryArea.class)
public class RegistryViaSecondaryNavigationRegression extends AbstractTest {
    
    /**
     * Prepares data fixtures & ensures feature flag is enabled.
     */
    @Before
    public void setUp() {
        Assume.assumeTrue(context.supportsFeature(FeatureFlag.REGISTRY_SECONDARY_NAVIGATION)
                && context.isFullSiteExperience());
    }
    
    /**
     * Verifies whether a signed in user is able to find registry by hovering
     * registry from secondary navigation.
     * Test Case ID - RGSN-38277
     */
    @Test
    @TestRail(id = "104640")
    public void testFindRegistryWhenSignedInUserClicksFindsRegistryFromSecondaryNavigationExpectFindRegistryPage() {

        // given
        FindRegistryRequest findRegistry = dataService.getFindRegistryRequest("find-registry-common");
        final CustomerAccount account = dataService.findAccount("single-user-registry");
        final ProductGroup productGroup = dataService.findProductGroup("simple-pip", "!attributes");

        // when
        startPilotAt(context.getPage(LoginPage.class));
        AccountFlows.signInWithAccount(context, account);
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, productGroup.getUnmappedAttribute("categoryUrl"));
        categoryPage.waitForCategoryPageLoad();
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        navigationSection.hoverRegistryLink();
        FindRegistryResultsPage findRegistryResultsPage = RegistryFlows.navigateToFindRegistryFromSecondayNavigation(context, 
                 findRegistry.getFirstName(), findRegistry.getLastName());
        
        //then
        assertTrue("The Find Registry Result Page is empty", findRegistryResultsPage.isRegistryResultsDisplayed());
    }
    
    /**
     * Verifies whether a guest user is able to navigate to manage registry page
     * from secondary navigation.
     * Test Case ID - RGSN-38277
     */
    @Test
    @TestRail(id = "104641")
    public void testManageRegistryWhenGuestUserSelectsManageRegistryFromSecondaryNavigationExpectManageRegistryPage() {

        //given
        final Registry registry = dataService.findRegistry("multiple-registry", "simple-pip");
        final String email = registry.getRegistrant().getEmail();
        final String password = registry.getRegistrant().getPassword();
        final ProductGroup productGroup = dataService.findProductGroup("simple-pip", "!attributes");
        
        //when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, productGroup.getUnmappedAttribute("categoryUrl"));
        categoryPage.waitForCategoryPageLoad();
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        navigationSection.hoverRegistryLink();
        ManageListOfRegistriesPage manageListOfRegistriesPage = RegistryFlows.navigateToManageRegistryFromSecondaryNavigation(context, email, password);
        
        //then
        assertTrue("User is not navigated to manage registry page", manageListOfRegistriesPage.isCurrentUrl());
    }
    
    /**
     * Verifies whether a signed in user is able to navigate to manage registry page
     * from secondary navigation.
     * Test Case ID - RGSN-38277
     */
    @Test
    @TestRail(id = "104642")
    public void testManageRegistryWhenSignedInUserSelectsManageRegistryFromSecondaryNavigationExpectManageRegistryPage() {

        //given
        final Registry registry = dataService.findRegistry("multiple-registry", "simple-pip");
        final String email = registry.getRegistrant().getEmail();
        final String password = registry.getRegistrant().getPassword();
        final ProductGroup productGroup = dataService.findProductGroup("simple-pip", "!attributes");
        
        //when
        startPilotAt(context.getPage(LoginPage.class));
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, productGroup.getUnmappedAttribute("categoryUrl"));
        categoryPage.waitForCategoryPageLoad();
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        navigationSection.hoverRegistryLink();
        
        //then
        assertTrue("Manage My Registry button is not displayed", navigationSection.isManageMyRegistryButtonPresentAndDisplayed());
        
        //when
        ManageListOfRegistriesPage manageListOfRegistriesPage = navigationSection.clickManageMyRegistriesButton();
        
        //then
        assertTrue("User is not navigated to manage registry page", manageListOfRegistriesPage.isCurrentUrl());
    }
    
    /**
     * Verifies whether a guest user is able to navigate to create registry page
     * from secondary navigation.
     * Test Case ID - RGSN-38277
     */
    @Test
    @TestRail(id = "104643")
    public void testCreateRegistryWhenGuestUserSelectsCreateRegistryFromSecondaryNavigationExpectCreateRegistryPageSection() {

        //given
        final CustomerAccount account = dataService.findAccount("valid-user", "account-exist");
        final ProductGroup productGroup = dataService.findProductGroup("simple-pip", "!attributes");
        String expectedTitle = "Create Registry";
        
        //when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, productGroup.getUnmappedAttribute("categoryUrl"));
        categoryPage.waitForCategoryPageLoad();
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        navigationSection.hoverRegistryLink();
        navigationSection.clickGetStarted();
        RegistryFlows.registrySignInFlow(context, account);
        CreateRegistryOverlaySection createRegistryOverlaySection = context.getPageSection(CreateRegistryOverlaySection.class);
        createRegistryOverlaySection.waitForCreateRegistryPageToLoad();
        String acutalTitle = createRegistryOverlaySection.getCreateRegistryTitleText();

        //then
        assertTrue("User is not navigated to create registry overlay page section. Expected Title text - \'" + expectedTitle +
                "\' Acutal Title displayed is - \'" + acutalTitle + "\'", acutalTitle.equals(expectedTitle));
    }
    
    /**
     * Verifies whether a signed in user is able to navigate to create registry page
     * from secondary navigation.
     * Test Case ID - RGSN-38277
     */
    @Test
    @TestRail(id = "104644")
    public void testCreateRegistryWhenSignedInUserSelectsCreateRegistryFromSecondaryNavigationExpectCreateRegistryPageSection() {

        //given
        final CustomerAccount account = dataService.findAccount("single-user-registry");
        final ProductGroup productGroup = dataService.findProductGroup("simple-pip", "!attributes");
        String expectedTitle = "Create Registry";
        
        //when
        startPilotAt(context.getPage(LoginPage.class));
        AccountFlows.signInWithAccount(context, account);
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, productGroup.getUnmappedAttribute("categoryUrl"));
        categoryPage.waitForCategoryPageLoad();
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        navigationSection.hoverRegistryLink();
        navigationSection.clickGetStarted();
        CreateRegistryOverlaySection createRegistryOverlaySection = context.getPageSection(CreateRegistryOverlaySection.class);
        createRegistryOverlaySection.waitForCreateRegistryPageToLoad();
        String acutalTitle = createRegistryOverlaySection.getCreateRegistryTitleText();
        
        //then
        assertTrue("User is not navigated to create registry overlay page section. Expected Title text - \'" + expectedTitle +
                "\' Acutal Title displayed is - \'" + acutalTitle + "\'", acutalTitle.equals(expectedTitle));
    }
    
    /**
     * Verifies whether the registry log in is persistent when user Closed and reopened the Browser.
     * Test Case ID - RGSN-38880
     */
    @Test
    @TestRail(id = "108906")
    public void testSecondaryNavigationWhenSignedInUserCloseAndRelaunchTheBrowserExpectLoginPersistent() {
        
        // given
        final Registry registry = dataService.findRegistry("multiple-registry", "simple-pip");
        final String email = registry.getRegistrant().getEmail();
        final String password = registry.getRegistrant().getPassword();
        
        // when
        startPilotAt(context.getPage(LoginPage.class));
        AccountHomePage accountHomePage = AccountFlows.signIn(context, email, password, AccountHomePage.class);
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        
        String accountPageUrl = context.getPilot().getDriver().getCurrentUrl();
        // Close Browser Session and revisit shop page
        Set<Cookie> siteCookies = context.getPilot().getDriver().manage().getCookies();
        context.getPilot().getDriver().close();
        context = DefaultEvergreenContext.getContext();
        DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();
        for (Cookie cookie : siteCookies) {
            context.getPilot().getDriver().manage().addCookie(cookie);
        }
        context.getPilot().getDriver().get(accountPageUrl);
        accountHomePage.waitForAccountHomePageLoad();
        
        // then
        assertTrue("User is not signed in to the account", accountHomePage.isUserSignedIn());
        navigationSection.hoverRegistryLink();
        assertTrue("Manage My Registry button is not displayed while hovering registry link from secondary navigation", 
                navigationSection.isManageMyRegistryButtonPresentAndDisplayed());
        navigationSection.hoverRegistryLink();
        assertFalse("Email field is dispayed in registry flyout", navigationSection.isEmailFieldPresentInRegistryFlyout());
        assertFalse("Password field is displayed in registry flyout", navigationSection.isPasswordFieldPresentInRegistryFlyout());
    }
}
