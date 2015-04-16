package model.db.external;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
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

import model.Document;
import view.activity.SplashScreenActivity;

/**
 * Created by sauray on 23/03/15.
 */
public class Upload extends AsyncTask<Void, Document, Integer>{

    private Document document;
    private ProgressDialog pd;
    private Context c;

    public Upload(Context c, Document d){
        document = d;
        this.c = c;
        pd = new ProgressDialog(c);
        pd.setTitle("Uploading");
        pd.setTitle("Pending");
        pd.show();
    }

    @Override
    protected Integer doInBackground(Void... params) {
        Integer ret = -1;
        SharedPreferences preferences = c.getSharedPreferences("PREFERENCE", c.MODE_PRIVATE);
        String username = preferences.getString("username", null);
        String password = preferences.getString("password", null);

        if(username != null && password != null) {
            try {

                DataOutputStream dos = null;
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;
                File sourceFile = new File(document.getLocation());


                File file = new File(document.getLocation());
                FileInputStream fileInputStream = new FileInputStream(file);

                HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://sauray.me/nfcloud/json/upload_file.php").openConnection();
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setUseCaches(false); // Don't use a Cached Copy
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Connection", "Keep-Alive");
                urlConnection.setRequestProperty("ENCTYPE", "multipart/form-data");
                urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                urlConnection.setRequestProperty("file", document.getName());


                Log.d(document.getName(), "document name");
                Log.d(document.getFileName(), "document filename");
                Log.d(document.getLocation(), "document location");


                dos = new DataOutputStream(urlConnection.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);

//Adding Parameter name

                dos.writeBytes("Content-Disposition: form-data; name=\"username\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(username); // mobile_no is String variable
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);

                dos.writeBytes("Content-Disposition: form-data; name=\"password\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(password); // mobile_no is String variable
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);

                dos.writeBytes("Content-Disposition: form-data; name=\"room\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(document.getRoom()+""); // mobile_no is String variable
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);

                Log.d(document.getRoom()+"", "room");

                //Adding Parameter filepath


//Adding Parameter media file(audio,video and image)

                dos.writeBytes(twoHyphens + boundary + lineEnd);

                dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""+ document.getFileName() + "\"" + lineEnd);
                dos.writeBytes(lineEnd);
                // create a buffer of maximum size
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                Log.d(bytesRead+"", "bytes");

                while (bytesRead > 0){
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);


                int serverResponseCode = urlConnection.getResponseCode();
                String serverResponseMessage = urlConnection.getResponseMessage();

                Log.d(serverResponseMessage, "Response message");

                //urlConnection.connect();

                String jsonResult;
                if (serverResponseCode == 201 || serverResponseCode == 200) {
                    Log.d(urlConnection.getResponseCode()+"", "response code");
                    InputStream response = urlConnection.getInputStream();
                    jsonResult = NetworkUtils.convertStreamToString(response);
                    JSONObject jsonObj;
                    try {
                        jsonObj = new JSONObject(jsonResult.toString());
                        int authentication = jsonObj.optInt("authentication");
                        if (authentication == 1) {
                            Log.d("success", "authentication");
                            JSONObject result = jsonObj.getJSONObject("result");
                            ret = result.optInt("state");
                            Log.d(ret+"", "state");
                            Log.d(result.optString("name"), "name");
                        } else {
                            Log.d("fail", "authentication");
                            preferences.edit().clear().commit();
                            c.startActivity(new Intent(c, SplashScreenActivity.class));
                            cancel(true);
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        Log.d("error", "json exception");
                        e.printStackTrace();
                    }

                    fileInputStream.close();
                    dos.flush();
                    dos.close();;
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
            c.startActivity(new Intent(c, SplashScreenActivity.class));
            cancel(true);
        }

        return ret;
    }

    @Override
    protected void onPostExecute(Integer result){

        pd.dismiss();
        if(result == 1){
            AlertDialog.Builder builder = new AlertDialog.Builder(c);
            builder.setTitle("Succès");
            builder.setMessage("Le fichier a bien été transmis");
            builder.setNeutralButton("Ok", null);
            builder.show();
        }
        else if(result==-2){
            AlertDialog.Builder builder = new AlertDialog.Builder(c);
            builder.setTitle("Erreur");
            builder.setMessage("Vous n'avez pas les droits pour transmettre un fichier");
            builder.setNeutralButton("Ok", null);
            builder.show();
        }
        else if (result == -1){
            AlertDialog.Builder builder = new AlertDialog.Builder(c);
            builder.setTitle("Erreur");
            builder.setMessage("Erreur de transmission. Ceci est probablement du à votre explorateur de fichier.");
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
