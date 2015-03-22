package view.activity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcV;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.infotel.greenwav.infotel.R;

import model.Authenticate;
import model.Group;
import view.custom.adapter.GroupAdapter;
import view.fragment.NavigationDrawerFragment;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;


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
    private GroupAdapter adapter;
    /**
     * The layout manager
     */
    private LinearLayoutManager layoutManager;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_main);
        initInterface();

        new Authenticate(this).execute();

        adapter.add(new Group(0, "Programmation", "Michel Adam", "TestAlgo", "www.prog.com"));
        adapter.add(new Group(1, "Base de donnée", "Didier Bogdaniuk", "Vous pouvez rentrer chez vous","www.bdd.com"));
        adapter.add(new Group(2, "Système Réseau", "François Morice", "C'est reparti pour un tour","www.asr.com"));
        adapter.add(new Group(3, "Mobile", "Mathieu Le Lain", "Essayez l'introspection des méthodes","www.mobile.com"));
        adapter.add(new Group(4, "Economie", "Muriel Mannevy", "Business plan et chiffre d'affaire", "www.eco.com"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initInterface(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(this.getResources().getString(R.string.activity_main));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        recyclerView = (RecyclerView) findViewById(R.id.list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        // specify an adapter (see also next example)
        adapter = new GroupAdapter(this);

        recyclerView.setAdapter(adapter);

        //registerForContextMenu(list);
        //new GetLocalLines(this, currentNetwork, adapter).execute();

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

    }

}
