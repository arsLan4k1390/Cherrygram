package org.telegram.messenger;

import org.telegram.messenger.regular.BuildConfig;
import org.telegram.tgnet.TLRPC;

public class ApplicationLoaderImpl extends ApplicationLoader {
    @Override
    protected String onGetApplicationId() {
        return BuildConfig.APPLICATION_ID;
    }
}
