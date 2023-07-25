package com.wsgc.ecommerce.ui.regression.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.account.AccountFlows;
import com.wsgc.ecommerce.ui.endtoend.registry.RegistryFlows;
import com.wsgc.ecommerce.ui.endtoend.registry.RegistryFlows.PrivacySettings;
import com.wsgc.ecommerce.ui.endtoend.shop.PipFlows;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSectionFactory;
import com.wsgc.ecommerce.ui.pagemodel.account.AccountHomePage;
import com.wsgc.ecommerce.ui.pagemodel.account.LoginPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.CreateRegistryOverlaySection;
import com.wsgc.ecommerce.ui.pagemodel.registry.CrossBrandRegistryListPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.registrylist.RegistryListItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.RegistrySelectionOverlaySection;
import com.wsgc.ecommerce.ui.pagemodel.shop.RichAddToCartOverlaySection;
import com.wsgc.ecommerce.ui.pagemodel.shop.RichAddToRegistryOverlaySection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.CustomerAccount;
import com.wsgc.evergreen.entity.api.MutableAccount;
import com.wsgc.evergreen.entity.api.ProductGroup;
import com.wsgc.evergreen.entity.api.Registry;
import com.wsgc.evergreen.impl.DefaultEvergreenContext;

/**
 * Regression Tests for validating add to registry as signed-in user.
 */
@Category(Registry.class)
public class AddToRegistrySignedInUserRegression extends AbstractTest {

