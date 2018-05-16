package com.cipherScriptDevs.backDrop;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import adapter.ViewImageAdapter;
import picasa.Wallpaper;
import util.LPref;

import static android.content.Context.MODE_PRIVATE;

/**
 * Implementation of App Widget functionality.
 */
public class WallpaperWidget extends AppWidgetProvider {
    private int selectedPhotoPosition;
    private String selectedAlbum;
    private List<Wallpaper> photosList = new ArrayList<>();
    private boolean wallpaperLoaded = false;
    private boolean nextImage =false;
    private boolean prevImage = false;
    private JSONObject mediaObj;
    private RemoteViews remoteViews;
    private ComponentName widget;

    private static final String TAG_ENTRY = "entry",
            TAG_MEDIA_GROUP = "media$group",
            TAG_MEDIA_CONTENT = "media$content", TAG_IMG_URL = "url";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.wallpaper_widget);

        Intent openApp = new Intent(context,MainActivity.class);
        PendingIntent pendingIntent =  PendingIntent.getActivity(context,0,openApp,0);
        views.setOnClickPendingIntent(R.id.iconImage,pendingIntent);

        Intent next = new Intent(context, WallpaperWidget.class);
        next.setAction("Clicked Next Wallpaper"); // assign intent action
        int flagNext = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent piNext = PendingIntent.getBroadcast(context, 0, next, flagNext);
        views.setOnClickPendingIntent(R.id.next, piNext);

        Intent prev = new Intent(context, WallpaperWidget.class);
        prev.setAction("Clicked Previous Wallpaper"); // assign intent action
        int flagPrev = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent piPrev = PendingIntent.getBroadcast(context, 0, prev, flagPrev);
        views.setOnClickPendingIntent(R.id.previous, piPrev);


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        remoteViews = new RemoteViews( context.getPackageName(), R.layout.wallpaper_widget );
        assert conMgr != null;
        if ( conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED ) {
            String albumId;
            if (intent.getAction().equals("Clicked Next Wallpaper") && !wallpaperLoaded) {
                remoteViews.setViewVisibility(R.id.progressBar, View.VISIBLE);
                remoteViews.setViewVisibility(R.id.next,View.GONE);
                remoteViews.setViewVisibility(R.id.previous,View.GONE);
                widget = new ComponentName( context, WallpaperWidget.class );
                (AppWidgetManager.getInstance(context)).updateAppWidget( widget, remoteViews );
                SharedPreferences sharedPreferencesAlbumSelected = context.getSharedPreferences("albumSelected", MODE_PRIVATE);
                SharedPreferences sharedPreferencesAlbumId = context.getSharedPreferences("albumId", MODE_PRIVATE);
                SharedPreferences sharedPreferencesPhotosList = context.getSharedPreferences("photosList", MODE_PRIVATE);
                SharedPreferences sharedPreferencesSelectedPhotoPosition = context.getSharedPreferences("selectedPhotoPosition", MODE_PRIVATE);
                albumId = sharedPreferencesAlbumId.getString("albumId", "");
                selectedPhotoPosition = sharedPreferencesSelectedPhotoPosition.getInt("selectedPhotoPosition", 999999999);
                selectedAlbum = AppController.getInstance().getPrefManger().getCategories().get(sharedPreferencesAlbumSelected.getInt("albumSelected", 0)).getId();
                Gson gson = new Gson();
                String json = sharedPreferencesPhotosList.getString("photosList", "");
                Type type = new TypeToken<List<Wallpaper>>() {
                }.getType();
                photosList = gson.fromJson(json, type);

                if (albumId.equals(selectedAlbum)) {
                    if (photosList.size() > selectedPhotoPosition + 1) {
                        nextImage = true;
                        fetchFullResolutionImage(photosList.get(selectedPhotoPosition + 1),context);
                    } else {
                        nextImage = true;
                        fetchFullResolutionImage(photosList.get(0),context);
                    }
                } else {
                    nextImage = true;
                    fetchFullResolutionImage(photosList.get(selectedPhotoPosition+1),context);
                }
            }else if (intent.getAction().equals("Clicked Previous Wallpaper")){
                remoteViews.setViewVisibility(R.id.progressBar, View.VISIBLE);
                remoteViews.setViewVisibility(R.id.next,View.GONE);
                remoteViews.setViewVisibility(R.id.previous,View.GONE);
                widget = new ComponentName( context, WallpaperWidget.class );
                (AppWidgetManager.getInstance(context)).updateAppWidget( widget, remoteViews );
                SharedPreferences sharedPreferencesAlbumSelected = context.getSharedPreferences("albumSelected", MODE_PRIVATE);
                SharedPreferences sharedPreferencesAlbumId = context.getSharedPreferences("albumId", MODE_PRIVATE);
                SharedPreferences sharedPreferencesPhotosList = context.getSharedPreferences("photosList", MODE_PRIVATE);
                SharedPreferences sharedPreferencesSelectedPhotoPosition = context.getSharedPreferences("selectedPhotoPosition", MODE_PRIVATE);
                albumId = sharedPreferencesAlbumId.getString("albumId", "");
                selectedPhotoPosition = sharedPreferencesSelectedPhotoPosition.getInt("selectedPhotoPosition", 999999999);
                selectedAlbum = AppController.getInstance().getPrefManger().getCategories().get(sharedPreferencesAlbumSelected.getInt("albumSelected", 0)).getId();
                Gson gson = new Gson();
                String json = sharedPreferencesPhotosList.getString("photosList", "");
                Type type = new TypeToken<List<Wallpaper>>() {
                }.getType();
                photosList = gson.fromJson(json, type);

                if (albumId.equals(selectedAlbum)) {
                    if (photosList.size() > 0 && selectedPhotoPosition > 0) {
                        prevImage = true;
                        fetchFullResolutionImage(photosList.get(selectedPhotoPosition - 1),context);
                    } else if(photosList != null){
                        prevImage = true;
                        fetchFullResolutionImage(photosList.get(photosList.size()-1),context);
                    }
                } else {
                    prevImage = true;
                    fetchFullResolutionImage(photosList.get(selectedPhotoPosition),context);
                }
            }

        }
        else if ( conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.DISCONNECTED || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.DISCONNECTED) {
            Toast.makeText(context,"Please check you internet connection",Toast.LENGTH_SHORT).show();
            remoteViews.setViewVisibility(R.id.progressBar, View.GONE);
            remoteViews.setViewVisibility(R.id.next,View.VISIBLE);
            remoteViews.setViewVisibility(R.id.previous,View.VISIBLE);
            widget = new ComponentName( context, WallpaperWidget.class );
            (AppWidgetManager.getInstance(context)).updateAppWidget( widget, remoteViews );
        }
    }



    private void fetchFullResolutionImage(Wallpaper selectedPhoto, final Context context) {
        wallpaperLoaded = true;
        String url = selectedPhoto.getPhotoJson();
        final picassoTarget target = new picassoTarget(context);
        Toast.makeText(AppController.getAppContext(),"Processing request, Please wait..",Toast.LENGTH_LONG).show();

        // volley's json obj request
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    // Parsing the json response
                    JSONObject entry = response
                            .getJSONObject(TAG_ENTRY);

                    JSONArray mediaContentArray = entry.getJSONObject(
                            TAG_MEDIA_GROUP).getJSONArray(
                            TAG_MEDIA_CONTENT);

                    mediaObj = (JSONObject) mediaContentArray
                            .get(0);

                    final String fullResolutionUrl = mediaObj
                            .getString(TAG_IMG_URL);
                    SharedPreferences sharedPreferenceVideoAdSetFireBaseFireBase= AppController.getAppContext().getSharedPreferences("VideoAdSetFireBase",MODE_PRIVATE);
                    int VIDEO_AD_SET = sharedPreferenceVideoAdSetFireBaseFireBase.getInt("VideoAdSetFireBase",10);
                    SharedPreferences sharedPreferenceMaxLimitVideo = context.getSharedPreferences("maxLimitVideo", Context.MODE_PRIVATE);
                    if (sharedPreferenceMaxLimitVideo.getInt("maxLimitVideo",0) >= VIDEO_AD_SET && LPref.getIntPref(AppConst.PRODUCT_AD_ID, AppConst.PRODUCT_NOT_BOUGHT) == 2){
                        openDialog(context);
                        remoteViews.setViewVisibility(R.id.progressBar, View.GONE);
                        remoteViews.setViewVisibility(R.id.next,View.VISIBLE);
                        remoteViews.setViewVisibility(R.id.previous,View.VISIBLE);
                        widget = new ComponentName( context, WallpaperWidget.class );
                        (AppWidgetManager.getInstance(context)).updateAppWidget( widget, remoteViews );
                    }else {
                        Picasso.with(context).load(fullResolutionUrl).fetch(new Callback() {
                            @Override
                            public void onSuccess() {
                                Picasso.with(context).load(fullResolutionUrl).into(target);
                            }

                            @Override
                            public void onError() {
                                remoteViews.setViewVisibility(R.id.progressBar, View.GONE);
                                remoteViews.setViewVisibility(R.id.next,View.VISIBLE);
                                remoteViews.setViewVisibility(R.id.previous,View.VISIBLE);
                                widget = new ComponentName( context, WallpaperWidget.class );
                                (AppWidgetManager.getInstance(context)).updateAppWidget( widget, remoteViews );
                                Toast.makeText(context,"Please check you internet connection",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } catch (JSONException e) {
                    Toast.makeText(context,"Please check you internet connection",Toast.LENGTH_SHORT).show();
                    remoteViews.setViewVisibility(R.id.progressBar, View.GONE);
                    remoteViews.setViewVisibility(R.id.next,View.VISIBLE);
                    remoteViews.setViewVisibility(R.id.previous,View.VISIBLE);
                    widget = new ComponentName( context, WallpaperWidget.class );
                    (AppWidgetManager.getInstance(context)).updateAppWidget( widget, remoteViews );
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context,"Please check you internet connection",Toast.LENGTH_SHORT).show();
                remoteViews.setViewVisibility(R.id.progressBar, View.GONE);
                remoteViews.setViewVisibility(R.id.next,View.VISIBLE);
                remoteViews.setViewVisibility(R.id.previous,View.VISIBLE);
                widget = new ComponentName( context, WallpaperWidget.class );
                (AppWidgetManager.getInstance(context)).updateAppWidget( widget, remoteViews );
            }
        });

        // Remove the url from cache
        AppController.getInstance().getRequestQueue().getCache().remove(url);

        // Disable the cache for this url, so that it always fetches updated
        // json
        jsonObjReq.setShouldCache(false);

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }

    public void openDialog(Context context){
        Intent mainActivity = new Intent(context, MainActivity.class);
        mainActivity.putExtra("limitReached","widget");
        SharedPreferences sharedPreferenceLimitReached = AppController.getAppContext().getSharedPreferences("limitReached",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferenceLimitReached.edit();
        editor.putBoolean("limitReached", true);
        editor.apply();
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(mainActivity);
    }

    private class picassoTarget implements Target{
        Context context;
        public picassoTarget(Context context){
            this.context = context;
        }
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            WallpaperManager wm = WallpaperManager.getInstance(context);
            try {
                wm.setBitmap(bitmap);
                wallpaperLoaded = false;
                remoteViews.setViewVisibility(R.id.progressBar, View.GONE);
                remoteViews.setViewVisibility(R.id.next,View.VISIBLE);
                remoteViews.setViewVisibility(R.id.previous,View.VISIBLE);
                widget = new ComponentName( context, WallpaperWidget.class );
                (AppWidgetManager.getInstance(context)).updateAppWidget( widget, remoteViews );
                SharedPreferences sharedPreferenceMaxLimitVideo = context.getSharedPreferences("maxLimitVideo",Context.MODE_PRIVATE);
                SharedPreferences.Editor editorMaxLimit = sharedPreferenceMaxLimitVideo.edit();
                editorMaxLimit.putInt("maxLimitVideo",sharedPreferenceMaxLimitVideo.getInt("maxLimitVideo",0)+1);
                editorMaxLimit.apply();
                if (nextImage){
                    SharedPreferences sharedPreferencesSelectedPhotoPosition = context.getSharedPreferences("selectedPhotoPosition", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferencesSelectedPhotoPosition.edit();

                    if(photosList.size() != selectedPhotoPosition+1){
                        editor.putInt("selectedPhotoPosition",selectedPhotoPosition+1);
                        editor.apply();
                    }else {
                        editor.putInt("selectedPhotoPosition", 0);
                        editor.apply();
                    }
                    nextImage = false;
                }else if (prevImage){
                    SharedPreferences sharedPreferencesSelectedPhotoPosition = context.getSharedPreferences("selectedPhotoPosition", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferencesSelectedPhotoPosition.edit();
                    if(selectedPhotoPosition != 0){
                        editor.putInt("selectedPhotoPosition",selectedPhotoPosition-1);
                        editor.apply();
                    }else {
                        editor.putInt("selectedPhotoPosition", photosList.size()-1);
                        editor.apply();
                    }
                }
                SharedPreferences sharedPreferencesAlbumId = context.getSharedPreferences("albumId", MODE_PRIVATE);
                SharedPreferences.Editor editorAlbumId = sharedPreferencesAlbumId.edit();
                editorAlbumId.putString("albumId", selectedAlbum);
                editorAlbumId.apply();

            } catch (IOException e) {
                remoteViews.setViewVisibility(R.id.progressBar, View.GONE);
                remoteViews.setViewVisibility(R.id.next,View.VISIBLE);
                remoteViews.setViewVisibility(R.id.previous,View.VISIBLE);
                widget = new ComponentName( context, WallpaperWidget.class );
                (AppWidgetManager.getInstance(context)).updateAppWidget( widget, remoteViews );
                e.printStackTrace();
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    }

    public void addWidget(Context context){
        AppWidgetManager mAppWidgetManager = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAppWidgetManager = context.getSystemService(AppWidgetManager.class);
        }
        ComponentName myProvider = new ComponentName(context, WallpaperWidget.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            assert mAppWidgetManager != null;
            if (mAppWidgetManager.isRequestPinAppWidgetSupported()) {
                mAppWidgetManager.requestPinAppWidget(myProvider, null, null);
            }
        }
    }
}

