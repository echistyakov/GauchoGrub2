package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.fab.MaterialSheetFab;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.services.MenuScraperService;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String LOG_TAG = "MainActivity";

    private ActionBarDrawerToggle toggle;

    @Bind(R.id.fab)
    public MaterialSheetFab fab;

    @Bind(R.id.fab_cardView)
    public View fabSheetView;

    @Bind(R.id.dimOverLayFrameLayout)
    public View fabOverlay;

    int sheetColor = Color.WHITE;
    int fabColor = Color.CYAN;

    private com.gordonwong.materialsheetfab.MaterialSheetFab materialSheetFab;

    @Bind(R.id.fab_sheet_button1)
    public Button fabSheetButton1;

    @Bind(R.id.fab_sheet_button2)
    public Button fabSheetButton2;

    @Bind(R.id.fab_sheet_button3)
    public Button fabSheetButton3;

    public final String STATE_CURRENT_DINING_COMMON = "STATE_CURRENT_DINING_COMMON";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        materialSheetFab = new com.gordonwong.materialsheetfab.MaterialSheetFab<>(fab, fabSheetView, fabOverlay, sheetColor, fabColor);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {
            MenuScraperService.startActionScrapeMenu(this, new Date(System.currentTimeMillis()));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.removeDrawerListener(toggle);
    }

    @Override
    protected void onStart() {
        super.onStart();
        renderDiningCommonUpdates();
    }

    @OnClick ({R.id.fab_sheet_button1, R.id.fab_sheet_button2, R.id.fab_sheet_button3})
    public void updateCurrentDiningCommon(Button fabSheetButton) {
        String diningCommon = fabSheetButton.getText().toString();
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Set state
        editor.putString(STATE_CURRENT_DINING_COMMON, diningCommon);
        // Apply changes asynchronously
        editor.apply();
        // Render buttons
        renderDiningCommonUpdates();
    }

    public void renderDiningCommonUpdates() {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        // Get diningCommonStrings
        ArrayList<String> diningCommonStrings = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.dining_commons)));
        // Get constant strings for switch
        String defaultString = getResources().getString(R.string.DLG);
        // Get currently stored default Dining Common or else DLG string
        String currentDiningCommon = sharedPreferences.getString(STATE_CURRENT_DINING_COMMON, defaultString);
        if(!diningCommonStrings.contains(currentDiningCommon))
            currentDiningCommon = defaultString;
        // Create ArrayList of fabSheetButton references
        ArrayList<Button> fabSheetButtons = new ArrayList<>();
        fabSheetButtons.add(fabSheetButton1);
        fabSheetButtons.add(fabSheetButton2);
        fabSheetButtons.add(fabSheetButton3);

        // Set Page Title
        setTitle(currentDiningCommon);

        Log.d(LOG_TAG, "Current dining common: " + currentDiningCommon);

        // Set text for cardView buttons
        int i = 0;
        for(String diningCommonString : diningCommonStrings) {
            if(!diningCommonString.equals(currentDiningCommon))  {
                Log.d(LOG_TAG, diningCommonString);
                Button fabSheetButton = fabSheetButtons.get(i++);
                fabSheetButton.setText(diningCommonString);
            }
        }
    }
}
