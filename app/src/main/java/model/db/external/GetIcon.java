package model.db.external;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import com.infotel.greenwav.infotel.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class GetIcon extends AsyncTask<Void, Void, Bitmap>{

    private Activity a;

    private final String imagesLocation = "http://sauray.me/greenwav/images/";

    private ImageView image;
    public GetIcon(Activity a, ImageView iv) {
        this.a = a;
        this.image = iv;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        StringBuilder jsonResult = new StringBuilder();
        final String BASE_URL = "http://sauray.me/greenwav/gorilla_event.php?";
        jsonResult = new StringBuilder();
        StringBuilder sb = new StringBuilder(BASE_URL);
        //sb.append("reseau=" + network);
        Bitmap b = null;
        URL url = null;
        try {
            url = new URL(sb.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        InputStreamReader in = new InputStreamReader(conn.getInputStream());

        BufferedReader jsonReader = new BufferedReader(in);
        String lineIn;
        while ((lineIn = jsonReader.readLine()) != null) {
            jsonResult.append(lineIn);
        }

        JSONObject jsonObj;
        try {
            jsonObj = new JSONObject(jsonResult.toString());
            JSONArray jsonMainNode = jsonObj.optJSONArray("event");
            for (int i = 0; i < jsonMainNode.length(); i++) {
                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                int id = jsonChildNode.optInt("id");
                int type = jsonChildNode.optInt("type");
                String nom = jsonChildNode.optString("nom");
                String urlEvent = jsonChildNode.optString("url");
                double lat = jsonChildNode.optDouble("lat");
                double lng = jsonChildNode.optDouble("lng");
                DisplayMetrics metrics = new DisplayMetrics();
                a.getWindowManager().getDefaultDisplay().getMetrics(metrics);
                url = new URL(imagesLocation + id + "_" + metrics.densityDpi);
                Log.d(url.toString(), "URL");

                try{
                    b = RoundedBitmapDrawableFactory.create(a.getResources(), url.openConnection().getInputStream()).getBitmap();
                    //b = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    Log.d("success", "Bitmap online");
                }
                catch(FileNotFoundException e){
                    b = RoundedBitmapDrawableFactory.create(a.getResources(), BitmapFactory.decodeResource(a.getResources(), R.drawable.ic_account_box)).getBitmap();
                    Log.d("failed", "Bitmap offline");
                }
                Log.d(b.toString(), "bitmap");

            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return b;
    }

    protected void onPostExecute(Bitmap result){
        if(result != null){
            image.setImageBitmap(result);
        }
    }
}


