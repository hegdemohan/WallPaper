package com.cipherScriptDevs.backDrop;

/**
 * Created by mhegde on 04/25/2017.
 */

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import adapter.FullScreenImageAdapterSdCardImage;
import util.Utils;

public class FullScreenActivitySdCardImage extends AppCompatActivity{

    private Utils utils;
    private FullScreenImageAdapterSdCardImage adapter;
    private ViewPager viewPager;
    private FloatingActionButton fab;
    private FloatingActionButton fab1;
    private FloatingActionButton fab2;
    private FloatingActionButton fab3;
    private Animation fab_open,fab_close,rotate_forward,rotate_backward;
    private boolean isFabOpen = false;
    private GridFragment gridFragment = new GridFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_full_screen);

        setContentView(R.layout.activity_fullscreen_sdcard_image);

        viewPager = findViewById(R.id.pager);

        fab = findViewById(R.id.fabFullScreen);
        fab1 = findViewById(R.id.fabFullScreen1);
        fab2 = findViewById(R.id.fabFullScreen2);
        fab3 = findViewById(R.id.fabFullScreen3);

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
                setWallPaper();
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteImage();
            }
        });
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareImage();
            }
        });

        utils = new Utils(FullScreenActivitySdCardImage.this);

        Intent i = getIntent();
        int position = i.getIntExtra("position", 0);

        adapter = new FullScreenImageAdapterSdCardImage(FullScreenActivitySdCardImage.this,
                utils.getFilePaths());

        viewPager.setAdapter(adapter);

        // displaying selected image first
        viewPager.setCurrentItem(position);


    }

    public void shareImage(){
        SharedPreferences sharedPreferenceVideoAdShareFireBaseFireBase = getSharedPreferences("VideoAdShareFireBase",MODE_PRIVATE);
        int VIDEO_AD_SHARE = sharedPreferenceVideoAdShareFireBaseFireBase.getInt("VideoAdShareFireBase",10);
        SharedPreferences sharedPreferenceMaxLimitVideoShare = getSharedPreferences("maxLimitVideoShare", Context.MODE_PRIVATE);
        if (sharedPreferenceMaxLimitVideoShare.getInt("maxLimitVideoShare",0) >= VIDEO_AD_SHARE){
            String message = getResources().getString(R.string.max_limit);
            openDialog(message);
        }else {
            File shareImage = new File(utils.getFilePaths().get(viewPager.getCurrentItem()));
            if (shareImage.exists()) {
                File f = new File(shareImage.getAbsolutePath());
                Uri uri = Uri.parse("file://" + f.getAbsolutePath());
                Intent share = new Intent(Intent.ACTION_SEND);
                share.putExtra(Intent.EXTRA_STREAM, uri);
                share.setType("image/*");
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                getApplicationContext().startActivity(Intent.createChooser(share, "Share image File"));
            }
        }

    }

    public void setWallPaper(){

        SharedPreferences sharedPreferenceVideoAdShareFireBaseFireBase = getSharedPreferences("VideoAdShareFireBase",MODE_PRIVATE);
        int VIDEO_AD_SHARE = sharedPreferenceVideoAdShareFireBaseFireBase.getInt("VideoAdShareFireBase",10);
        SharedPreferences sharedPreferenceMaxLimitVideoShare = getSharedPreferences("maxLimitVideoShare", Context.MODE_PRIVATE);
        if (sharedPreferenceMaxLimitVideoShare.getInt("maxLimitVideoShare",0) >= VIDEO_AD_SHARE){
            String message = getResources().getString(R.string.max_limit);
            openDialog(message);
        }else {
            try {
                WallpaperManager wm = WallpaperManager.getInstance(getApplicationContext());
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(utils.getFilePaths().get(viewPager.getCurrentItem()), options);
                wm.setBitmap(bitmap);
                Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_wallpaper_set), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_wallpaper_set_failed), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void deleteImage(){
        File deleteImage = new File(utils.getFilePaths().get(viewPager.getCurrentItem()));
        if (deleteImage.exists()) {
            if (deleteImage.delete()) {
                Toast.makeText(getApplicationContext(),"Wallpaper deleted",Toast.LENGTH_SHORT).show();
                    Intent i = getIntent();
                    int position = i.getIntExtra("position", 0);
                    adapter = new FullScreenImageAdapterSdCardImage(FullScreenActivitySdCardImage.this,
                            utils.getFilePaths());
                    viewPager.setAdapter(adapter);
                    // displaying selected image first
                    viewPager.setCurrentItem(position);
                if(utils.getFilePaths().size() == 0 ){
                    finish();
                }
            } else {
                Toast.makeText(getApplicationContext(),"Wallpaper could not be deleted",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void openDialog(String message){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View dialogBox = getLayoutInflater().inflate(R.layout.dialog_max_limit, null);
        TextView messageText = (TextView) dialogBox.findViewById(R.id.message);
        messageText.setText(message);

        dialogBuilder.setView(dialogBox);
        dialogBuilder.setView(dialogBox);
        final AlertDialog dialog = dialogBuilder.create();
        dialog.show();
        Button watch_video = (Button) dialogBox.findViewById(R.id.watch_video);
        watch_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RewardedVideoAds.getInstance().showAd();
                dialog.dismiss();
            }
        });
    }

    public void animateFAB(){
        if(isFabOpen){

            fab.startAnimation(rotate_backward);
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab3.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            fab3.setClickable(false);
            isFabOpen = false;

        } else {

            fab.startAnimation(rotate_forward);
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab3.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            fab3.setClickable(true);
            isFabOpen = true;

        }
    }
}
