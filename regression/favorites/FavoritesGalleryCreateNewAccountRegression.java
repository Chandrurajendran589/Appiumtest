package com.wsgc.ecommerce.ui.regression.favorites;

import static org.junit.Assert.assertTrue;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.favorites.FavoritesGalleryFlows;
import com.wsgc.ecommerce.ui.pagemodel.account.AccountHomePage;
import com.wsgc.ecommerce.ui.pagemodel.favorites.FavoritesGalleryPage;
import com.wsgc.evergreen.entity.api.CustomerAccount;
import com.wsgc.evergreen.entity.api.MutableAccount;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tag.area.AccountArea;

/**
 * Regression Tests that validates Favorite Gallery User is able to create an account.
 * 
 */
@Category(AccountArea.class)
public class FavoritesGalleryCreateNewAccountRegression extends AbstractTest {

    /**
     * Verifies that the user is able to create an account in the Favorites gallery page.
     * Test Case ID - RGSN-38012
     */
    @Test
    @TestRail(id = "98438")
    public void testFavoriteGalleryWhenUserCreatesNewAccountExpectAccountHomePage(){
       
        //given
        MutableAccount mutableAccount = getMutableAccount("default");
        CustomerAccount newAccount = accountCreator.getRandomEmailAccount(mutableAccount);

        // when
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage); 
       
        //then
        favoritesGalleryPage.clickOnSignInToViewFavoritesButton();
        AccountHomePage accountHomePage = FavoritesGalleryFlows.createAccountFromFavoritesGallery(context, newAccount, AccountHomePage.class);
        accountHomePage.waitForAccountHomePageLoad();
        assertTrue("Name is not displayed in the greeting message",
                accountHomePage.getGreetingText().toLowerCase().contains(newAccount.getFullName().toLowerCase()));
    }
}
