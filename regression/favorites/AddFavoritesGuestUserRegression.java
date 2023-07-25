package com.wsgc.ecommerce.ui.regression.favorites;

import static org.junit.Assert.assertTrue;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.favorites.FavoritesGalleryPage;
import com.wsgc.ecommerce.ui.pagemodel.favorites.FavoritesItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.ProductGroup;
import groovy.lang.Category;
import org.junit.Assume;
import org.junit.Test;
import tag.area.FavoritesArea;

/**
 * Regression Tests for validating favorite item added to Favorite Gallery as Guest user.
 */
@Category(FavoritesArea.class)
public class AddFavoritesGuestUserRegression extends AbstractTest {

    /**
     * Verifies whether guest user is able to add an item to favorites from simple pip page.
     * Test Case ID - RGSN-38232
     */
    @Test
    @TestRail(id = "100719")
    public void testAddToFavoritesWhenGuestUserFavoritesSimplePipItemExpectItemInFavoritesGallery() {

        // given
        ProductGroup productGroup = dataService.findProductGroup("simple-pip-favorite");

        // when
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
     * Verifies whether guest user is able to add an item to favorites from multibuy pip page.
     * Test Case ID - RGSN-38232
     */
    @Test
    @TestRail(id = "100720")
    public void testAddToFavoritesWhenGuestUserFavoritesMultibuyPipItemExpectItemInFavoritesGallery() {

        // given
        ProductGroup productGroup = dataService.findProductGroup("multi-buy-pip-favorite");

        // when
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
     * Verifies whether guest user is able to add an item to favorites from guided pip page.
     * Test Case ID - RGSN-38232, RGSN-38245
     */
    @Test
    @TestRail(id = "100721")
    public void testAddToFavoritesWhenGuestUserFavoritesGuidedPipItemExpectItemInFavoritesGallery() {

        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP) 
                       || context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP_REDESIGN));

        // given
        ProductGroup productGroup = dataService.findProductGroup("guided-pip-favorite");

        // when
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
}
