package view.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.infotel.greenwav.infotel.R;

import java.security.NoSuchAlgorithmException;

import model.db.external.json.Authentication;
import model.db.external.json.Register;
import model.security.Hash;

/**
 * SplashScreen of the application
 * It shows up when the application is started to perform operations in the background.
 * @author Antoine Sauray
 * @version 1.0
 */
public class SplashScreenActivity extends Activity{

    // ----------------------------------- UI
    /**
     * Unique identifier for this activity
     */
    private static final String TAG = "SPLASHSCREEN_ACTIVITY";

    // ----------------------------------- Constants
    /**
     * Allows to check if this is the first time the user launches the application
     */
    private final String PREFS_FL = "firstLaunch";
    /**
     * The animation for the Greenwav' logo
     */
    private ImageView logo;
    private Button connexion, inscription;
    private EditText usernameEdit, passwordEdit, mail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        usernameEdit = (EditText) findViewById(R.id.username);
        passwordEdit = (EditText) findViewById(R.id.password);
        connexion = (Button) findViewById(R.id.connexion);
        mail = (EditText) findViewById(R.id.mail);
        inscription = (Button) findViewById(R.id.inscription);

        Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isfirstrun", true);
        if (isFirstRun) {
            getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("isfirstrun", false).commit();
            this.finish();
        }

        logo = (ImageView) findViewById(R.id.logoSplashScreen);

        ObjectAnimator animY = ObjectAnimator.ofFloat(logo, "translationY", -800f);
        animY.setDuration(2000);//1.5sec
        animY.setRepeatCount(0);
        animY.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                SharedPreferences preferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
                String username = preferences.getString("username", null);
                String password = preferences.getString("password", null);

                Intent intent = null;
                if(username != null && password != null) {
                    intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                    SplashScreenActivity.this.startActivity(intent);
                }
                else{
                    usernameEdit.setVisibility(View.VISIBLE);
                    passwordEdit.setVisibility(View.VISIBLE);
                    connexion.setVisibility(View.VISIBLE);
                    inscription.setVisibility(View.VISIBLE);

                    passwordEdit.setOnKeyListener(new View.OnKeyListener() {
                        @Override
                        public boolean onKey(View v, int keyCode, KeyEvent event) {
                            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                                new Authentication(SplashScreenActivity.this, usernameEdit.getText().toString(), passwordEdit.getText().toString()).execute();
                                return true;
                            }
                            return false;
                        }
                    });
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animY.start();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void onClick(View v){
        if(v == inscription){
            Log.d("", "onClick");
            ObjectAnimator animY = null;
            if(mail.getVisibility()==View.GONE){
                animY = ObjectAnimator.ofFloat(logo, "translationY", -100f);
            }
            else{
                animY = ObjectAnimator.ofFloat(logo, "translationY", -800f);
            }
            animY.setDuration(2000);//1.5sec
            animY.setRepeatCount(0);
            animY.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if(mail.getVisibility()==View.GONE){
                        mail.setVisibility(View.VISIBLE);
                        inscription.setText("Connexion");
                        connexion.setText("Inscription");
                    }
                    else{
                        mail.setVisibility(View.GONE);
                        inscription.setText("Inscription");
                        connexion.setText("Connexion");
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animY.start();
        }
        else{
            if(mail.getVisibility()==View.VISIBLE){
                try {
                    new Register(SplashScreenActivity.this, usernameEdit.getText().toString(), mail.getText().toString(), Hash.sha256(passwordEdit.getText().toString())).execute();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
            else {
                new Authentication(SplashScreenActivity.this, usernameEdit.getText().toString(), passwordEdit.getText().toString()).execute();
            }
        }
    }

}
