package model.db.external;

import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.TextView;

/**
 * Created by sauray on 22/03/15.
 */
public class CheckHashAuthenticity extends AsyncTask<String,Void, Boolean>{

    private TextView textView;

    public CheckHashAuthenticity(TextView textView){
        this.textView = textView;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result){
        if(result) {
            textView.setTextColor(Color.GREEN);
            textView.setText("La carte est authentique");
        }
        else{
            textView.setTextColor(Color.RED);
            textView.setText("La carte n'est pas authentique. Renvoyez la au fabricant.");
        }
    }
}
