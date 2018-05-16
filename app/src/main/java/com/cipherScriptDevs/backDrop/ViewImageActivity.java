package com.cipherScriptDevs.backDrop;

/**
 * Created by mhegde on 04/20/2017.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import adapter.ViewImageAdapter;
import picasa.Wallpaper;
import util.InAppPurchase;
import util.LPref;
import util.Utils;

public class ViewImageActivity extends AppCompatActivity implements OnClickListener {
    private static final String TAG = ViewImageActivity.class.getSimpleName();
    public static final String TAG_SEL_IMAGE = "selectedImage";
    public static String TAG_SEL_AlBUM = "selectedAlbum";
    private Wallpaper selectedPhoto;
    private ImageView fullImageView;
    private LinearLayout llSetWallpaper, llDownloadWallpaper;
    private Utils utils;
    private String fullResolutionUrl;
    private ProgressBar pbLoader;
    private LinearLayout processing;
    private FloatingActionButton fab;
    private Bitmap bitmap;
    private LinearLayout fullLayout;
    private final int SWIPE_MIN_DISTANCE = 120;
    private final int SWIPE_THRESHOLD_VELOCITY = 200;
    private boolean isProcessed;
    private AdView mAdView;
    private String message = "";
    private boolean isLoaded = true;
    private String url;
    private ViewImageAdapter viewImageAdapter;
    private boolean isLoadedInterstitial = false;

    private InAppPurchase inAppPurchase;

    private JSONObject mediaObj;
    private int selectedPhotoPosition;

    private String selectedAlbum;
    private InterstitialAd interstitialAd;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_view_image);
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("B43FCE5959423443AF7C9BFA9DAF18C3").build();
        if(LPref.getIntPref(AppConst.PRODUCT_AD_ID, AppConst.PRODUCT_NOT_BOUGHT) == 2){
            mAdView.loadAd(adRequest);
        }

        Toolbar myToolbar = findViewById(R.id.viewImageToolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        fullLayout = findViewById(R.id.fullLayout);
        llSetWallpaper = findViewById(R.id.llSetWallpaper);
        llDownloadWallpaper = findViewById(R.id.llDownloadWallpaper);
        fab = findViewById(R.id.fab);
//        pbLoader = findViewById(R.id.progressBarViewImage);
        processing =  findViewById(R.id.processing);
//        pbLoader.setVisibility(View.VISIBLE);
        utils = new Utils(getApplicationContext());

        // layout click listeners
        llSetWallpaper.setOnClickListener(this);
        llDownloadWallpaper.setOnClickListener(this);
  //      fullImageView.setOnClickListener(this);
        fab.setOnClickListener(this);

        Intent i = getIntent();
        selectedPhoto = (Wallpaper) i.getSerializableExtra(TAG_SEL_IMAGE);
        selectedAlbum = i.getStringExtra(TAG_SEL_AlBUM);
        selectedPhotoPosition = i.getIntExtra("selectedPhotoPosition",0);



        SharedPreferences sharedPreferencesPhotosListForSlider = getSharedPreferences("photosListSlider", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferencesPhotosListForSlider.getString("photosListSlider", "");
        Type type = new TypeToken<List<Wallpaper>>(){}.getType();
        List<Wallpaper> photosList = gson.fromJson(json, type);
        viewPager = findViewById(R.id.viewImagePager);

        viewImageAdapter = new ViewImageAdapter(this, photosList,viewPager,selectedAlbum,interstitialAd);
        viewImageAdapter.LoadInterstitialAd();
        viewPager.setAdapter(viewImageAdapter);
        viewPager.setCurrentItem(selectedPhotoPosition);
    }

    /**
     * View click listener
     */
    @Override
    public void onClick(View v) {
        viewImageAdapter.performClickOperations(v,selectedAlbum,viewPager.getCurrentItem(),processing,llSetWallpaper);
    }

    //back button pressed on toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }



    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_report, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.report: {
                Resources res = getResources();
                String body = String.format(res.getString(R.string.content), fullResolutionUrl);
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:cipherscriptdevelopers@gmail.com"));
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject));
                intent.putExtra(Intent.EXTRA_TEXT,body);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                break;
            }
        }
        return false;
    }


}