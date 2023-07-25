package com.wsgc.ecommerce.ui.regression.favorites;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.favorites.SignInOverlayPageFlows;
import com.wsgc.ecommerce.ui.pagemodel.favorites.FavoritesGalleryPage;
import com.wsgc.evergreen.entity.api.CustomerAccount;
import java.util.HashMap;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tag.area.FavoritesArea;

/**
 * Regression Tests for validating empty favorites gallery as Signed In user.
 */
@Category(FavoritesArea.class)
public class EmptyFavoritesGallerySignedInUserRegression extends AbstractTest {

    /**
     * Verifies whether user is able to navigate to home page via bread crumbs
     * in an empty favorites gallery page.
     * Test Case ID - RGSN-38016
     */
    @Test
    @TestRail(id = "102471")
    public void testEmptyFavoritesGalleryWhenUserClicksHomeInBreadcrumbsExpectHomePage() {

        //given
        CustomerAccount account = dataService.findAccount("valid-user", "account-exist");
        
        //when
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);
        SignInOverlayPageFlows.signInOnFavoritesGallery(context, false, account);
        String expectedHomePageUrl = favoritesGalleryPage.getHomePageUrlInBreadcrumbs();
        favoritesGalleryPage.goToHomePageViaBreadcrumbs();
        String actualHomePageUrl = context.getPilot().getDriver().getCurrentUrl();

