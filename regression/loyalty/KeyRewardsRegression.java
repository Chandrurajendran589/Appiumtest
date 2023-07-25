package com.wsgc.ecommerce.ui.regression.loyalty;

import com.wsgc.ecommerce.testscommon.tag.tier.StandardTier;
import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.account.AccountFlows;
import com.wsgc.ecommerce.ui.pagemodel.account.AccountHomePage;
import com.wsgc.ecommerce.ui.pagemodel.account.LoginPage;
import com.wsgc.ecommerce.ui.pagemodel.loyalty.AboutTheKeyPage;
import com.wsgc.evergreen.entity.api.CustomerAccount;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tag.area.AccountArea;

import static org.junit.Assert.assertTrue;

/**
 * Regression Tests that validates that User experience with Key Rewards.
 */
@Category(AccountArea.class)
public class KeyRewardsRegression extends AbstractTest {

    /**
     * Verifies whether the user is redirected to 'The Key Rewards' page if the Account is not Enrolled with Key Reward.
     */
    @Category(StandardTier.class)
    @Test
    @TestRail(id = "105279")
    public void testTheKeyRewardsPageWhenUnenrolledKeyUserClicksOnTheKeyRewardsInLeftNavExpectTheKeyRewardsPage() {

        // given
        final String  expectedTheKeyRewardsPageURL = "/the-key-rewards/";
        CustomerAccount account = dataService.findAccount("non-loyalty-member");
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountHomePage accountHomePage = AccountFlows.signInWithAccount(context, account);

        // when
        AboutTheKeyPage aboutTheKeyPage = accountHomePage.clickOnKeyRewardsLinkOnAccountPage(AboutTheKeyPage.class);
        String actualTheKeyRewardsPageURL = context.getPilot().getDriver().getCurrentUrl();

        // then
        assertTrue(String.format("The Key Rewards Page URL ends with '%s'. Actual the Key Rewards Page URL is: '%s'", expectedTheKeyRewardsPageURL,
                actualTheKeyRewardsPageURL), actualTheKeyRewardsPageURL.endsWith(expectedTheKeyRewardsPageURL));
        assertTrue("The Join Now button doesn't present on the page", aboutTheKeyPage.isJoinNowButtonVisible());
        assertTrue("The Manage My Credit Card link doesn't present on the page", aboutTheKeyPage.isManageMyCreditCardLinkPresent());
        assertTrue("The View My Rewards link doesn't present on the page", aboutTheKeyPage.isViewMyRewardsLinkPresent());
    }
}
