package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub;

import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.dining_cams.DiningCamsFragment;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.dummy.DummyContent;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                   MenuFragment.OnListFragmentInteractionListener,
                   ScheduleFragment.OnFragmentInteractionListener,
                   FavoritesFragment.OnFragmentInteractionListener,
                   AboutFragment.OnFragmentInteractionListener,
                   SwipesFragment.OnFragmentInteractionListener {

    private final String STATE_FRAGMENT_ID = "FRAGMENT_ID";
    private int currentFragmentId = R.id.nav_menus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
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
        // Not using optionsMenu

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();



        if (id == R.id.nav_menus) {
            MenuFragment fragment = new MenuFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.MainActivity_fragmentWrapper, fragment)
                    .commit();
        } else if (id == R.id.nav_favorites) {
            FavoritesFragment fragment = new FavoritesFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.MainActivity_fragmentWrapper, fragment)
                    .commit();
        } else if (id == R.id.nav_schedules) {
            ScheduleFragment fragment = new ScheduleFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.MainActivity_fragmentWrapper, fragment)
                    .commit();

        } else if (id == R.id.nav_cams) {
            DiningCamsFragment fragment = new DiningCamsFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.MainActivity_fragmentWrapper, fragment)
                    .commit();
        } else if (id == R.id.nav_swipes) {
            SwipesFragment fragment = new SwipesFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.MainActivity_fragmentWrapper, fragment)
                    .commit();
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            this.startActivity(intent);
        } else if (id == R.id.nav_about) {
            AboutFragment fragment = new AboutFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.MainActivity_fragmentWrapper, fragment)
                    .commit();
        }
        // Update current fragment id
        currentFragmentId = id;
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putInt(STATE_FRAGMENT_ID, currentFragmentId);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }
}
