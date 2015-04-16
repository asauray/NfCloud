package view.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.infotel.greenwav.infotel.R;

import java.util.GregorianCalendar;

import model.Document;
import model.Room;
import model.db.external.Upload;
import model.db.external.json.GetDocuments;
import view.custom.adapter.DocumentAdapter;


public class DocumentActivity extends ActionBarActivity{

    private Toolbar toolbar;

    private Room currentRoom;

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

    private SwipeRefreshLayout swipeRefreshLayout;

    final int ACTIVITY_CHOOSE_FILE = 1;
    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_document);
        currentRoom = getIntent().getExtras().getParcelable("ROOM");
        initInterface();
    }

    @Override
    public void onResume(){
        super.onResume();
        refreshItems();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.document, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
            case R.id.action_add:
                Intent chooseFile;
                Intent intent;
                chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("file/*");
                intent = Intent.createChooser(chooseFile, "Choose a file");
                startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case ACTIVITY_CHOOSE_FILE:
                if (resultCode == RESULT_OK){
                    Uri uri = data.getData();
                    String filePath = uri.getPath();
                    Document d = new Document(filePath, currentRoom.getId());
                    if(d.getName() == null || d.getExtension()==null){
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Erreur");
                        builder.setMessage("NFCloud a rencontré un problème avec votre explorateur de fichier");
                        builder.setNeutralButton("Ok", null);
                        builder.show();
                    }
                    else {
                        new Upload(this, d).execute();
                    }
                }
                break;
        }
    }

    private void initInterface(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeColors(R.color.accent);

        ((TextView)toolbar.findViewById(R.id.title)).setText(currentRoom.getName());
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

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }
        });
    }

    private void refreshItems() {
        int size = adapter.getItemCount();
        adapter.removeAll();
        adapter.notifyItemRangeRemoved(0, size);
        new GetDocuments(this, adapter, swipeRefreshLayout, currentRoom.getId()).execute();
    }

}
