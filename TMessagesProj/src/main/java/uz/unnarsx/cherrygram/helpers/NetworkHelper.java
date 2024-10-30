package uz.unnarsx.cherrygram.helpers;

import org.telegram.messenger.Utilities;

public class NetworkHelper {

    public static final String[] deviceModels = {
            "Galaxy S6", "Galaxy S7", "Galaxy S8", "Galaxy S9", "Galaxy S10", "Galaxy S21",
            "Pixel 3", "Pixel 4", "Pixel 5",
            "OnePlus 6", "OnePlus 7", "OnePlus 8", "OnePlus 9", "Xperia XZ", "Xperia XZ2", "Xperia XZ3", "Xperia 1", "Xperia 5", "Xperia 10", "Xperia L4"
    };
    private static final String[] chromeVersions = {
            "111.0.5563.57", "94.0.4606.81", "80.0.3987.119", "69.0.3497.100", "92.0.4515.159", "71.0.3578.99"
    };

    public static String formatUserAgent() {
        String androidVersion = String.valueOf(Utilities.random.nextInt(7) + 6);
        String deviceModel = deviceModels[Utilities.random.nextInt(deviceModels.length)];
        String chromeVersion = chromeVersions[Utilities.random.nextInt(chromeVersions.length)];
        return String.format("Mozilla/5.0 (Linux; Android %s; %s) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/%s Mobile Safari/537.36", androidVersion, deviceModel, chromeVersion);
    }

}
