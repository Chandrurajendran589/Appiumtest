package com.wsgc.ecommerce.ui.regression.shop;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSectionFactory;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.NonCategoryTree;
import com.wsgc.evergreen.entity.api.ProductGroup;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tag.area.ShopArea;

/**
 * Regression Tests for validating components of a non category page.
 */
@Category(ShopArea.class)
public class NonCategoryPageRegression extends AbstractTest {

    /**
     * Verifies whether a user is able to navigate to a non-shopping page from the Hamburger menu.
     * Test Case ID - RGSN-38207
     */
    @Test
    @TestRail(id = "100715")
    public void testSiteNavigationWhenUserClicksNonShoppingPageFromHamburgerMenuExpectNonShoppingPage() {

        Assume.assumeTrue(context.isMobileExperience() 
                 && context.getTargetSite().supportsFeature(FeatureFlag.NON_CATEGORY_PAGE));
        // given
        final ProductGroup expectedProductGroup = dataService
                .findProductGroup(productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null);
        final NonCategoryTree nonCategoryTree = dataService.findNonCategoryTree("noncategory-navigation");
        final String expectedNonCategoryPageUrl = nonCategoryTree.getUrlPathSegment();

        // when
        CategoryPage productCategoryPage = ShopFlows.navigateToCategoryPage(context, expectedProductGroup.getUnmappedAttribute("categoryUrl"));
        productCategoryPage.waitForCategoryPageLoad();
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        navigationSection.navigateToNonCategoryPage(expectedNonCategoryPageUrl);
        String actualNonCategoryPageUrl = context.getPilot().getDriver().getCurrentUrl();

        //then
        Assert.assertTrue("Expected Non Category page URL '" + expectedNonCategoryPageUrl + "' is not present in actual '"
                + actualNonCategoryPageUrl + "' of Non Category page URL ", actualNonCategoryPageUrl.contains(expectedNonCategoryPageUrl));
    }
}
