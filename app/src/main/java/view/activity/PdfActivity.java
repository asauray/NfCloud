package view.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.infotel.greenwav.infotel.R;
import com.joanzapata.pdfview.PDFView;
import com.joanzapata.pdfview.listener.OnPageChangeListener;

import java.io.File;

import model.Document;

/**
 * Created by sauray on 21/03/15.
 */
public class PdfActivity extends ActionBarActivity implements OnPageChangeListener {

    private Toolbar toolbar;
    private Document currentDocument;

    private PDFView pdfView;
    private boolean jumpToFirstPage;
    private Integer pageNumber;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_document_detail);
        currentDocument = getIntent().getExtras().getParcelable("DOCUMENT");
        jumpToFirstPage=true;
        pageNumber = 1;
        initInterface();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initInterface() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.main);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        pdfView = (PDFView) findViewById(R.id.pdfview);
        if (jumpToFirstPage) pageNumber = 1;

        pdfView.fromFile(new File(currentDocument.getLocation()))
                .defaultPage(pageNumber)
                .onPageChange(this)
                .load();
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
    }
}
