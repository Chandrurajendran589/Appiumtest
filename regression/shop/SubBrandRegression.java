package com.wsgc.ecommerce.ui.regression.shop;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSectionFactory;
import com.wsgc.ecommerce.ui.pagemodel.favorites.FavoritesGalleryPage;
import com.wsgc.ecommerce.ui.pagemodel.favorites.FavoritesItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.ProductGroup;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tag.area.ShopArea;

/**
 * Regression Tests for Sub Brand Navigation.
 */
@Category(ShopArea.class)
public class SubBrandRegression extends AbstractTest {

    @Before
    public void setup() {
        assumeFeatureIsSupported(FeatureFlag.SUB_BRAND);
    }

    /**
     * Verify if the user is able to navigate to the Sub-Brand from the Global header.
     * Test Case ID - RGSN-38217, RGSN-38195
     */
    @Test
    @TestRail(id = "100710")
    public void testNavigationWhenUserClicksOnSubBrandFromHeaderExpectSubBrandHomePage() {

        // given
        final ProductGroup expectedProductGroup = dataService.findProductGroup("favorites");
        final ProductGroup productGroup = dataService.findProductGroup("sub-brand");
        String expectedSubBrandUrl = productGroup.getUnmappedAttribute("subBrandUrl");

        // when
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        ShopFlows.navigateToCategoryPage(context, expectedProductGroup.getUnmappedAttribute("categoryUrl"));
        navigationSection.navigateToBrand(expectedSubBrandUrl);
        String actualSubBrandUrl = context.getPilot().getDriver().getCurrentUrl();

        // then
        assertTrue("The user is not navigated to the Sub Brand", actualSubBrandUrl.contains(expectedSubBrandUrl));
        assertTrue("Sub Brand logo is not present", navigationSection.isBrandLogoPresent(expectedSubBrandUrl));
    }

    /**
     * Verifies if a guest user is able to navigate to the Sub-Brand Product page from the Favorites Gallery
     * after adding an item to favorites.
     * Test Case ID - RGSN-38217
     */
    @Test
    @TestRail(id = "100711")
    public void testProductPageWhenUserClicksOnAnItemFromFavoritesGalleryExpectSubBrandLogo() {

        // given
        final ProductGroup productGroup = dataService.findProductGroup("sub-brand");
        String expectedSubBrandUrl = productGroup.getUnmappedAttribute("subBrandUrl");

        // when
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(
                context, productGroup.getUnmappedAttribute("categoryUrl"));
        CategoryItemRepeatableSection productInShopList = categoryPage
                .getProductSectionByGroupId(productGroup.getGroupId());
        assertTrue("Item is not added to favorites", productGroup.getName().equalsIgnoreCase(
                productInShopList.favoriteProductOnCategory()));
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);
        assertTrue("Favorites Gallery is not populated with items.", favoritesGalleryPage.isFavoriteGalleryPopulatedWithItems());
        FavoritesItemRepeatableSection favoritesItemRepeatableSection = favoritesGalleryPage
                .getProductSectionByGroupId(productGroup.getGroupId());
        favoritesItemRepeatableSection.clickProductName();
        String actualSubBrandProductPageUrl = context.getPilot().getDriver().getCurrentUrl();

        // then
        assertTrue("The user is not navigated to the Sub Brand product page", 
                actualSubBrandProductPageUrl.contains(productGroup.getGroupId()));
        assertTrue("Sub Brand logo is not present on Product Page", 
                navigationSection.isBrandLogoPresent(expectedSubBrandUrl));
    }

    /**
     * Verify whether My Store module is suppressed in Sub-Brand's Global Header.
     * Test Case ID - RGSN-38195
     */
    @Test
    @TestRail(id = "100712")
    public void testGlobalHeaderWhenUserNavigatesToCategoryPageExpectMyStoreSuppressed() {

        //given
        ProductGroup productGroup = dataService.findProductGroup("sub-brand");
        String expectedSubBrandUrl = productGroup.getUnmappedAttribute("categoryUrl");

        //when
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, expectedSubBrandUrl);
        categoryPage.waitForCategoryPageLoad();

        //then
        assertFalse("My Store information is displayed in global header.", navigationSection.isMyStorePresentAndDisplayed());
    }

    /**
     * Verify whether user is able to navigate to a category page of a Sub-Brand.
     * Test Case ID - RGSN-38195
     */
    @Test
    @TestRail(id = "100713")
    public void testCategoryPageNavigationWhenUserNavigatesFromMenuExpectCategoryPage() {

        //given
        ProductGroup productGroup = dataService.findProductGroup("sub-brand");
        String categoryUrl = productGroup.getUnmappedAttribute("categoryUrl");
        String menuItemNames = productGroup.getUnmappedAttribute("categoryUrl");

        //when
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, categoryUrl);
        categoryPage.waitForCategoryPageLoad();
        if (context.isFullSiteExperience()) {
            navigationSection.hoverMainCategoryMenuItem(menuItemNames.split("\\/")[0]);
            navigationSection.navigateToCategoryPage(menuItemNames);
        } else {
            navigationSection.navigateToCategoryPage(productGroup);
        }
        String currentPageUrl = context.getPilot().getDriver().getCurrentUrl();

        //then
        assertTrue("User is not navigated to category Page", currentPageUrl.contains(categoryUrl));
    }

    /**
     * Verify whether user is able to navigate to a product page of Sub-Brand via category navigation.
     * Test Case ID - RGSN-38195
     */
    @Test
    @TestRail(id = "100714")
    public void testSiteNavigationWhenUserClicksOnProductFromCategoryPageExpectProductPage() {

        //given
        ProductGroup productGroup = dataService.findProductGroup("sub-brand");
        String subBrandcategoryUrl = productGroup.getUnmappedAttribute("categoryUrl");

        //when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, subBrandcategoryUrl);
        categoryPage.waitForCategoryPageLoad();
        categoryPage.clickProductOnCategoryPage(productGroup);
        String currentPageUrl = context.getPilot().getDriver().getCurrentUrl();

        //then
        assertTrue("The user is not navigated to the product page.", currentPageUrl.contains(productGroup.getGroupId()));       
    }

    /**
     * Verify user is able to navigate to Main brand home page from Sub-Brand product page.
     * Test Case ID - RGSN-38195
     */
    @Test
    @TestRail(id = "100730")
    public void testSiteNavigationWhenUserClicksMainBrandFromGlobalHeaderExpectBrandLogoInHomePage() {

        //given
        ProductGroup productGroup = dataService.findProductGroup("sub-brand");
        String mainBrandUrl = context.getBaseUrl().getUrlAsString();

        //when
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        ShopFlows.goToPipPageFlow(context, productGroup);
        navigationSection.navigateToBrand(mainBrandUrl);
        if (mainBrandUrl.contains(":") && mainBrandUrl.contains("@")) {
            mainBrandUrl = mainBrandUrl.substring(0, 8) + mainBrandUrl.substring(mainBrandUrl.indexOf('@') + 1, mainBrandUrl.length());
        }

        //then
        assertTrue("Brand logo is not present in Homepage", navigationSection.isBrandLogoPresent(mainBrandUrl));
    }
}
