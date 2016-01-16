package de.tu_berlin.indoornavigation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Jan on 1. 12. 2015.
 */
public class PropertiesSingleton {

    private static PropertiesSingleton mInstance;
    private String msiUrl;
    private String backendServerUrl;

    public PropertiesSingleton() {
        Properties prop = new Properties();
        InputStream input;

        try {
            input = IndoorNavigation.getContext().getAssets().open("config.properties");
            prop.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        msiUrl = prop.getProperty("msiUrl");
        backendServerUrl = prop.getProperty("backendServerUrl");
    }

    public static synchronized PropertiesSingleton getInstance() {
        if (mInstance == null) {
            mInstance = new PropertiesSingleton();
        }
        return mInstance;
    }

    public String getMsiUrl() {
        return this.msiUrl;
    }

    public String getBackendServerUrl() {
        return this.backendServerUrl;
    }

}
