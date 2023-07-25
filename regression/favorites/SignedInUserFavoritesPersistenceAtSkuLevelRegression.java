package com.wsgc.ecommerce.ui.regression.favorites;

import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.account.AccountFlows;
import com.wsgc.ecommerce.ui.pagemodel.account.LoginPage;
import com.wsgc.ecommerce.ui.pagemodel.favorites.FavoritesGalleryPage;
import com.wsgc.ecommerce.ui.pagemodel.favorites.FavoritesItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.CustomerAccount;
import com.wsgc.evergreen.entity.api.ProductGroup;

import tag.area.FavoritesArea;

/**
 * Regression Tests for Adding items to favorites at SKU level.
 */
@Category(FavoritesArea.class)
public class SignedInUserFavoritesPersistenceAtSkuLevelRegression extends AbstractTest {
 
    /**
     * Verifies whether a signed in user is able to perform sku level favorites and it
     * is persistent between favorite gallery page to simple pip page. 
     * Test case ID - RGSN-38144, RGSN-38678
     */
    @Test
    @TestRail(id = "98460")
    public void testSimplePipPageWhenSignedInUserNavigatesViaFavoritesGalleryPageExpectSkuLevelFavorites() {

        // given
        final ProductGroup expectedProductGroup = dataService.findProductGroup("simple-pip", "attributes");
        CustomerAccount account = dataService.findAccount("sku-favorites", "simple-pip");
        String expectedSku = expectedProductGroup.getSkus().toString().replaceAll("\\[", "");
        expectedSku = expectedSku.replaceAll("\\]", "");

        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        startPilotAt(loginPage);
        AccountFlows.signInWithAccount(context, account);

        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);
        favoritesGalleryPage.isFavoriteGalleryPopulatedWithItems();

        // then
        FavoritesItemRepeatableSection favoritesItemRepeatableSection = favoritesGalleryPage.getProductSectionByGroupId(expectedProductGroup.getGroupId());
        ProductPage productPage = favoritesItemRepeatableSection.goToProductInformationPage(expectedProductGroup);
        Assert.assertTrue(String.format("SKU of the product selected \'" + productPage.getProductSkuFromPIP() 
                + "\' is not matching with the expected SKU \'" + expectedSku + "\'"), expectedSku.equalsIgnoreCase(productPage.getProductSkuFromPIP()));
        assertTrue("The item is not added to favorites at SKU Level in Simple PIP Page", productPage.isItemAddedToFavorites());
   }

    /**
     * Verifies whether a signed in user is able to perform sku level favorites and it
     * is persistent between favorite gallery page to guided pip page. 
     * Test case ID - RGSN-38144.
     */
    @Test
    @TestRail(id = "98461")
    public void testGuidedPipPageWhenSignedInUserNavigatesViaFavoritesGalleryPageExpectSkuLevelFavorites() {

        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP) 
                || context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP_REDESIGN));
        final ProductGroup expectedProductGroup = dataService.findProductGroup("guided-pip", "attributes");
        CustomerAccount account = dataService.findAccount("sku-favorites", "guided-pip");
        String expectedSku = expectedProductGroup.getSkus().toString().replaceAll("\\[", "");
        expectedSku = expectedSku.replaceAll("\\]", "");

        // when	
        LoginPage loginPage = context.getPage(LoginPage.class);
        startPilotAt(loginPage);
        AccountFlows.signInWithAccount(context, account);

        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);
        favoritesGalleryPage.isFavoriteGalleryPopulatedWithItems();

        // then
        FavoritesItemRepeatableSection favoritesItemRepeatableSection = favoritesGalleryPage.getProductSectionByGroupId(expectedProductGroup.getGroupId());
        ProductPage productPage = favoritesItemRepeatableSection.goToProductInformationPage(expectedProductGroup);
        Assert.assertTrue(String.format("SKU of the product selected \'" + productPage.getProductSkuFromPIP() 
                + "\' is not matching with the expected SKU \'" + expectedSku + "\'"), expectedSku.equalsIgnoreCase(productPage.getProductSkuFromPIP()));
        assertTrue("The item is not added to favorites at SKU Level in Guided PIP Page", productPage.isItemAddedToFavorites());
    }
}
