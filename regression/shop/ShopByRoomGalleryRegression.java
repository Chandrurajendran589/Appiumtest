package com.wsgc.ecommerce.ui.regression.shop;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.ProductGroup;

import tag.area.ShopArea;

/**
 * Regression Tests for validating quick look link in shop pages.
 */
@Category(ShopArea.class)
public class ShopByRoomGalleryRegression extends AbstractTest {
	
    /**
     * Prepares data fixtures & ensures feature flag is enabled.
     */
    @Before
    public void setup() {
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.SHOP_BY_ROOM_GALLERY));
    }

    /**
     * Verifies if the user is able to see the top navigation buttons along with title and 
     * description in Shop by room Gallery Page.
     * Test Case ID - RGSN-38192
     */
    @Test
    @TestRail(id = "108343")
    public void testTitleAndDescriptionTextWhenUserVisitsShopByRoomGalleryPageExpectTopNavButtonsWithTitleAndDescription() {

        // given
        final ProductGroup productGroup = dataService.findProductGroup("shop-by-room-gallery");
        final String expectedGalleryTitleText = productGroup.getUnmappedAttribute("titleText");
        final String expectedGalleryDescriptionText = productGroup.getUnmappedAttribute("descriptionText");
        
        // when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, (productGroup.getUnmappedAttribute("categoryUrl")));
        String actualGalleryTitleText = categoryPage.getGalleryPageTitle();
        String actualGalleryDescriptionText = categoryPage.getGalleryPageDescription();
        
        // then
        assertTrue("Gallery Navigation buttons in shop by room gallery is not displayed.", 
                categoryPage.isGalleryNavigationButtonsDisplayed());
        assertTrue("Shop by room Gallery Title is not same. Expected - " + expectedGalleryTitleText + 
                " Actual - " + actualGalleryTitleText, actualGalleryTitleText.equals(expectedGalleryTitleText));
        assertTrue("Shop by room Gallery Description is not same. Expected - " + expectedGalleryDescriptionText + 
                " Actual - " + actualGalleryDescriptionText, actualGalleryDescriptionText.equals(expectedGalleryDescriptionText));
    }
    
    /**
     * Verifies if the user is able to see the hotspots while hovering on gallery room image.
     * Test Case ID - RGSN-38192
     */
    @Test
    @TestRail(id = "108344")
    public void testHotspotsWhenUserHoverOverRoomImageExpectHotspotsAppear() {
    	
        // given
        final ProductGroup productGroup = dataService.findProductGroup("shop-by-room-gallery");
        
        // when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, (productGroup.getUnmappedAttribute("categoryUrl")));
        if (context.isFullSiteExperience()) {
            categoryPage.hoverRoomImage();
        } else {
            categoryPage.clickMobileHotspotToggle();
        }
        
        // then
        assertTrue("Hotspots in shop by room gallery is not displayed while hovering on room image.", 
                categoryPage.isHotspotsDisplayed());
    }
    
    /**
     * Verifies if the user is able to navigate to product page while click on product from product list carousel.
     * Test Case ID - RGSN-38192
     */
    @Test
    @TestRail(id = "108345")
    public void testProductPageWhenUserClicksProductFromCarouselExpectProductPage() {
    	
        // given
        final ProductGroup productGroup = dataService.findProductGroup("shop-by-room-gallery");
        
        // when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, (productGroup.getUnmappedAttribute("categoryUrl")));
        String productSku = productGroup.getSkus().get(0);
        categoryPage.clickProductNameFromCarousel(productSku);
        if (context.isFullSiteExperience()) {
            List<String> tabs = new ArrayList<String>(context.getPilot().getDriver().getWindowHandles());
            context.getPilot().getDriver().switchTo().window(tabs.get(1));
        }
        String currentPageUrl = context.getPilot().getDriver().getCurrentUrl();
        
        // then
        assertTrue("User is not redirected to the product page.", currentPageUrl.contains(productSku));
    }
    
    /**
     * Verifies if the Product image with product name and price details are displayed in product list carousel.
     * Test Case ID - RGSN-38192
     */
    @Test
    @TestRail(id = "108346")
    public void testProductTileWhenUserVisitsShopByRoomGalleryExpectProductImageWithNameAndPriceDetailsDisplayed() {
    	
        // given
        Assume.assumeTrue(context.isFullSiteExperience());
        final ProductGroup productGroup = dataService.findProductGroup("shop-by-room-gallery");
        
        // when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, (productGroup.getUnmappedAttribute("categoryUrl")));
        
        // then
        assertTrue("Product thumbnail image is not displayed in product carousel", categoryPage.isProductThumbnailPresentAndDisplayed());
        assertTrue("Product name details is not displayed in product carousel", categoryPage.isProductNamePresentAndDisplayed());
        assertTrue("Product price detais is not displayed in product carousel", categoryPage.isProductPricePresentAndDisplayed());
    }
    
    /**
     * Verifies if the shop by room gallery page title is updated while navigating 
     * to new gallery page from top navigation.
     * Test Case ID - RGSN-38192
     */
    @Test
    @TestRail(id = "108347")
    public void testGalleryPageTitleWhenUserNavigatesToNewGalleryPageExpectTitleIsUpdated() {
    	
        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.SHOP_BY_ROOM_GALLERY_TITLE_UPDATE));
        final ProductGroup productGroup = dataService.findProductGroup("shop-by-room-gallery");
        
        // when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, (productGroup.getUnmappedAttribute("categoryUrl")));
        int navigationsAvailable = categoryPage.getShopByRoomNavigationItems();
        int navigationIndex = RandomUtils.nextInt(1, navigationsAvailable);
        categoryPage.selectShopByRoomNavigation(navigationIndex);
        String expectedGalleryTitleText = categoryPage.getGalleryTitleFromTopNav(navigationIndex);
        String actualGalleryTitleText = categoryPage.getGalleryPageTitle();
        
        // then
        assertTrue("Shop by room Gallery Title is not same. Expected - " + expectedGalleryTitleText + 
                " Actual - " + actualGalleryTitleText, actualGalleryTitleText.equals(expectedGalleryTitleText));
    }
}