        //then
        Assert.assertTrue(String.format("Expected Home page url - %s does not match actual home page url  - %s", 
        		expectedHomePageUrl, actualHomePageUrl), actualHomePageUrl.contains(expectedHomePageUrl));
    }
    
    /**
     * Verifies whether user is able to view the Favorites items count 
     * in an empty favorites gallery page.
     * Test Case ID - RGSN-38016
     */
    @Test
    public void testFavoriteItemsLabelWhenUserNavigatesToEmptyFavoritesGalleryExpectFavoritesCount() {
    	
        Assume.assumeFalse(context.getTargetSite().getBrand().getCode().equalsIgnoreCase("pb"));

        //given
        final String expectedFavoritesCountTextTemplate = "0 Items";
        final String expectedFavoritesCountTemplate = "( 0 )";
        CustomerAccount account = dataService.findAccount("valid-user", "account-exist");
        
        //when
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);
        SignInOverlayPageFlows.signInOnFavoritesGallery(context, false, account);
        String actualFavoritesCountText = favoritesGalleryPage.getFavoritesCountText();

        //then
        Assert.assertTrue(String.format("Expected Favorites count texts - %s or %s does not match actual favorites count text - %s", 
                expectedFavoritesCountTextTemplate, expectedFavoritesCountTemplate, actualFavoritesCountText), 
                actualFavoritesCountText.equalsIgnoreCase(expectedFavoritesCountTextTemplate) 
                || actualFavoritesCountText.equalsIgnoreCase(expectedFavoritesCountTemplate));
    }
    
    /**
     * Verifies whether user is able to view the description
     * in an empty favorites gallery page.
     * Test Case ID - RGSN-38016
     */
    @Test
    @TestRail(id = "102472")
    public void testFavoritesGalleryWhenUserNavigatesToEmptyFavoritesGalleryExpectEmptyGalleryDescription() {

        //given
        final HashMap<String, String> expectedEmptyGalleryTitleTemplates = new HashMap<>();
        expectedEmptyGalleryTitleTemplates.put("WS", "See it, love it, save it.");
        expectedEmptyGalleryTitleTemplates.put("PB", "Find Your Favorites");
        expectedEmptyGalleryTitleTemplates.put("PT", "Lots of room for the things you love.");
        expectedEmptyGalleryTitleTemplates.put("PK", "Lots of room for the things you love.");
        expectedEmptyGalleryTitleTemplates.put("WE", "Lots of room for the things you love.");
        expectedEmptyGalleryTitleTemplates.put("MG", "Find Your Favorites");
        
        final HashMap<String, String> expectedEmptyGalleryDescriptionTemplates = new HashMap<>();
        expectedEmptyGalleryDescriptionTemplates.put("WS", "Keep track of your favorite items and "
                + "inspiration by selecting the heart icon.");
        expectedEmptyGalleryDescriptionTemplates.put("PB", "See something you like? Keep track of "
                + "your favorite items and inspiration by selecting the heart icon.");
        expectedEmptyGalleryDescriptionTemplates.put("PT", "Explore our products and add your favorites anywhere you see a heart.");
        expectedEmptyGalleryDescriptionTemplates.put("PK", "Explore our products and add your favorites anywhere you see a heart.");
        expectedEmptyGalleryDescriptionTemplates.put("WE", "Explore our products and add your favorites anywhere you see a heart.");
        expectedEmptyGalleryDescriptionTemplates.put("MG", "See it, love it, save it. Keep track of "
                + "your favorite items and inspiration by selecting the heart icon.");
        CustomerAccount account = dataService.findAccount("valid-user", "account-exist");
        
        //when
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);
        SignInOverlayPageFlows.signInOnFavoritesGallery(context, false, account);
        String expectedEmptyGalleryTitle = expectedEmptyGalleryTitleTemplates.get(context.getBrand().getCode()).toLowerCase();
        String expectedEmptyGalleryDescription = expectedEmptyGalleryDescriptionTemplates.get(context.getBrand().getCode()).toLowerCase();
        String actualEmptyGalleryTitle = favoritesGalleryPage.getEmptyFavoritesGalleryTitle().toLowerCase();
        String actualEmptyGalleryDescription = favoritesGalleryPage.getEmptyFavoritesGalleryDescription().toLowerCase();
        if (context.getTargetEnvironment().getName().contains("-")) {
        	expectedEmptyGalleryTitle = expectedEmptyGalleryTitle.replaceAll("favorite", "favourite");
        	expectedEmptyGalleryDescription = expectedEmptyGalleryDescription.replaceAll("favorite", "favourite");
        }

        //then
        Assert.assertEquals(String.format("Expected empty gallery title - %s does not match actual empty gallery title  - %s", 
        		expectedEmptyGalleryTitle, actualEmptyGalleryTitle), expectedEmptyGalleryTitle, actualEmptyGalleryTitle);
        Assert.assertEquals(String.format("Expected empty gallery description - %s does not match actual empty gallery description  - %s", 
        		expectedEmptyGalleryDescription, actualEmptyGalleryDescription), expectedEmptyGalleryDescription, actualEmptyGalleryDescription);
    }
    
    /**
     * Verifies whether user is able to view the start shopping button
     * in an empty favorites gallery page.
     * Test Case ID - RGSN-38016
     */
    @Test
    @TestRail(id = "102473")
    public void testEmptyFavoritesGalleryWhenUserClicksStartShoppingExpectHomePage() {
    	
        Assume.assumeFalse(context.getTargetSite().getBrand().getCode().equalsIgnoreCase("pb"));

        //given
        CustomerAccount account = dataService.findAccount("valid-user", "account-exist");
        
        //when
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);
        SignInOverlayPageFlows.signInOnFavoritesGallery(context, false, account);
        String expectedHomePageUrl = favoritesGalleryPage.getUrlInStartShoppingButton();
        favoritesGalleryPage.clickStartShopping();
        String actualHomePageUrl = context.getPilot().getDriver().getCurrentUrl();

        //then
        Assert.assertTrue(String.format("Expected Home page url - %s does not match actual home page url  - %s", 
        		expectedHomePageUrl, actualHomePageUrl), actualHomePageUrl.contains(expectedHomePageUrl));
    }
    
    /**
     * Verifies whether Sign In To Save And Share Button is not displayed 
     * in an empty favorites gallery page.
     * Test Case ID - RGSN-38016
     */
    @Test
    @TestRail(id = "102474")
    public void testFavoriteGalleryWhenUserNavigatesToEmptyFavoritesGalleryExpectSignIntoShareButtonNotDisplayed() {

        //given
        CustomerAccount account = dataService.findAccount("valid-user", "account-exist");
        
        //when
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);
        SignInOverlayPageFlows.signInOnFavoritesGallery(context, false, account);

        //then
        Assert.assertFalse("Sign in to share button is displayed", favoritesGalleryPage.isSignInSaveAndShareButtonDisplayed());
    }

}
