package com.wsgc.ecommerce.ui.regression.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.account.AccountFlows;
import com.wsgc.ecommerce.ui.endtoend.registry.RegistryFlows;
import com.wsgc.ecommerce.ui.endtoend.shop.PipFlows;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.account.AccountHomePage;
import com.wsgc.ecommerce.ui.pagemodel.account.LoginPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.CreateRegistryOverlaySection;
import com.wsgc.ecommerce.ui.pagemodel.registry.CrossBrandRegistryListPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.GuidedPipFlows;
import com.wsgc.ecommerce.ui.pagemodel.registry.registrylist.RegistryListItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.QuicklookOverlay;
import com.wsgc.ecommerce.ui.pagemodel.shop.RegistrySelectionOverlaySection;
import com.wsgc.ecommerce.ui.pagemodel.shop.RichAddToRegistryOverlaySection;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.CustomerAccount;
import com.wsgc.evergreen.entity.api.ProductGroup;
import com.wsgc.evergreen.entity.api.Registry;
import com.wsgc.evergreen.impl.DefaultEvergreenContext;

import java.util.List;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Regression Tests for validating multiple registry overlay.
 */
@Category(Registry.class)
public class MultipleRegistryOverlayRegression extends AbstractTest {

    /**
     * Prepares data fixtures & ensures feature flag is enabled.
     */
    @Before
    public void setUp() {
    	Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.REGISTRY_V2));
    }

    /**
     * Verifies whether a customer is able to add an item to registry from multiple registry
     * overlay from add to cart button overlay at category page.
     * Test Case ID - RGSN-38278
     */
    @Test
    @TestRail(id = "102651")
    public void testAddToCartQuickLookButtonOverlayWhenUserAddsItemFromMultipleRegistryOverlayExpectRegistryHasProduct() {

        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.ADD_TO_CART_ON_SHOP_AND_SEARCH)
                && context.isFullSiteExperience());
        final Registry registry = dataService.findRegistry("multiple-registry", "add-to-cart-button");
        final String email = registry.getRegistrant().getEmail();
        final String password = registry.getRegistrant().getPassword();
        final ProductGroup quicklookItem = dataService.findProductGroup(productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null, 
                "simple-pip", "attributes", "add-to-cart-button");
        final String itemSku = quicklookItem.getSkus().get(0);
        final String registryId = registry.getExternalId();
        final String expectedQuantity = "1";
        int expectedStillNeedQuantity = 1;
        CrossBrandRegistryListPage registryListPage;
        
        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);
        List<RegistryListItemRepeatableSection> registryListRepeatableSection = registryListPage.getRegistryListItemSections();
        if ((registryListRepeatableSection.size() > 0)) {
            for (RegistryListItemRepeatableSection registryListItem : registryListRepeatableSection) {
                if (registryListItem.getItemSku().equals(itemSku)) {
                    registryListItem.clickDeleteIcon();
                    break;
                }
            }
        }
        QuicklookOverlay quicklookOverlay = ShopFlows.goToAddToCartQuicklookButtonFlow(context, quicklookItem);
        quicklookOverlay.setProductAttributeSelections(quicklookItem);
        quicklookOverlay.setQuantity(expectedQuantity);
        RegistrySelectionOverlaySection registrySelectionOverlaySection = quicklookOverlay.addToRegistryMultipleRegistriesPresent();
        RichAddToRegistryOverlaySection richAddToRegistryOverlaySection = registrySelectionOverlaySection.selectRegistryAndClickContinue(registryId);
        richAddToRegistryOverlaySection.viewRegistry();
        RegistryListItemRepeatableSection registryListItemRepeatableSection = registryListPage.getItem(itemSku);
        String actualStillNeedQuantity = registryListItemRepeatableSection.getStillNeedsQuantityText();
        String currentPageUrl = context.getPilot().getDriver().getCurrentUrl();
        
        // then
        assertTrue("The item is not added to \'" + registryId + " \' registry list page", currentPageUrl.contains(registryId));
        assertEquals("Expected still need quantity \'" + String.valueOf(expectedStillNeedQuantity) 
                + "\' does not match with actual still need quantity \'" + actualStillNeedQuantity, 
                String.valueOf(expectedStillNeedQuantity), actualStillNeedQuantity);
    }
    
    /**
     * Verifies whether a customer is able to add an item to registry from multiple registry
     * overlay from quicklook overlay at category page.
     * Test Case ID - RGSN-38278
     */
    @Test
    @TestRail(id = "102652")
    public void testQuickLookOverlayWhenUserAddsItemFromMultipleRegistryOverlayExpectRegistryHasProduct() {

        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.QUICKLOOK)
                && context.isFullSiteExperience());
        final Registry registry = dataService.findRegistry("multiple-registry", "add-to-cart-button");
        final String email = registry.getRegistrant().getEmail();
        final String password = registry.getRegistrant().getPassword();
        final ProductGroup quicklookItem = dataService.findProductGroup(productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null, 
                "simple-pip", "attributes", "add-to-cart-button");
        final String itemSku = quicklookItem.getSkus().get(0);
        final String registryId = registry.getExternalId();
        final String expectedQuantity = "1";
        int expectedStillNeedQuantity = 1;
        CrossBrandRegistryListPage registryListPage;
        
        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);
        List<RegistryListItemRepeatableSection> registryListRepeatableSection = registryListPage.getRegistryListItemSections();
        if ((registryListRepeatableSection.size() > 0)) {
            for (RegistryListItemRepeatableSection registryListItem : registryListRepeatableSection) {
                if (registryListItem.getItemSku().equals(itemSku)) {
                    registryListItem.clickDeleteIcon();
                    break;
                }
            }
        }
        QuicklookOverlay quicklookOverlay = ShopFlows.goToQuicklookFlow(context, quicklookItem);
        quicklookOverlay.setProductAttributeSelections(quicklookItem);
        quicklookOverlay.setQuantity(expectedQuantity);
        RegistrySelectionOverlaySection registrySelectionOverlaySection = quicklookOverlay.addToRegistryMultipleRegistriesPresent();
        RichAddToRegistryOverlaySection richAddToRegistryOverlaySection = registrySelectionOverlaySection.selectRegistryAndClickContinue(registryId);
        richAddToRegistryOverlaySection.viewRegistry();
        RegistryListItemRepeatableSection registryListItemRepeatableSection = registryListPage.getItem(itemSku);
        String actualStillNeedQuantity = registryListItemRepeatableSection.getStillNeedsQuantityText();
        String currentPageUrl = context.getPilot().getDriver().getCurrentUrl();
        
        // then
        assertTrue("The item is not added to \'" + registryId + " \' registry list page", currentPageUrl.contains(registryId));
        assertEquals("Expected still need quantity \'" + String.valueOf(expectedStillNeedQuantity) 
                + "\' does not match with actual still need quantity \'" + actualStillNeedQuantity, 
                String.valueOf(expectedStillNeedQuantity), actualStillNeedQuantity);
    }
    
    /**
     * Verifies whether the user is able to add complex PIP item to registry as a guest user.
     * Test Case ID - RGSN-38271, RGSN-38222
     */
    @Test
    @TestRail(id = "104659")
    public void testAddToRegistryWhenUserWithMultipleRegistryAddsMultiBuyItemToRegistryExpectItemAddedToSelectedRegistry() {

        // given
        final CustomerAccount account = dataService.findAccount("multiple-registry", "multi-buy-pip", "add-item-to-registry");
        final ProductGroup productGroup = dataService.findProductGroup("multi-buy-multi-sku-pip", "attributes");
        final Registry registry = dataService.findRegistry("multiple-registry", "multi-buy-pip");
        List<String> itemSkus = productGroup.getSkus();
        String registryId = registry.getExternalId();
        String email = registry.getRegistrant().getEmail();
        String password = registry.getRegistrant().getPassword();
        String expectedQuantity = "1";
        String registryIndex = registry.getUnmappedAttribute("registryIndex");
        String expectedStillNeedQuantity = "1";
        CrossBrandRegistryListPage registryListPage;
        
        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);
        RegistryFlows.deleteProductsFromRegistryListFlow(registryListPage);
        context.getPilot().getDriver().quit();
        context = DefaultEvergreenContext.getContext();
        DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();
        
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        PipFlows.setMultiBuyPipQuantityAndAttributes(productPage, expectedQuantity);
        RegistrySelectionOverlaySection registrySelectionOverlaySection = productPage.addToRegistryMultipleRegistriesPresent();
        RegistryFlows.registrySignIn(context, account);
        if (context.supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY)) {
            CreateRegistryOverlaySection createRegistryOverlaySection = context.getPageSection(CreateRegistryOverlaySection.class);
            createRegistryOverlaySection.selectRegistryFromMultipleRegistryList(registryIndex);
            createRegistryOverlaySection.clickAddToRegistryButton();
            registryListPage = createRegistryOverlaySection.clickViewRegistry();
        } else {
            RichAddToRegistryOverlaySection richAddToRegistryOverlaySection = registrySelectionOverlaySection.selectRegistryAndClickContinue(registryId);
            registryListPage = richAddToRegistryOverlaySection.viewRegistry();
        }
        
        // then
        for (String itemSku : itemSkus) {
            RegistryListItemRepeatableSection registryListRepeatableSection = registryListPage.getItem(itemSku);
            String actualStillNeedQuantity = registryListRepeatableSection.getStillNeedsQuantityText();
            String currentPageUrl = context.getPilot().getDriver().getCurrentUrl();

            assertTrue("The item is not added to \'" + registryId + " \' registry list page", currentPageUrl.contains(registryId));
            assertEquals("Expected still need quantity \'" + expectedStillNeedQuantity + "\' does not match with actual still need quantity \'"
                    + actualStillNeedQuantity, (expectedStillNeedQuantity), actualStillNeedQuantity);
        }
    }

    /**
     * Verifies whether the user is able to add guided PIP item to registry as a guest user.
     * Test Case ID - RGSN-38272, RGSN-38222
     */
    @Test
    @TestRail(id = "104660")
    public void testAddToRegistryWhenUserWithMultipleRegistryAddsItemToRegistryFromGuidedPipExpectItemAddedToSelectedRegistry() {

        // given
        final CustomerAccount account = dataService.findAccount("multiple-registry", "guided-pip", "add-item-to-registry");
        final ProductGroup productGroup = dataService.findProductGroup("guided-pip", "attributes");
        final Registry registry = dataService.findRegistry("multiple-registry", "guided-pip");
        String itemSku = productGroup.getSkus().get(0);
        String registryId = registry.getExternalId();
        String email = registry.getRegistrant().getEmail();
        String password = registry.getRegistrant().getPassword();
        String expectedQuantity = "1";
        String registryIndex = registry.getUnmappedAttribute("registryIndex");
        int expectedStillNeedQuantity = 1;
        CrossBrandRegistryListPage registryListPage;

        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);
        RegistryFlows.deleteProductsFromRegistryListFlow(registryListPage);
        context.getPilot().getDriver().quit();
        context = DefaultEvergreenContext.getContext();
        DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();

        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        GuidedPipFlows.setGuidedPipQuantityAndAttributes(context, expectedQuantity);
        RegistrySelectionOverlaySection registrySelectionOverlaySection = productPage.addToRegistryMultipleRegistriesPresent();
        RegistryFlows.registrySignIn(context, account);

        if (context.supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY)) {
            CreateRegistryOverlaySection createRegistryOverlaySection = context.getPageSection(CreateRegistryOverlaySection.class);
            createRegistryOverlaySection.selectRegistryFromMultipleRegistryList(registryIndex);
            createRegistryOverlaySection.clickAddToRegistryButton();
            registryListPage = createRegistryOverlaySection.clickViewRegistry();
        } else {
            RichAddToRegistryOverlaySection richAddToRegistryOverlaySection = registrySelectionOverlaySection.selectRegistryAndClickContinue(registryId);
            registryListPage = richAddToRegistryOverlaySection.viewRegistry();
        }

        RegistryListItemRepeatableSection registryListRepeatableSection = registryListPage.getItem(itemSku);
        String actualStillNeedQuantity = registryListRepeatableSection.getStillNeedsQuantityText();
        String currentPageUrl = context.getPilot().getDriver().getCurrentUrl();

        // then
        assertTrue("The item is not added to \'" + registryId + " \' registry list page", currentPageUrl.contains(registryId));
        assertEquals("Expected still need quantity \'" + String.valueOf(expectedStillNeedQuantity) 
               + "\' does not match with actual still need quantity \'" + actualStillNeedQuantity, 
               String.valueOf(expectedStillNeedQuantity), actualStillNeedQuantity);
    }
}
