package model.db.external.json;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.infotel.greenwav.infotel.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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

import model.Nfcard;
import model.db.external.NetworkUtils;
import model.security.Hash;
import view.activity.MainActivity;
import view.activity.SplashScreenActivity;

/**
 * Created by sauray on 22/03/15.
 */
public class Authentication extends AsyncTask<String,Integer, Integer>{

    private String username, password;
    private Context c;
    private ProgressDialog pd;

    public Authentication(Context c, String username, String password){
        this.c = c;
        this.username = username;
        this.password = password;
        pd = new ProgressDialog(c);
        pd.setTitle("Connexion en cours");
        pd.setMessage("Connexion aux services NFCloud");
        pd.show();
    }

    @Override
    protected Integer doInBackground(String... params) {
        Integer ret = 0;

        if(username != null && password != null) {
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://sauray.me/nfcloud/json/check_authentication.php").openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                List<NameValuePair> paramsPost = new ArrayList<NameValuePair>();
                paramsPost.add(new BasicNameValuePair("username", username));
                paramsPost.add(new BasicNameValuePair("password", Hash.sha256(password)));

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(NetworkUtils.getQuery(paramsPost));
                writer.flush();
                writer.close();
                os.close();
                urlConnection.connect();

                String jsonResult;
                if (urlConnection.getResponseCode() == 201 || urlConnection.getResponseCode() == 200) {
                    InputStream response = urlConnection.getInputStream();
                    jsonResult = NetworkUtils.convertStreamToString(response);
                    JSONObject jsonObj;
                    try {
                        jsonObj = new JSONObject(jsonResult.toString());
                        ret = jsonObj.optInt("authentication");
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
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    @Override
    protected void onPostExecute(Integer result){
        pd.dismiss();
        if(result==1) {
            SharedPreferences preferences = c.getSharedPreferences("PREFERENCE", c.MODE_PRIVATE);
            SharedPreferences.Editor e = preferences.edit();
            try {
                e.putString("username", username);
                e.putString("password", Hash.sha256(password));
                e.commit();
                c.startActivity(new Intent(c, MainActivity.class));
            } catch (NoSuchAlgorithmException e1) {
                e1.printStackTrace();
            }

        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(c);
            builder.setTitle("Erreur de connexion");
            builder.setMessage("identifiants erronés");
            builder.setNeutralButton("Ok", null);
            builder.show();
        }
    }
}
