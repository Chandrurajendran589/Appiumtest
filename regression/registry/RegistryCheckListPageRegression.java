package com.wsgc.ecommerce.ui.regression.registry;

import static com.wsgc.ecommerce.ui.pagemodel.registry.RegistryChecklistPage.CHILD_CATEGORY_ADDED_COUNT_TEXT_PATTERN;
import static com.wsgc.ecommerce.ui.pagemodel.registry.RegistryChecklistPage.PARENT_CATEGORY_COMPLETED_COUNT_TEXT_PATTERN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wsgc.ecommerce.ui.endtoend.AbstractTest;
import com.wsgc.ecommerce.ui.endtoend.TestRail;
import com.wsgc.ecommerce.ui.endtoend.account.AccountFlows;
import com.wsgc.ecommerce.ui.endtoend.registry.RegistryFlows;
import com.wsgc.ecommerce.ui.pagemodel.account.AccountHomePage;
import com.wsgc.ecommerce.ui.pagemodel.account.LoginPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.CrossBrandRegistryListPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.RegistryChecklistPage;
import com.wsgc.ecommerce.ui.pagemodel.registry.RegistryChecklistPage.CategoryType;
import com.wsgc.evergreen.api.FeatureFlag;
import com.wsgc.evergreen.entity.api.Registry;

import tag.area.RegistryArea;

/**
 * Regression Tests for validating Registry check list page.
 * 
 * @author dkang
 *
 */
@Category(RegistryArea.class)
public class RegistryCheckListPageRegression extends AbstractTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private RegistryChecklistPage registryChecklistPage;

    private Registry registry;

    /**
     * Sets up configuration before each test.
     */
    @Before
    public void setup() {
        Assume.assumeTrue(context.getTargetSite().supportsFeature(FeatureFlag.REGISTRY_V2));
        registry = dataService.findRegistry("checklist-registry");
        final String email = registry.getRegistrant().getEmail();
        final String password = registry.getRegistrant().getPassword();
        LoginPage loginPage = context.getPage(LoginPage.class);
        context.getPilot().startAt(loginPage);
        AccountFlows.signIn(context, email, password, AccountHomePage.class);
        CrossBrandRegistryListPage registryListPage = RegistryFlows.goToRegistryListPageFlow(context, registry);
        registryChecklistPage = registryListPage.clickCheckListTab();
    }

    /**
     * Verifies that registry check list page has valid header name and image.
     * Test Case ID - RGSN-38276
     */
    @Test
    @TestRail(id = "103012")
    public void testRegistryChecklistWhenChecklistPageDisplayedExpectValidHeaderAndHeroImage() {

        //given
        String expectedHeader = String.format("%s's Checklist", registry.getRegistrant().getFirstName());

        //when
        String actualHeader = registryChecklistPage.getChecklistHeader();
        WebElement heroImage = registryChecklistPage.getHeaderHeroImage();

        //then
        assertNotNull("hero image should not be null", heroImage);
        assertTrue("hero image url shoud not be blank", StringUtils.isNotBlank(heroImage.getAttribute("src")));
        assertEquals("actual header should be equal to exptected header", expectedHeader, actualHeader);
    }

    /**
     * Verifies that all parent categories in registry check list page has
     * valid name, image and count text. Test Case ID - RGSN-38276
     */
    @Test
    @TestRail(id = "103013")
    public void testRegistryChecklistWhenChecklistPageDisplayedExpectAllParentCategoriesHaveValidNameAndImageAndCompletedCountText() {

        //given
        List<WebElement> parentCategories = registryChecklistPage.getAllParentCategories();
        
        //then
        for (WebElement category : parentCategories) {
            assertTrue("parent category should have a valid name", hasValidName(category, CategoryType.PARENT));
            assertTrue("parent category should have a valid image", hasValidImage(category));
            assertTrue("parent category should have a valid count text", hasValidCountText(category, CategoryType.PARENT));
        }
    }

    /**
     * Verifies that all child categories for each parent category in registry
     * check list page has valid name and count text. Test Case ID -
     * RGSN-38276
     */
    @Test
    @TestRail(id = "103014")
    public void testRegistryChecklistWhenChecklistPageDisplayedExpectAllChildCategoriesHaveValidNameAndAddedCountText() {
    	
        //given
        List<WebElement> parentCategories = registryChecklistPage.getAllParentCategories();
        
        //then
        for (WebElement parentCategory : parentCategories) {
            List<WebElement> childCategories = registryChecklistPage.getAllChildCategoriesForParentCategory(parentCategory);
            for (WebElement childCategory : childCategories) {
                assertTrue("child category should have a valid name", hasValidName(childCategory, CategoryType.CHILD));
                assertTrue("child category should have a valid count text", hasValidCountText(childCategory, CategoryType.CHILD));
            }

        }
    }

    /**
     * Check if a given category has valid name.
     * 
     * @param category the category
     * @param categoryType the category type
     * @return true if the given category has valid name
     */
    private boolean hasValidName(WebElement category, CategoryType categoryType) {
        String categoryName = registryChecklistPage.getCategoryName(category, categoryType);
        log.debug("CategoryType:{} Name:{}", categoryType, categoryName);
        return StringUtils.isNotBlank(categoryName);
    }

    /**
     * Check if a given parent category has valid image.
     * 
     * @param category the parent category
     * @return true if the given category has valid image
     */
    private boolean hasValidImage(WebElement category) {
        WebElement image = registryChecklistPage.getParentCategoryImage(category);
        boolean isImageDisplayed = (image != null && StringUtils.isNotBlank(image.getAttribute("src")));
        String categoryName = registryChecklistPage.getCategoryName(category, CategoryType.PARENT);
        if (!isImageDisplayed) {
            log.warn("Image is not found in the CategoryType:PARENT Name:{}", categoryName);
        } else {
            log.debug("CategoryType:PARENT Name:{} ImageUrl:{}", categoryName, image.getAttribute("src"));
        }

        return isImageDisplayed;
    }

    /**
     * Check if a given category has valid count text (X out of Y).
     * 
     * @param category the category
     * @param categoryType the category type
     * @return true if the given category has valid count text
     */
    private boolean hasValidCountText(WebElement category, CategoryType categoryType) {
        String categoryCountText = registryChecklistPage.getCategoryCountText(category, categoryType);
        String categoryName = registryChecklistPage.getCategoryName(category, categoryType);
        if (log.isDebugEnabled()) {
            log.debug("CategoryType:{} Name:{} CounterText:{}", categoryType, categoryName, categoryCountText);
        }
        Pattern countTextPattern = categoryType == CategoryType.PARENT ? PARENT_CATEGORY_COMPLETED_COUNT_TEXT_PATTERN : CHILD_CATEGORY_ADDED_COUNT_TEXT_PATTERN;
        boolean isPatternMatched = countTextPattern.matcher(categoryCountText).matches();
        if (!isPatternMatched) {
            log.warn("CategoryType:{} Name:{} CounterText:{} doesn't match to the pattern", categoryType, categoryName, categoryCountText);
        }
        return isPatternMatched;
    }

}
