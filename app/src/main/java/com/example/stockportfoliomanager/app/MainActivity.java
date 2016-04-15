package com.example.stockportfoliomanager.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.stockportfoliomanager.app.sync.StockPriceSyncAdapter;
import com.google.android.gms.analytics.Tracker;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    NavigationView mNavigationView;
    final String LOG_TAG = MainActivity.class.getSimpleName();
    private String mPortId;
    private Tracker mTracker;
    private final String STOCK_PORTFOLIO_HASH_TAG = "#StockPortfolioManager";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Obtain the shared Tracker instance.
        StockPortfolioManagerApplication application = (StockPortfolioManagerApplication) getApplication();
        mTracker = application.getDefaultTracker();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        StockPriceSyncAdapter.initializeSyncAdapter(this);

        mPortId = Utilities.getPreferencePortId(this);

        if( null == savedInstanceState) { //first time show snapshot fragment
            //load snapshot fragment by default
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragment = new SnapshotFragment();
            String fragName = fragment.getClass().getSimpleName();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment, fragName)
                    .addToBackStack(fragName)
                    .commit();
            mNavigationView.getMenu().getItem(2).setChecked(true); //2 fragment index to navigation drawer
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            getSupportFragmentManager().addOnBackStackChangedListener(getListener());
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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment nextFragment;

        if (id == R.id.create_port) {
            nextFragment = new CreatePortFragment();

        } else if (id == R.id.add_investment) {
            nextFragment = new AddInvestmentFragment();
        } else if (id == R.id.snapshot) {
            nextFragment = new SnapshotFragment();
        } else if (id == R.id.transaction) {
            nextFragment = new TransactionFragment();
        } else if (id == R.id.capital_gain) {
            nextFragment = new CapitalGainFragment();
        } else{
            nextFragment = new SnapshotFragment();
        }

        if (id == R.id.share) {
            //add Share Action
            this.startActivity(createShareIntent(getString(R.string.share_text)));
            Utilities.sendActionEvent(mTracker, getString(R.string.share));
        }
        else {
            String fragName = nextFragment.getClass().getSimpleName();
            Utilities.sendScreenImageName(mTracker, fragName);
            fragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.container, nextFragment, fragName)
                    .addToBackStack(fragName)
                    .commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public Intent createShareIntent(String ShareText) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, ShareText + STOCK_PORTFOLIO_HASH_TAG);
        return shareIntent;
    }

    public void switchContent(int id, Fragment fragment, int selectedNavigationMenuIndex) {
        String fragName = fragment.getClass().getSimpleName();
        Utilities.sendScreenImageName(mTracker, fragName);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        ft.replace(id, fragment, fragName)
                .addToBackStack(fragName)
                .commit();

        mNavigationView.getMenu().getItem(selectedNavigationMenuIndex).setChecked(true);
    }

    /** Called when returning to the activity */
    @Override
    protected void onResume() {
        super.onResume();

        String portId = Utilities.getPreferencePortId(this);
        if (!portId.equals(mPortId)) {
            String curFragTag = getCurrentFragmentTag();
            Log.d(LOG_TAG, "Current fragment: " + curFragTag);

            if (null != curFragTag) {
                Fragment curFrag = getSupportFragmentManager().findFragmentByTag(curFragTag);

                if(null != curFrag) {
                    getSupportFragmentManager().beginTransaction().detach(curFrag).commit();

                    if (curFragTag.equalsIgnoreCase("AddInvestmentFragment")) {
                        curFrag = new AddInvestmentFragment();
                    }
                    else if (curFragTag.equalsIgnoreCase("SnapshotFragment")) {
                        curFrag = new SnapshotFragment();
                    }
                    else if (curFragTag.equalsIgnoreCase("TransactionFragment")) {
                        curFrag = new TransactionFragment();
                    }
                    else if (curFragTag.equalsIgnoreCase("CapitalGainFragment")) {
                        curFrag = new CapitalGainFragment();
                    }

                    String fragName = curFrag.getClass().getSimpleName();
                    Utilities.sendScreenImageName(mTracker, fragName);
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .add(R.id.container, curFrag, fragName)
                            .addToBackStack(fragName)
                            .commit();
                }
            }
            mPortId = portId;
        }
    }

    private String getCurrentFragmentTag(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        String fragmentTag = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName();
        Log.d(LOG_TAG, "getCurrentFragment fragmentTag: " + fragmentTag);
        //Fragment currentFragment = fragmentManager.findFragmentByTag(fragmentTag);
        return fragmentTag;
    }

    private FragmentManager.OnBackStackChangedListener getListener() {
        final FragmentManager.OnBackStackChangedListener result = new FragmentManager.OnBackStackChangedListener() {
            public void onBackStackChanged() {
                FragmentManager manager = getSupportFragmentManager();
                if (manager != null) {
                    int backStackEntryCount = manager.getBackStackEntryCount();
                    Log.d(LOG_TAG, "OnBackStackChange backStackEntryCount: " + backStackEntryCount);

                    if (backStackEntryCount <= 1) {
                        finish();
                    }
                    else {
                        Fragment fragment = manager.getFragments()
                                .get(backStackEntryCount - 1);
                        Utilities.sendScreenImageName(mTracker, fragment.getClass().getSimpleName());
                        fragment.onResume();
                    }
                }
            }
        };
        return result;
    }
}
