package de.tu_berlin.indoornavigation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class CompanionRequestsActivity extends AppCompatActivity implements
        CompanionRequestFragment.OnListFragmentInteractionListener {

    private static final String TAG = CompanionRequestsActivity.class.toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_companion_requests);
    }

    public void onListFragmentInteraction(CompanionRequest companionRequest) {
        Log.d(TAG, companionRequest.getId());
    }
}
