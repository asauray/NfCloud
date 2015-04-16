package view.activity;

import android.animation.Animator;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcV;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.infotel.greenwav.infotel.R;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import model.Nfcard;
import model.db.external.json.AssociateCard;
import model.db.external.json.Genuine;
import model.db.external.json.GetRoomInformations;
import model.db.external.json.JoinRoom;

/**
 * Created by sauray on 20/03/15.
 */
public class NfcActivity extends ActionBarActivity implements View.OnLayoutChangeListener {

    public static final String MIME_TEXT_PLAIN = "text/plain";

    private NdefMessage[] msgs;

    private NfcAdapter nfcAdapter;
    private Tag tag;
    private Ndef ndefTag;

    private Toolbar toolbar;

    private FloatingActionButton fab;

    private CardView nfcInfo, nfcAuthenticity, nfcAssociate, nfcJoin;

    private Nfcard nfcard;

    private Button associate;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_nfc);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        initInterface();
        handleIntent(getIntent());
        nfcInfo.addOnLayoutChangeListener(this);
        nfcAuthenticity.addOnLayoutChangeListener(this);
        nfcAssociate.addOnLayoutChangeListener(this);
        nfcJoin.addOnLayoutChangeListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        nfcJoin.setVisibility(View.GONE);
        nfcAssociate.setVisibility(View.GONE);
    }

    private void initInterface(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(this.getResources().getString(R.string.activity_main));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        nfcInfo = (CardView) findViewById(R.id.card_view);
        nfcAuthenticity = (CardView) findViewById(R.id.card_view_hash);
        nfcAssociate = (CardView) findViewById(R.id.card_view_associate);
        nfcJoin = (CardView) findViewById(R.id.card_view_join);

        associate = (Button) findViewById(R.id.associate);
        associate.setClickable(false);
        associate.setTextColor(getResources().getColor(android.R.color.tertiary_text_light));
    }

    public void onResume() {
        super.onResume();
        final Intent intent = new Intent(getApplicationContext(), getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, techList);
    }

    @Override
    public void onPause(){
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent){
        handleIntent(intent);
    }

    private void handleIntent(Intent intent)
    {
        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        nfcInfo.addOnLayoutChangeListener(this);
        nfcAuthenticity.addOnLayoutChangeListener(this);
        nfcAssociate.addOnLayoutChangeListener(this);
        nfcJoin.addOnLayoutChangeListener(this);
        // Parse the intent
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            ndefTag = Ndef.get(tag);
            Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            new NdefReaderTask(ndefTag.isWritable()).execute(tag);
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
        else{
            ndefTag = Ndef.get(tag);
            Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            new NdefReaderTask(ndefTag.isWritable()).execute(tag);
        }
    }

    /**
     * Background task for reading the data. Do not block the UI thread while reading.
     *
     * @author Ralf Wondratschek
     *
     */
    private class NdefReaderTask extends AsyncTask<Tag, Void, String[]> {

        boolean writable;

        NdefReaderTask(boolean writable){
        this.writable=writable;
        }

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
                ret[i] = new String(ndefRecord.getPayload());
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
                if(result.length==3){
                        nfcard = new Nfcard(Integer.parseInt(result[1]), result[0]);
                        nfcAssociate.setVisibility(View.GONE);
                        nfcJoin.setVisibility(View.VISIBLE);
                    ((TextView)NfcActivity.this.findViewById(R.id.authenticity)).setText("Salle de partage");
                    new GetRoomInformations(NfcActivity.this, ((TextView)NfcActivity.this.findViewById(R.id.name)),((TextView)NfcActivity.this.findViewById(R.id.description)), nfcard.getSha256()).execute();
                }
                else if (result.length==4){
                    nfcJoin.setVisibility(View.GONE);
                    nfcAssociate.setVisibility(View.VISIBLE);
                    try {
                        nfcard = new Nfcard(Integer.parseInt(result[1]),Integer.parseInt(result[2]));
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    new GetRoomInformations(NfcActivity.this, ((TextView)NfcActivity.this.findViewById(R.id.name)),((TextView)NfcActivity.this.findViewById(R.id.description)), nfcard.getRoom()).execute();
                    new Genuine(((TextView)NfcActivity.this.findViewById(R.id.authenticity)), ((ImageView)NfcActivity.this.findViewById(R.id.genuine)),associate).execute(nfcard);
                }
            }
        }
    }

    @Override
    public void onLayoutChange(final View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        v.removeOnLayoutChangeListener(this);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            int cx = (v.getLeft() + v.getRight()) / 2;
            int cy = (v.getTop() + v.getBottom()) / 2;

            int finalRadius = Math.max(v.getWidth(), v.getHeight());
            Animator anim = ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, finalRadius);
            v.setVisibility(View.GONE);
            if(v == nfcInfo){
                anim.setStartDelay(500);
            }
            else if(v == nfcAuthenticity){
                anim.setStartDelay(600);
            }
            else if(v == nfcAssociate || v == nfcJoin){
                anim.setStartDelay(700);
            }
            anim.setDuration(500);
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    v.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            anim.start();

        }
        else{
            /*
            v.setVisibility(View.GONE);
            ObjectAnimator animY = ObjectAnimator.ofFloat(v, "translationY", 150f);
            int amountOffscreen = (int)(v.getHeight() * 0.8); /* or whatever */
            boolean offscreen = true;/* true or false

            int xOffset = (offscreen) ? amountOffscreen : 0;
            RelativeLayout.LayoutParams rlParams =
                    (RelativeLayout.LayoutParams)v.getLayoutParams();
            rlParams.setMargins(-1*xOffset, 0, xOffset, 0);
            v.setLayoutParams(rlParams);
            int deltaY = v.getTop() - button.getTop();
            if(v == nfcInfo){
                animY.setStartDelay(500);

            }
            else if(v == nfcAuthenticity){
                animY.setStartDelay(600);
            }
            else if(v == nfcAssociate || v == nfcJoin){
                animY.setStartDelay(700);
            }
            animY.setFloatValues(deltaY);
            animY.setDuration(500);//1.5sec
            animY.setRepeatCount(0);
            animY.setDuration(500);

            animY.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    v.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animY.start();
            */
        }
    }

    public void onClick(View v){
        switch(v.getId()){

            case R.id.associate:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Association de carte");
                builder.setMessage("Veillez à maintenir votre périphérique à proximité de la puce nfc");
                builder.setNeutralButton("Je suis prêt", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new AssociateCard(NfcActivity.this, tag, nfcard).execute();
                    }
                });
                builder.show();
                break;
            case R.id.join:
                new JoinRoom(this, nfcard).execute();
                break;
            case R.id.cancelAssociate:
                finish();
                break;
            case R.id.cancelJoin:
                finish();
                break;
        }

    }


}
