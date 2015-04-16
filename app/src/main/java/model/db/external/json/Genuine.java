package model.db.external.json;

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
import view.activity.SplashScreenActivity;

/**
 * Created by sauray on 22/03/15.
 */
public class Genuine extends AsyncTask<Nfcard,Integer, Integer>{

    private TextView textView;
    private Button button;
    private ImageView genuine;


    public Genuine(TextView textView, ImageView genuine, Button button){
        this.textView = textView;
        this.button = button;
        this.genuine = genuine;
    }

    @Override
    protected Integer doInBackground(Nfcard... params) {
        Integer ret = -1;
        SharedPreferences preferences = textView.getContext().getSharedPreferences("PREFERENCE", textView.getContext().MODE_PRIVATE);
        String username = preferences.getString("username", null);
        String password = preferences.getString("password", null);

        if(username != null && password != null) {
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://sauray.me/nfcloud/json/check_genuine.php").openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                List<NameValuePair> paramsPost = new ArrayList<NameValuePair>();
                paramsPost.add(new BasicNameValuePair("username", username));
                paramsPost.add(new BasicNameValuePair("password", password));
                paramsPost.add(new BasicNameValuePair("id", params[0].getId() + ""));
                paramsPost.add(new BasicNameValuePair("room", params[0].getRoom() + ""));

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
                        int authentication = jsonObj.optInt("authentication");
                        if (authentication == 1) {
                            JSONObject result = jsonObj.getJSONObject("result");
                            ret = result.optInt("state");
                        } else {
                            preferences.edit().clear().commit();
                            textView.getContext().startActivity(new Intent(textView.getContext(), SplashScreenActivity.class));
                            cancel(true);
                        }
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
        }
        else{
            textView.getContext().startActivity(new Intent(textView.getContext(), SplashScreenActivity.class));
            cancel(true);
        }
        return ret;
    }

    @Override
    protected void onPostExecute(Integer result){
        if(result==1) {
            textView.setTextColor(textView.getResources().getColor(R.color.green));
            textView.setText("La carte est authentique.");
            genuine.setImageResource(R.drawable.ic_authentication_success);
            button.setClickable(true);
            button.setTextColor(textView.getResources().getColor(R.color.accent));
        }
        else if(result==0){
            textView.setTextColor(textView.getResources().getColor(R.color.red));
            genuine.setImageResource(R.drawable.ic_authentication_failed);
            textView.setText("La carte n'est pas authentique. Renvoyez la au fabricant.");
            button.setClickable(false);
            button.setTextColor(textView.getResources().getColor(android.R.color.tertiary_text_light));
        }
        else if(result==-1){
            textView.setTextColor(textView.getResources().getColor(R.color.yellow));
            genuine.setImageResource(R.drawable.ic_transmission_problem);
            textView.setText("Erreur de transmission. Veuillez réessayer ultérieurement.");

            button.setClickable(false);
            button.setTextColor(textView.getResources().getColor(android.R.color.tertiary_text_light));
        }
        else if(result==-2){
            textView.setTextColor(textView.getResources().getColor(android.R.color.secondary_text_light));
            textView.setText("Vous n'êtes pas connecté.");
            genuine.setImageResource(R.drawable.ic_transmission_problem);
            button.setClickable(false);
            button.setTextColor(textView.getResources().getColor(android.R.color.tertiary_text_light));
        }
    }
}
