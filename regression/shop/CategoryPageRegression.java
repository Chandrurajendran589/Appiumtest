package com.wsgc.ecommerce.ui.regression.shop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import com.wsgc.ecommerce.ui.pagemodel.i18n.InternationalShippingOverlay;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.shop.PipFlows;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSectionFactory;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.RelatedProductsSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.RichAddToCartOverlaySection;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartHeaderRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.ProductGroup;

import tag.area.ShopArea;

/**
 * Regression Tests for validating components of a category page.
 */
@Category(ShopArea.class)
public class CategoryPageRegression extends AbstractTest {

    /**
     * Verifies whether a user is taken to the top of the category page on clicking the back to top icon.
     * Test Case ID - RGSN-38193
     */
    @Test
    @TestRail(id = "99811")
    public void testCategoryPageWhenUserClickBackToTopIconExpectGlobalHeader() {

        // given
        final ProductGroup expectedProductGroup = dataService
                .findProductGroup(productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null,
                                  "simple-pip", "!attributes", "!monogrammable");

        // when
        CategoryPage productCategoryPage = ShopFlows.navigateToCategoryPage(context, expectedProductGroup.getUnmappedAttribute("categoryUrl"));
        productCategoryPage.waitForCategoryPageLoad();
        productCategoryPage.getNumberOfProductsLoaded();
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);

