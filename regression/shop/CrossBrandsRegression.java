package com.wsgc.ecommerce.ui.regression.shop;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSectionFactory;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.ProductGroup;

import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.WebElement;
import tag.area.ShopArea;

/**
 * Regression test for validating cross brands links on the site.
 */
@Category(ShopArea.class)
public class CrossBrandsRegression extends AbstractTest {

    /**
     * Verify if the user is able to navigate to the cross family brand from megamenu at shop page.
     * Test Case ID - RGSN-38174
     */
    @Test
    @TestRail(id = "100701")
    public void testSiteNavigationWhenUserClicksOnCrossFamilyBrandFromMegaMenuExpectCrossBrandHomePage() {

        // given
        Assume.assumeTrue((context.isFullSiteExperience()) && context.getTargetSite().supportsFeature(FeatureFlag.CROSS_BRAND_LINKS_ON_MEGA_MENU));
        final ProductGroup productGroup = dataService.findProductGroup("simple-pip");
        String categoryUrl = productGroup.getUnmappedAttribute("categoryUrl");
        String[] menuItemNames = productGroup.getUnmappedAttribute("categoryUrl").split("\\/");
        String EXPECTED_CROSS_BRAND_PARAM = "cm_ven=CrossBrandReferral";
        String currentUrl;

        // when
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, categoryUrl);
        categoryPage.waitForCategoryPageLoad();
        navigationSection.hoverMainCategoryMenuItem(menuItemNames[0]);
        List<WebElement> crossBrandMenuItemWebElementsList = navigationSection.getTotalCrossBrandLinksFromMegaMenu();
        
        //then
        for (int index = 1; index <= crossBrandMenuItemWebElementsList.size(); index++) {
            navigationSection.hoverMainCategoryMenuItem(menuItemNames[0]);
            navigationSection.clickCrossBrandLinkFromMegaMenu(index);
            String originalHandle = context.getPilot().getDriver().getWindowHandle();
            for (String handle : context.getPilot().getDriver().getWindowHandles()) {
               if (!handle.equals(originalHandle)) {
                   context.getPilot().getDriver().switchTo().window(handle);
                   currentUrl = context.getPilot().getDriver().getCurrentUrl();
                   assertTrue(String.format("The cross brand parameter %s is not present in the current page URL", EXPECTED_CROSS_BRAND_PARAM),
                           currentUrl.contains(EXPECTED_CROSS_BRAND_PARAM));
                   context.getPilot().getDriver().close();
               }
            }
            context.getPilot().getDriver().switchTo().window(originalHandle);
        }
    }
}
