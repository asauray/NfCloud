package view.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.infotel.greenwav.infotel.R;

import java.util.ArrayList;

import model.Mode;
import view.activity.MainActivity;
import view.custom.adapter.DrawerAdapter;

/**
 * The fragment which contains the navigation drawer
 * @author Antoine Sauray
 * @version 1.0
 */
public class NavigationDrawerFragment extends Fragment {

    // ----------------------------------- UI
    /**
     * Remember the position of the selected item_suggestion.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    /**
     * Unique identifier
     */
    private static final int DEFAULT_PAGE_SELECTED = 0;
    /**
     * Unique identifier
     */
    private static final String TAG = "NAVIGATIONDRAWER_FRAGMENT";
    /**
     * The button to toggle the navigation drawer
     */
    private ActionBarDrawerToggle drawerToggle;

    // ----------------------------------- Model
    /**
     * The drawer layout which contains all the elements
     */
    private DrawerLayout drawerLayout;
    /**
     * The list of modes available for selection
     */
    private ListView mDrawerListView;
    /**
     * The view which contains the fragment
     */
    private View fragmentContainerView;

    // ----------------------------------- Contants
    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;
    /**
     * The current position in the list
     */
    private ArrayList<Integer> positionHistory;
    /**
     *
     */
    private boolean fromSavedInstanceState;
    /**
     * The current position
     */
    private boolean userLearnedDrawer;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
        positionHistory = new ArrayList<>();
        positionHistory.add(0); // pour éviter le plantage due à la taille = 0. On a pas aucune position précédente au lancement.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);

        mDrawerListView = (ListView) root.findViewById(R.id.list);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        int displayMode = getResources().getConfiguration().orientation;

        return root;
    }

    public boolean isDrawerOpen() {
        return drawerLayout != null && drawerLayout.isDrawerOpen(fragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        fragmentContainerView = getActivity().findViewById(fragmentId);
        this.drawerLayout = drawerLayout;

        mDrawerListView.setAdapter(new DrawerAdapter(
                this.getActivity(),
                R.layout.item_drawer,
                new Mode[]{new Mode("Bus", R.drawable.ic_account_box, R.drawable.ic_account_box_w),
                        new Mode("Vélo", R.drawable.ic_account_box, R.drawable.ic_account_box_w),
                        new Mode("Voiture Electrique", R.drawable.ic_account_box, R.drawable.ic_account_box_w)}));

        // set a custom shadow that overlays the main content when the drawer opens
        //drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        drawerToggle = new ActionBarDrawerToggle(
                this.getActivity(),                    /* host Activity */
                NavigationDrawerFragment.this.drawerLayout,                    /* DrawerLayout object */
                R.string.app_name,  /* "open drawer" description for accessibility */
                R.string.option  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!userLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    userLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!userLearnedDrawer && !fromSavedInstanceState) {
            this.drawerLayout.openDrawer(Gravity.LEFT);
        }

        // Defer code dependent on restoration of previous instance state.
        this.drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                drawerToggle.syncState();
            }
        });

        this.drawerLayout.setDrawerListener(drawerToggle);
    }

    public void selectItem(int position) {
        ((Mode) mDrawerListView.getItemAtPosition(positionHistory.get(positionHistory.size()-1))).setChecked(false);
        positionHistory.add(position);
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
            ((Mode) mDrawerListView.getItemAtPosition(position)).setChecked(true);
            ((DrawerAdapter)mDrawerListView.getAdapter()).notifyDataSetChanged();
        }
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }

    public int getPreviousPosition(){
        return positionHistory.get(positionHistory.size()-2);
    }

    public int getCurrentModeSelected(){
        return mDrawerListView.getSelectedItemPosition();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, positionHistory.get(positionHistory.size()-1));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        /*
        if (item_suggestion.getItemId() == R.id.action_example) {
            Toast.makeText(getActivity(), "Example action.", Toast.LENGTH_SHORT).show();
            return true;
        }
        */
        return super.onOptionsItemSelected(item);
    }

    private ActionBar getActionBar() {
        return ((MainActivity) getActivity()).getSupportActionBar();
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item_suggestion in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
    }

    public boolean getModeAvailability(int position){
        return ((Mode)mDrawerListView.getItemAtPosition(position)).getVersion() != 0;
    }
}
