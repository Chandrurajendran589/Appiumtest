package com.wsgc.ecommerce.ui.regression.favorites.syndication;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

import org.openqa.selenium.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wsgc.ecommerce.ui.endtoend.favorites.SignInOverlayPageFlows;
import com.wsgc.ecommerce.ui.pagemodel.favorites.FavoritesGalleryPage;
import com.wsgc.ecommerce.ui.pagemodel.settings.ConfigurationSetting;
import com.wsgc.ecommerce.ui.pagemodel.settings.ConfigurationSettingGroup;
import com.wsgc.ecommerce.ui.pagemodel.settings.ConfigurationSettingsJsonPage;
import com.wsgc.evergreen.api.EvergreenContext;
import com.wsgc.evergreen.entity.api.CustomerAccount;

import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 * A utility for favorites API services.
 *
 * @author jkrishnan1
 *
 */
public final class FavoritesApiServices {
	
    private static Logger log = LoggerFactory.getLogger(FavoritesApiServices.class);
    
    private static EvergreenContext context;
	
    private static String FAVORITES_SERVICE_URL;
    
    private static String ACCESS_TOKEN_URI;

    private static String FAVORITES_DELETE_LIST_SERVICE_URL_TEMPLATE = "/lists/id/%s";
    
    private static String FAVORITES_ADD_ITEM_SERVICE_URL_TEMPLATE = "/lists/%s/items/";
    
    private static String FAVORITES_GET_LIST_SERVICE_URL_TEMPLATE = "/lists/market/%s/id/%s?brand=%s";
    
    private static String FAVORITES_CREATE_LIST_SERVICE_URL_TEMPLATE = "/lists/market/%s/id/%s";

    private static String ADD_ITEM_REQUEST_BODY_TEMPLATE = "{\n\"itemRefType\": \"GROUP\", \n  \"itemRefId\": \"%s\", \n  \"concept\": \"%s\" \n}";

    private static String CREATE_LIST_REQUEST_BODY_TEMPLATE = "{\n\"name\":\"My Favorites\",\n\"favoriteItems\": [\n{\n \"itemRefType\": "
    		+ "\"GROUP\", \n  \"itemRefId\": \"%s\", \n  \"concept\": \"%s\"  \n} \n]\n}";

    private static final String DEFAULT_MARKET_CODE = "USA";
    
    private static final String UAT_ENVIRONMENT_TEMPLATE = "uat";

    private static final String OUATH_UAT_AUTHORIZATION_VALUE = "Basic ZmF2b3JpdGVzL1VBVDpmYXZvcml0ZXNfdWF0";

    private static final String OUATH_QA_AUTHORIZATION_VALUE = "Basic ZmF2b3JpdGVzX3NlcnZpY2VzL1FBOmZhdm9yaXRlc19xYQ==";

    /**
     * Private constructor to prevent instantiation.
     */
    private FavoritesApiServices() {
        throw new IllegalStateException("This utility class cannot be instantiated");
    }
    
    /**
     * Prepares data for favorites services.
     */
    public static void setUp(EvergreenContext evergreenContext) {
        context = evergreenContext;
        ConfigurationSettingsJsonPage page = null;
        page = context.getPage(ConfigurationSettingsJsonPage.class);
        page.visit();
        try {
            FAVORITES_SERVICE_URL = page
                    .getConfigurationSettingByGroupAndName(ConfigurationSettingGroup.FAVORITES, ConfigurationSetting.SERVICE_URL).get().getValue();
            ACCESS_TOKEN_URI = page
                    .getConfigurationSettingByGroupAndName(ConfigurationSettingGroup.ACCOUNT_SERVICES, ConfigurationSetting.OAUTH_ACCESS_TOKEN_URI).get().getValue();
        } catch (IOException e) {
            log.info("Set Up for Favorites Services failed with Exception {}", e);
        }
    }
    
    /**
     * This method gets the profile id of the user from the site cookies.
     *
     * @param account The test customer account containing credentials
     *            to log in
     *
     * @return a string containing the user profile id.
     */
    public static String getProfileId(CustomerAccount account) {
        String userNameCookie = "UI_USERNAME_COOKIE";
        String profileId = null;
        FavoritesGalleryPage favoritesGalleryPage = context.getPage(FavoritesGalleryPage.class);
        context.getPilot().startAt(favoritesGalleryPage);
        SignInOverlayPageFlows.signInOnFavoritesGallery(context, false, account);
        Set<Cookie> siteCookies = context.getPilot().getDriver().manage().getCookies();
        for (Cookie cookie : siteCookies) {
            if (cookie.getName().equalsIgnoreCase(userNameCookie)) {
                profileId = cookie.getValue().split("\\|")[2].trim();
            }
        }
        return profileId;
    }
    
