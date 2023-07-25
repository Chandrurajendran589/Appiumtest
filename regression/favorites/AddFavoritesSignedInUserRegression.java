package com.wsgc.ecommerce.ui.regression.favorites;

import static org.junit.Assert.assertTrue;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.account.AccountFlows;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.account.LoginPage;
import com.wsgc.ecommerce.ui.pagemodel.favorites.FavoritesGalleryPage;
import com.wsgc.ecommerce.ui.pagemodel.favorites.FavoritesItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.ecommerce.ui.regression.favorites.syndication.FavoritesApiServices;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.CustomerAccount;
import com.wsgc.evergreen.entity.api.ProductGroup;
import com.wsgc.evergreen.impl.DefaultEvergreenContext;
import groovy.lang.Category;
import org.junit.Assume;
import org.junit.Test;
import tag.area.FavoritesArea;

/**
 * Regression Tests for validating favorite item added to Favorite Gallery from pip page as Signed In User.
 */
@Category(FavoritesArea.class)
public class AddFavoritesSignedInUserRegression extends AbstractTest {

    /**
     * Verifies whether signed in user is able to add an item to favorites from simple pip page.
     * Test Case ID - RGSN-38232
     */
    @Test
    @TestRail(id = "100722")
    public void testAddToFavoritesWhenSignedInUserFavoritesSimplePipItemExpectItemInFavoritesGallery() {

        // given
        ProductGroup productGroup = dataService.findProductGroup("simple-pip-favorite");
        CustomerAccount account = dataService.findAccount("favorites-add-persistence", "product-page");

        // when
        FavoritesApiServices.setUp(context);
        FavoritesApiServices.deleteFavoritesList(account);
        context.getPilot().getDriver().close();
        context = DefaultEvergreenContext.getContext();
        DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();
        startPilotAt(context.getPage(LoginPage.class));
        AccountFlows.signInWithAccount(context, account);
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        assertTrue(String.format("%s - is not added to favorites", 
                productGroup.getName()), productGroup.getName().contains(productPage.favoriteProductOnPip()));
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);
        FavoritesItemRepeatableSection favoritesItemRepeatableSection = favoritesGalleryPage.getProductSectionByGroupId(productGroup.getGroupId());

        // then
        assertTrue("Favorites Gallery is empty", favoritesGalleryPage.isFavoriteGalleryPopulatedWithItems());
        assertTrue(String.format("Item %s is not added to Favorites Gallery Page", 
                productGroup.getName()), productGroup.getName().contains((favoritesItemRepeatableSection.getProductName())));
    }

    /**
     * Verifies whether signed in user is able to add an item to favorites from multibuy pip page.
     * Test Case ID - RGSN-38232
     */
    @Test
    @TestRail(id = "100723")
    public void testAddToFavoritesWhenSignedInUserFavoritesMultibuyPipItemExpectItemInFavoritesGallery() {

        // given
        ProductGroup productGroup = dataService.findProductGroup("multi-buy-pip-favorite");
        CustomerAccount account = dataService.findAccount("favorites-add-persistence", "product-page");

        // when
        FavoritesApiServices.setUp(context);
        FavoritesApiServices.deleteFavoritesList(account);
        context.getPilot().getDriver().close();
        context = DefaultEvergreenContext.getContext();
        DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();
        startPilotAt(context.getPage(LoginPage.class));
        AccountFlows.signInWithAccount(context, account);
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        assertTrue(String.format("%s - is not added to favorites", productGroup.getName()), 
                productGroup.getName().contains(productPage.favoriteProductOnPip()));
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);
        FavoritesItemRepeatableSection favoritesItemRepeatableSection = favoritesGalleryPage.getProductSectionByGroupId(productGroup.getGroupId());

        // then
        assertTrue("Favorites Gallery is empty", favoritesGalleryPage.isFavoriteGalleryPopulatedWithItems());
        assertTrue(String.format("Item %s is not added to Favorites Gallery Page", 
                productGroup.getName()), productGroup.getName().contains((favoritesItemRepeatableSection.getProductName())));
    }

    /**
     * Verifies whether signed in user is able to add an item to favorites from guided pip page.
     * Test Case ID - RGSN-38232
     */
    @Test
    @TestRail(id = "100724")
    public void testAddToFavoritesWhenSignedInUserFavoritesGuidedPipItemExpectItemInFavoritesGallery() {

        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP) 
                       || context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP_REDESIGN));

        // given
        ProductGroup productGroup = dataService.findProductGroup("guided-pip-favorite");
        CustomerAccount account = dataService.findAccount("favorites-add-persistence", "product-page");

        // when
        FavoritesApiServices.setUp(context);
        FavoritesApiServices.deleteFavoritesList(account);
        context.getPilot().getDriver().close();
        context = DefaultEvergreenContext.getContext();
        DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();
        startPilotAt(context.getPage(LoginPage.class));
        AccountFlows.signInWithAccount(context, account);
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        assertTrue(String.format("%s - is not added to favorites", productGroup.getName()), 
                productGroup.getName().contains(productPage.favoriteProductOnPip()));
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);
        FavoritesItemRepeatableSection favoritesItemRepeatableSection = favoritesGalleryPage.getProductSectionByGroupId(productGroup.getGroupId());

        // then
        assertTrue("Favorites Gallery is empty", favoritesGalleryPage.isFavoriteGalleryPopulatedWithItems());
        assertTrue(String.format("Item %s is not added to Favorites Gallery Page", 
                productGroup.getName()), productGroup.getName().contains((favoritesItemRepeatableSection.getProductName())));
    }
}
