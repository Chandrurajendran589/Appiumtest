package com.wsgc.ecommerce.ui.regression.customerservice;

import static org.junit.Assert.assertTrue;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.pagemodel.customerservice.CatalogRequestMailingInfoPage;
import com.wsgc.ecommerce.ui.pagemodel.customerservice.CatalogRequestPage;
import com.wsgc.evergreen.api.FeatureFlag;

import tag.area.CustomerSupportArea;

/**
 * Regression Tests for Validating the Catalog Request pages.
 */
@Category(CustomerSupportArea.class)
public class CatalogRequestPageRegression extends AbstractTest {

    /**
     * Prepares data fixtures & ensures feature flag is enabled.
     */
    @Before
    public void setUp() {
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.CATALOG_REQUEST) &&
                context.isFullSiteExperience());
    }
    
    /**
     * Verifies whether Email Unsubscribe Link is present in catalog request page.
     * Test Case ID - RGSN-38808
     */
    @Test
    @TestRail(id = "105278")
    public void testCatalogRequestPageWhenUserClicksContinueButtonExpectUnsubscribeLink() {
        
        // given
        CatalogRequestPage catalogRequestPage = context.getPage(CatalogRequestPage.class);
        context.getPilot().startAt(catalogRequestPage);
        
        // when
        if (catalogRequestPage.isContinueButtonPresentAndDisplayed()) {
            catalogRequestPage.clickContinueButton();
        } 
        
        // then
        CatalogRequestMailingInfoPage catalogRequestMailingInfoPage = context.getPage(CatalogRequestMailingInfoPage.class);
        assertTrue("Unsubscribe link is not present in catalog request mailing info page", 
                catalogRequestMailingInfoPage.isUnsubscribeLinkPresentAndDispayed());
    }
}