    /**
     * This method adds an item to a given favorites list.
     *
     * @param account The test customer account containing credentials
     *            to log in
     */
    public static void addItemToFavoritesList(CustomerAccount account) {
        String profileId = getProfileId(account);
        String favoritesListId = getFavoritesList(profileId);
        String accessToken = getFavoritesAccessToken();
        String favoritesServiceUri = String.format(FAVORITES_SERVICE_URL + FAVORITES_ADD_ITEM_SERVICE_URL_TEMPLATE, favoritesListId);
        String groupId = account.getUnmappedAttribute("groupId");
        String requestBody = String.format(ADD_ITEM_REQUEST_BODY_TEMPLATE, groupId, context.getBrand().getCode().toUpperCase());
        Response favoritesResponse = RestAssured.given().relaxedHTTPSValidation()
                .contentType("application/json")
                .header("Authorization", "Bearer " + accessToken)
                .and().body(requestBody).when()
                .post(favoritesServiceUri).then().extract().response();

        assertEquals(200, favoritesResponse.getStatusCode());
        System.out.print("Favorite List ID:" + getFavoritesList(profileId));
    }

    /**
     * This method is used to delete a given favorites list.
     *
     * @param context The current context object (contains current brand and
     *            experience)
     * @param account The test customer account containing credentials
     *            to log in
     */
    public static void deleteFavoritesList(CustomerAccount account) {
        String profileId = getProfileId(account);
        String accessToken = getFavoritesAccessToken();
        String favoritesServiceUri = String.format(FAVORITES_SERVICE_URL + FAVORITES_DELETE_LIST_SERVICE_URL_TEMPLATE, profileId);
        Response favoritesResponse = RestAssured.given().relaxedHTTPSValidation()
                .contentType("application/json")
                .header("Authorization", "Bearer " + accessToken)
                .delete(favoritesServiceUri).then().extract().response();
        
        assertEquals(200, favoritesResponse.getStatusCode());
    }
    
    /**
     * This method gets the favorites list id.
     *
     * @param profileId a string containing the user profile id
     *
     * @return a string containing the favorites list id.
     */
    public static String getFavoritesList(String profileId) {
        String favoritesListServiceUri = String.format(FAVORITES_SERVICE_URL + FAVORITES_GET_LIST_SERVICE_URL_TEMPLATE, getMarketCode(), 
                profileId, context.getBrand().getCode().toUpperCase());
        String accessToken = getFavoritesAccessToken();
        Response favoritesResponse = RestAssured.given().relaxedHTTPSValidation()
                .header("Authorization", "Bearer " + accessToken)
                .get(favoritesListServiceUri).then().extract().response();

        assertEquals(200, favoritesResponse.getStatusCode());
        String favoritesListId = favoritesResponse.getBody().jsonPath().get("identifier");
        return favoritesListId;
    }
    
    /**
     * This method creates a favorites list.
     * 
     * @param account The test customer account containing credentials
     *            to log in
     *
     * @return a string containing the favorites list id.
     */
    public static String createFavoritesList(CustomerAccount account) {
        String profileId = getProfileId(account);
        String favoritesListServiceUri = String.format(FAVORITES_SERVICE_URL + FAVORITES_CREATE_LIST_SERVICE_URL_TEMPLATE, getMarketCode(), profileId);
        String accessToken = getFavoritesAccessToken();
        String groupId = account.getUnmappedAttribute("groupId");
        String requestBody = String.format(CREATE_LIST_REQUEST_BODY_TEMPLATE, groupId, context.getBrand().getCode().toUpperCase());
        Response favoritesResponse = RestAssured.given().relaxedHTTPSValidation()
                .contentType("application/json")
                .header("Authorization", "Bearer " + accessToken)
                .and().body(requestBody).when()
                .post(favoritesListServiceUri).then().extract().response();

        assertEquals(200, favoritesResponse.getStatusCode());
        String favoritesListId = favoritesResponse.getBody().jsonPath().get("identifier");
        return favoritesListId;
    }
    
    /**
     * Gets a Favorites Enterprise Service access token by calling OAuth server.
     *
     * @return the access token
     */
    private static String getFavoritesAccessToken() {
        String grantTypeValue = "client_credentials";
        String accessTokenText = "access_token";
        String authorizationValue = getAuthorizationValue();
        Response oauthResponse = RestAssured.given().relaxedHTTPSValidation()
                .header("Authorization", authorizationValue)
                .queryParam("grant_type", grantTypeValue)
                .post(URI.create(ACCESS_TOKEN_URI)).then().extract().response();
        String accessToken = oauthResponse.getBody().jsonPath().get(accessTokenText);
        return accessToken;
    }
    
    /**
     * Gets the market code of the site.
     *
     * @return a string containing the market code of the site.
     */
    public static String getMarketCode() {
        String marketCode;
        if (context.getTargetEnvironment().getName().contains("-")) {
            marketCode = context.getTargetEnvironment().getName().split("-")[1];
            marketCode = marketCode.substring(0, 3).toUpperCase();
        } else {
            marketCode = DEFAULT_MARKET_CODE;
        }
        return marketCode;
    }
    
    /**
     * Gets the authorization value to call the OAuth server.
     *
     * @return the authorization value
     */
    public static String getAuthorizationValue() {
        String authorizationValue;
        if (context.getTargetEnvironment().getName().contains(UAT_ENVIRONMENT_TEMPLATE)) {
            authorizationValue = OUATH_UAT_AUTHORIZATION_VALUE;
        } else {
            authorizationValue = OUATH_QA_AUTHORIZATION_VALUE;
        }
        return authorizationValue;
    }
}
