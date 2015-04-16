package model.db.external.json;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
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
public class Register extends AsyncTask<Nfcard,Integer, Integer>{

    private String username, mail, password;
    private Context c;


    public Register(Context c, String username, String mail, String password){
        this.c = c;
        this.username = username;
        this.mail = mail;
        this.password = password;
    }

    @Override
    protected Integer doInBackground(Nfcard... params) {
        Integer ret = -3;
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://sauray.me/nfcloud/json/create_user.php").openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                List<NameValuePair> paramsPost = new ArrayList<NameValuePair>();
                paramsPost.add(new BasicNameValuePair("mail", mail));
                paramsPost.add(new BasicNameValuePair("username", username));
                paramsPost.add(new BasicNameValuePair("password", password));

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

                    // Do JSON handling here....
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
        return ret;
    }

    @Override
    protected void onPostExecute(Integer result) {

        if (result == 1) {
            try {
                SharedPreferences preferences = c.getSharedPreferences("PREFERENCE", c.MODE_PRIVATE);
                SharedPreferences.Editor e = preferences.edit();
                e.putString("username", username);
                e.putString("password", Hash.sha256(password));
                e.commit();
                c.startActivity(new Intent(c, MainActivity.class));
            } catch (NoSuchAlgorithmException e1) {
                e1.printStackTrace();
            }

        }
        else if(result == 0){
            // erreur de parametre
            AlertDialog.Builder builder = new AlertDialog.Builder(c);
            builder.setTitle("Erreur");
            builder.setMessage("Erreur de transmission");
            builder.setNeutralButton("Ok", null);
            builder.show();
        }
        else if (result == -1) {
            // username déjà pris
            AlertDialog.Builder builder = new AlertDialog.Builder(c);
            builder.setTitle("Erreur");
            builder.setMessage("Ce nom d'utilisateur est déjà utilisé.");
            builder.setNeutralButton("Ok", null);
            builder.show();
        }
        else if (result == -2) {
            // username déjà pris
            AlertDialog.Builder builder = new AlertDialog.Builder(c);
            builder.setTitle("Erreur");
            builder.setMessage("Cette adresse mail est déjà utilisé.");
            builder.setNeutralButton("Ok", null);
            builder.show();
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(c);
            builder.setTitle("Erreur");
            builder.setMessage("Erreur de connexion");
            builder.setNeutralButton("Ok", null);
            builder.show();
        }
    }
}
