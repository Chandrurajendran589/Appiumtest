package com.wsgc.ecommerce.ui.regression.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Month;
import java.util.List;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.account.AccountFlows;
import com.wsgc.ecommerce.ui.pagemodel.account.AccountHomePage;
import com.wsgc.ecommerce.ui.pagemodel.registry.CrossBrandRegistryListPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.find.FindRegistryPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.registrylist.RegistryListItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.RichAddToCartOverlaySection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartHeaderRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartPage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.CustomerAccount;
import com.wsgc.evergreen.entity.api.MutableAccount;
import com.wsgc.evergreen.entity.impl.FindRegistryRequest;

import tag.area.RegistryArea;

/**
 * Regression tests to validate Find Registry on brand site.
 *
 * @author knamboothiry
 */
@Category(RegistryArea.class)
public class FindRegistryRegression extends AbstractTest {

    /**
     * Prepares data fixtures & ensures feature flag is enabled.
     */
    @Before
    public void setUp() {
        Assume.assumeTrue(context.supportsFeature(FeatureFlag.FIND_REGISTRY));
    }
 
    /**
     *
     * Verify whether signed in user is able to navigate to Registry list page
     * from Find registry and able to add product to cart.
     * Test case ID - RGSN-38260
     */
    @Test
    @TestRail(id = "102650")
    public void testAddToCartFromRegistryWhenUserFindsARegistryExpectItemAddedToCart() {

        // given
        FindRegistryRequest findRegistry = dataService.getFindRegistryRequest("find-registry-common");
        MutableAccount mutableAccount = getMutableAccount("default");
        CustomerAccount newAccount = accountCreator.getRandomEmailAccount(mutableAccount);
        String registryId = findRegistry.getUnmappedAttribute("registryId");
        String expectedQuantity = "2";
        String eventMonth = Month.of(Integer.parseInt(findRegistry.getEventMonth())).name();
        String eventyear = findRegistry.getEventYear();
        final int expectedTotalItem = 1;

        // when
        AccountFlows.goToLoginPageFlow(context);
        AccountFlows.createAccount(context, newAccount, AccountHomePage.class);
        FindRegistryPage findRegistryPage = context.getPage(FindRegistryPage.class);
        startPilotAt(findRegistryPage);
        findRegistryPage.setFirtsName(findRegistry.getFirstName());
        findRegistryPage.setLastName(findRegistry.getLastName());
        findRegistryPage.clickFindRegistryButton();
        
        //then
        String actualEventDate = findRegistryPage.getEventDate(registryId).toLowerCase();
        assertTrue("Incorrect Event date displayed : actual " + actualEventDate + " expected " + eventMonth + " " + eventyear,
                actualEventDate.startsWith(eventMonth.toLowerCase()) && actualEventDate.endsWith(eventyear));
        
        //when
        CrossBrandRegistryListPage registryListPage = findRegistryPage.clickRegistryName(registryId);
        RegistryListItemRepeatableSection registryListRepeatableSection = registryListPage.getFirstRegistryListItem();
        String sku = registryListRepeatableSection.getItemSku();
        RichAddToCartOverlaySection richAddToCartOverlaySection = registryListRepeatableSection.setQuantityAndAddToCart(expectedQuantity);

        ShoppingCartPage shoppingCartPage = richAddToCartOverlaySection.goToShoppingCartPage();

        // then
        List<ShoppingCartItemRepeatableSection> itemSectionList = shoppingCartPage.getShoppingCartItems();
        assertEquals(String.format("The shopping cart should have %s item.", expectedTotalItem), expectedTotalItem,
                itemSectionList.size());
        ShoppingCartItemRepeatableSection item = shoppingCartPage.getFirstShoppingCartItem();
        assertTrue(String.format("The shopping cart item's sku should ends with '%s'. Actual sku: '%s'", sku,
                item.getSku()), item.getSku().endsWith(sku.toString()));
        assertEquals(String.format("The shopping cart item's quantity should be '%s'", expectedQuantity),
                String.valueOf(expectedQuantity), item.getQuantity());
        List<ShoppingCartHeaderRepeatableSection> headerSectionList = shoppingCartPage.getSuborderHeadersOnCart();
        assertEquals(String.format("The shopping cart should have %s header.", expectedTotalItem), expectedTotalItem,
                headerSectionList.size());
    }

    /**
     * Verifies that user details are not appended to the URL when user perform find registry using first name and last name.
     * 
     * Test case ID - RGSN-38705
     */
    @Test
    public void testFindRegistryPageWhenUserFindsARegistryExpectUserDetailsNotAppendedInPageUrl() {

        // given
        FindRegistryRequest findRegistry = dataService.getFindRegistryRequest("find-registry-common");

        // when
        FindRegistryPage findRegistryPage = context.getPage(FindRegistryPage.class);
        startPilotAt(findRegistryPage);
        String findRegistryUrl = context.getPilot().getDriver().getCurrentUrl();
        findRegistryPage.setFirtsName(findRegistry.getFirstName());
        findRegistryPage.setLastName(findRegistry.getLastName());
        findRegistryPage.clickFindRegistryButton();

        // then
        assertTrue("Find Registry URL and Find Registry result URLs are not same", 
     		   findRegistryUrl.equalsIgnoreCase(context.getPilot().getDriver().getCurrentUrl()));

        // when
        context.getPilot().getDriver().navigate().refresh();
        String firstNameValue = findRegistryPage.getfirstName();
        String lastNameValue = findRegistryPage.getlastName();
        assertTrue("The First Name field is not empty", firstNameValue.isEmpty());
        assertTrue("The Last Name field is not empty", lastNameValue.isEmpty());
    }
}
