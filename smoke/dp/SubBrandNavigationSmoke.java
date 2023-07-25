package com.wsgc.ecommerce.ui.smoke.dp;

import static org.junit.Assert.assertTrue;

import java.util.List;
import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.pagemodel.HomePage;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSectionFactory;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.ProductGroup;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tag.area.ShopArea;

/**
 * Smoke Test for Sub Brand Navigation
 */

@Category(ShopArea.class)
public class SubBrandNavigationSmoke extends AbstractTest {

	@Before
	public void setup() {
	    assumeFeatureIsSupported(FeatureFlag.SUB_BRAND);
	}
	
    /**
     * Verify if the user is able to navigate to the Sub Brand from the Global header.
     */
    @Test
	public void testNavigationWhenUserClicksOnSubBrandFromHeaderExpectSubBrandHomePage(){
        //given
        List<ProductGroup> productGroups = dataService.findProductGroups("sub-brand");
        HomePage homePage = context.getPage(HomePage.class);
        //when
        for (ProductGroup productGroup : productGroups) {
            startPilotAt(homePage);
            String expectedSubBrandUrl = productGroup.getUnmappedAttribute("subBrandUrl");
            NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
            navigationSection.navigateToBrand(expectedSubBrandUrl);
            String actualSubBrandUrl = context.getPilot().getDriver().getCurrentUrl();
            assertTrue("The user should be navigated to the Sub Brand", actualSubBrandUrl.contains(expectedSubBrandUrl));
            assertTrue("Sub Brand logo should be present", navigationSection.isBrandLogoPresent(expectedSubBrandUrl));            
        }
	}
}
