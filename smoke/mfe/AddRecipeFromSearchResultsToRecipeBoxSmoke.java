package com.wsgc.ecommerce.ui.smoke.mfe;

import static org.junit.Assert.assertTrue;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.account.AccountFlows;
import com.wsgc.ecommerce.ui.pagemodel.HomePage;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSectionFactory;
import com.wsgc.ecommerce.ui.pagemodel.account.AccountHomePage;
import com.wsgc.ecommerce.ui.pagemodel.account.LoginPage;
import com.wsgc.ecommerce.ui.pagemodel.account.RecipeBoxPage;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchResultsItemRepeatableSection;
import com.wsgc.ecommerce.ui.pagemodel.search.SearchResultsPage;
import com.wsgc.ecommerce.ui.pagemodel.shop.RecipePage;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.RecipeGroup;
import com.wsgc.evergreen.impl.DefaultEvergreenContext;

import org.junit.Before;
import org.junit.Test;

/**
 * Smoke Tests that validates that user is able to add recipe to recipe box from search results.
 */
public class AddRecipeFromSearchResultsToRecipeBoxSmoke extends AbstractTest {

    /**
     * Prepares user data by deleting existing recipe from recipe box.
     */
    @Before
    public void setUp() {
        assumeFeatureIsSupported(FeatureFlag.RECIPE_PAGES);
        RecipeGroup recipe = dataService.findRecipeGroup("recipe-search");
        final String userName = recipe.getUnmappedAttribute("username");
        final String password = recipe.getUnmappedAttribute("password");
        LoginPage loginPage = context.getPage(LoginPage.class);
        startPilotAt(loginPage);
        AccountFlows.signIn(context, userName, password, AccountHomePage.class);
        RecipeBoxPage recipeBox = context.getPage(RecipeBoxPage.class);
        startPilotAt(recipeBox);
        if (recipeBox.isRecipeDisplayedInRecipeBox(recipe.getRecipeId())) {
            recipeBox.clickRemoveThisRecipeLink(recipe.getRecipeId());
        }
        assertTrue("Recipe is not removed from the recipe box", !recipeBox.isRecipeDisplayedInRecipeBox(recipe.getRecipeId()));
        // Close Browser session and re-launch
        context.getPilot().getDriver().close();
        context = DefaultEvergreenContext.getContext();
        DefaultEvergreenContext.getLifeCycleManager().beforeEachTest();
    }
    
    /**
     * Verifies whether the user is able to add recipe to recipe box from search result.
     * 
     */
    @Test
    public void testAddRecipeToRecipeBoxFromSearchResultsExpectRecipeAddedToRecipeBox() {

        //given
        RecipeGroup recipe = dataService.findRecipeGroup("recipe-search");
        final String userName = recipe.getUnmappedAttribute("username");
        final String password = recipe.getUnmappedAttribute("password");
        final String searchKeyword = recipe.getUnmappedAttribute("searchKeyword");

        //when
        startPilotAt(context.getPage(HomePage.class));
        NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
        SearchResultsPage searchResultsPage = navigationSection.search(searchKeyword);
        searchResultsPage.waitForSearchResultsHeading();
        
        //then
        assertTrue("Recipe tab is not displayed ", searchResultsPage.isRecipesTabDisplayed());
        
        //when
        searchResultsPage.clickRecipesTab();
        SearchResultsItemRepeatableSection recipeSection = searchResultsPage.getRecipeSectionByGroupId(recipe.getRecipeId());
        RecipePage recipePage = recipeSection.goToRecipePage(recipe.getRecipeId());
        if (context.isFullSiteExperience()) {
            recipePage.clickAddToRecipeBoxLink(LoginPage.class);
            AccountHomePage accountHome = AccountFlows.signIn(context, userName, password, AccountHomePage.class);
            RecipeBoxPage recipeBox = accountHome.goToRecipeBoxPage();
            assertTrue("Recipe is not added to the recipe box", recipeBox.isRecipeDisplayedInRecipeBox(recipe.getRecipeId()));  
        }
    }
    
}
