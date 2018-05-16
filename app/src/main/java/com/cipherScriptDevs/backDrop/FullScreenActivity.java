package com.cipherScriptDevs.backDrop;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.List;

import adapter.FullScreenImageAdapter;
import picasa.Wallpaper;
import util.InAppPurchase;
import util.LPref;
import util.Utils;

public class FullScreenActivity extends AppCompatActivity {
    Boolean isCalled = false;
    String fullResolutionUrl;
    LinearLayout llSetWallpaper;
    Utils utils = new Utils(AppController.getAppContext());
    LinearLayout llDownloadWallpaper;
    HorizontalScrollView horizontalScroll;
    private Boolean isFabOpen = false;
    private FloatingActionButton fab,fab1,fab2;
    TranslateAnimation _translateAnimation;
    private Animation fab_open,fab_close,rotate_forward,rotate_backward;
    private String message = "";
    private String selectedAlbumId;
    private int selectedPhotoPosition;
    private InAppPurchase inAppPurchase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_full_screen);

        _translateAnimation = new TranslateAnimation(TranslateAnimation.ABSOLUTE, 100f, TranslateAnimation.ABSOLUTE, 0f, TranslateAnimation.ABSOLUTE, 0f, TranslateAnimation.ABSOLUTE, 0f);
        llSetWallpaper = findViewById(R.id.llSetWallpaper);
        llDownloadWallpaper = findViewById(R.id.llDownloadWallpaper);
//        horizontalScroll = findViewById(R.id.hsl);

        fab = findViewById(R.id.fab);
        fab1 = findViewById(R.id.fab1);
        fab2 = findViewById(R.id.fab2);

        final ViewPager viewPager = findViewById(R.id.fullScreenViewPager);
        fullResolutionUrl = getIntent().getStringExtra("url");
        selectedAlbumId = getIntent().getStringExtra("selectedAlbumId");
        selectedPhotoPosition = getIntent().getIntExtra("selectedPhotoPosition",999999999);
        SharedPreferences sharedPreferencesPhotosListForSlider = getSharedPreferences("photosListSlider", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferencesPhotosListForSlider.getString("photosListSlider", "");
        Type type = new TypeToken<List<Wallpaper>>(){}.getType();
        List<Wallpaper> photosList = gson.fromJson(json, type);
        final FullScreenImageAdapter fullScreenImageAdapter = new FullScreenImageAdapter(this,photosList,selectedAlbumId);
        viewPager.setAdapter(fullScreenImageAdapter);
        fullScreenImageAdapter.LoadInterstitialAd();
        viewPager.setCurrentItem(selectedPhotoPosition);

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backward);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFAB();
            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferenceVideoAdSetFireBase = getSharedPreferences("VideoAdSetFireBase",Context.MODE_PRIVATE);
                int VIDEO_AD_SET = sharedPreferenceVideoAdSetFireBase.getInt("VideoAdSetFireBase",30);
                SharedPreferences sharedPreferenceMaxLimitVideo = getSharedPreferences("maxLimitVideo", Context.MODE_PRIVATE);
                if (sharedPreferenceMaxLimitVideo.getInt("maxLimitVideo", 0) >= VIDEO_AD_SET && LPref.getIntPref(AppConst.PRODUCT_AD_ID, AppConst.PRODUCT_NOT_BOUGHT) == 2) {
                    message = getResources().getString(R.string.max_limit);
                    openDialog(message);
                } else {
                    fullScreenImageAdapter.setWallpaper(viewPager.getCurrentItem());
                }
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferenceVideoAdDownloadFireBase = getSharedPreferences("VideoAdDownloadFireBase",Context.MODE_PRIVATE);
                int VIDEO_AD_DOWNLOAD = sharedPreferenceVideoAdDownloadFireBase.getInt("VideoAdDownloadFireBase",10);
                SharedPreferences sharedPreferenceMaxLimitVideoDownload = getSharedPreferences("maxLimitVideoDownload", Context.MODE_PRIVATE);
                if (sharedPreferenceMaxLimitVideoDownload.getInt("maxLimitVideoDownload",0) >= VIDEO_AD_DOWNLOAD&& LPref.getIntPref(AppConst.PRODUCT_AD_ID, AppConst.PRODUCT_NOT_BOUGHT) == 2){
                    message = getResources().getString(R.string.max_limit);
                    openDialog(message);
                }else {
                    fullScreenImageAdapter.saveToSdCard(viewPager.getCurrentItem());
                }
            }
        });
    }

    public void animateFAB(){

        if(isFabOpen){

            fab.startAnimation(rotate_backward);
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            isFabOpen = false;

        } else {

            fab.startAnimation(rotate_forward);
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            isFabOpen = true;

        }
    }

    public void openDialog(String message){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View dialogBox = getLayoutInflater().inflate(R.layout.dialog_max_limit, null);
        dialogBuilder.setView(dialogBox);
        TextView messageText = dialogBox.findViewById(R.id.message);
        messageText.setText(message);
        final AlertDialog dialog = dialogBuilder.create();
        dialog.show();
        Button watch_video = dialogBox.findViewById(R.id.watch_video);
        watch_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RewardedVideoAds.getInstance().showAd();
                dialog.dismiss();
            }
        });

        Button removeAds = dialogBox.findViewById(R.id.premium);
        removeAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inAppPurchase = new InAppPurchase(FullScreenActivity.this);
                inAppPurchase.removeAds();
                dialog.dismiss();
            }
        });
    }
}

