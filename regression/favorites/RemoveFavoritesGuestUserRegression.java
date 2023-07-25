package com.wsgc.ecommerce.ui.regression.favorites;

import static org.junit.Assert.assertTrue;

import org.junit.Assume;
import org.junit.Test;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.favorites.FavoritesGalleryPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.ProductGroup;

import groovy.lang.Category;
import tag.area.FavoritesArea;

/**
 * Regression Tests for validating remove favorite item from all kinds of Pip Page as Guest user.
 */
@Category(FavoritesArea.class)
public class RemoveFavoritesGuestUserRegression extends AbstractTest {

    /**
     * Verifies whether guest user is able to remove item from favorites on simple pip page.
     * Test Case ID - RGSN-38232
     */
    @Test
    @TestRail(id = "100725")
    public void testRemoveFavoritesWhenGuestUserRemoveFavoritesFromSimplePipItemExpectEmptyFavoritesGallery() {

        // given
        ProductGroup productGroup = dataService.findProductGroup("simple-pip-favorite");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        assertTrue(String.format("%s - is not added from favorites", productGroup.getName()), 
                productGroup.getName().contains(productPage.favoriteProductOnPip()));
        assertTrue("Item is not added to favorites", productPage.isItemAddedToFavorites());
        assertTrue(String.format("%s - is not removed from favorites", productGroup.getName()), 
                productGroup.getName().contains(productPage.unfavoriteProductOnPip()));
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);

        // then
        assertTrue("Favorites Gallery is not empty", favoritesGalleryPage.isGalleryEmpty());
    }

    /**
     * Verifies whether guest user is able to remove favorites from multibuy pip page.
     * Test Case ID - RGSN-38232
     */
    @Test
    @TestRail(id = "100726")
    public void testRemoveFavoritesWhenGuestUserRemoveFavoritesFromMultibuyPipExpectEmptyFavoritesGallery() {

        // given
        ProductGroup productGroup = dataService.findProductGroup("multi-buy-pip-favorite");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        assertTrue(String.format("%s - is not added from favorites", productGroup.getName()), 
                productGroup.getName().contains(productPage.favoriteProductOnPip()));
        assertTrue(String.format("%s - is not removed from favorites", productGroup.getName()), 
                productGroup.getName().contains(productPage.unfavoriteProductOnPip()));
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);

        // then
        assertTrue("Favorites Gallery is not empty", favoritesGalleryPage.isGalleryEmpty());
    }

    /**
     * Verifies whether guest user is able to remove favorites from guided pip page.
     * Test Case ID - RGSN-38232
     */
    @Test
    @TestRail(id = "100727")
    public void testRemoveFavoritesWhenGuestUserRemoveFavoritesFromGuidedPipExpectEmptyFavoritesGallery() {

        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP) 
                       || context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP_REDESIGN));

        // given
        ProductGroup productGroup = dataService.findProductGroup("guided-pip-favorite");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        assertTrue(String.format("%s - is not added from favorites", productGroup.getName()), 
                productGroup.getName().contains(productPage.favoriteProductOnPip()));
        assertTrue(String.format("%s - is not removed from favorites", productGroup.getName()), 
                productGroup.getName().contains(productPage.unfavoriteProductOnPip()));
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);

        // then
        assertTrue("Favorites Gallery is not empty", favoritesGalleryPage.isGalleryEmpty());
    }
}
