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
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import model.Document;
import model.db.external.NetworkUtils;
import model.db.internal.Cloud;
import model.db.internal.CloudDAO;
import model.security.Hash;
import view.activity.SplashScreenActivity;
import view.custom.adapter.DocumentAdapter;


public class GetDocuments extends AsyncTask<String, Document, List<Document>> {

    private Context context;
    private int room;
    private DocumentAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    public GetDocuments(Context context, DocumentAdapter adapter, SwipeRefreshLayout swipeRefreshLayout, int room) {
        this.context = context;
        this.adapter = adapter;
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.room = room;
    }

    @Override
    protected List<Document> doInBackground(String...params) {

       ArrayList<Document> ret = new ArrayList<Document>();

        SharedPreferences preferences = context.getSharedPreferences("PREFERENCE", context.MODE_PRIVATE);
        String username = preferences.getString("username", null);
        String password = preferences.getString("password", null);

        if(username != null && password != null) {

            try {
                HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://sauray.me/nfcloud/json/get_documents.php").openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                List<NameValuePair> paramsPost = new ArrayList<NameValuePair>();
                paramsPost.add(new BasicNameValuePair("username", username));
                paramsPost.add(new BasicNameValuePair("password", password));
                paramsPost.add(new BasicNameValuePair("room", room + ""));

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
                        jsonObj = new JSONObject(jsonResult.toString());
                        JSONObject result = jsonObj.getJSONObject("result");
                        int authentication = jsonObj.optInt("authentication");
                        if(authentication == 1) {
                            JSONArray jsonMainNode = result.optJSONArray("documents");
                            CloudDAO dao = new CloudDAO(context);
                            dao.open();
                            for (int i = 0; i < jsonMainNode.length(); i++) {
                                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                                int id = jsonChildNode.optInt("id");
                                String name = jsonChildNode.optString("name");
                                String specification = jsonChildNode.optString("specification");
                                String description = jsonChildNode.optString("description");
                                // calendar ?
                                Document d = new Document(id, name, specification, description, null, room);
                                ret.add(d);
                                Document local = dao.findDocument(d.getId());
                                if(local != null){
                                    Log.d(local.getLocation(), "location local");
                                   d.setLocation(local.getLocation());
                                }
                                dao.insertDocument(d);
                                publishProgress(d);
                            }
                            dao.close();
                        }
                        else{
                            preferences.edit().clear().commit();
                            context.startActivity(new Intent(context, SplashScreenActivity.class));
                            cancel(true);
                        }
                    }


            }
            catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }catch (Exception e) {
                CloudDAO dao = new CloudDAO(context);
                dao.open();
                Iterator<Document> it = dao.findDocuments(room).iterator();
                while(it.hasNext()){
                    Document d = it.next();
                    Log.d(d.toString(), "document");
                    publishProgress(d);
                }
                dao.close();
            }
        }
        else{
            context.startActivity(new Intent(context, SplashScreenActivity.class));
            cancel(true);
        }
        return ret;
    }

    @Override
    protected void onProgressUpdate(Document...progress){
        adapter.add(progress[0]);
    }

    @Override
    protected void onPostExecute(List<Document> result){
        if(swipeRefreshLayout != null){
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}