    /**
     * Prepares data fixtures & ensures feature flag is enabled.
     */
    @Before
    public void setUp() {
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.REGISTRY_V2));
    }

    /**
     * Sets the Brand to be tested.
     *
     * @param brand The concept code for the Brand to be set
     */
    private void setBrand(String brand) {
        System.setProperty("brand", brand);
        DefaultEvergreenContext.getLifeCycleManager().beforeAllTests();
    }
    
    /**
     * Verifies if user is able to navigate to the product page on clicking a 
     * recommendation product from rich add to registry overlay section.
     * Test Case ID - RGSN-38240
     */
    @Test
    @TestRail(id = "102647")
    public void testRichAddToRegistryOverlaySectionWhenUserClicksRecommendationProductExpectProductPage() {

        // given
        final Registry registry = dataService.findRegistry("single-registry", "recommendation-product-in-rar");
        final String email = registry.getRegistrant().getEmail();
        final String password = registry.getRegistrant().getPassword();
        final ProductGroup productGroup = dataService.findProductGroup("simple-pip", "!attributes");
        final String EXPECTED_RAR_RECSTART_PARAM = "?cm_src=WsiRar";

        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        RichAddToRegistryOverlaySection richAddToRegistryOverlaySection = productPage.addToRegistryOneRegistryPresent();
        richAddToRegistryOverlaySection.waitForProductRecommendationCarousel();

        assertTrue("The product recommendation section is not displayed in the Rich Add To Registry Overlay", 
                richAddToRegistryOverlaySection.isProductRecommendationsDisplayed());
        
        int productRecommendationsAvailable = richAddToRegistryOverlaySection.getNumberOfProductRecommendationsAvailable();
        int productRecommendationIndex = RandomUtils.nextInt(0, productRecommendationsAvailable - 1);
        productPage = richAddToRegistryOverlaySection.clickProductRecommendation(productRecommendationIndex);
        String productPageUrl = productPage.getCurrentPageUrl();
        
        //then
        assertTrue(String.format("Product page url - %s - doesn't contain RAR recommendation query parameter, %s",
                productPageUrl, EXPECTED_RAR_RECSTART_PARAM), productPageUrl.contains(EXPECTED_RAR_RECSTART_PARAM));
    }
    
    /**
     * Verifies if signed in user without a registry is able to create a registry  
     * and quantity is pre-selected while navigating to Product page from 
     * create registry flyout.
     * 
     * Test Case ID - RGSN-38242
     */
    @Test
    @TestRail(id = "102648")
    public void testProductPageWhenSignedInUserNavigateToPipFromCreateRegistryFlyoutExpectQuantityPreSelected() {
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.SMART_LOGIN_REGISTRY));
    	
        // given
        final Registry registry = dataService.findRegistry("create-registry");
        final ProductGroup productGroup = dataService.findProductGroup("simple-pip", "!attributes");
        MutableAccount mutableAccount = getMutableAccount("default");
        CustomerAccount newAccount = accountCreator.getRandomEmailAccount(mutableAccount);
        final boolean useDefaultAddress = false ;
        final String expectedSku = productGroup.getSkus().get(0);        
        final String expectedQuantity = "1";        

        // when
        AccountFlows.goToLoginPageFlow(context);
        AccountFlows.createAccount(context, newAccount, AccountHomePage.class);
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        productPage.clickAddToRegistry();
        context.getPageSection(CreateRegistryOverlaySection.class).clickCreateRegistryButton();
        CreateRegistryOverlaySection createRegistry = RegistryFlows.submitCreateRegistryForm(context,
                registry, newAccount, PrivacySettings.PUBLIC, useDefaultAddress);     
        productPage = createRegistry.clickProductName(expectedSku);
        String actualQuantity = productPage.getQuantity();
        
        //then
        assertTrue("Quantity is not preselected in Product page, expected : " + expectedQuantity + " actual : " + actualQuantity,
        		actualQuantity.equalsIgnoreCase(expectedQuantity));
    }

    /**
     * Verifies if a signed-in user is able to add a monogrammed product page in
     * multiple registry account.
     * Test Case ID - RGSN-38259
     */
    @Test
    @TestRail(id = "104377")
    public void testRegistrySelectionOverlayWhenSignedInUserHavingMultipleRegistriesAddsMonogramItemToRegistryExpectItemAddedToRegistry() {

        // given
        int inlineMonoStyleIndex = 0;
        final int colorIndex = 0;
        final int expectedQuantity = 1;
        int expectedStillNeedQuantity = 1;
        String monogramText = "ABC";

        final Registry registry = dataService.findRegistry("multiple-registry", "simple-pip");
        final ProductGroup monogrammableProduct = dataService.findProductGroup("simple-pip", "attributes", "monogrammable");
        final String itemSku = monogrammableProduct.getSkus().get(0);
        final String email = registry.getRegistrant().getEmail();
        final String password = registry.getRegistrant().getPassword();
        String registryId = registry.getExternalId();
        CrossBrandRegistryListPage registryListPage;

        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);

        // then
        RegistryFlows.deleteProductsFromRegistryListFlow(registryListPage);

        ProductPage productPage = ShopFlows.goToPipPageFlow(context, monogrammableProduct);
        PipFlows.setSimplePipAttributeSelections(productPage);
        PipFlows.setMonogramDetails(productPage, inlineMonoStyleIndex, monogramText, colorIndex, expectedQuantity);
        RegistrySelectionOverlaySection registrySelectionOverlaySection = productPage.addToRegistryMultipleRegistriesPresent();
        RichAddToRegistryOverlaySection richAddToRegistryOverlaySection = registrySelectionOverlaySection.selectRegistryAndClickContinue(registryId);
        richAddToRegistryOverlaySection.viewRegistry();
        RegistryListItemRepeatableSection registryItemRepeatableSection = registryListPage.getItem(itemSku);
        String actualStillNeedQuantity = registryItemRepeatableSection.getStillNeedsQuantity();
        String actualPersonalizationLabelOnRegistryListPage = registryItemRepeatableSection.getPersonalizationText();

        //then
        assertEquals("Expected still need quantity \'" + String.valueOf(expectedStillNeedQuantity)
                        + "\' does not match with actual still need quantity \'" + actualStillNeedQuantity,
                String.valueOf(expectedStillNeedQuantity), actualStillNeedQuantity);

        assertEquals("Personalization text \'" + monogramText
                        + "\' does not match with actual Monogram texts on the registry list page \'" + actualPersonalizationLabelOnRegistryListPage,
                "Personalization: " + monogramText, actualPersonalizationLabelOnRegistryListPage);
    }

    /**
     * Verifies if a signed-in user is able to add a saved monogrammed product to shopping cart from registry list page.
     * Test Case ID - RGSN-38682
     */
    @Test
    @TestRail(id = "104380")
    public void testAddCartMonogramItemFromRegistryWhenSignedInUserAddsMonogramItemToCartFromRegistryListExpectItemAddedToCart() {

        // given
        int inlineMonoStyleIndex = 0;
        final int colorIndex = 0;
        final int expectedQuantity = 1;
        int expectedStillNeedQuantity = 1;
        final int expectedTotalItem = 1;
        String monogramText = "ABC";

        final Registry registry = dataService.findRegistry("single-registry", "simple-pip");
        final ProductGroup monogrammableProduct = dataService.findProductGroup("simple-pip", "attributes", "monogrammable");
        final String itemSku = monogrammableProduct.getSkus().get(0);
        final String email = registry.getRegistrant().getEmail();
        final String password = registry.getRegistrant().getPassword();
        CrossBrandRegistryListPage registryListPage;


        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);
        RegistryFlows.deleteProductsFromRegistryListFlow(registryListPage);

        ProductPage productPage = ShopFlows.goToPipPageFlow(context, monogrammableProduct);
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        int cartItemCountBeforeAddingTheItem = navigationSection.getCartCountFromGlobalHeader();
        PipFlows.setSimplePipAttributeSelections(productPage);
        PipFlows.setMonogramDetails(productPage, inlineMonoStyleIndex, monogramText, colorIndex, expectedQuantity);
        productPage.clickAddToRegistry();
        RichAddToRegistryOverlaySection richAddToRegistryOverlaySection = context.getPageSection(RichAddToRegistryOverlaySection.class);
        registryListPage = richAddToRegistryOverlaySection.viewRegistry();
        RegistryListItemRepeatableSection registryItemRepeatableSection = registryListPage.getItem(itemSku);
        String actualStillNeedQuantity = registryItemRepeatableSection.getStillNeedsQuantity();

        // then
        assertEquals("Expected still need quantity \'" + String.valueOf(expectedStillNeedQuantity)
                        + "\' does not match with actual still need quantity \'" + actualStillNeedQuantity,
                String.valueOf(expectedStillNeedQuantity), actualStillNeedQuantity);

        // when
        registryItemRepeatableSection.clickAddToCartButton();
        RichAddToCartOverlaySection richAddToCartOverlaySection = context.getPageSection(RichAddToCartOverlaySection.class);
        ShoppingCartPage shoppingCartPage = richAddToCartOverlaySection.goToShoppingCartPage();
        shoppingCartPage.scrollIntoCartSection();
        List<ShoppingCartItemRepeatableSection> itemSectionList = shoppingCartPage.getShoppingCartItems();

        // then
        assertEquals(String.format("The shopping cart should have %s item.", expectedTotalItem), expectedTotalItem,
                itemSectionList.size());

        // when
        ShoppingCartItemRepeatableSection item = shoppingCartPage.getFirstShoppingCartItem();

        // then
        assertTrue(String.format("The shopping cart item's sku should ends with '%s'. Actual sku: '%s'", itemSku,
                item.getSku()), item.getSku().endsWith(itemSku));
        assertEquals(String.format("The shopping cart item's quantity should be '%s'", expectedQuantity),
                String.valueOf(cartItemCountBeforeAddingTheItem+expectedQuantity), item.getQuantity());
    }

    /**
     * Verifies if a signed-in user is able to edit monogrammed product from registry list page and edited monogram item correctly
     * updated on registry list page.
     * Test Case ID - RGSN-38681
     */
    @Test
    @TestRail(id = "104590")
    public void testEditMonogramItemFromRegistryWhenSigednInUserClicksMonogramItemFromRegistryListAndEditsMonogramTextExpectMonogramTextUpdated() {

        // given
        final int colorIndex = 0;
        final int expectedQuantityAfterEdit = 2;
        boolean flagMonogramItem = false;

        final Registry registry = dataService.findRegistry("registry-with-monogram-item");
        final String itemSku = registry.getUnmappedAttribute("sku");
        final String email = registry.getRegistrant().getEmail();
        final String password = registry.getRegistrant().getPassword();
  
        //when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        CrossBrandRegistryListPage registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);
        RegistryListItemRepeatableSection registryItemRepeatableSection = registryListPage.getItem(itemSku);
        registryItemRepeatableSection.clickProductName();
        ProductPage productPage = context.getPage(ProductPage.class);

        // then
        assertTrue("Monogram details are not pre-populated", productPage.isAddMonoPzCheckboxPresentAndSelected());

        // when
        int randomQuantity = RandomUtils.nextInt(1, 99);
        String newMonogramText = String.valueOf(randomQuantity);
        productPage.typeInlineMonoPzText(newMonogramText);
        if (productPage.isInlineMonoPZColorDisplayed()) {
            productPage.clickColor(colorIndex);
        }
        productPage.setQuantity(expectedQuantityAfterEdit);
        productPage.clickAddToRegistry();
        RichAddToRegistryOverlaySection richAddToRegistryOverlaySection = context.getPageSection(RichAddToRegistryOverlaySection.class);
        richAddToRegistryOverlaySection.viewRegistry();
        List<RegistryListItemRepeatableSection> listItemRepeatableSections = registryListPage.getRegistryListItemSections();
        for (RegistryListItemRepeatableSection  listItem : listItemRepeatableSections) {
            if (listItem.getSkuNumber().equals(itemSku)) {
                listItem.getPersonalizationText().contains(newMonogramText);
                flagMonogramItem = true;
                break;
            }
        }

        // then
        assertTrue("Edited monogram details doesn't present on registry list page", flagMonogramItem);
    }
    
    /*
     * Verifies that product added to a registry is displayed in registry across the brands 
     * and user able to update the quantity from cross brand.
     * Test Case ID - RGSN-38265, RGSN-38267
     */
    @Test
    @TestRail(id = "104371")
    public void testUpdateRegistryWhenRegistrantUpdatesQuantityExpectUpdatedQuantityAcrossBrands(){

        // given
        String initialBrand = System.getProperty("brand");

        final Registry registry = dataService.findRegistry("cross-brand-update");
        final String email = registry.getRegistrant().getEmail();
        final String password = registry.getRegistrant().getPassword();
        final ProductGroup productGroup = dataService.findProductGroup("simple-pip", "!attributes");
        final String expectedSku = productGroup.getSkus().get(0);
        String expectedQuantity = "1";        

        String[] brands = { "PB", "PK", "WE", "WS"};
        String currentBrand = context.getBrand().getCode();

        RegistryFlows.removeItem(context, registry, expectedSku);
        context.getPilot().getDriver().close();
        context = DefaultEvergreenContext.getContext();
        DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();

        //when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        productPage.setQuantity(expectedQuantity);
        productPage.addToRegistryOneRegistryPresent(); 
        productPage.waitForRichAddToRegistryOverlaySection();

        for (String brand : brands) {
            if (!brand.equalsIgnoreCase(currentBrand)) {
                try {
                    setBrand(brand);
                    context.getPilot().getDriver().close();
                    context = DefaultEvergreenContext.getContext();
                    DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();

                    AccountFlows.goToLoginPageFlow(context);
                    AccountFlows.signIn(context, email, password, AccountHomePage.class);
                    CrossBrandRegistryListPage registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);

                    RegistryListItemRepeatableSection registryListItemRepeatableSection = registryListPage.getItem(expectedSku);      
                    String actualSku = registryListItemRepeatableSection.getItemSku();
                    String actualQuantity = registryListItemRepeatableSection.getStillNeedsQuantityText();

                    //then
                    assertEquals("The item " + expectedSku + " is not added to registry " , expectedSku , actualSku);
                    assertEquals("Expected quantity: "+ expectedQuantity + " of item is not added to registry, actual is " + actualQuantity ,
                            expectedQuantity , actualQuantity);

                    int randomQuantity = RandomUtils.nextInt(1, 99);
                    String expectedUpdatedQuantity = String.valueOf(randomQuantity);
                    expectedQuantity = expectedUpdatedQuantity;
                    registryListItemRepeatableSection.setRequestedQuantity(expectedUpdatedQuantity);

                    String updatedQuantity = registryListItemRepeatableSection.getStillNeedsQuantityText();
                    assertTrue("Quantity is not updated to " + expectedUpdatedQuantity + ", actual qunatity : " + updatedQuantity,
                            updatedQuantity.equalsIgnoreCase(expectedUpdatedQuantity));
                } finally {
                    setBrand(initialBrand);
                }
            }
        }
    }

    /**
     * Verifies that user is redirected to cross brand registry list page when user click on add to cart 
     *  for cross brand product from home brand registry list page and user able to add to cart.
     * Test Case ID - RGSN-38265, RGSN-38267
     */
    @Test
    @TestRail(id = "104372")
    public void testAddToCartWhenRegistrantAddsCrossBrandProductFromRegistryExpectAddToCartOverlayInTheCrossBrand() {

        //given
        final Registry registry = dataService.findRegistry("cross-brand-product");
        final String email = registry.getRegistrant().getEmail();
        final String password = registry.getRegistrant().getPassword();
        final String expectedSku = registry.getUnmappedAttribute("sku");
        final String registryListId = registry.getExternalId(); 
        String expectedQuantity = "2";

        //when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);

        AccountFlows.signIn(context, email, password, AccountHomePage.class);

        CrossBrandRegistryListPage registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);        

        RegistryListItemRepeatableSection registryListItemRepeatableSection = registryListPage.getItem(expectedSku);
        String brandCode = registryListItemRepeatableSection.getConceptCode();

        registryListItemRepeatableSection.clickCrossBrandAddToCartButton();     
        List<String> windows = new ArrayList<String>(context.getPilot().getDriver().getWindowHandles());
        context.getPilot().getDriver().switchTo().window(windows.get(1));
        String currentUrl = context.getPilot().getDriver().getCurrentUrl();

        //then
        assertTrue(String.format("The registyry id %s is not present in the current page URL", registryListId),
                currentUrl.contains(registryListId));

        setBrand(brandCode);
        context.getPilot().getDriver().close();
        context = DefaultEvergreenContext.getContext();
        DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();
        CrossBrandRegistryListPage crossbrandRegistryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);        

        RegistryListItemRepeatableSection crossBrandRegistryListItemRepeatableSection = crossbrandRegistryListPage.getItem(expectedSku);
        RichAddToCartOverlaySection richAddToCartOverlaySection = crossBrandRegistryListItemRepeatableSection.setQuantityAndAddToCart(expectedQuantity);
        ShoppingCartPage shoppingCartPage = richAddToCartOverlaySection.goToShoppingCartPage();

        // then
        ShoppingCartItemRepeatableSection item = shoppingCartPage.getFirstShoppingCartItem();
        assertTrue(String.format("The shopping cart item's sku should ends with '%s'. Actual sku: '%s'", expectedSku,
                item.getSku()), item.getSku().endsWith(expectedSku.toString()));
        assertEquals(String.format("The shopping cart item's quantity should be '%s'", expectedQuantity),
                String.valueOf(expectedQuantity), item.getQuantity());
    }
    
    /**
     * Verifies if a signed-in user is able to edit monogrammed product with the same details from registry list page and edited quantity
     * is updated on registry list page.
     * Test Case ID - RGSN-38028
     */
    @Test
    @TestRail(id = "104591")
    public void testEditMonogramItemFromRegistryWhenSigednInUserEditsOnlyQuantityFromProductPageExpectRequestedQuantityUpdated() {

        // given
        final Registry registry = dataService.findRegistry("registry-with-monogram-item");
        final String itemSku = registry.getUnmappedAttribute("sku");
        final String email = registry.getRegistrant().getEmail();
        final String password = registry.getRegistrant().getPassword();

        //when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        CrossBrandRegistryListPage registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);
        RegistryListItemRepeatableSection registryItemRepeatableSection = registryListPage.getItem(itemSku);
        registryItemRepeatableSection.clickProductName();
        ProductPage productPage = context.getPage(ProductPage.class);

        // then
        assertTrue("Monogram details are not pre-populated", productPage.isAddMonoPzCheckboxPresentAndSelected());

        // when
        int updatedQuantity = RandomUtils.nextInt(1, 99); 
        String expectedUpdatedQuantity = String.valueOf(updatedQuantity);
        productPage.setQuantity(expectedUpdatedQuantity);
        productPage.clickAddToRegistry();
        RichAddToRegistryOverlaySection richAddToRegistryOverlaySection = context.getPageSection(RichAddToRegistryOverlaySection.class);
        registryListPage = richAddToRegistryOverlaySection.viewRegistry();
        String actualQuantity = registryItemRepeatableSection.getStillNeedsQuantityText();

        // then
        assertTrue("Quantity is not updated to " + expectedUpdatedQuantity + ", actual qunatity : " + actualQuantity,
        		actualQuantity.equalsIgnoreCase(expectedUpdatedQuantity));    
    }
    
    /**
     * Verifies if the user is not able to add protection plan for the item to registry list page.
     * Test Case ID - RGSN-38770
     */
    @Test
    @TestRail(id = "104669")
    public void testProtectionPlanItemWhenUserAddsProtectionPlanItemToRegistryExpectNoProtectionPlanAddedToThatItem() {

        // given
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.PROTECTION_PLAN));
        final CustomerAccount account = dataService.findAccount("add-protection-plan-item-to-registry");
        final String email = account.getEmail();
        final String password = account.getPassword();
        final ProductGroup expectedProductGroup = dataService.findProductGroup("all-state-protection-plan");
        final String expectedQuantity = "1";
        final String itemSku = expectedProductGroup.getSkus().get(0);
        
        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, expectedProductGroup);
        PipFlows.setSimplePipQuantityAndAttributes(productPage, expectedQuantity);
        productPage.selectAllStateProtectionPlanCheckbox();
        
        // then
        assertTrue("AllState Protection Plan checkbox is not selected", productPage.isAllStateProtectionPlanCheckboxSelected());
        
        // when
        productPage.clickAddToRegistry();
        RichAddToRegistryOverlaySection richAddToRegistryOverlaySection = context.getPageSection(RichAddToRegistryOverlaySection.class);
        richAddToRegistryOverlaySection.viewRegistry();
        RegistryListItemRepeatableSection registryItemRepeatableSection = context.getPageSection(RegistryListItemRepeatableSection.class);
        registryItemRepeatableSection.clickAddToCartButton();
        RichAddToCartOverlaySection richAddToCartOverlaySection = context.getPageSection(RichAddToCartOverlaySection.class);
        ShoppingCartPage shoppingCartPage = richAddToCartOverlaySection.goToShoppingCartPage();
        shoppingCartPage.scrollIntoCartSection();
        ShoppingCartItemRepeatableSection cartItem = shoppingCartPage.getFirstShoppingCartItem();

        // then
        assertTrue(String.format("The shopping cart item's sku should ends with '%s'. Actual sku: '%s'", itemSku,
                cartItem.getSku()), cartItem.getSku().endsWith(itemSku));
        assertTrue("You may also add AllState Protection Plan Section is displayed in shopping cart page for registry item",
                !shoppingCartPage.isYouMayAlsoAddSectionPresentAndDisplayed());
    }
    
    /**
     * Verifies if a signed-in user is able to add a item to multiple registry account.
     * Test Case ID - RGSN-38222
     */
    @Test
    @TestRail(id = "105249")
    public void testRegistrySelectionOverlayWhenSignedInUserHavingMultipleRegistriesAddsItemToRegistryExpectItemAddedToRegistry() {

        // given
        final Registry registry = dataService.findRegistry("multiple-registry", "simple-pip");
        final ProductGroup productGroup = dataService.findProductGroup("simple-pip", "attributes", "swatch-selection");
        final String email = registry.getRegistrant().getEmail();
        final String password = registry.getRegistrant().getPassword();
        CrossBrandRegistryListPage registryListPage;
        final String itemSku = productGroup.getSkus().get(0);
        String registryId = registry.getExternalId();
        int expectedStillNeedQuantity = 1;

        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);

        // then
        RegistryFlows.deleteProductsFromRegistryListFlow(registryListPage);

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        PipFlows.setSimplePipAttributeSelections(productPage);
        RegistrySelectionOverlaySection registrySelectionOverlaySection = productPage.addToRegistryMultipleRegistriesPresent();
        RichAddToRegistryOverlaySection richAddToRegistryOverlaySection = registrySelectionOverlaySection.selectRegistryAndClickContinue(registryId);
        richAddToRegistryOverlaySection.viewRegistry();
        RegistryListItemRepeatableSection registryItemRepeatableSection = registryListPage.getItem(itemSku);
        String actualStillNeedQuantity = registryItemRepeatableSection.getStillNeedsQuantity();
        
        //then
        assertEquals("Expected still need quantity \'" + String.valueOf(expectedStillNeedQuantity)
                + "\' does not match with actual still need quantity \'" + actualStillNeedQuantity + "\'",
                String.valueOf(expectedStillNeedQuantity), actualStillNeedQuantity);
    }
    
    /**
     * Verifies if a signed-in user is not able to see rich add to registry overlay when user navigates
     * back to product page from search result page.
     * Test Case ID - RGSN-38222
     */
    @Test
    @TestRail(id = "105255")
    public void testProductPageWhenSignedInUserAddsItemToRegistryAndPerformsSearchNavigateBacktToProductPageExpectNoRegistryOverlayDisplayed() {

        // given
        final Registry registry = dataService.findRegistry("multiple-registry", "simple-pip");
        final ProductGroup productGroup = dataService.findProductGroup("simple-pip", "attributes", "swatch-selection");
        final String email = registry.getRegistrant().getEmail();
        final String password = registry.getRegistrant().getPassword();
        CrossBrandRegistryListPage registryListPage;
        String registryId = registry.getExternalId();
        String searchCriteria = productGroup.getGroupId().replaceAll("-", " ");

        // when
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);

        // then
        RegistryFlows.deleteProductsFromRegistryListFlow(registryListPage);

        // when
        ProductPage productPage = ShopFlows.goToPipPageFlow(context, productGroup);
        PipFlows.setSimplePipAttributeSelections(productPage);
        RegistrySelectionOverlaySection registrySelectionOverlaySection = productPage.addToRegistryMultipleRegistriesPresent();
        RichAddToRegistryOverlaySection richAddToRegistryOverlaySection = registrySelectionOverlaySection.selectRegistryAndClickContinue(registryId);
        richAddToRegistryOverlaySection.clickCloseButton();
        
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        navigationSection.search(searchCriteria);
        
        context.getPilot().getDriver().navigate().back();
        
        //then
        assertTrue("Rich Add to Registry Overlay is displayed when the user navigates back to pip page.",
                !productPage.isRichAddToRegistryOverlayPresentAndDisplayed());
    }
}
