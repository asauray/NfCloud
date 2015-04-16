package view.activity;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.infotel.greenwav.infotel.R;

import java.util.HashMap;
import java.util.Map;

import model.Mode;
import model.Room;
import model.db.external.json.GetRooms;
import view.custom.adapter.RoomAdapter;
import view.fragment.NavigationDrawerFragment;


public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks{



    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private Toolbar toolbar;

    /**
     * The list of lines from the current network
     */
    private RecyclerView recyclerView;
    /**
     * The list of lines from the groups the user belongs to
     */
    private RoomAdapter adapter;
    /**
     * The layout manager
     */
    private LinearLayoutManager layoutManager;

    private SwipeRefreshLayout swipeRefreshLayout;

    private String username, password;

    private Map<Integer, Room> rooms;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_main);
        Bundle extras = getIntent().getExtras();
        initInterface();
        //new GetRooms(this, adapter, swipeRefreshLayout).execute(Mode.ALL);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mNavigationDrawerFragment.close();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initInterface(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeColors(R.color.accent);
        toolbar.setTitle(this.getResources().getString(R.string.activity_main));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        recyclerView = (RecyclerView) findViewById(R.id.list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        // specify an adapter (see also next example)
        adapter = new RoomAdapter(this);

        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }
        });

        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

    }

    private void refreshItems() {
        int size = adapter.getItemCount();
        adapter.removeAll();
        adapter.notifyItemRangeRemoved(0, size);
        new GetRooms(this, adapter, swipeRefreshLayout, mNavigationDrawerFragment.getCurrentModeSelected(), (android.widget.TextView) findViewById(R.id.noRooms)).execute();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        refreshItems();
    }


}
