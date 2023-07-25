package com.wsgc.ecommerce.ui.regression.shop;

import static org.junit.Assert.assertTrue;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.HomePage;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSectionFactory;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.ProductGroup;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 * Regression Tests to validate Spotlight section on mega menu.
 *
 * @author smanjunath
 */
public class SpotlightSectionRegression extends AbstractTest {

    /**
     * Verifies the spotlight section on the megamenu and navigates to the category page highlighted in the spotlight.
     * Test Case ID - RGSN-38176, RGSN-38191
     */
    @Test
    @TestRail(id = "99791")
    public void testSpotlightSectionWhenUserClicksOnSpotlightInMegaMenuExpectCategoryPage() {

       //given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.SPOTLIGHT)
                && context.isFullSiteExperience());
       final ProductGroup expectedProductGroup = dataService
              .findProductGroup(productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null,
                         "favorites");
       String productGroup = expectedProductGroup.getUnmappedAttribute("categoryUrl");
       String[] menuItemNames = expectedProductGroup.getUnmappedAttribute("categoryUrl").split("\\/");
       
       // when
       NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
       CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, productGroup);   
       categoryPage.waitForCategoryPageLoad();
       navigationSection.hoverMainCategoryMenuItem(menuItemNames[0]);
       assertTrue("Spotlight section is not in Megamenu.", navigationSection.isSpotLightSectionPresentAndDisplayed());
       String categoryPageUrlFromSpotLightSection = navigationSection.getUrlOfCategoryPageInSpotLight();
       navigationSection.clickSpotLightSection();
       
       //then
       String currentPageUrl = context.getPilot().getDriver().getCurrentUrl();
       Assert.assertEquals("Category page URL from spot light section '" + categoryPageUrlFromSpotLightSection + "' is not matching with '"
               + currentPageUrl + "' of Category page URL ", categoryPageUrlFromSpotLightSection, currentPageUrl);
    }

    /**
     * Verifies the spotlight section on the Mega Menu and navigates to the category page highlighted in the spotlight.
     * Test Case ID - RGSN-38786
     */
    @Test
    @TestRail(id = "104532")
    public void testSpotlightImagesWhenUserHoverOnMegaMenuExpectSpotlightImages() {

        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.SPOTLIGHT)
                && context.isFullSiteExperience());
        final ProductGroup expectedProductGroup = dataService
                .findProductGroup(productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null,
                        "simple-pip", "!attributes");
        String[] menuItemNames = expectedProductGroup.getUnmappedAttribute("categoryUrl").split("\\/");

        // when
        startPilotAt(context.getPage(HomePage.class));
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        navigationSection.hoverMainCategoryMenuItem(menuItemNames[0]);

        // then
        assertTrue("Spotlight Images are not shown in Mega Menu.", navigationSection.isSpotLightImagePresentAndDisplayed(menuItemNames[0]));
    }
}
