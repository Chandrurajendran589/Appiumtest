package com.wsgc.ecommerce.ui.regression.favorites;

import static org.junit.Assert.assertTrue;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.account.AccountFlows;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.account.LoginPage;
import com.wsgc.ecommerce.ui.pagemodel.favorites.FavoritesGalleryPage;
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
 * Regression Tests for validating remove favorite item from all kinds of Pip Page as Signed In user.
 */
@Category(FavoritesArea.class)
public class RemoveFavoritesSignedInUserRegression extends AbstractTest{

    /**
     * Verifies whether signed in user is able to remove item from favorites on simple pip page.
     * Test Case ID - RGSN-38232
     */
    @Test
    @TestRail(id = "100728")
    public void testRemoveFavoritesWhenSignedInUserRemoveFavoritesFromSimplePipItemExpectEmptyFavoritesGallery() {
        // given
        ProductGroup productGroup = dataService.findProductGroup("simple-pip-favorite");
        CustomerAccount account = dataService.findAccount("favorites-remove-persistence", "product-page");

        // when
        FavoritesApiServices.setUp(context);
        FavoritesApiServices.addItemToFavoritesList(account);
        context.getPilot().getDriver().close();
        context = DefaultEvergreenContext.getContext();
        DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();
        startPilotAt(context.getPage(LoginPage.class));
        AccountFlows.signInWithAccount(context, account);
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        productPage.favoriteProductOnPip();
        assertTrue("Item is not added to favorites", productPage.isItemAddedToFavorites());
        assertTrue(String.format("%s - is not removed from favorites", productGroup.getName()), 
                productGroup.getName().contains(productPage.unfavoriteProductOnPip()));
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);

        // then
        assertTrue("Favorites Gallery is not empty", favoritesGalleryPage.isGalleryEmpty());
    }

    /**
     * Verifies whether signed in user is able to remove favorites from multibuy pip page.
     * Test Case ID - RGSN-38232
     */
    @Test
    @TestRail(id = "100708")
    public void testRemoveFavoritesWhenSignedInUserRemoveFavoritesFromMultibuyPipItemExpectEmptyFavoritesGallery() {

        // given
        ProductGroup productGroup = dataService.findProductGroup("multi-buy-pip-favorite");
        CustomerAccount account = dataService.findAccount("favorites-remove-persistence", "product-page");

        // when
        FavoritesApiServices.setUp(context);
        FavoritesApiServices.addItemToFavoritesList(account);
        context.getPilot().getDriver().close();
        context = DefaultEvergreenContext.getContext();
        DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();
        startPilotAt(context.getPage(LoginPage.class));
        AccountFlows.signInWithAccount(context, account);
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        assertTrue(String.format("%s - is not removed from favorites", productGroup.getName()), 
                productGroup.getName().contains(productPage.unfavoriteProductOnPip()));
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);

        // then
        assertTrue("Favorites Gallery is not empty", favoritesGalleryPage.isGalleryEmpty());
    }

    /**
     * Verifies whether signed in user is able to remove favorites from guided pip page.
     * Test Case ID - RGSN-38232
     */
    @Test
    @TestRail(id = "100709")
    public void testRemoveFavoritesWhenSignedInUserRemoveFavoritesFromGuidedPipExpectEmptyFavoritesGallery() {

        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP) 
                       || context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP_REDESIGN));

        // given
        ProductGroup productGroup = dataService.findProductGroup("guided-pip-favorite");
        CustomerAccount account = dataService.findAccount("favorites-add-persistence", "product-page");

        // when
        FavoritesApiServices.setUp(context);
        FavoritesApiServices.addItemToFavoritesList(account);
        context.getPilot().getDriver().close();
        context = DefaultEvergreenContext.getContext();
        DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();
        startPilotAt(context.getPage(LoginPage.class));
        AccountFlows.signInWithAccount(context, account);
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        assertTrue(String.format("%s - is not removed from favorites", productGroup.getName()), 
                productGroup.getName().contains(productPage.unfavoriteProductOnPip()));
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);

        // then
        assertTrue("Favorites Gallery is not empty", favoritesGalleryPage.isGalleryEmpty());
    }
}
