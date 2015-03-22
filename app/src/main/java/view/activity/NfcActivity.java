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
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.infotel.greenwav.infotel.R;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Created by sauray on 20/03/15.
 */
public class NfcActivity extends ActionBarActivity{

    private NdefMessage[] msgs;

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] writeTagFilters;
    private Tag tag;

    private Toolbar toolbar;

    private FloatingActionButton fab;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_nfc);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[]{tagDetected};

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

    private void initInterface(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(this.getResources().getString(R.string.activity_main));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    public void onResume() {
        super.onResume();
        Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            new NdefReaderTask().execute(tag);
        }
        else if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction())){
            NfcV nfcvTag = NfcV.get(tag);
            try {
                nfcvTag.connect();
                //{flags:0x00, read multiple blocks command: 0x23, start at block 0: 0x00, read 9 blocks (0 to 8): 0x08}
                byte[] response = nfcvTag.transceive(new byte[nfcvTag.getMaxTransceiveLength()]);
                for(int i=0;i<response.length;i++){
                    Toast.makeText(this, response[i], Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                Log.d("NFCService", nfcvTag.toString());
            } finally {
                try {
                    nfcvTag.close();
                } catch (IOException e) {
                }
            }
        }
        else if(NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction())){
            format("yolo");
        }
    }


    @Override
    protected void onNewIntent(Intent intent){
        onResume();
    }


    private void format(String text){
        NfcV nfcvTag = NfcV.get(tag);
        NdefFormatable ndefFormatable = NdefFormatable.get(tag);
        NdefRecord records = null;
        try {
            records = createRecord(text);
            NdefMessage message = new NdefMessage(records);
            ndefFormatable.format(message);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            nfcvTag.connect();
            //{flags:0x00, read multiple blocks command: 0x23, start at block 0: 0x00, read 9 blocks (0 to 8): 0x08}
            byte[] response = nfcvTag.transceive(new byte[nfcvTag.getMaxTransceiveLength()]);
            for(int i=0;i<response.length;i++){
                Toast.makeText(this, response[i], Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.d("NFCService", nfcvTag.toString());
        } finally {
            try {
                nfcvTag.close();
            } catch (IOException e) {
            }
        }
    }

    private NdefRecord createRecord(String text) throws UnsupportedEncodingException {

        //create the message in according with the standard
        String lang = "en";
        byte[] textBytes = text.getBytes();
        byte[] langBytes = lang.getBytes("US-ASCII");
        int langLength = langBytes.length;
        int textLength = textBytes.length;

        byte[] payload = new byte[1 + langLength + textLength];
        payload[0] = (byte) langLength;

        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1, langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
        return recordNFC;
    }

    private void write(String text) throws IOException, FormatException {

        NdefRecord[] records = { createRecord(text) };
        NdefMessage message = new NdefMessage(records);
        Ndef ndef = Ndef.get(tag);
        ndef.connect();
        ndef.writeNdefMessage(message);
        ndef.close();
    }

    /**
     * Background task for reading the data. Do not block the UI thread while reading.
     *
     * @author Ralf Wondratschek
     *
     */
    private class NdefReaderTask extends AsyncTask<Tag, Void, String[]> {

        @Override
        protected String[] doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            String[] ret = new String[records.length];
            int i=0;
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        ret[i] = readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e("TASK", "Unsupported Encoding", e);
                    }
                }
                i++;
            }

            return ret;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

            byte[] payload = record.getPayload();

            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                String[] hints = new String[]{"id", "nom", "role", "id carte"};
                String id = result[0];
                String nom = result[1];
                String role = result[2];
                String idCard = result[3];
                ((TextView)NfcActivity.this.findViewById(R.id.name)).setText(nom);
                ((TextView)NfcActivity.this.findViewById(R.id.privilege)).setText(role);
                ((TextView)NfcActivity.this.findViewById(R.id.idCard)).setText(idCard);
            }
        }
    }


}
