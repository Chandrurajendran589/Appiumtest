package com.wsgc.ecommerce.ui.smoke.mfe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartHeaderRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.shoppingcart.ShoppingCartPage;
import com.wsgc.evergreen.entity.api.ProductGroup;
import java.util.List;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tag.area.ShopArea;

/**
 * Smoke Tests for Adding Gift Card Items to the cart.
 */
@Category(ShopArea.class)
public class AddGiftCardToCartSmoke extends AbstractTest {

    /**
     * Verifies whether the shopper is able to add a gift card to the cart with gift card message.
     */
    @Test
    public void testAddToCartWhenProductIsGiftCardWithMessageExpectCartHasProductAndMessage() {

        // given
        final int itemIndex = 0;
        final int expectedTotalItem = 1;
        final int expectedQuantity = 1;
        final String expectedToFieldValue = "ToFieldValue";
        final String expectedFromFieldValue = "FromFieldValue";
        final String expectedMSG1FieldValue = "MsgOneFieldValue";
        final String expectedMSG2FieldValue = "MsgTwoFieldValue";
        final ProductGroup expectedProductGroup = dataService.findProductGroup("gift-card");
        final String expectedSku = expectedProductGroup.getSkus().get(itemIndex);

        // when
        ShoppingCartPage shoppingCartPage = ShopFlows.addGiftCardToCartSetSimplePipAttributeSelectionsEnterGiftCardMessageFlow(context, expectedProductGroup,
                String.valueOf(expectedQuantity), expectedToFieldValue, expectedFromFieldValue,
                expectedMSG1FieldValue, expectedMSG2FieldValue);

        shoppingCartPage.scrollIntoCartSection();

        // then
        List<ShoppingCartItemRepeatableSection> itemSectionList = shoppingCartPage.getShoppingCartItems();
        assertEquals(String.format("The shopping cart should have %s item.", expectedTotalItem), expectedTotalItem,
                itemSectionList.size());
        ShoppingCartItemRepeatableSection item = shoppingCartPage.getFirstShoppingCartItem();
        final String[] giftCardMessage = item.getGiftCardMessage();
        assertTrue(String.format("The shopping cart item's sku should ends with '%s'. Actual sku: '%s'", expectedSku,
                item.getSku()), item.getSku().endsWith(expectedSku));
        assertEquals(String.format("The shopping cart item's quantity should be '%s'", expectedQuantity),
                String.valueOf(expectedQuantity), item.getQuantity());

        List<ShoppingCartHeaderRepeatableSection> headerSectionList = shoppingCartPage.getSuborderHeadersOnCart();
        assertEquals(String.format("The shopping cart should have %s header.", expectedTotalItem), expectedTotalItem,
                headerSectionList.size());

        assertEquals(String.format("The To value should be: %s", String.format("to: %s", expectedToFieldValue.toLowerCase())),
                String.format("to: %s", expectedToFieldValue.toLowerCase()), giftCardMessage[0].toLowerCase());
        assertEquals(String.format("The From value should be: %s", String.format("from: %s", expectedFromFieldValue.toLowerCase())),
                String.format("from: %s", expectedFromFieldValue.toLowerCase()), giftCardMessage[1].toLowerCase());
        assertEquals(
                String.format("The Message value should be: %s",
                        String.format("message: %s %s", expectedMSG1FieldValue.toLowerCase(), expectedMSG2FieldValue.toLowerCase())),
                String.format("message: %s %s", expectedMSG1FieldValue.toLowerCase(), expectedMSG2FieldValue.toLowerCase()), giftCardMessage[2].toLowerCase());
    }
}
