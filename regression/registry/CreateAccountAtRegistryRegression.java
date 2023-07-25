package com.wsgc.ecommerce.ui.regression.registry;

import static org.junit.Assert.assertEquals;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.registry.RegistryFlows;
import com.wsgc.ecommerce.ui.endtoend.registry.RegistryFlows.PrivacySettings;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.registry.CreateRegistryOverlaySection;
import com.wsgc.ecommerce.ui.pagemodel.registry.CreateRegistryPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.CreateRegistrySignInPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.CrossBrandRegistryListPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.RegistryLandingPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.RegistryLoginPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.registrylist.RegistryListItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.QuicklookOverlay;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.CustomerAccount;
import com.wsgc.evergreen.entity.api.MutableAccount;
import com.wsgc.evergreen.entity.api.ProductGroup;
import com.wsgc.evergreen.entity.api.Registry;

/**
 * Regression Tests for validating create account at registry page.
 */
@Category(Registry.class)
public class CreateAccountAtRegistryRegression extends AbstractTest {
	
	/**
     * Prepares data fixtures & ensures feature flag is enabled.
     */
    @Before
    public void setUp() {
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.REGISTRY_V2));
    }
    
    /**
     * Verifies if user is able to create an account and create a registry 
     * while adding an item to registry from product page.
     * Test Case ID - RGSN-38243, RGSN-38256
     */
    @Test
    @TestRail(id = "102649")
    public void testCreateAccountWhenAddingAnItemToRegistryExpectAccountAndRegistryCreatedWithItem() {
    	
        //given
        final Registry registry = dataService.findRegistry("create-registry");
        final ProductGroup productGroup = dataService.findProductGroup("simple-pip", "!attributes");
        MutableAccount mutableAccount = getMutableAccount("default");
        CustomerAccount newAccount = accountCreator.getRandomEmailAccount(mutableAccount);
        final String expectedSku = productGroup.getSkus().get(0);        
        final boolean useDefaultAddress = false;

        //when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);        
        productPage.clickAddToRegistry();

        if (context.supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY)) {
            RegistryFlows.createAccount(context, newAccount, RegistryLandingPage.class);
            context.getPageSection(CreateRegistryOverlaySection.class).clickCreateRegistryButton();
        } else {
            if (context.isFullSiteExperience()) {
                RegistryLoginPage registryLoginPage = context.getPage(RegistryLoginPage.class);
                registryLoginPage.clickCreateRegistryButton(CreateRegistrySignInPage.class);
            }
            RegistryFlows.createAccount(context, newAccount, CreateRegistryPage.class);
        }
        CrossBrandRegistryListPage registryListPage = RegistryFlows.
        		createRegistryFlow(context, registry, newAccount, PrivacySettings.PUBLIC , useDefaultAddress);
        RegistryListItemRepeatableSection registryListItemRepeatableSection = registryListPage.getItem(expectedSku);      
        String actualSku = registryListItemRepeatableSection.getItemSku();

        //then
        assertEquals("The item "+ expectedSku + " is not added to registry " , expectedSku , actualSku);
    }
    
    /**
     * Verifies whether a user is able to add an item to registry from QuickLook 
     * by creating a new Account surfaced from the Category page.
     * Test Case ID - RGSN-38237
     */
    @Test
    @TestRail(id = "105741")
    public void testCreateAccountWhenAddingAnItemToRegistryFromQuickLookOverlayExpectAccountAndRegistryCreatedWithItem() {

        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.ADD_TO_CART_ON_SHOP_AND_SEARCH) 
                && context.isFullSiteExperience());
        MutableAccount mutableAccount = getMutableAccount("default");
        CustomerAccount newAccount = accountCreator.getRandomEmailAccount(mutableAccount);
        final Registry registry = dataService.findRegistry("create-registry");
        final ProductGroup quicklookItem = dataService.findProductGroup("simple-pip", "attributes");
        final String expectedSku = quicklookItem.getSkus().get(0);
        final String expectedQuantity = "1";
        final boolean useDefaultAddress = false;

        // when
        QuicklookOverlay quicklookOverlay = ShopFlows.goToAddToCartQuicklookButtonFlow(context, quicklookItem);
        quicklookOverlay.setProductAttributeSelections(quicklookItem);
        quicklookOverlay.setQuantity(expectedQuantity);
        quicklookOverlay.addToRegistryNoRegistryPresent();
        if (context.supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY)) {
            RegistryFlows.createAccount(context, newAccount, RegistryLandingPage.class);
            context.getPageSection(CreateRegistryOverlaySection.class).clickCreateRegistryButton();
        } else {
            context.getPage(RegistryLoginPage.class).clickCreateRegistryButton(CreateRegistrySignInPage.class);
            RegistryFlows.createAccount(context, newAccount, CreateRegistryPage.class);
        }
        CrossBrandRegistryListPage registryListPage = RegistryFlows.
                createRegistryFlow(context, registry, newAccount, PrivacySettings.PUBLIC , useDefaultAddress);
        RegistryListItemRepeatableSection registryListItemRepeatableSection = registryListPage.getItem(expectedSku);      
        String actualSku = registryListItemRepeatableSection.getItemSku();

        //then
        assertEquals("The item \'" + expectedSku + "\' is not added to registry", expectedSku, actualSku);
    }
}
