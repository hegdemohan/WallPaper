package com.cipherScriptDevs.backDrop;

import android.Manifest;
import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;

import java.util.List;

import picasa.Category;
import util.InAppPurchase;
import util.LPref;
import util.SharedPreferencesHelper;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    TabLayout tabLayout;
    ViewPager viewPager;
    List<Category> albumList;
    private static final int PERMISSION_REQUEST_CODE = 123;
    Spinner spinner;
    InAppPurchase inAppPurchase;
    RewardedVideoAds rewardedVideoAds;
    PackageInfo packageInfo;
    Menu menu;
    int version = 101;
    SharedPreferencesHelper sharedPreferences = new SharedPreferencesHelper(this);
    private int count = 0;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this,getResources().getString(R.string.app_id));
        SharedPreferences sharedPreferenceLimitReached = AppController.getAppContext().getSharedPreferences("limitReached",MODE_PRIVATE);
        if(sharedPreferenceLimitReached.getBoolean("limitReached",false)){
            String message = getResources().getString(R.string.max_limit);
            SharedPreferences.Editor editor = sharedPreferenceLimitReached.edit();
            editor.putBoolean("limitReached", false);
            editor.apply();
            openDialog(message);
        }
        inAppPurchase = new InAppPurchase(this);
        inAppPurchase.checkAppPurchase();
        rewardedVideoAds = RewardedVideoAds.getInstance();
        rewardedVideoAds.loadAd(this);
        sharedPreferences.setSharedPreferences();
        albumList = AppController.getInstance().getPrefManger().getCategories();
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new CustomAdapter(getSupportFragmentManager()));

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        menu = navigationView.getMenu();
        //Make categories white
        MenuItem tools = menu.findItem(R.id.categories);
        SpannableString s = new SpannableString(tools.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearance44), 0, s.length(), 0);
        tools.setTitle(s);

        navigationView.setNavigationItemSelectedListener(this);
        sharedPreferences.fetchRemoteConfigParams();
        boolean perm = hasPermissions();
        if (perm) {
            //Do Nothing
        } else {
            requestPerms();
        }


        askForRating("onCreate");

        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        LPref.putIntPref("login_count_update",LPref.getIntPref("login_count_update",0));
        if ( version < LPref.getIntPref("newUpdate",100)) {
            askForUpdate();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferenceLimitReached = AppController.getAppContext().getSharedPreferences("limitReached",MODE_PRIVATE);
        if(sharedPreferenceLimitReached.getBoolean("limitReached",false)){
            String message = getResources().getString(R.string.max_limit);
            SharedPreferences.Editor editor = sharedPreferenceLimitReached.edit();
            editor.putBoolean("limitReached", false);
            editor.apply();
            openDialog(message);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.category_abstract) {
            viewPager.setCurrentItem(0);
        } else if (id == R.id.category_animals) {
            viewPager.setCurrentItem(1);
        } else if (id == R.id.category_anime) {
            viewPager.setCurrentItem(2);
        } else if (id == R.id.category_architecture) {
            viewPager.setCurrentItem(3);
        } else if (id == R.id.category_auto) {
            viewPager.setCurrentItem(4);
        } else if (id == R.id.category_backgrounds) {
            viewPager.setCurrentItem(5);
        } else if (id == R.id.category_brands) {
            viewPager.setCurrentItem(6);
        } else if (id == R.id.category_fantasy){
            viewPager.setCurrentItem(7);
        } else if (id == R.id.category_games){
            viewPager.setCurrentItem(8);
        } else if (id == R.id.category_landscape){
            viewPager.setCurrentItem(9);
        } else if (id == R.id.category_sports){
            viewPager.setCurrentItem(10);
        } else if (id == R.id.category_downloads) {
            viewPager.setCurrentItem(11);
        } else if (id == R.id.floatingWidget) {
            floatingWidgetDialog();
        } else if (id == R.id.settings) {
            settingsDialog();
        } else if (id == R.id.policy){
            policyDialog();
        } else if (id == R.id.premium){
            inAppPurchase.removeAds();
        } else if (id == R.id.rate){
            askForRating("navButton");
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void openDialog(String message){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View dialogBox = getLayoutInflater().inflate(R.layout.dialog_max_limit, null);
        TextView messageText = dialogBox.findViewById(R.id.message);
        messageText.setText(message);

        dialogBuilder.setView(dialogBox);
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
    }

    public void policyDialog(){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View dialogBox = getLayoutInflater().inflate(R.layout.dialog_policy, null);
        dialogBuilder.setView(dialogBox);
        final AlertDialog dialog = dialogBuilder.create();
        dialog.show();
        Button okay = dialogBox.findViewById(R.id.okay);
        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void settingsDialog() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        final View dialogBox = getLayoutInflater().inflate(R.layout.dialog_settings, null);
        dialogBuilder.setView(dialogBox);
        final AlertDialog dialogSetting = dialogBuilder.create();
        dialogSetting.show();
        spinner = dialogBox.findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, getResources().getStringArray(R.array.Album_array));
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(adapter);

        final SharedPreferences sharedPreferencesAlbumSelected = getSharedPreferences("albumSelected", MODE_PRIVATE);
        spinner.setSelection(sharedPreferencesAlbumSelected.getInt("albumSelected", 0));
        Button saveButton = dialogBox.findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editorHour = sharedPreferencesAlbumSelected.edit();
                editorHour.putInt("albumSelected", spinner.getSelectedItemPosition());
                editorHour.apply();
                dialogSetting.dismiss();
                sharedPreferences.setSharedPreferences();
                Toast.makeText(MainActivity.this, "Preference saved successfully", Toast.LENGTH_SHORT).show();
            }
        });
        TextView cancelButton = dialogBox.findViewById(R.id.cancelSettings);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogSetting.dismiss();
            }
        });
    }

    private void floatingWidgetDialog() {
        AppWidgetManager mAppWidgetManager = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAppWidgetManager = this.getSystemService(AppWidgetManager.class);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mAppWidgetManager != null && mAppWidgetManager.isRequestPinAppWidgetSupported()) {
                WallpaperWidget wallpaperWidget = new WallpaperWidget();
                wallpaperWidget.addWidget(this);
            }
        }else {
            final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
            final View dialogBox = getLayoutInflater().inflate(R.layout.layout_floating_widget_dialog, null);
            dialogBuilder.setView(dialogBox);
            final AlertDialog dialogFloaterWidget = dialogBuilder.create();
            dialogFloaterWidget.show();
            Button okayButton = dialogBox.findViewById(R.id.okay);
            okayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogFloaterWidget.dismiss();
                }
            });
        }
    }

    private class CustomAdapter extends FragmentPagerAdapter {
        public CustomAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            if (position != albumList.size()) {
                bundle.putString("albumId", albumList.get(position).getId());
                return GridFragment.newInstance(bundle);
            } else {
                bundle.putString("albumId", "Downloads");
                return RecyclerFragment.newInstance(bundle);
            }
        }

        @Override
        public int getCount() {
            return albumList.size()+1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position == albumList.size()){
                return "Downloads";
            }else {
                return albumList.get(position).title;
            }
        }
    }

    public boolean hasPermissions() {
        int res;
        String[] permissions = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};

        for (String perms : permissions) {
            res = checkCallingOrSelfPermission(perms);
            if (!(res == PackageManager.PERMISSION_GRANTED)) {
                return false;
            }
        }
        return true;
    }

    private void requestPerms() {
        String[] permissions = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }

    private void askForRating(String from){
        if(LPref.getBooleanPref("rate_given",false)){
            menu.findItem(R.id.rate).setVisible(false);
        }
        count = LPref.getIntPref("login_count", 0);
        if (count >= 50 && !LPref.getBooleanPref("rate_given",false)) {
            LPref.putIntPref("login_count",0);
            performRatingRequest();
        }else if(from.equals("navButton") && !LPref.getBooleanPref("rate_given",false)) {
            performRatingRequest();
        }else {
            LPref.putIntPref("login_count",count+1);
        }
    }

    private void askForUpdate(){
        if (LPref.getIntPref("login_count_update",0) == 0){
            performUpdateRequest();
            LPref.putIntPref("login_count_update",LPref.getIntPref("login_count_update",0)+1);
        }else if (LPref.getIntPref("login_count_update",0) >= 5){
            performUpdateRequest();
            LPref.putIntPref("login_count_update",0);
        }else {
            LPref.putIntPref("login_count_update",LPref.getIntPref("login_count_update",0)+1);
        }
    }

    public void performUpdateRequest(){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View dialogBox = getLayoutInflater().inflate(R.layout.dialog_update, null);
        TextView heading = dialogBox.findViewById(R.id.message_heading);
        heading.setText(this.getResources().getString(R.string.update_heading));
        TextView messageText = dialogBox.findViewById(R.id.message_content);
        messageText.setText(this.getString(R.string.message_update_now));
        dialogBuilder.setView(dialogBox);
        final AlertDialog dialog = dialogBuilder.create();

        TextView rate = dialogBox.findViewById(R.id.update);
        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                try {
                    menu.findItem(R.id.rate).setVisible(false);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + AppConst.APPLICATION_ID)));
                } catch (android.content.ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + AppConst.APPLICATION_ID)));
                }
            }
        });

        TextView later = dialogBox.findViewById(R.id.update_later);
        later.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(true);

        try {
            dialog.show();
        } catch (Exception e) {

        }
    }

    public void performRatingRequest(){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View dialogBox = getLayoutInflater().inflate(R.layout.dialog_rate_us, null);
        TextView messageText = dialogBox.findViewById(R.id.message);
        messageText.setText(this.getString(R.string.message_rate_now));
        dialogBuilder.setView(dialogBox);
        final AlertDialog dialog = dialogBuilder.create();

        TextView rate = dialogBox.findViewById(R.id.rate);
        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                try {
                    menu.findItem(R.id.rate).setVisible(false);
                    LPref.putBooleanPref("rate_given",true);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + AppConst.APPLICATION_ID)));
                } catch (android.content.ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + AppConst.APPLICATION_ID)));
                }
            }
        });

        TextView later = dialogBox.findViewById(R.id.later);
        later.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(true);

        try {
            dialog.show();
        } catch (Exception e) {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean allowed = true;
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                for (int res : grantResults) {
                    allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
                }
                break;
        }
        if (allowed) {
            //user granted permission
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "Downloading image will not work", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