        //then
        if (context.isMobileExperience()) {
            Actions builder = new Actions(context.getPilot().getDriver());
            builder.sendKeys(Keys.PAGE_UP).build().perform();
        }
        assertTrue("Sticky Header should be displayed.", navigationSection.isStickyGlobalHeaderDisplayed());
        productCategoryPage.clickBackToTop();
        assertFalse("Sticky Global Header is displayed instead of regular global header.", navigationSection.isStickyGlobalHeaderDisplayed());
    }
    
    /**
     * Verifies whether a user is taken to the super category page when they click on the super category bread crumbs.
     * Test Case ID - RGSN-38193
     */
    @Test
    @TestRail(id = "99812")
    public void testCategoryPageWhenUserClicksBreadCrumbsExpectSuperCategoryPage() {

        // given
        final ProductGroup expectedProductGroup = dataService
                .findProductGroup(productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null,
                                  "simple-pip", "!attributes", "!monogrammable");
        String[] menuItemNames = expectedProductGroup.getUnmappedAttribute("categoryUrl").split("\\/");

        // when
        CategoryPage productCategoryPage = ShopFlows.navigateToCategoryPage(context, expectedProductGroup.getUnmappedAttribute("categoryUrl"));
        productCategoryPage.waitForCategoryPageLoad();
        String expectedSuperCategoryPageUrl = productCategoryPage.getSuperCategoryUrlFromBreadCrumb(menuItemNames[0]);
        if (context.isMobileExperience()) {
            expectedSuperCategoryPageUrl = expectedSuperCategoryPageUrl.replace("/m/", "/");
        }
        productCategoryPage.clickSuperCategoryBreadCrumb(menuItemNames[0]);
        String currentPageUrl = context.getPilot().getDriver().getCurrentUrl();

        //then
        Assert.assertEquals("Super Category page URL from breadcrumb '" + expectedSuperCategoryPageUrl + "' is not matching with '"
                + currentPageUrl + "' of Category page URL ", expectedSuperCategoryPageUrl, currentPageUrl);
    }
    
    /**
     * Verify the user is able to navigate to a Simple MFE PIP via category navigation.
     * Test Case ID - RGSN-38205, RGSN-38186, RGSN-38230
     */
    @Test
    @TestRail(id = "99813")
    public void testNavigationWhenUserClicksOnSimplePipFromCategoryPageExpectProductPage() {
        //given
        final ProductGroup expectedProductGroup = dataService.findProductGroup(
                 productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null, "secondary-nav");

        //when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, expectedProductGroup.getUnmappedAttribute("categoryUrl"));
        categoryPage.waitForCategoryPageLoad();
        ProductPage productPage = categoryPage.clickProductOnCategoryPage(expectedProductGroup);
        String productPageUrl = productPage.getCurrentPageUrl();
        //then
        Assert.assertTrue("Product Page is not displayed", productPageUrl.contains(productPage.getExpectedUrlPath()));
    }
    
    /**
     * Verify the user is able to navigate to a Multibuy MFE PIP via category navigation.
     * Test Case ID - RGSN-38205, RGSN-38186
     */
    @Test
    @TestRail(id = "100052")
    public void testNavigationWhenUserClicksOnMultiBuyPipFromCategoryPageExpectProductPage() {
        //given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.MULTI_BUY_PIP));
        final ProductGroup expectedProductGroup = dataService.findProductGroup(
                productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null, "multi-buy-multi-sku-pip");
        
        //when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, expectedProductGroup.getUnmappedAttribute("categoryUrl"));
        categoryPage.waitForCategoryPageLoad();
        ProductPage productPage = categoryPage.clickProductOnCategoryPage(expectedProductGroup);
        String productPageUrl = productPage.getCurrentPageUrl();
        //then
        Assert.assertTrue("Product Page is not displayed", productPageUrl.contains(productPage.getExpectedUrlPath()));
    }
     
    /**
     * Verify the user is able to navigate to a Guided MFE PIP via category navigation.
     * Test Case ID - RGSN-38205, RGSN-38186
     */
    @Test
    @TestRail(id = "100053")
    public void testNavigationWhenUserClicksOnGuidedPipFromCategoryPageExpectProductPage() {
        //given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP) 
                || context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP_REDESIGN));
        final ProductGroup expectedProductGroup = dataService.findProductGroup(
                productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null, "guided-pip");
        
        //when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, expectedProductGroup.getUnmappedAttribute("categoryUrl"));
        categoryPage.waitForCategoryPageLoad();
        ProductPage productPage = categoryPage.clickProductOnCategoryPage(expectedProductGroup);
        String productPageUrl = productPage.getCurrentPageUrl();
        //then
        Assert.assertTrue("Product Page is not displayed", productPageUrl.contains(productPage.getExpectedUrlPath()));
    }
    
    /**
     * Verify the user is able to navigate to multiple PIPs via category navigation.
     * Test Case ID - RGSN-38205, RGSN-38186
     */
    @Test
    @TestRail(id = "100054")
    public void testNavigationWhenUserClicksOnMultiplePipsFromCategoryPageExpectProductPage() {
        //given
        final ProductGroup expectedProductGroup = dataService.findProductGroup(productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null);

        //when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, expectedProductGroup.getUnmappedAttribute("categoryUrl"));
        categoryPage.waitForCategoryPageLoad();
        int numberOfproductCategoryItems = categoryPage.getProductCategoryItems().size();
        List<Integer> productIndices = new ArrayList<>();
        productIndices.add(RandomUtils.nextInt(1, numberOfproductCategoryItems));
        productIndices.add(RandomUtils.nextInt(1, numberOfproductCategoryItems));
        for (int productIndex : productIndices) {
            CategoryItemRepeatableSection categoryItemRepeatableSection = categoryPage.getCategoryItemByIndex(productIndex);
            String productPageUrlFromCategoryPage = categoryItemRepeatableSection.getProductUrlFromCategoryPage();
            ProductPage productPage = categoryItemRepeatableSection.clickProductImage();
            String productPageUrl = productPage.getCurrentPageUrl();
        
            //then
            Assert.assertTrue("Product URL from Category Page \'" + productPageUrlFromCategoryPage + "\' is not matching with \'"
                       + productPageUrl + "\' of PIP page URL ", productPageUrl.contains(productPageUrlFromCategoryPage.split("/products")[1]));
            context.getPilot().getDriver().navigate().back();
            categoryPage.waitForCategoryPageLoad();
        }
    }
    
    /**
     * Verifies that on a resized browser window at (990px), the shopper is able to navigate to a Category
     * page. The customer then selects a product from the
     * Category Page to land on the PIP (Product Information Page) and successfully
     * add the product to the Cart.
     * Test Case ID - RGSN-38208
     */
    @Test
    @TestRail(id = "100698")
    public void testAddToCartFromCategoryPageForTheResizedBrowserWindowExpectCartHasProduct() {
        // given
        assumeFeatureIsSupported(FeatureFlag.RESPONSIVE_CATEGORY_PAGE);
        Dimension dimension = new Dimension(990, 763);
        context.getPilot().getDriver().manage().window().setSize(dimension);
        final int itemIndex = 0;
        final int expectedTotalItem = 1;
        final int expectedQuantity = 1;
        final ProductGroup expectedProductGroup = dataService.findProductGroup(productGroup -> productGroup.
                getUnmappedAttribute("categoryUrl") != null, "simple-pip", "attributes");
        final String expectedSku = expectedProductGroup.getSkus().get(itemIndex);
        // when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, expectedProductGroup.getUnmappedAttribute("categoryUrl"));
        categoryPage.waitForCategoryPageLoad();
        ProductPage productPage = categoryPage.clickProductOnCategoryPage(expectedProductGroup);
        PipFlows.setSimplePipQuantityAndAttributes(productPage, String.valueOf(expectedQuantity));
        RichAddToCartOverlaySection overlaySection = productPage.addToCart();
        ShoppingCartPage shoppingCartPage = overlaySection.goToShoppingCartPage();
        shoppingCartPage.scrollIntoCartSection();
        // then
        List<ShoppingCartItemRepeatableSection> itemSectionList = shoppingCartPage.getShoppingCartItems();
        assertEquals(String.format("The shopping cart should have %s item.", expectedTotalItem), expectedTotalItem,
                     itemSectionList.size());
        ShoppingCartItemRepeatableSection item = shoppingCartPage.getFirstShoppingCartItem();
        assertTrue(String.format("The shopping cart item's sku should ends with '%s'. Actual sku: '%s'", expectedSku,
                                 item.getSku()), item.getSku().endsWith(expectedSku));
        assertEquals(String.format("The shopping cart item's quantity should be '%s'", expectedQuantity),
                     String.valueOf(expectedQuantity), item.getQuantity());
        List<ShoppingCartHeaderRepeatableSection> headerSectionList = shoppingCartPage.getSuborderHeadersOnCart();
        assertEquals(String.format("The shopping cart should have %s header.", expectedTotalItem), expectedTotalItem,
                     headerSectionList.size());
    }
    
    /**
     * Verifies whether a user is able to navigate to the product page via
     * the Related Product's section on the category pages.
     * Test case ID - RGSN-38183
     */
    @Test
    @TestRail(id = "100699")
    public void testCategoryPageWhenUserClicksRelatedProductExpectProductPage() {

        //given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.RELATED_PRODUCTS));
        final ProductGroup expectedProductGroup = dataService.findProductGroup(
                productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null, "related-products");

        //when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, expectedProductGroup.getUnmappedAttribute("categoryUrl"));
        categoryPage.waitForCategoryPageLoad();
        RelatedProductsSection relatedProductSection = context.getPageSection(RelatedProductsSection.class);
        int relatedProductsAvailable = relatedProductSection.getRelatedProductsCount();
        int relatedProductIndex = RandomUtils.nextInt(1, relatedProductsAvailable);
        String expectedProductPageUrl = relatedProductSection.getRelatedProductUrl(relatedProductIndex);
        relatedProductSection.clickRelatedProduct(relatedProductIndex);
        String actualProductPageUrl = context.getPilot().getDriver().getCurrentUrl();

        //then
        Assert.assertEquals("Product Page URL from Related Product \'" + expectedProductPageUrl + "\' is not matching with \'"
                + actualProductPageUrl + "\' of Product Page URL ", expectedProductPageUrl, actualProductPageUrl);
    }

    /**
     * Verifies whether a user is able to see the Related Product's section 
     * on the category pages when navigates back from PIP.
     * Test case ID - RGSN-38183
     */
    @Test
    @TestRail(id = "100700")
    public void testCategoryPageWhenUserNavigatesFromPipExpectRelatedProductsSection() {
        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.RELATED_PRODUCTS));
        final ProductGroup expectedProductGroup = dataService.findProductGroup(
                productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null, "related-products");

        //when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, expectedProductGroup.getUnmappedAttribute("categoryUrl"));
        categoryPage.waitForCategoryPageLoad();
        RelatedProductsSection relatedProductSection = context.getPageSection(RelatedProductsSection.class);
        int expectedRelatedProducts = relatedProductSection.getRelatedProductsCount();
        int relatedProductIndex = RandomUtils.nextInt(1, expectedRelatedProducts);
        relatedProductSection.clickRelatedProduct(relatedProductIndex);
        context.getPilot().getDriver().navigate().back();
        int actualRelatedProducts = relatedProductSection.getRelatedProductsCount();

        //then
        Assert.assertEquals("Expected count of Related Products \'" + expectedRelatedProducts + "\'"
                + " before navigating to Product page is not matching with \'"
                + actualRelatedProducts + "\' actual count of Related Products ", expectedRelatedProducts, actualRelatedProducts);
    }

    /**
     * Verifies whether a user is navigated to the sale category page from sale category menu.
     * Test Case ID - RGSN-38202
     */
    @Test
    @TestRail(id = "99806")
    public void testCategoryPageWhenUserClickSalesCategoryFromMenuExpectSaleCategoryPage() {

        // given
        final ProductGroup expectedProductGroup = dataService.findProductGroup("simple-pip", "!attributes", "!monogrammable");
        final ProductGroup productGroup = dataService.findProductGroup("sale-category");
        final String expectedCategoryPageUrl = productGroup.getUnmappedAttribute("categoryUrl");

        // when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, expectedProductGroup.getUnmappedAttribute("categoryUrl"));
        categoryPage.waitForCategoryPageLoad();
        String menuItemNames = productGroup.getUnmappedAttribute("categoryUrl");
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        if (context.isFullSiteExperience()) {
            navigationSection.hoverMainCategoryMenuItem(menuItemNames.split("\\/")[0]);
            navigationSection.navigateToCategoryPage(menuItemNames);
        } else {
            navigationSection.navigateToCategoryPage(productGroup);
        }
        String actualCategoryPageUrl = context.getPilot().getDriver().getCurrentUrl();

        //then
        Assert.assertTrue("Expected Category page URL '" + expectedCategoryPageUrl + "' is not present in'"
                + actualCategoryPageUrl + "' of Category page URL ", actualCategoryPageUrl.contains(expectedCategoryPageUrl));
    }
    
    /**
     * Verifies In-stock toggle is displayed and persistent after navigating
     * from Product page to category page.
     * Test Case ID - RGSN-38185
     */
    @Test
    @TestRail(id = "99803")
    public void testInStockTogglePersistenceWhenUserNavigatesBackToCategoryPageFromProductPageExpectInStockToggleEnabled() {

        Assume.assumeTrue(context.supportsFeature(FeatureFlag.IN_STOCK_TOGGLE));

        //given
        final ProductGroup expectedProductGroup = dataService.findProductGroup("simple-pip", "attributes");
        String productCategoryUrl = expectedProductGroup.getUnmappedAttribute("categoryUrl");

        //when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, productCategoryUrl);
        categoryPage.waitForCategoryPageLoad();
        assertTrue("In Stock Toggle is not displayed in shop page", categoryPage.isInStockToggleDisplayed());

        categoryPage.clickInStockToggle();
        categoryPage.waitForCategoryPageLoad();
        int productCountAfterFacetSelection = categoryPage.getNumberOfProductsLoaded();
        int productCountFromProductCountMessage = categoryPage.getProductCountFromCategoryPage();
        assertEquals("Products are not filtered after selecting toggle", productCountFromProductCountMessage, productCountAfterFacetSelection);

        categoryPage.clickProductOnCategoryPage(expectedProductGroup);
        context.getPilot().getDriver().navigate().back();
        categoryPage.waitForCategoryPageLoad();
        int productCountAfterVisitingProductPage = categoryPage.getNumberOfProductsLoaded();

        //then
        assertEquals("Product Count is not same after visiting pip page", productCountAfterFacetSelection, productCountAfterVisitingProductPage);
        assertTrue("In Stock Toggle is not enabled in shop page", categoryPage.isInstockToggleEnabled());
    }

    /**
     * Verifies user is able to switch off the In-stock toggle in category page.
     * Test Case ID - RGSN-38185
     */
    @Test
    @TestRail(id = "99805")
    public void testCategoryPageWhenUserSwitchesOffInstockToggleExpectAllProductsLoaded() {

        Assume.assumeTrue(context.supportsFeature(FeatureFlag.IN_STOCK_TOGGLE));

        //given
        final ProductGroup expectedProductGroup = dataService.findProductGroup("simple-pip", "attributes");
        String productCategoryUrl = expectedProductGroup.getUnmappedAttribute("categoryUrl");

        //when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, productCategoryUrl);
        categoryPage.waitForCategoryPageLoad();
        int expectedProductCountBeforeFacetSelection = categoryPage.getProductCountFromCategoryPage();
        assertTrue("In Stock Toggle is not displayed in shop page", categoryPage.isInStockToggleDisplayed());
        categoryPage.clickInStockToggle();
        assertTrue("In Stock Toggle is disabled in shop page", categoryPage.isInstockToggleEnabled());
        int actualProductCountAfterFacetSelection = categoryPage.getNumberOfProductsLoaded();
        int productCountFromProductCountMessage = categoryPage.getProductCountFromCategoryPage();
        assertEquals(String.format("Products are not filtered after selecting toggle - expected = '%s', actual = '%s'", 
        		productCountFromProductCountMessage, actualProductCountAfterFacetSelection), 
        		productCountFromProductCountMessage, actualProductCountAfterFacetSelection);
        categoryPage.clickBackToTop();
        categoryPage.clickInStockToggle();
        assertFalse("In Stock Toggle is enabled in shop page", categoryPage.isInstockToggleEnabled());
        int actualProductCount = categoryPage.getNumberOfProductsLoaded();
        assertEquals(String.format("All Products are not loaded after un-selecting toggle - expected = '%s', actual = '%s'", 
                expectedProductCountBeforeFacetSelection, actualProductCount), expectedProductCountBeforeFacetSelection, actualProductCount);
    }

    /**
     * Verifies whether product price is converted to selected international country's
     * currency in shop page.
     * Test Case ID - RGSN-38182
     */
    @Test
    @TestRail(id = "105263")
    public void testCategoryPageWhenUserSelectsInternationalCountryExpectCurrencyConversion() {

        // given
        Assume.assumeTrue(context.isFullSiteExperience());
        final ProductGroup productGroup = dataService.findProductGroup("simple-pip", "!attributes");
        String categoryUrl = productGroup.getUnmappedAttribute("categoryUrl");

        // when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, categoryUrl);
        NavigationSection navigationSection = context.getPageSection(NavigationSection.class);
        InternationalShippingOverlay internationalShippingOverlay = navigationSection.goToInternationalShippingOverlay();
        int countryOptionsAvailable = internationalShippingOverlay.getNonUsDollarInternationalShippingCountries();
        int countryNameIndex = RandomUtils.nextInt(1, countryOptionsAvailable);
        internationalShippingOverlay.selectNonUsDollarCurrencyInternationalCountry(countryNameIndex);
        String expectedCurrencyCode = internationalShippingOverlay.getCurrencyCodeFromInternationalShippingOverlay(countryNameIndex);
        internationalShippingOverlay.clickUpdateCurrencyAndCountryButton();
        categoryPage.waitForCategoryPageLoad();
        String actualCurrencyCode = categoryPage.getInternationalCurrencyCode();

        // then
        Assert.assertEquals("Expected currency code \'" + expectedCurrencyCode + "\'" + "is not matching with actual currency code \'"
                + actualCurrencyCode + "\'", expectedCurrencyCode, actualCurrencyCode);
    }


}
