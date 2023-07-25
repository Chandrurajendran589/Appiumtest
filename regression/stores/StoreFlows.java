package com.wsgc.ecommerce.ui.regression.stores;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wsgc.ecommerce.ui.endtoend.registry.RegistryFlows;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSection;
import com.wsgc.ecommerce.ui.pagemodel.NavigationSectionFactory;
import com.wsgc.ecommerce.ui.pagemodel.store.MyStoreOverlay;
import com.wsgc.evergreen.api.EvergreenContext;

/**
 * A group of reusable flows related to Storesfunctionality.
 *
 * @author ssanath
 *
 */
public final class StoreFlows {

    private static final Logger LOG = LoggerFactory.getLogger(RegistryFlows.class);

    /**
     * This is a utility class that should not get instantiated.
     */
    private StoreFlows() {
    }
    
    /**
     * Choose Store from Mystore overlay.
     *
     * @param context the evergreen context
     * @param postal code of Store
     * @return an instance of MyStoreOverlay
     */
    public static MyStoreOverlay chooseStore(EvergreenContext context, String postalCode) {
        MyStoreOverlay mystoreoverlayPage = null;
        MyStoreOverlay myStoreOverlay = context.getPageSection(MyStoreOverlay.class);
        myStoreOverlay.setPostalCode(postalCode);
        myStoreOverlay.clickSearchButton();
        myStoreOverlay.clickMyStoreButton();
        if (context.isMobileExperience()) {
            NavigationSection navigationSection = NavigationSectionFactory.getNavigationSection(context);
            navigationSection.clickShop();
        }
        mystoreoverlayPage = context.getPageSection(MyStoreOverlay.class);
        return mystoreoverlayPage;
    }
}
