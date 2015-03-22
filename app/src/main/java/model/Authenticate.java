package model;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.widget.Toast;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by sauray on 20/03/15.
 */
public class Authenticate extends AsyncTask<Void, Void, Void> {

    private Context c;

    public Authenticate(Context c){
        this.c = c;
    }

    @Override
    protected Void doInBackground(Void... params) {

        DefaultHttpClient httpClient = new DefaultHttpClient();
        ResponseHandler<String> resonseHandler = new BasicResponseHandler();
        HttpPost postMethod = new HttpPost("http://sauray.me/studcard/login.php");
        try {
            postMethod.setEntity(new StringEntity("{\"amount_adult\" : 1, \"object_id\" : 13}"));
            postMethod.setHeader( "Content-Type", "application/json");

            String authorizationString = "Basic " + Base64.encodeToString(("antoine" + ":" + "test").getBytes(), Base64.DEFAULT); //this line is diffe
            authorizationString.replace("\n", "");
            postMethod.setHeader("Authorization", authorizationString);

            String response = httpClient.execute(postMethod,resonseHandler);
            Toast.makeText(c, response, Toast.LENGTH_SHORT).show();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
