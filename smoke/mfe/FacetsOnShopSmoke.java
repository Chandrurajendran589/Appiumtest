package com.wsgc.ecommerce.ui.smoke.mfe;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.shop.ShopFlows;
import com.wsgc.ecommerce.ui.pagemodel.shop.category.CategoryPage;
import com.wsgc.evergreen.entity.api.ProductGroup;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tag.area.ShopArea;

/**
 * Smoke Tests for Facets on Shop page.
 */
@Category(ShopArea.class)
public class FacetsOnShopSmoke extends AbstractTest {
    /**
     * Verifies whether the products are filtered accordingly on shop page, when user selected single facet.
     */
    @Test
    public void testShopFacetsWhenProductsAreFilteredWithSingleFacetExpectedFilteredResults() {
        // given
        final ProductGroup expectedProductGroup = dataService
                .findProductGroup(productGroup -> productGroup.getUnmappedAttribute("categoryUrl") != null,
                        "simple-pip", "attributes");
        String productCategoryUrl = expectedProductGroup.getUnmappedAttribute("categoryUrl");
        int facetGroup = 3;
        int facetValue = 1;

        // when
        CategoryPage categoryPage = ShopFlows.navigateToCategoryPage(context, productCategoryUrl);
        categoryPage.waitForCategoryPageLoad();

        // then
        Assert.assertTrue("User specified Facet Group: " + facetGroup +" or " + "Facet Value: " + facetValue + ""
                + " is not present", categoryPage.isFacetsDisplayed(facetGroup, facetValue));
        String hrefOfChosenFacet = categoryPage.getHrefAttributeOfGivenFacetRefinement(facetGroup, facetValue);
        int numberOfProductsForTheGivenFacetRefinement = categoryPage.getNumberOfProductsForGivenFacetRefinement(facetGroup, facetValue);
        categoryPage.clickFacetRefinement(facetGroup, facetValue);
        /*
        We are refreshing the page after facets selection, to avoid race condition errors.
         */
        context.getPilot().refresh();
        String currentPageURL = categoryPage.getCurrentPageUrl();
        if(currentPageURL.contains("?cm_type=lnav")) {
            currentPageURL = currentPageURL.replaceAll("\\?cm_type=lnav","");
        }
        Assert.assertEquals("User specified facet is not selected", hrefOfChosenFacet, currentPageURL);
        int numberOfProductsAfterSelectingTheGivenFacetRefinement = categoryPage.getNumberOfProductsLoaded();
        Assert.assertEquals("Incorrect number of products were filtered upon facet selection", numberOfProductsForTheGivenFacetRefinement,
                numberOfProductsAfterSelectingTheGivenFacetRefinement);
    }
}
