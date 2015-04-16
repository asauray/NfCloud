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
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import model.Mode;
import model.Room;
import model.db.external.NetworkUtils;
import model.db.internal.CloudDAO;
import model.security.Hash;
import view.activity.SplashScreenActivity;
import view.custom.adapter.RoomAdapter;


public class GetRooms extends AsyncTask<Integer, Room, List<Room>> {

    private Context context;
    private RoomAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int request;
    private TextView noRooms;

    public GetRooms(Context context, RoomAdapter adapter, SwipeRefreshLayout swipeRefreshLayout, int request, TextView noRooms) {
        this.context = context;
        this.adapter = adapter;
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.request = request;
        this. noRooms = noRooms;
    }

    @Override
    protected List<Room> doInBackground(Integer...params) {

       ArrayList<Room> ret = new ArrayList<Room>();

        SharedPreferences preferences = context.getSharedPreferences("PREFERENCE", context.MODE_PRIVATE);
        String username = preferences.getString("username", null);
        String password = preferences.getString("password", null);

        if(username != null && password != null) {
            ret = getLocalRooms();

            try {
                HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://sauray.me/nfcloud/json/get_rooms.php").openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                List<NameValuePair> paramsPost = new ArrayList<NameValuePair>();
                paramsPost.add(new BasicNameValuePair("username", username));
                paramsPost.add(new BasicNameValuePair("password", password));
                paramsPost.add(new BasicNameValuePair("request", request + ""));

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

                            if (request == Mode.ALL || request == Mode.ADMIN_ROOMS) {
                                JSONArray rooms = result.optJSONArray("admin_rooms");
                                CloudDAO dao = new CloudDAO(context);
                                dao.open();
                                for (int i = 0; i < rooms.length(); i++) {
                                    JSONObject jsonChildNode = rooms.getJSONObject(i);
                                    int id = jsonChildNode.optInt("id");
                                    String name = jsonChildNode.optString("name");
                                    String description = jsonChildNode.optString("description");
                                    String userGroup = jsonChildNode.optString("userGroup");
                                    int category = jsonChildNode.optInt("admin_room");
                                    Room r = new Room(id, name, description, userGroup, category);
                                    if(!adapter.roomAlreadyVisible(r.getId())) {
                                        ret.add(r);
                                        dao.insertRoom(r);
                                        publishProgress(r);
                                    }
                                    else{
                                        Log.d(r.toString(), "user of (locally available)");
                                    }
                                }
                            }
                            if (request == Mode.ALL || request == Mode.USER_ROOMS) {
                                JSONArray rooms = result.optJSONArray("user_rooms");
                                CloudDAO dao = new CloudDAO(context);
                                dao.open();
                                for (int i = 0; i < rooms.length(); i++) {
                                    JSONObject jsonChildNode = rooms.getJSONObject(i);
                                    int id = jsonChildNode.optInt("id");
                                    String name = jsonChildNode.optString("name");
                                    String description = jsonChildNode.optString("description");
                                    String userGroup = jsonChildNode.optString("userGroup");
                                    int category = jsonChildNode.optInt("user_room");
                                    Room r = new Room(id, name, description, userGroup, category);
                                    Log.d(r.toString(), "user of");
                                    if(!adapter.roomAlreadyVisible(r.getId())) {
                                        ret.add(r);
                                        dao.insertRoom(r);
                                        publishProgress(r);
                                    }
                                    else{
                                        Log.d(r.toString(), "user of (locally available)");
                                    }
                                }
                                dao.close();
                            }
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
    protected void onProgressUpdate(Room...progress){
        adapter.add(progress[0]);
    }

    @Override
    protected void onPostExecute(List<Room> result){
        if(swipeRefreshLayout != null){
            swipeRefreshLayout.setRefreshing(false);
        }

        if(result.size()==0){
            noRooms.setVisibility(View.VISIBLE);
        }
        else{
            noRooms.setVisibility(View.INVISIBLE);
        }
    }

    private ArrayList<Room> getLocalRooms(){
        ArrayList<Room> ret = new ArrayList<Room>();
        CloudDAO dao = new CloudDAO(context);
        dao.open();
        Map<Integer, Room> rooms =  dao.findRooms(request);
        dao.close();
        for(Room r : rooms.values()){
            ret.add(r);
            publishProgress(r);
        }
        return ret;
    }

}



