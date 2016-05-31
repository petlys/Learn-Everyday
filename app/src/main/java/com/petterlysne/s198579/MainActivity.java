package com.petterlysne.s198579;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easyandroidanimations.library.FlipVerticalAnimation;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private TextView content, title;
    private ImageView image;
    private int currentPage;
    private SwipeRefreshLayout swipeView;
    private HelpFragment helpFragment;
    private ProgressBar imageLoading;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        swipeView = (SwipeRefreshLayout) findViewById(R.id.swipe);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00b1ec")));

        content = (TextView) findViewById(R.id.articleContent);
        title = (TextView) findViewById(R.id.articleTitle);
        image = (ImageView) findViewById(R.id.imageView);
        imageLoading = (ProgressBar) findViewById(R.id.imageLoading);
        helpFragment = new HelpFragment();

        content.setMovementMethod(new ScrollingMovementMethod());

        // Tilstandsbevaring
        if (savedInstanceState != null)
            currentPage = savedInstanceState.getInt("currentPage");

        else currentPage = 0;

        updateContent();

        setUpListeners();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Oppdaterer bilde, tittel og innhold
    private void updateContent() {
        imageLoading.setVisibility(View.VISIBLE);
        content.setText(ContentFactory.pages.get(currentPage).getContent());
        title.setText(ContentFactory.pages.get(currentPage).getTitle());

        Picasso picasso = new Picasso.Builder(getApplicationContext()).listener(new Picasso.Listener() {

            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                if (exception instanceof IOException)
                    picasso.load(R.drawable.missingimage).fit().centerCrop().into(image);
            }

        }).build();

        picasso.load(ContentFactory.pages.get(currentPage).getImageURL()).fit().centerCrop().into(image, new Callback() {
            @Override
            public void onSuccess() {
                imageLoading.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError() {
                imageLoading.setVisibility(View.INVISIBLE);
            }
        });

    }

    // Tilstandsbevaring
    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putInt("currentPage", currentPage);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.help) {
            showHelpFragment();
            return true;
        }

        if (id == R.id.language) {
            startActivity(new Intent(Settings.ACTION_LOCALE_SETTINGS));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Viser eller skjuler hjelp-fragmentet
    private void showHelpFragment() {
        if (!helpFragment.isVisible()) {
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.relativeLayout, helpFragment, "helpFragment");
            transaction.commit();
        }

        else getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentByTag("helpFragment")).commit();
    }

    // Skjuler hjelp-fragmentet dersom det trykkes noe sted på skjermen samtidig som fragmentet vises
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(helpFragment.isVisible())
            getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentByTag("helpFragment")).commit();

        return super.onTouchEvent(event);
    }

    // Henter ny artikkel i bakgrunnen
    private class FetchNewData extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            ContentFactory.getInstance().addPage();

            return null;
        }

    }

    // Alert dialog som vises dersom man ikke er koblet til internett
    private void showNetworkAlert() {
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

    // Sjekker hvorvidt man er koblet til internett eller ei
    private boolean checkNetworkState() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager.getActiveNetworkInfo() == null || !connectivityManager.getActiveNetworkInfo().isConnected()) {
            showNetworkAlert();
            return false;
        }

        else return true;
    }

    // Setter opp lyttere
    private void setUpListeners() {
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(helpFragment.isVisible())
                    getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentByTag("helpFragment")).commit();

                else {
                    Intent intent = new Intent(getBaseContext(), WebActivity.class);
                    intent.putExtra("pageid", ContentFactory.pages.get(currentPage).getPageid());
                    startActivity(intent);
                }
            }
        });

        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeView.setRefreshing(true);
                currentPage++;

                if (checkNetworkState())
                    new FetchNewData().execute();

                updateContent();
                swipeView.setRefreshing(false);
                new FlipVerticalAnimation(swipeView).animate();
            }
        });
    }
}
