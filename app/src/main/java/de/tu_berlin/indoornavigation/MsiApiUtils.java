package de.tu_berlin.indoornavigation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Jan on 1. 12. 2015.
 */
public class MsiApiUtils {

    private static MsiApiUtils mInstance;
    private String url;

    public MsiApiUtils() {
        Properties prop = new Properties();
        InputStream input;

        try {
            input = IndoorNavigation.getContext().getAssets().open("config.properties");
            prop.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        url = prop.getProperty("msiUrl");
    }

    public static synchronized MsiApiUtils getInstance() {
        if (mInstance == null) {
            mInstance = new MsiApiUtils();
        }
        return mInstance;
    }

    public String getUrl() {
        return this.url;
    }

}
