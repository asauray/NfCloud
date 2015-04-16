package model.db.external.json;

/**
 * Created by sauray on 25/03/15.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import model.Mode;
import model.Room;
import model.db.external.NetworkUtils;
import model.db.internal.CloudDAO;
import view.activity.SplashScreenActivity;
import view.custom.adapter.RoomAdapter;


public class GetRoomInformations extends AsyncTask<Integer, Room, Room> {

    private Context context;
    private int room;
    private String hash;
    private TextView name, desc;

    public GetRoomInformations(Context context, TextView name, TextView desc, int room) {
        this.context = context;
        this.room = room;
        this.name = name;
        this.desc = desc;
    }

    public GetRoomInformations(Context context, TextView name, TextView desc, String hash) {
        this.context = context;
        this.hash = hash;
        this.name = name;
        this.desc = desc;
    }


    @Override
    protected Room doInBackground(Integer...params) {

       Room ret = null;

        SharedPreferences preferences = context.getSharedPreferences("PREFERENCE", context.MODE_PRIVATE);
        String username = preferences.getString("username", null);
        String password = preferences.getString("password", null);

        if(username != null && password != null) {
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://sauray.me/nfcloud/json/get_room_informations.php").openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                List<NameValuePair> paramsPost = new ArrayList<NameValuePair>();
                paramsPost.add(new BasicNameValuePair("username", username));
                paramsPost.add(new BasicNameValuePair("password", password));
                if(hash != null){
                    paramsPost.add(new BasicNameValuePair("hash", hash));
                }
                else{
                    paramsPost.add(new BasicNameValuePair("room", room + ""));
                }


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
                        if(authentication==1) {
                            JSONObject result = jsonObj.getJSONObject("result");
                            JSONObject room = result.getJSONObject("room");
                            ret = new Room(room.optString("name"), room.optString("description"));
                        }
                        else{
                            context.startActivity(new Intent(context, SplashScreenActivity.class));
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
            context.startActivity(new Intent(context, SplashScreenActivity.class));
            cancel(true);
        }
        return ret;
    }


    @Override
    protected void onPostExecute(Room result){
        if(result != null) {
            name.setText(result.getName());
            desc.setText(result.getDescription());
        }
        else{
            name.setText("Erreur de connexion");
        }
    }

}



