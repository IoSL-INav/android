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
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
    private BeaconManager beaconManager;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Application started.");

        // initialise beacon manager
        beaconManager = new BeaconManager(getApplicationContext());
        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                createOnRegionEnteredNotification(region.getIdentifier());
            }

            @Override
            public void onExitedRegion(Region region) {
                //createOnHotspotEnteredNotification("BEACON AREA EXITED");
            }
        });
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(new Region(
                        "Door",
                        UUID.fromString("D0D3FA86-CA76-45EC-9BD9-6AF4BB14CA82"),
                        42882, 54653));
            }
        });
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(new Region(
                        "Car",
                        UUID.fromString("D0D3FA86-CA76-45EC-9BD9-6AF41DFC866B"),
                        62242, 28193));
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

                            String url = MsiApiUtils.URL;

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
    }

    /**
     * Creates notification. Not finished jet.
     *
     * @param hotspot Name of the hotspot.
     */
    private void createOnHotspotEnteredNotification(String hotspot) {

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

    private void createOnRegionEnteredNotification(String region) {

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
