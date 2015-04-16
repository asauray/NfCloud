package model.db.external.json;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import model.Mode;
import model.Nfcard;
import model.Room;
import model.db.external.NetworkUtils;
import model.db.internal.CloudDAO;
import model.security.Hash;
import view.activity.MainActivity;
import view.activity.SplashScreenActivity;

/**
 * Created by sauray on 25/03/15.
 */
public class AssociateCard extends AsyncTask<Void, Void, Integer>{

    public static final String MIME_TEXT_PLAIN = "text/plain";
    private Tag tag;
    private Nfcard nfcard;
    private Activity a;
    private ProgressDialog pd;

    public AssociateCard(Activity a, Tag tag, Nfcard nfcard){
        this.a = a;
        this.tag = tag;
        this.nfcard = nfcard;
        pd = new ProgressDialog(a);
        pd.setTitle("Association de carte");
        pd.setMessage("Téléchargement de la nouvelle empreinte");
        pd.show();
    }

    @Override
    protected Integer doInBackground(Void... params) {
        Integer ret = -1;
        SharedPreferences preferences = a.getSharedPreferences("PREFERENCE", a.MODE_PRIVATE);
        String username = preferences.getString("username", null);
        String password = preferences.getString("password", null);

        if(username != null && password != null){
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://sauray.me/nfcloud/json/associate_card.php").openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                List<NameValuePair> paramsPost = new ArrayList<NameValuePair>();
                paramsPost.add(new BasicNameValuePair("username", username));
                paramsPost.add(new BasicNameValuePair("password", password));
                paramsPost.add(new BasicNameValuePair("id", nfcard.getId()+""));
                paramsPost.add(new BasicNameValuePair("room", nfcard.getRoom()+""));

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(NetworkUtils.getQuery(paramsPost));
                writer.flush();
                writer.close();
                os.close();
                urlConnection.connect();

                String jsonResult;
                if(urlConnection.getResponseCode()==201 || urlConnection.getResponseCode()==200)
                {
                    InputStream response = urlConnection.getInputStream();
                    jsonResult = NetworkUtils.convertStreamToString(response);
                    JSONObject jsonObj;
                    try {
                        jsonObj = new JSONObject(jsonResult.toString());
                        int authentication = jsonObj.optInt("authentication");
                        Log.d(authentication+"", "authentication");
                        if(authentication==1){
                            JSONObject result = jsonObj.getJSONObject("result");
                            ret = result.getInt("state");
                            Log.d(ret+"", "ret");
                            if(ret == 1) {
                                JSONObject room = result.getJSONObject("room");
                                Room r = new Room(room.optInt("id"), room.optString("name"), room.optString("description"), room.optString("url"), Mode.ADMIN_ROOMS);
                                String hash = result.optString("hash");
                                nfcard.setSha256(hash);
                                try {
                                    write(tag, nfcard);
                                    CloudDAO dao = new CloudDAO(a);
                                    dao.open();
                                    dao.insertNfCard(nfcard);
                                    dao.insertRoom(r);
                                    dao.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (FormatException e) {
                                    e.printStackTrace();
                                }
                            }
                            else{

                            }
                        }
                        else{
                            preferences.edit().clear().commit();
                            a.startActivity(new Intent(a, SplashScreenActivity.class));
                            cancel(true);
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }

            } catch (MalformedURLException e) {
                Log.d(e.getMessage(), "MalformedURLException");
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                Log.d(e.getMessage(), "UnsupportedEncodingException");
                e.printStackTrace();
            } catch (ProtocolException e) {
                Log.d(e.getMessage(), "ProtocolException");
                e.printStackTrace();
            } catch (IOException e) {
                Log.d(e.getMessage(), "IOException");
                e.printStackTrace();
            }
        }
        else{
            a.startActivity(new Intent(a, SplashScreenActivity.class));
            cancel(true);
        }
        return ret;
    }

    @Override
    protected void onPostExecute(Integer result){
        pd.dismiss();
        if(result == 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(a);
            builder.setTitle("Erreur carte");
            builder.setMessage("Association impossible. Contactez le fabricant");
            builder.setNeutralButton("Ok", null);
            builder.show();
        }
    }



    private void write(Tag tag, Nfcard nfcard) throws IOException, FormatException {
        if(tag != null) {
            NdefMessage msg = new NdefMessage(
                    new NdefRecord[]{
                            NdefRecord.createMime(MIME_TEXT_PLAIN, nfcard.getSha256().getBytes()),
                            NdefRecord.createMime(MIME_TEXT_PLAIN, (nfcard.getId()+"").getBytes()),
                            NdefRecord.createApplicationRecord("com.infotel.greenwav.infotel")});
            Ndef ndefTag = Ndef.get(tag);
            if (ndefTag != null) {
                Log.d("begining", "write");
                ndefTag.connect();
                ndefTag.writeNdefMessage(msg);
                if(ndefTag.canMakeReadOnly()){
                    Toast.makeText(a, "Carte verouillable", Toast.LENGTH_SHORT).show();
                }

                ndefTag.close();
                Log.d("success", "write");
                Intent intent = new Intent(a, MainActivity.class);
                a.startActivity(intent);
            } else {

            }
        }
        else{

        }
    }
}
