package model.db.external;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import model.Document;
import model.db.internal.CloudDAO;
import view.activity.PdfActivity;


public class GetDocument extends AsyncTask<Document, Integer, Document>{

    private Activity a;
    private ProgressDialog pd;
    private String fileUrl;

    public GetDocument(Activity a) {
        this.a = a;
// instantiate it within the onCreate method
        pd = new ProgressDialog(a);
        pd.setIndeterminate(true);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setCancelable(true);
        pd.show();
    }

    @Override
    protected Document doInBackground(Document... params) {
        if(params[0].getLocation()==null) {

            pd.setMessage("Téléchargement de "+params[0].getName());

            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                fileUrl = "http://sauray.me/nfcloud/data/rooms/"+params[0].getRoom()+"/"+params[0].getFileName();
                Log.d(fileUrl, "url");
                URL url = new URL(fileUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.d("ERROR", "http error");
                    return null;
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                if(!isExternalStorageWritable()){
                    String location = Environment.getExternalStorageDirectory().getPath()+"/"+params[0].getName()+"."+params[0].getExtension();
                    Log.d(location, "location");
                    output = new FileOutputStream(location);
                    params[0].setLocation(location);
                }
                else{
                    String location = a.getExternalFilesDir(null).getPath()+"/"+params[0].getName()+"."+params[0].getExtension();
                    Log.d(location, "location");
                    output = new FileOutputStream(location);
                    params[0].setLocation(location);
                }

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        Log.d("ERROR", "cancelled");
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
                CloudDAO dao = new CloudDAO(a);
                dao.open();
                dao.insertDocument(params[0]);
                dao.close();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
        }
        return params[0];
    }

    protected void onProgressUpdate(Integer...progress){
        pd.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(Document result){
        pd.dismiss();
        if(result != null) {
            if(result.getExtension().equals("pdf")) {
                Intent intent = new Intent(a, PdfActivity.class);
                intent.putExtra("DOCUMENT", result);
                a.startActivity(intent);
            }
            else{
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                String contentType=null;
                Log.d(result.getExtension(), "extension");
                if(result.getExtension().matches("mp3|ogg|flac")){
                    contentType="audio/*";
                }
                else if(result.getExtension().matches("mp4|mov|avi")){
                    contentType="video/*";
                }
                else if(result.getExtension().matches("jpg|png|gif")){
                    contentType="image/*";
                }
                else{

                }
                intent.setDataAndType(Uri.fromFile(new File(result.getLocation())),contentType);
                a.startActivity(intent);
            }
        }
        else{
            if(NetworkUtils.isConnected(a)){
                AlertDialog.Builder builder = new AlertDialog.Builder(a);
                builder.setMessage("Erreur téléchargement")
                        .setTitle("Le fichier n'a pas pu être téléchargé.");
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            else{
                AlertDialog.Builder builder = new AlertDialog.Builder(a);
                builder.setMessage("Ouverture impossible")
                        .setTitle("Vous n'avez pas préchargé ce fichier.");
                AlertDialog dialog = builder.create();
                dialog.show();
            }

        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}


