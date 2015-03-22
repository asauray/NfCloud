package view.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.infotel.greenwav.infotel.R;

import java.util.GregorianCalendar;

import model.Document;
import model.Group;
import view.custom.adapter.DocumentAdapter;


public class DocumentActivity extends ActionBarActivity{

    private Toolbar toolbar;

    private Group currentGroup;

    /**
     * The list of lines from the current network
     */
    private RecyclerView recyclerView;
    /**
     * The list of lines from the groups the user belongs to
     */
    private DocumentAdapter adapter;
    /**
     * The layout manager
     */
    private LinearLayoutManager layoutManager;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_document);
        currentGroup = getIntent().getExtras().getParcelable("GROUP");
        initInterface();

        adapter.add(new Document(0, "Cryptographie", "Sécuriser les échanges", "Chiffrement et déchiffrement", "http://sauray.me/studcard/document/asr/asr.pdf", (GregorianCalendar) GregorianCalendar.getInstance()));
        adapter.add(new Document(1, "Création d'entreprise", "Démarches administratives", "Le cours sur la création d'entreprise", "www.creation-entreprise.com", (GregorianCalendar) GregorianCalendar.getInstance()));
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
        toolbar.inflateMenu(R.menu.main);

        ((TextView)toolbar.findViewById(R.id.title)).setText(currentGroup.getName());
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        // specify an adapter (see also next example)
        adapter = new DocumentAdapter(this);

        recyclerView.setAdapter(adapter);
    }

}
