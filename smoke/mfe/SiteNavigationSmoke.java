package com.wsgc.ecommerce.ui.smoke.mfe;

import static org.junit.Assert.assertTrue;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.HomePage;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSectionFactory;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryPage;
import com.wsgc.evergreen.entity.api.ProductGroup;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tag.area.ShopArea;

/**
 * Smoke Test for Site Navigation.
 */

@Category(ShopArea.class)
public class SiteNavigationSmoke extends AbstractTest {

    /**
     * Verify if the user is able to navigate to the super category and the sub category pages via category tile.
     */
    @Test
    public void testCategoryNavigationWhenNavigatesViaCategoryTileExpectCategoryPage() {
        //given
        final ProductGroup expectedProductGroup = dataService
                .findProductGroup(productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null,
                        "simple-pip", "attributes");
        String[] menuItemNames = expectedProductGroup.getUnmappedAttribute("categoryUrl").split("\\/");
        String categoryUrl = expectedProductGroup.getUnmappedAttribute("categoryUrl");
        
        //when
        startPilotAt(context.getPage(HomePage.class));
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        if (context.isFullSiteExperience()) {
            ShopFlows.navigateToSupercategoryPage(context, menuItemNames[0]);
        } else {
            ShopFlows.navigateToCategoryPage(context, categoryUrl);
        }
        CategoryPage categoryPage = navigationSection.navigateToCategoryPage(expectedProductGroup);
        Assert.assertTrue("The Current Category Page URL \'" +  categoryPage.getCurrentPageUrl() + "\' is not matching with expected Category Page URL\'"
                + categoryUrl + "\'", categoryPage.getCurrentPageUrl().contains(categoryUrl));
    }
    
    /**
     * Verify if the user is able to navigate to the super category and the sub category pages via Mega Menu.
     */
    @Test
    public void testCategoryNavigationWhenNavigatesViaMegaMenuExpectCategoryPage() {
        //given
        Assume.assumeTrue(context.isFullSiteExperience()); 
        final ProductGroup expectedProductGroup = dataService
                .findProductGroup(productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null,
                        "simple-pip", "attributes");
        String[] menuItemNames = expectedProductGroup.getUnmappedAttribute("categoryUrl").split("\\/");
        String categoryUrl = expectedProductGroup.getUnmappedAttribute("categoryUrl");
        
        //when
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, categoryUrl);
        categoryPage.waitForCategoryPageLoad();
        navigationSection.hoverMainCategoryMenuItem(menuItemNames[0]);
        navigationSection.clickSuperCategoryMenuItem(menuItemNames);
        Assert.assertTrue("The Current Page URL \'" +  categoryPage.getCurrentPageUrl() + "\' is not matching with expected Category Page URL\'"
                + categoryUrl + "\'", categoryPage.getCurrentPageUrl().contains(categoryUrl));
    }
    
    /**
     * Verify if the user is able to navigate to the pages through Secondary Navigation.
     */
    @Test
    public void testSecondaryNavigationWhenNavigatesViaGlobalHeaderExpectCategoryPage() {
        //given
        Assume.assumeTrue(context.isFullSiteExperience()); 
        final ProductGroup expectedProductGroup = dataService.findProductGroup(productGroup -> productGroup.getUnmappedAttribute("secondaryNavUrl") != null);
        String categoryUrl = expectedProductGroup.getUnmappedAttribute("categoryUrl");
        ProductGroup productGroup = dataService.findProductGroup("secondary-nav");
        
        //when
        startPilotAt(context.getPage(HomePage.class));
        ShopFlows.navigateToCategoryPage(context, categoryUrl);
        String secondaryNavUrl = productGroup.getUnmappedAttribute("secondaryNavUrl");
        String secNavTitle = productGroup.getUnmappedAttribute("secondaryNavTitle");
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        navigationSection.hoverSecondaryNavigationMainMenuItem(secNavTitle);
        navigationSection.clickSecondaryNaviagtionMenuItem(secondaryNavUrl);
        String currentUrl = context.getPilot().getDriver().getCurrentUrl();
        //then
        assertTrue("The user should be navigated to the Brand page via Secondary Navigation", currentUrl.contains(secondaryNavUrl));
    }
}
