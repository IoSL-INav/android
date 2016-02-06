package de.tu_berlin.indoornavigation.singletons;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import de.tu_berlin.indoornavigation.IndoorNavigation;

/**
 * Created by Jan on 1. 12. 2015.
 * <p/>
 * Singleton reads properties file at initialization and store read properties for further use.
 */
public class PropertiesSingleton {

    private static PropertiesSingleton mInstance;

    // tubIt MSI API URL
    private String msiUrl;
    // backend server URL
    private String backendServerUrl;

    /**
     * Read properties file at initialization.
     */
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

    /**
     * Get singleton instance.
     *
     * @return
     */
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
