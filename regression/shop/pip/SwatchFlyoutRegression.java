package com.wsgc.ecommerce.ui.regression.shop.pip;
import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.registry.GuidedPipFlows;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.ProductGroup;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tag.area.ShopArea;
import static org.junit.Assert.assertTrue;

/**
 * Regression Tests for Order Free Swatch Flyout related to Guided PIPs on Shop page.
 */
@Category(ShopArea.class)
public class SwatchFlyoutRegression extends AbstractTest {

    @Before
    public void setup() {
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP)
                || context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP_REDESIGN));
    }

    /**
     * Verifies whether Order free swatch is displayed when user clicks on Order free
     * swatch link on the swatch details pop down on product page.
     * Test Case ID - RGSN-38683
     */
    @Test
    @TestRail(id = "104365")
    public void testSwatchFlyOutWhenFreeOrderSwatchLinkOnSwatchPopDownIsClickedExpectSwatchFlyOutIsDisplayed() {
        
        //given
        final int expectedQty = 1;
        ProductGroup expectedProductGroup = dataService.findProductGroup("guided-pip", "swatch-selection");

        //when
        ProductPage pip = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        Assume.assumeTrue("PIP-mfe or Desktop view is not present" , pip.isPipMfePresent() && context.isFullSiteExperience());
        GuidedPipFlows.setGuidedPipQuantityAndAttributes(context, String.valueOf(expectedQty));
        pip.clickSelectedSwatch();
        pip.clickOrderSwatchButtonOnSwatchDetails();

        //then
        boolean isFlyoutModalDisplayed = pip.isOrderFreeSwatchFlyoutDisplayed();
        assertTrue(String.format("Header of the Order Free swatch FlyOut is not displayed : %s", isFlyoutModalDisplayed), isFlyoutModalDisplayed);
        boolean isColorFilterDisplayed = pip.isColorFilterDisplayedInOrderSwatchFlyout();
        assertTrue(String.format("Color Filter of Order Free swatch FlyOut is not displayed : %s", isColorFilterDisplayed), isColorFilterDisplayed);
        boolean isFabricFilterDisplayed = pip.isFabricFilterDisplayedInOrderSwatchFlyout();
        assertTrue(String.format("Fabric Filter of Order Free swatch FlyOut is not displayed : %s", isFabricFilterDisplayed), isFabricFilterDisplayed);
    }

    /**
     * Verifies whether Order free swatch is displayed when user clicks on Order free swatch button at product page.
     * Test Case ID - RGSN-38683
     */
    @Test
    @TestRail(id = "104364")
    public void testSwatchFlyOutWhenFreeOrderSwatchButtonIsClickedExpectSwatchFlyOutIsDisplayed() {
        
        //given
        final int expectedQty = 1;
        ProductGroup expectedProductGroup = dataService.findProductGroup("guided-pip", "swatch-selection");

        //when
        ProductPage pip = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        Assume.assumeTrue ("PIP-mfe is not present", pip.isPipMfePresent());
        GuidedPipFlows.setGuidedPipQuantityAndAttributes(context, String.valueOf(expectedQty));
        pip.clickOrderFreeSwatchButton();

        //then
        boolean isFlyoutModalDisplayed = pip.isOrderFreeSwatchFlyoutDisplayed();
        assertTrue(String.format("Header of the Order Free swatch FlyOut is not displayed : %s", isFlyoutModalDisplayed), isFlyoutModalDisplayed);
        boolean isColorFilterDisplayed = pip.isColorFilterDisplayedInOrderSwatchFlyout();
        assertTrue(String.format("Color Filter of Order Free swatch FlyOut is not displayed : %s", isColorFilterDisplayed), isColorFilterDisplayed);
        boolean isFabricFilterDisplayed = pip.isFabricFilterDisplayedInOrderSwatchFlyout();
        assertTrue(String.format("Fabric Filter of Order Free swatch FlyOut is not displayed : %s", isFabricFilterDisplayed), isFabricFilterDisplayed);
    }
}
