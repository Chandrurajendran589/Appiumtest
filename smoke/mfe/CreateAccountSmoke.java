package com.wsgc.ecommerce.ui.smoke.mfe;

import com.wsgc.ecommerce.ui.pagemodel.account.AccountHomePage;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import com.wsgc.ecommerce.ui.endtoend.account.AccountFlows;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.evergreen.entity.api.CustomerAccount;
import com.wsgc.evergreen.entity.api.MutableAccount;
import com.wsgc.evergreen.impl.DefaultEvergreenContext;

import tag.area.AccountArea;
import static org.junit.Assert.assertTrue;

/**
 * Smoke Tests that validates that User create an account and sign in
 */
@Category(AccountArea.class)
public class CreateAccountSmoke extends AbstractTest {
    /**
     * Verifies whether the user is able to sign in with an account created in
     * the same brand.
     */
    @Test
    public void testSignInWhenUserCreatesNewAccountExpectAccountHomePage() {
        // given
        MutableAccount mutableAccount = getMutableAccount("default");
        CustomerAccount newAccount = accountCreator.getRandomEmailAccount(mutableAccount);

        // when
        AccountFlows.goToLoginPageFlow(context);
        AccountFlows.createAccount(context, newAccount, AccountHomePage.class);
        // then
        // Close Browser and re-launch
        context.getPilot().getDriver().close();
        context = DefaultEvergreenContext.getContext();
        DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();

        AccountFlows.goToLoginPageFlow(context);
        AccountHomePage accountHomePage = AccountFlows.signInWithAccount(context, newAccount);
        accountHomePage.waitForAccountHomePageLoad();
        assertTrue("Name is not displayed in the greeting message",
                accountHomePage.getGreetingText().toLowerCase().contains(newAccount.getFullName().toLowerCase()));
    }
}
