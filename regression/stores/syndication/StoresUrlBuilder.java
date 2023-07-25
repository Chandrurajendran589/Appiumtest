package com.wsgc.ecommerce.ui.regression.stores.syndication;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * A utility for building end point URIs.
 * 
 * @author jkrishnan1
 *
 */
public final class StoresUrlBuilder {
    
    private static final String STORE_LANDING_URI_PATH = "/stores/%s";

    /**
     * Private constructor to prevent instantiation.
     */
    private StoresUrlBuilder() {
        throw new IllegalStateException("This utility class cannot be instantiated");
    }
    
    /**
     * Builds a URI consisting in the store landing path 
     * with a provided store ID.
     * 
     * @param storeId the store id
     * @return the constructed URI
     */
    public static URI buildStoreLandingUri(String storeId) {
        try {
            return new URI(String.format(STORE_LANDING_URI_PATH, storeId));
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
