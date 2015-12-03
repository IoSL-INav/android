package de.tu_berlin.indoornavigation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.estimote.sdk.SystemRequirementsChecker;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // access authentication token from Shared Preferences
        SharedPreferences sharedPreferences = getSharedPreferences(AuthUtils.PREFS_NAME, 0);
        AuthUtils.token = sharedPreferences.getString("authToken", null);

        // show or hide texts and buttons, based on token availability
        TextView loginNameText1 = (TextView) findViewById(R.id.loggedin_text1);
        TextView loginNameText2 = (TextView) findViewById(R.id.loggedin_text2);
        Button loginButton = (Button) findViewById(R.id.login_button);
        Button logoutButton = (Button) findViewById(R.id.logout_button);
        if (AuthUtils.token != null) {
            loginButton.setVisibility(View.INVISIBLE);
            logoutButton.setVisibility(View.VISIBLE);
            loginNameText1.setVisibility(View.VISIBLE);
            loginNameText2.setVisibility(View.VISIBLE);
            loginNameText2.setText(AuthUtils.token);
        } else {
            loginButton.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.INVISIBLE);
            loginNameText1.setVisibility(View.INVISIBLE);
            loginNameText2.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this); //TODO: this should be moved to
        // point where we actually need bluetooth
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Starts login (web view) activity
     */
    public void showLoginActivity(View view) {

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();

    }

    /**
     * Starts maps activity
     */
    public void showMapsActivity(View view) {

        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);

    }

    /**
     * Deletes token from Shared Preferences and AuthUtils. Deletes cookies. Restarts activity.
     */
    public void logout(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences(AuthUtils.PREFS_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("authToken");
        editor.commit();

        AuthUtils.token = null;

        CookieUtils.deleteCookies(); //TODO: probably don't need to delete cookies here again

        //TODO: is this ok? will check. Jan
        finish();
        startActivity(getIntent());
    }

    /**
     * Exit application.
     */
    public void exit(View view) {

        finish();
        System.exit(0);
    }
}
