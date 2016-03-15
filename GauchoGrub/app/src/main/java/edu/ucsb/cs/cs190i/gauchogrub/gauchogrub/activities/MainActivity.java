package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.activities;

import android.content.Intent;
import android.net.Uri;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.R;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.fab.MaterialSheetFab;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.dining_cams.DiningCamsFragment;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.fragments.AboutFragment;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.fragments.FavoritesFragment;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.fragments.MenuFragment;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.fragments.ScheduleFragment;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.fragments.SwipesFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                   MenuFragment.OnListFragmentInteractionListener,
                   ScheduleFragment.OnFragmentInteractionListener,
                   FavoritesFragment.OnFragmentInteractionListener,
                   AboutFragment.OnFragmentInteractionListener,
                   SwipesFragment.OnFragmentInteractionListener {

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

    public final static String STATE_CURRENT_DINING_COMMON = "STATE_CURRENT_DINING_COMMON";

    private final static String STATE_FRAGMENT_ID = "FRAGMENT_ID";
    private int currentFragmentId = R.id.nav_menus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        materialSheetFab = new com.gordonwong.materialsheetfab.MaterialSheetFab<>(fab, fabSheetView, fabOverlay, sheetColor, fabColor);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Restore state
        this.restoreInstanceState(savedInstanceState);

        // Set the dining common to preferred (if available)
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.MainActivity_dining_common_shared_prefs), MODE_PRIVATE);
        String defaultDiningCommon = sharedPreferences.getString(STATE_CURRENT_DINING_COMMON, getString(R.string.DLG));
        String diningCommon = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.pref_key_default_dining_common), defaultDiningCommon);
        sharedPreferences.edit().putString(STATE_CURRENT_DINING_COMMON, diningCommon).apply();

        // Set to Menu Fragment on open
        showFragment(currentFragmentId);
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
        // Not using optionsMenu
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        showFragment(id);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showFragment(int id) {
        Fragment fragment = null;
        if (id == R.id.nav_menus) {
            fragment = new MenuFragment();
        } else if (id == R.id.nav_favorites) {
            fragment = new FavoritesFragment();
        } else if (id == R.id.nav_schedules) {
            fragment = new ScheduleFragment();
        } else if (id == R.id.nav_cams) {
            // check if feed available for current dining common
            SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.MainActivity_dining_common_shared_prefs),MODE_PRIVATE);
            String defaultDiningCommon = getString(R.string.dining_cam_default);
            String diningCommon = sharedPreferences.getString(MainActivity.STATE_CURRENT_DINING_COMMON, defaultDiningCommon);
            if (!Arrays.asList(getResources().getStringArray(R.array.dining_cams)).contains(diningCommon)) {
                Toast.makeText(this, R.string.dining_cam_no_portola, Toast.LENGTH_SHORT).show();
                sharedPreferences.edit().putString(STATE_CURRENT_DINING_COMMON, getString(R.string.dining_cam_default)).commit();
                renderDiningCommonUpdates();
            }
            fragment = new DiningCamsFragment();
        } else if (id == R.id.nav_swipes) {
            fragment = new SwipesFragment();
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            this.startActivity(intent);
        } else if (id == R.id.nav_about) {
            fragment = new AboutFragment();
        }
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.MainActivity_fragmentWrapper, fragment)
                    .commit();
        }
        // Update current fragment id
        currentFragmentId = id;
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        renderDiningCommonUpdates();
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
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.MainActivity_dining_common_shared_prefs),MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Set state
        editor.putString(STATE_CURRENT_DINING_COMMON, diningCommon);
        // Apply changes asynchronously
        editor.apply();
        // Hide the sheet
        materialSheetFab.hideSheet();
        // Render buttons
        renderDiningCommonUpdates();
        // call update function from fragment
        if (currentFragmentId == R.id.nav_menus) {
            MenuFragment menuFragment = (MenuFragment) getSupportFragmentManager().findFragmentById(R.id.MainActivity_fragmentWrapper);
            if (menuFragment != null) {
                menuFragment.switchDiningCommon(diningCommon);
            }
        } else if (currentFragmentId == R.id.nav_favorites) {
            FavoritesFragment favoritesFragment = (FavoritesFragment) getSupportFragmentManager().findFragmentById(R.id.MainActivity_fragmentWrapper);
            if (favoritesFragment != null) {
                favoritesFragment.switchDiningCommon(diningCommon);
            }
        } else if (currentFragmentId == R.id.nav_schedules) {
            ScheduleFragment scheduleFragment = (ScheduleFragment) getSupportFragmentManager().findFragmentById(R.id.MainActivity_fragmentWrapper);
            if (scheduleFragment != null) {
                scheduleFragment.updateDiningCommon(diningCommon);
            }
        } else if (currentFragmentId == R.id.nav_cams) {
            DiningCamsFragment fragment = (DiningCamsFragment) getSupportFragmentManager().findFragmentById(R.id.MainActivity_fragmentWrapper);
            if (fragment != null) {
                fragment.switchDiningCommon();
            }
        }

    }

    public void renderDiningCommonUpdates() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.MainActivity_dining_common_shared_prefs), MODE_PRIVATE);
        // Get diningCommonStrings
        ArrayList<String> diningCommonStrings = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.dining_commons)));
        // Get constant strings for switch
        String defaultString = getResources().getString(R.string.DLG);
        // Get currently stored default Dining Common or else DLG string
        String currentDiningCommon = sharedPreferences.getString(STATE_CURRENT_DINING_COMMON, defaultString);
        if (!diningCommonStrings.contains(currentDiningCommon)) {
            currentDiningCommon = defaultString;
        }
        // Create ArrayList of fabSheetButton references
        ArrayList<Button> fabSheetButtons = new ArrayList<>();
        fabSheetButtons.add(fabSheetButton1);
        fabSheetButtons.add(fabSheetButton2);
        fabSheetButtons.add(fabSheetButton3);

        // Set Page Title
        updateAppBarTitle("Menu: " + currentDiningCommon, false);

        //Log.d(LOG_TAG, "Current dining common: " + currentDiningCommon);

        // Set text for cardView buttons
        int i = 0;
        for(String diningCommonString : diningCommonStrings) {
            if (!diningCommonString.equals(currentDiningCommon))  {
                //Log.d(LOG_TAG, diningCommonString);
                Button fabSheetButton = fabSheetButtons.get(i++);
                fabSheetButton.setText(diningCommonString);
                // if we are in the dining cams fragment, remove the button for Portola
                if (diningCommonString.equals(getString(R.string.POR))) {
                    if (currentFragmentId == R.id.nav_cams) {
                        fabSheetButton.setVisibility(View.GONE);
                    } else {
                        fabSheetButton.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Preserve fragment id
        outState.putInt(STATE_FRAGMENT_ID, currentFragmentId);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.restoreInstanceState(savedInstanceState);
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Restore fragment id
            currentFragmentId = savedInstanceState.getInt(STATE_FRAGMENT_ID, currentFragmentId);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * 
     * @param title the base title for the appbar
     * @param showDiningCommonName true if you should append ": " + currentDiningCommon
     */
    public void updateAppBarTitle(@Nullable String title, Boolean showDiningCommonName) {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.MainActivity_dining_common_shared_prefs),MODE_PRIVATE);
        String defaultString = getResources().getString(R.string.DLG);
        String currentDiningCommon = sharedPreferences.getString(STATE_CURRENT_DINING_COMMON, defaultString);
        if (title == null) {
            setTitle(currentDiningCommon);
        } else if (showDiningCommonName) {
            setTitle(title + ": " + currentDiningCommon);
        } else {
            setTitle(title);
        }
    }
}
