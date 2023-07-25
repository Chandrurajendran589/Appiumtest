package com.wsgc.ecommerce.ui.regression.registry.syndication;

import com.wsgc.evergreen.api.EvergreenContext;

/**
 * A utility for building search results URIs.
 * 
 * @author asanath
 */
public class RegistryListUrlBuilder {

    /**
     * Builds a URL to open a registry list using registryID parameter
     * 
     * @return the constructed URL
     */
    public static String buildRegistryListUrl(EvergreenContext context, String registryId) {

        final String REGISTRY_URI_PATH = "registry";
        final String REGISTRY_LIST_URI_PATH = "registry-list.html";
        String baseUrl = context.getBaseUrl().getUrlAsString();
        String registryListUri = String.format("%s/%s/%s/%s", baseUrl, 
                    REGISTRY_URI_PATH, registryId, REGISTRY_LIST_URI_PATH);
        return registryListUri;
    }
}
