package com.wsgc.ecommerce.ui.regression.favorites;

import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.shop.PipFlows;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.favorites.FavoritesGalleryPage;
import com.wsgc.ecommerce.ui.pagemodel.favorites.FavoritesItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.registry.GuidedPipFlows;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.ProductGroup;

import tag.area.FavoritesArea;

/**
 * Regression Tests for Adding items to favorites at SKU level.
 */
@Category(FavoritesArea.class)
public class GuestUserFavoritesPersistenceAtSkuLevelRegression extends AbstractTest {

    /**
     * Verifies whether a guest user is able to perform sku level favorites and it
     * is persistent between favorite gallery page to simple pip page. 
     * Test case ID - RGSN-38144, RGSN-38678
     */
    @Test
    @TestRail(id = "98443")
    public void testSimplePipPageWhenGuestUserFavoriteAnItemExpectSkuLevelFavorites() {

        // given
        final ProductGroup expectedProductGroup = dataService.findProductGroup("simple-pip", "attributes");
        String expectedSku = expectedProductGroup.getSkus().toString().replaceAll("\\[", "");
        expectedSku = expectedSku.replaceAll("\\]", "");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        PipFlows.setSimplePipAttributeSelections(productPage);
        assertTrue(String.format("%s - is not added to favorites", expectedProductGroup.getName()), 
                expectedProductGroup.getName().equalsIgnoreCase(productPage.favoriteProductOnPip()));

        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);
        favoritesGalleryPage.isFavoriteGalleryPopulatedWithItems();

        // then
        FavoritesItemRepeatableSection favoritesItemRepeatableSection = favoritesGalleryPage.getProductSectionByGroupId(expectedProductGroup.getGroupId());
        favoritesItemRepeatableSection.goToProductInformationPage(expectedProductGroup);
        Assert.assertTrue(String.format("SKU of the product selected \'" + productPage.getProductSkuFromPIP() 
                + "\' is not matching with the expected SKU \'" + expectedSku + "\'"), expectedSku.equalsIgnoreCase(productPage.getProductSkuFromPIP()));
        assertTrue("The item is not added to favorites at SKU Level in Simple PIP Page", productPage.isItemAddedToFavorites());
    }

    /**
     * Verifies whether a guest user is able to perform sku level favorites and it
     * is persistent between favorite gallery page to guided pip page. 
     * Test case ID - RGSN-38144.
     */
    @Test
    @TestRail(id = "98444")
    public void testGuidedPipPageWhenGuestUserFavoriteAnItemExpectSkuLevelFavorites() {

        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP)
                || context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP_REDESIGN));
        final ProductGroup expectedProductGroup = dataService.findProductGroup("guided-pip", "attributes");
        String expectedSku = expectedProductGroup.getSkus().toString().replaceAll("\\[", "");
        expectedSku = expectedSku.replaceAll("\\]", "");

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        GuidedPipFlows.setGuidedPipAttributeSelections(context); 
        assertTrue(String.format("%s - is not added to favorites", expectedProductGroup.getName()), 
                expectedProductGroup.getName().equalsIgnoreCase(productPage.favoriteProductOnPip()));

        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);
        favoritesGalleryPage.isFavoriteGalleryPopulatedWithItems();

        // then
        FavoritesItemRepeatableSection favoritesItemRepeatableSection = favoritesGalleryPage.getProductSectionByGroupId(expectedProductGroup.getGroupId());
        favoritesItemRepeatableSection.goToProductInformationPage(expectedProductGroup);
        Assert.assertTrue(String.format("SKU of the product selected \'" + productPage.getProductSkuFromPIP() 
                + "\' is not matching with the expected SKU \'" + expectedSku + "\'"), expectedSku.equalsIgnoreCase(productPage.getProductSkuFromPIP()));
        assertTrue("The item is not added to favorites at SKU Level in Guided PIP Page", productPage.isItemAddedToFavorites());
    }
}
