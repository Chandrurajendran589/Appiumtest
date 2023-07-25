package com.wsgc.ecommerce.ui.smoke.mfe;

import static org.junit.Assert.assertTrue;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.favorites.FavoritesGalleryPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryPage;
import com.wsgc.evergreen.entity.api.ProductGroup;
import org.junit.Test;

/**
 * Smoke Tests that validates that Guest User is able to add or remove to or from favorites.
 */
public class FavoritesGuestUserSmoke extends AbstractTest {

    /**
     * Verifies whether a guest user is able to add items to favorites via the Shop Page.
     */
    @Test
    public void testAddToFavoritesWhenGuestUserViaShopPageExpectFavoritedItemsInFavoritesGallery() {
        //given
        ProductGroup productGroup = dataService.findProductGroup("favorites");

        //when
        CategoryPage productCategoryPage = ShopFlows.navigateToCategoryPage(context, productGroup.getUnmappedAttribute("categoryUrl"));
        CategoryItemRepeatableSection productInShopList = productCategoryPage.getProductSectionByGroupId(productGroup.getGroupId());
        assertTrue(productGroup.getName().equalsIgnoreCase(productInShopList.favoriteProductOnCategory()));

        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);
        favoritesGalleryPage.waitForFavoriteGallerySpinnerToDisappear();

        //then
        assertTrue("Favorites Gallery is not populated with items.", favoritesGalleryPage.isFavoriteGalleryPopulatedWithItems());
    }
}
