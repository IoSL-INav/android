package de.tu_berlin.indoornavigation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;

public class UsersActivity extends AppCompatActivity implements UserFragment.OnListFragmentInteractionListener {

    private static final String TAG = UsersActivity.class.toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
    }

    public void onListFragmentInteraction(User user) {
        Log.d(TAG, user.getUsername());
    }

    public ArrayList<User> getMembers() {
        return (ArrayList<User>) getIntent().getExtras().get("members");
    }
}
