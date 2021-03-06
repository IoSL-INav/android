package de.tu_berlin.indoornavigation;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.util.Xml;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Nearable;
import com.estimote.sdk.Region;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.tu_berlin.indoornavigation.entities.Beacon;
import de.tu_berlin.indoornavigation.singletons.LocationSharingSingleton;
import de.tu_berlin.indoornavigation.singletons.PropertiesSingleton;
import de.tu_berlin.indoornavigation.singletons.VolleyQueueSingleton;

/**
 * Created by Jan on 30. 11. 2015.
 * <p/>
 * Class extends application. Initializes services.
 */
public class IndoorNavigation extends Application {

    private static final String TAG = IndoorNavigation.class.toString();
    private static final UUID ESTIMOTE_PROXIMITY_UUID = UUID.fromString
            ("B9407F30-F5F8-466E-AFF9-25556B57FE6D");
    private static final Region ALL_ESTIMOTE_BEACONS = new Region("rid", ESTIMOTE_PROXIMITY_UUID,
            null, null);
    private static Context mContext;
    private static BeaconManager beaconManager;
    private static String nearableScanId;
    private static ScheduledExecutorService scheduler;

    // getters and setters
    public static Context getContext() {
        return mContext;
    }

    /**
     * Method stops all estimote services and MSI scanning service.
     */
    public static void stopBeaconAndNearableDiscoveryAndMSIScanning() {

        if (beaconManager != null) {
            Log.d(TAG, "Stopping beacon and nearable scanning.");
            beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS);
            beaconManager.stopNearableDiscovery(nearableScanId);
            beaconManager.disconnect();
        }
        scheduler.shutdown();

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        Log.d(TAG, "Application started.");

        // if bluetooth adapter is available run beacon scanning
        if (BluetoothAdapter.getDefaultAdapter() != null) {
            // initialise beacon manager
            beaconManager = new BeaconManager(getApplicationContext());
            beaconManager.setForegroundScanPeriod(5000, 5000);

            // set nearable listener
            beaconManager.setNearableListener(new BeaconManager.NearableListener() {
                @Override
                public void onNearablesDiscovered(List<Nearable> list) {
                    Log.d(TAG, "onNearablesDiscovered listener");

                    // clear detected nearables
                    LocationSharingSingleton.getInstance().getDetectedNearables().clear();

                    // add newly detected nearables
                    for (Nearable nearable : list) {
                        Log.d(TAG, nearable.toString());

                        LocationSharingSingleton.getInstance().addDetectedNearable(new Beacon
                                (nearable.region.getProximityUUID().toString(), nearable.region.getMajor(),
                                        nearable.region.getMinor(), nearable.rssi));
                    }
                }
            });

            // set beacon listener
            beaconManager.setRangingListener(new BeaconManager.RangingListener() {
                @Override
                public void onBeaconsDiscovered(Region region, final List beacons) {
                    Log.d(TAG, "onBeaconsDiscovered listener");
                    Log.d(TAG, "Ranged beacons: " + beacons);

                    LinkedList<com.estimote.sdk.Beacon> beaconss = new LinkedList<>(beacons);

                    // clear detected beacons
                    LocationSharingSingleton.getInstance().getDetectedBeacons().clear();

                    // add newly detected beacons
                    for (com.estimote.sdk.Beacon beacon : beaconss) {
                        Log.d(TAG, beacon.toString());

                        LocationSharingSingleton.getInstance().addDetectedBeacon(new Beacon
                                (beacon.getProximityUUID().toString(), beacon.getMajor(), beacon.getMinor
                                        (), beacon.getRssi()));

                    }
                }
            });

            // run estimote beacon and nearable scanning services
            beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
                @Override
                public void onServiceReady() {

                    // Beacons ranging.
                    beaconManager.startRanging(ALL_ESTIMOTE_BEACONS);

                    // Nearable discovery.
                    nearableScanId = beaconManager.startNearableDiscovery();
                }
            });
        }

        // create service to monitor MSI API
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate
                (new Runnable() {
                    public void run() {

                        Log.d(TAG, "MSI scheduler run.");

                        WifiManager wifiManager = (WifiManager) getApplicationContext()
                                .getSystemService(Context.WIFI_SERVICE);

                        Log.d(TAG, "Current SSID: " + wifiManager.getConnectionInfo().getSSID()
                                .toString());

                        // if phone is connected to eduroam, scan MSI API URL
                        if (wifiManager.getConnectionInfo().getSSID().equals("\"eduroam\"")) {

                            String url = PropertiesSingleton.getInstance().getMsiUrl();

                            // Request a string response from the provided URL.
                            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {

                                            Log.d(TAG, "MSI response: " + response);

                                            // parse response, get building name and floor
                                            String[] buildingNameFloor = parseMsiApiResponse
                                                    (response);

                                            // save building name and floor info in
                                            // LocationSharingSingleton
                                            LocationSharingSingleton.getInstance()
                                                    .setMSIBuildingName(buildingNameFloor[0]);
                                            LocationSharingSingleton.getInstance().setMSIFloor
                                                    (buildingNameFloor[1]);
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e(TAG, "Error while making MSI request. " + error.toString
                                            ());
                                }
                            });

                            VolleyQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

                        }

                    }
                }, 0, 30, TimeUnit.SECONDS);

        // create service to automatically update location
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                // update location
                LocationSharingSingleton.getInstance().updateLocation();

            }
        }, 0, 15, TimeUnit.SECONDS);
    }

    /**
     * Function parses MSI API response and returns building name.
     *
     * @param response MSI API response.
     * @return Building name.
     */
    private String[] parseMsiApiResponse(String response) {

        String[] buildingNameFloor = new String[2];

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new ByteArrayInputStream(response.getBytes()), null);
            parser.next();
            if (parser.getAttributeCount() >= 2) {
                buildingNameFloor[0] = parser.getAttributeValue(2);
                buildingNameFloor[1] = parser.getAttributeValue(3);
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buildingNameFloor;

    }

}
