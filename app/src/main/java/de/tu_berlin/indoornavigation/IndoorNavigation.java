package de.tu_berlin.indoornavigation;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.wifi.WifiManager;
import android.support.v4.app.NotificationCompat;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jan on 30. 11. 2015.
 */
public class IndoorNavigation extends Application {

    private static final String TAG = IndoorNavigation.class.toString();
    private static final UUID ESTIMOTE_PROXIMITY_UUID = UUID.fromString
            ("B9407F30-F5F8-466E-AFF9-25556B57FE6D");
    private static final Region ALL_ESTIMOTE_BEACONS = new Region("rid", ESTIMOTE_PROXIMITY_UUID,
            null, null);
    private static Context mContext;
    private static ArrayList<Group> groups;
    private BeaconManager beaconManager;

    public static Context getContext() {
        return mContext;
    }

    public static ArrayList<Group> getGroups() {
        return IndoorNavigation.groups;
    }

    public static void setGroups(ArrayList<Group> groups) {
        IndoorNavigation.groups = groups;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        Log.d(TAG, "Application started.");

        // initialise beacon manager
        beaconManager = new BeaconManager(getApplicationContext());
        beaconManager.setForegroundScanPeriod(5000, 5000); // TODO: set appropriate time
        beaconManager.setNearableListener(new BeaconManager.NearableListener() {
            @Override
            public void onNearablesDiscovered(List<Nearable> list) {
                Log.d(TAG, "onNearablesDiscovered listener");

                // clear detected nearables
                HotspotDataSingleton.getInstance().getDetectedNearables().clear();

                // add newly detected nearables
                for (Nearable nearable : list) {
                    Log.d(TAG, nearable.toString());

                    HotspotDataSingleton.getInstance().addDetectedNearable(new Beacon
                            (nearable.region.getProximityUUID().toString(), nearable.region.getMajor(),
                                    nearable.region.getMinor(), nearable.rssi));
                }
            }
        });

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List beacons) {
                Log.d(TAG, "onBeaconsDiscovered listener");
                Log.d(TAG, "Ranged beacons: " + beacons);

                LinkedList<com.estimote.sdk.Beacon> beaconss = new LinkedList<>(beacons);

                // clear detected beacons
                HotspotDataSingleton.getInstance().getDetectedBeacons().clear();


                for (com.estimote.sdk.Beacon beacon : beaconss) {
                    Log.d(TAG, beacon.toString());

                    HotspotDataSingleton.getInstance().addDetectedBeacon(new Beacon
                            (beacon.getProximityUUID().toString(), beacon.getMajor(), beacon.getMinor
                                    (), beacon.getRssi()));

                }
            }
        });

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {

                // Beacons ranging.
                beaconManager.startRanging(ALL_ESTIMOTE_BEACONS);

                // Nearable discovery.
                beaconManager.startNearableDiscovery();
            }
        });


        // create service to monitor MSI API
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate
                (new Runnable() {
                    public void run() {

                        Log.d(TAG, "Scheduler run.");

                        WifiManager wifiManager = (WifiManager) getApplicationContext()
                                .getSystemService(Context.WIFI_SERVICE);

                        Log.d(TAG, "Current SSID: " + wifiManager.getConnectionInfo().getSSID()
                                .toString());

                        if (wifiManager.getConnectionInfo().getSSID().equals("\"eduroam\"")) {

                            String url = PropertiesSingleton.getInstance().getMsiUrl();

                            // Request a string response from the provided URL.
                            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            String buildingName = parseMsiApiResponse(response);
                                            if (HotspotUtils.getHotspots().contains(buildingName)) {
                                                Log.d(TAG, buildingName);
                                                createOnHotspotEnteredNotification(buildingName);
                                            } else {
                                                Log.d(TAG, "Current building is not a hotspot.");
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e(TAG, "Error while making request");
                                }
                            });

                            VolleyQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

                        }

                    }
                }, 0, 1, TimeUnit.MINUTES);

        // update location
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "All beacons and nearables");
                for (Beacon beacon : HotspotDataSingleton.getInstance().getDetectedNearablesAndBeacons()) {
                    Log.d(TAG, beacon.toString());
                }
                Log.d(TAG, "closest beacon or nearable: " + HotspotDataSingleton.getInstance()
                        .getClosestNearableOrBeacon());
            }
        }, 0, 15, TimeUnit.SECONDS);
    }

    /**
     * Creates notification. Not finished jet.
     *
     * @param hotspot Name of the hotspot.
     */
    private void createOnHotspotEnteredNotification(String hotspot) { //TODO: possibly remove

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_setting_light)
                        .setContentTitle("Hotspot entered")
                        .setContentText("You have entered hotspot " + hotspot + ". Would you like" +
                                "to share your location?");

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity. This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        mBuilder.setAutoCancel(true);
        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        //mBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
        mBuilder.setLights(Color.GRAY, 5000, 5000);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(123, mBuilder.build());

    }

    private void createOnRegionEnteredNotification(String region) {//TODO: possibly remove

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_setting_light)
                        .setContentTitle("Region entered")
                        .setContentText("You have entered region " + region + ". Would you like" +
                                "to share your location?");

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity. This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        mBuilder.setAutoCancel(true);
        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        //mBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
        mBuilder.setLights(Color.GRAY, 5000, 5000);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(321, mBuilder.build()); // TODO: change notification ID

    }

    /**
     * Function parses MSI API response and returns building name.
     *
     * @param response MSI API response.
     * @return Building name.
     */
    private String parseMsiApiResponse(String response) {

        String buildingName = null;

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new ByteArrayInputStream(response.getBytes()), null);
            parser.next();
            if (parser.getAttributeCount() >= 2) {
                buildingName = parser.getAttributeValue(2);
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buildingName;

    }

}
