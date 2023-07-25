package com.wsgc.ecommerce.ui.regression.stores;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.account.AccountFlows;
import com.wsgc.ecommerce.ui.endtoend.designprojects.DesignProjectsFlows;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSectionFactory;
import com.wsgc.ecommerce.ui.pagemodel.mydesignprojects.MyDesignProjectsPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.CustomerAccount;

/**
 * Regression Tests for Validating the My Design projects pages.
 * 
 */
public class MyDesignProjectsRegression extends AbstractTest {

    /**
     * Prepares data fixtures & ensures feature flag is enabled.
     */
    @Before
    public void setUp() {
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.MY_DESIGN_PROJECTS));
    }
    
    /**
     * Verifies whether design project message and sign in button is displayed at My Design projects page 
     * for a guest user.
     * Test Case ID - RGSN-38727
     */
    @Test
    @TestRail(id = "104381")
    public void testMyDesignProjectsWhenGuestUserVisitsMyDesignProjectPageExpectDesignProjectPageMessageAndSignInButton() {
        
        //given
        final String expectedMessage = "Log in to view your design ideas, all in one place.";
        
        //when
        MyDesignProjectsPage myDesignProjectsPage = DesignProjectsFlows.goToDesignStudioPageFlow(context);
        
        //then
        String actualMessage = myDesignProjectsPage.getDesignProjectMessage();
        assertTrue(String.format("Design project message is not displayed expected : %s , actual : %s ", expectedMessage, actualMessage),
                actualMessage.equalsIgnoreCase(expectedMessage));
        assertTrue("Sign in button is not displayed at My Design project page", myDesignProjectsPage.isSignInButtonDisplayed());
    }
    
    /**
     * Verifies whether user able to sign in from My Design project page and user is redirected to 
     * my design page after signing in.
     * Test Case ID - RGSN-38727
     */
    @Test
    @TestRail(id = "104382")
    public void testSignInWhenUserSignInWithValidAccountFromMyDesignProjectExpectMyDesignProjectPage() {
        
        //given
        CustomerAccount account = dataService.findAccount("account-exist", "valid-user");
        final String expectedHeader ="My Design Projects";
        
        //when
        MyDesignProjectsPage myDesignProjectsPage = DesignProjectsFlows.goToDesignStudioPageFlow(context);
        myDesignProjectsPage.clickSignInButton();
        MyDesignProjectsPage loggedInMyDesignProjectsPage = AccountFlows.signIn(context, account.getEmail(), account.getPassword(), MyDesignProjectsPage.class);
        String actualHeader = loggedInMyDesignProjectsPage.getMyDesignProjectPageHeader();
        
        //then
        assertEquals("My Design project page title is not matching ", expectedHeader, actualHeader);
        assertTrue("Design chat is not displayed at My Design projects page", loggedInMyDesignProjectsPage.isDesignChatDisplayed());

        if (context.getTargetSite().supportsFeature(FeatureFlag.MY_DESIGN_PROJECTS_GLOBAL_NAVIGATION)) {
            NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
            if (context.isFullSiteExperience()) {
                navigationSection.openMyAccountDropdownByHovering();
            } else {
                navigationSection.clickShop();
            }
            assertTrue("My Design projects link is not displayed", navigationSection.isMyDesignProjectsLinkDisplayed());
        }
    }
}
