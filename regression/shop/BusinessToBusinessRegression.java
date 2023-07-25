package com.wsgc.ecommerce.ui.regression.shop;

import static org.junit.Assert.assertTrue;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.account.AccountFlows;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSectionFactory;
import com.wsgc.ecommerce.ui.pagemodel.account.AccountHomePage;
import com.wsgc.ecommerce.ui.pagemodel.account.BusinessToBusinessPage;
import com.wsgc.ecommerce.ui.pagemodel.account.LoginPage;
import com.wsgc.ecommerce.ui.pagemodel.account.TradeMemberLoginPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.CustomerAccount;
import com.wsgc.evergreen.entity.api.ProductGroup;
import org.junit.Assume;
import org.junit.Test;

/**
 * Regression Tests for Business to Business page.
 *
 * @author smanjunath
 *
 */
public class BusinessToBusinessRegression  extends AbstractTest {

    /**
     * Verifies if the user is able to navigate to the Business to Business page from the Global header.
     * Test Case ID - RGSN-38215
     */
    @Test
    @TestRail(id = "102781")
    public void testSiteNavigationWhenUserClicksOnBusinessToBusinessExpectBusinessToBusinessPage() {

        Assume.assumeTrue(context.supportsFeature(FeatureFlag.BUSINESS_TO_BUSINESS_HEADER));

        //given
        final ProductGroup expectedProductGroup = dataService.findProductGroup(
                 productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null);

        //when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, expectedProductGroup.getUnmappedAttribute("categoryUrl"));
        categoryPage.waitForCategoryPageLoad();
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        BusinessToBusinessPage businessToBusinessPage = navigationSection.goToBusinessToBusinessPage();  
        
        //then
        assertTrue("Business to Business Page is not displayed.", businessToBusinessPage.isCurrentUrl()); 
    }
    
    /**
     * Verifies if the user is able to view the Business to Business logo on a category page.
     * Test Case ID - RGSN-38215
     */
    @Test
    @TestRail(id = "102875")
    public void testBuisnessToBusinessWhenUserNavigatesToCategoryPageExpectBusinessToBusinessLogo() {

        Assume.assumeTrue(context.supportsFeature(FeatureFlag.BUSINESS_TO_BUSINESS_LOGO));

        //given
        final ProductGroup expectedProductGroup = dataService.findProductGroup(
                 productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null, "business-to-business");

        //when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, expectedProductGroup.getUnmappedAttribute("categoryUrl"));
        categoryPage.waitForCategoryPageLoad();
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        BusinessToBusinessPage businessToBusinessPage = context.getPage(BusinessToBusinessPage.class);
        //then
        assertTrue("Business To Business logo is not present", 
                    navigationSection.isBrandLogoPresent(businessToBusinessPage.getExpectedUrlPath()));
    }
    
    /**
     * Verifies if the Business To Business link is available in the product page.
     * Test Case ID - RGSN-38215
     */
    @Test
    @TestRail(id = "102996")
    public void testGloablHeaderWhenUserNavigatesToProductPageExpectBusinessToBusinessInGlobalHeader() {

        Assume.assumeTrue(context.supportsFeature(FeatureFlag.BUSINESS_TO_BUSINESS_HEADER));

        //given
        final ProductGroup expectedProductGroup = dataService.findProductGroup(
                 productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null, "simple-pip" ,"!attributes");

        //when
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        
        //then
        assertTrue("Business to Business link is not present in Global Header", 
                navigationSection.isBusinessToBusinessPresentAndDisplayed());  
    }
    
    /**
     * Verifies if a guest user is able to navigate to the trade member login page.
     * Test Case ID - RGSN-38215
     */
    @Test
    @TestRail(id = "102997")
    public void testBusinessToBusinessWhenGuestUserClicksTradeMemberSignInExpectTradeMemberLoginPage() {

        Assume.assumeTrue(context.supportsFeature(FeatureFlag.DESIGN_TRADE_PARTNER_GLOBAL_NAV_LOGIN)
                && context.isFullSiteExperience());

        //given
        final ProductGroup expectedProductGroup;
        if (context.supportsFeature(FeatureFlag.BUSINESS_TO_BUSINESS_LOGO)) {
            expectedProductGroup = dataService.findProductGroup(
                    productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null, "business-to-business");
        } else {
            expectedProductGroup = dataService.findProductGroup(
                    productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null);
        }

        //when
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, expectedProductGroup.getUnmappedAttribute("categoryUrl"));
        categoryPage.waitForCategoryPageLoad();
        navigationSection.openMyAccountDropdownByHovering();
        TradeMemberLoginPage tradeMemberLoginPage = navigationSection.clickTradeMemberSignIn();
       
        //then
        assertTrue("Trade Member Login Page is not displayed.", tradeMemberLoginPage.isCurrentUrl());  
    }
    
    /**
     * Verifies if a signed in user is able to navigate to the trade member login page.
     * Test Case ID - RGSN-38215
     */
    @Test
    @TestRail(id = "102998")
    public void testBusinessToBusinessWhenSignedInUserClicksTradeMemberSignInExpectTradeMemberLoginPage() {

        Assume.assumeTrue(context.supportsFeature(FeatureFlag.DESIGN_TRADE_PARTNER_GLOBAL_NAV_LOGIN)
                && context.isFullSiteExperience());

        //given
        final ProductGroup expectedProductGroup;
        if (context.supportsFeature(FeatureFlag.BUSINESS_TO_BUSINESS_LOGO)) {
            expectedProductGroup = dataService.findProductGroup(
                    productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null, "business-to-business");
        } else {
            expectedProductGroup = dataService.findProductGroup(
                    productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null);
        }
        CustomerAccount account = dataService.findAccount("account-exist", "valid-user");

        //when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountHomePage accountHomePage = AccountFlows.signInWithAccount(context, account);
        accountHomePage.waitForAccountHomePageLoad();
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, expectedProductGroup.getUnmappedAttribute("categoryUrl"));
        categoryPage.waitForCategoryPageLoad();
        navigationSection.openMyAccountDropdownByHovering();
        TradeMemberLoginPage tradeMemberLoginPage = navigationSection.clickTradeMemberSignIn();
       
        //then
        assertTrue("Trade Member Login Page is not displayed.", tradeMemberLoginPage.isCurrentUrl());  
    }
}
