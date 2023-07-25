package com.wsgc.ecommerce.ui.regression.shop.pip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.pip.OrderFreeSwatchFlyout;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.ProductGroup;

import groovy.lang.Category;
import tag.area.ShopArea;

/**
 * Regression Tests for validating the order free swatch filter in Guided
 * PIPs.
 * 
 * @author dkang
 *
 */
@Category(ShopArea.class)
public class GuidedPipWithOrderFreeSwatchFilterRegression extends AbstractTest {
    /**
     * Sets up any configuration before each test.
     */
    @Before
    public void setUp() {
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP)
                || context.getTargetSite().supportsFeature(FeatureFlag.GUIDED_PIP_REDESIGN));
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.ORDER_FREE_SWATCH_FLYOUT_FILTER));
    }

    /**
     * Verify if a filter in order free swatch flayout works correctly. Test
     * Case ID - RGSN-38684
     */
    @Test
    public void testOrderFreeSwatchFlyoutGuidedPipWhenCalledExpectValidValue() {

        // given
        ProductGroup productGroup = dataService.findProductGroup("guided-pip", "order-free-swatch-flyout");
        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        WebElement orderFreeSwatchLink = productPage.getOrderFreeSwatchLink();
        // then
        assertNotNull("orderFreeSwatchLink should not be null.", orderFreeSwatchLink);

        // when
        OrderFreeSwatchFlyout orderFreeSwatchFlyout = productPage.clickOrderFreeSwatchLink(orderFreeSwatchLink);
        int initialTotalNumberOfSwatchProducts = orderFreeSwatchFlyout.getTotalNumberOfSwatchProducts();
        WebElement swatchFilter = orderFreeSwatchFlyout.clickSwatchFilterButton(0);
        WebElement swatchFilterItem = orderFreeSwatchFlyout.clickSwatchFilterItem(swatchFilter, 0);
        String swatchFilterItemLabel = orderFreeSwatchFlyout.getLabelForSwatchFilterItem(swatchFilterItem);
        // then
        assertTrue("swatchFilterItemLabel should not be blank.", StringUtils.isNotBlank(swatchFilterItemLabel));

        // when
        int parenthesisStartIndex = swatchFilterItemLabel.indexOf('(');
        int parenthesisEndIndex = swatchFilterItemLabel.indexOf(')');
        // then
        assertTrue("parenthesisStartIndex should be more than 0.", parenthesisStartIndex > 0);
        assertTrue("parenthesisEndIndex should be more than 0.", parenthesisEndIndex > 0);

        // when
        String filterItemValue = swatchFilterItemLabel.substring(0, parenthesisStartIndex).trim();
        int filterItemNumber = Integer.parseInt(swatchFilterItemLabel.substring(parenthesisStartIndex + 1, parenthesisEndIndex));
        int numberOfSwatchProductsAfterFiltering = orderFreeSwatchFlyout.getTotalNumberOfSwatchProducts();
        // then
        assertTrue("FilterOption should be displayed.", orderFreeSwatchFlyout.isFilterOptionDisplayedForFilterItemValue(filterItemValue));
        assertEquals("filterItemNumber should be equal to numberOfSwatchProductsAfterFiltering", filterItemNumber, numberOfSwatchProductsAfterFiltering);

        // when
        orderFreeSwatchFlyout.clickClearAllFiltersButton();
        // then
        assertTrue("isFilterOptionsRemoved() should return true.", orderFreeSwatchFlyout.isFilterOptionsRemoved());

        // when
        int totalNumberOfSwatchProductAfterClearingFilters = orderFreeSwatchFlyout.getTotalNumberOfSwatchProducts();
        // then
        assertEquals("initialTotalNumberOfSwatchProducts should be equal to totalNumberOfSwatchProductAfterClearingFilters.",
                     initialTotalNumberOfSwatchProducts, totalNumberOfSwatchProductAfterClearingFilters);
        assertTrue("hasCorrectProductView() should return true. ", orderFreeSwatchFlyout.hasCorrectProductView());

        if (context.isMobileExperience()) {
            // when
            orderFreeSwatchFlyout.clickViewSwatchesAsGrid();
            // then
            assertTrue("hasGridViews() should return true.", orderFreeSwatchFlyout.hasGridViews());
        }
    }
}
