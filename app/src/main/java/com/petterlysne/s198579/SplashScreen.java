package com.petterlysne.s198579;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;

public class SplashScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        isSplashShown();

        if(checkNetworkState())
            new FetchDataDuringSplash().execute();

    }

    /*
     Tilstandsbevaring. Sjekker om splashscreen allerede er vist.
     I såfall går vi direkte til hoved-aktivitet
      */
    private void isSplashShown() {
        if(!getSharedPreferences("myPrefs", 0).getString("splashShown", "").equals("")) {
            Intent i = new Intent(SplashScreen.this, MainActivity.class);
            startActivity(i);
            finish();
        }

        else getSharedPreferences("myPrefs", 0).edit().putString("splashShown", "true");
    }

    // Sjekker hvorvidt en er koblet til internett eller ei
    private boolean checkNetworkState() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager.getActiveNetworkInfo() == null || !connectivityManager.getActiveNetworkInfo().isConnected()) {
            visAlert();
            return false;
        }

        else return true;
    }

    // Henter 5 wikipedia artikler under splashscreen, og går deretter videre til hoved-aktivitet
    private class FetchDataDuringSplash extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            for (int i = 0; i < 5; i++)
                ContentFactory.getInstance().addPage();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Intent i = new Intent(SplashScreen.this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }

    // Alert dialog vist dersom man ikke er koblet til internett
    private void visAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Not connected");
        alertDialog.setMessage("This application requires an internet connection.");
        alertDialog.setIcon(R.drawable.wifi);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
        alertDialog.show();
    }

}
