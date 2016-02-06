package de.tu_berlin.indoornavigation.singletons;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Jan on 30. 11. 2015.
 * <p/>
 * Singleton for performing REST requests using Volley.
 */
public class VolleyQueueSingleton {

    private static VolleyQueueSingleton mInstance;
    private static Context mCtx;
    private RequestQueue mRequestQueue;

    private VolleyQueueSingleton(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized VolleyQueueSingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleyQueueSingleton(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
