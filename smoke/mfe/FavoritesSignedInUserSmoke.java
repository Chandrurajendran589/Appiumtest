package com.wsgc.ecommerce.ui.smoke.mfe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.favorites.SignInOverlayPageFlows;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSectionFactory;
import com.wsgc.ecommerce.ui.pagemodel.favorites.FavoritesGalleryPage;
import com.wsgc.ecommerce.ui.pagemodel.favorites.FavoritesItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.shop.ProductPage;
import com.wsgc.evergreen.entity.api.CustomerAccount;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tag.area.FavoritesArea;


/**
 * Smoke Tests that validates that User is able to add , view and share favorite items.
 */
@Category(FavoritesArea.class)
public class FavoritesSignedInUserSmoke extends AbstractTest {
    
    /**
     * Verifies whether the user is able to sign in with an account created in
     * the same brand.
     */
    @Test
    public void testFavoritesGalleryWhenCustomerSignsInExpectFavoritesGalleryWithItems() {
        //given
        String productName;
        CustomerAccount account = dataService.findAccount("favorites-available");
        List<Entry<String, String>> productNames = account.getUnmappedAttributes().entrySet().stream().filter(entry -> entry.getKey().contains("productName"))
                .sorted(Map.Entry.comparingByKey()).collect(Collectors.toList());
        List<Entry<String, String>> groupIds = account.getUnmappedAttributes().entrySet().stream().filter(entry -> entry.getKey().contains("groupId"))
                .sorted(Map.Entry.comparingByKey()).collect(Collectors.toList());
        Iterator<Entry<String, String>> productNamesIterator = productNames.iterator();
        
        //when
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        startPilotAt(favoritesGalleryPage);
        SignInOverlayPageFlows.signInOnFavoritesGallery(context, false, account);
 
        //then
        favoritesGalleryPage.waitForFavoriteGallerySpinnerToDisappear();
        assertTrue("Favorites Gallery is not populated with items.", favoritesGalleryPage.isFavoriteGalleryPopulatedWithItems());
        if (navigationSection.isFavoritesIconIntheHeaderDisplayed()) {
            assertEquals(String.valueOf(favoritesGalleryPage.getfavoriteCount()), String.valueOf(favoritesGalleryPage.getFavoriteCountFromGlobalHeader()));
        }
        assertEquals(String.valueOf(productNames.size()), String.valueOf(favoritesGalleryPage.getfavoriteCount()));
        
        for (Entry<String, String> groupId : groupIds) {
            productName = productNamesIterator.next().getValue();
            FavoritesItemRepeatableSection favoritesItemRepeatableSection = favoritesGalleryPage.getProductSectionByGroupId(groupId.getValue());
            if (favoritesGalleryPage.isGridViewToggleButtonPresent()) {
                favoritesGalleryPage.clickGridViewToggleButton();
            }
            assertTrue(productName.equalsIgnoreCase(favoritesItemRepeatableSection.getProductName()));
        }
        
        String groupID = groupIds.get(0).getValue();
        FavoritesItemRepeatableSection favoritesItemRepeatableSection = favoritesGalleryPage.getProductSectionByGroupId(groupID);        
        ProductPage productPage = favoritesItemRepeatableSection.clickProductName();
        String productPageUrl = productPage.getCurrentPageUrl();
        Assert.assertTrue("Group ID of the product from Favorites Page \'" + groupID + "\' is not matching with Group ID of the product in PIP page URL\'"
                + productPageUrl + "\'", productPageUrl.contains(groupID));
    }
}
