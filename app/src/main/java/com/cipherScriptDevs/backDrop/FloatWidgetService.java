package com.cipherScriptDevs.backDrop;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import picasa.Wallpaper;

/**
 * Created by mhegde on 05/17/2017.
 */

public class FloatWidgetService extends Service  {
    private WindowManager mWindowManager;
    private View mFloatingWidget;
    private String albumId;
    private static final String TAG_ENTRY = "entry",
            TAG_MEDIA_GROUP = "media$group",
            TAG_MEDIA_CONTENT = "media$content", TAG_IMG_URL = "url";
    private String selectedAlbum;
    private JSONObject mediaObj;
    public int selectedPhotoPosition;
    public Boolean clickedNext = false;
    public Boolean clickedPrev = false;
    private boolean prevImage = false;
    private boolean nextImage = false;
    private boolean wallPaperChanged = false;
    private ProgressBar spinner;
    private List<Wallpaper> photosList = new ArrayList<>();
    private LinearLayout afterLoader;

    public FloatWidgetService() {
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mFloatingWidget = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingWidget, params);
        final View collapsedView = mFloatingWidget.findViewById(R.id.collapse_view);
        final View expandedView = mFloatingWidget.findViewById(R.id.expanded_container);
        spinner = (ProgressBar)mFloatingWidget.findViewById(R.id.progressBar);

        afterLoader = (LinearLayout) mFloatingWidget.findViewById(R.id.afterLoader);
        ImageView closeButtonCollapsed = (ImageView) mFloatingWidget.findViewById(R.id.close_btn);
        closeButtonCollapsed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSelf();
            }
        });
        ImageView expanded_image = (ImageView) mFloatingWidget.findViewById(R.id.expanded_image);
        expanded_image.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                collapsedView.setVisibility(View.VISIBLE);
                expandedView.setVisibility(View.GONE);
            }
        });
        ImageView closeButton = (ImageView) mFloatingWidget.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collapsedView.setVisibility(View.VISIBLE);
                expandedView.setVisibility(View.GONE);
            }
        });

        final Target target = new Target() {


            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                WallpaperManager wm = WallpaperManager.getInstance(getApplicationContext());
                try {
                    wm.setBitmap(bitmap);
                    spinner.setVisibility(View.GONE);
                    afterLoader.setVisibility(View.VISIBLE);
                    wallPaperChanged = true;
                    if (nextImage){
                        SharedPreferences sharedPreferencesSelectedPhotoPosition = getApplicationContext().getSharedPreferences("selectedPhotoPosition",MODE_PRIVATE);
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
                        SharedPreferences sharedPreferencesSelectedPhotoPosition = getApplicationContext().getSharedPreferences("selectedPhotoPosition",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferencesSelectedPhotoPosition.edit();
                        if(selectedPhotoPosition != 0){
                            editor.putInt("selectedPhotoPosition",selectedPhotoPosition-1);
                            editor.apply();
                        }else {
                            editor.putInt("selectedPhotoPosition", photosList.size()-1);
                            editor.apply();
                        }
                    }
                    SharedPreferences sharedPreferencesAlbumId = getSharedPreferences("albumId",MODE_PRIVATE);
                    SharedPreferences.Editor editorAlbumId = sharedPreferencesAlbumId.edit();
                    editorAlbumId.putString("albumId", selectedAlbum);
                    editorAlbumId.apply();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        final SharedPreferences sharedPreferences = getSharedPreferences("albumSelected",MODE_PRIVATE);
        final int album = sharedPreferences.getInt("albumSelected",0);
        final ImageView next = (ImageView) mFloatingWidget.findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(!clickedNext) {
                    spinner.setVisibility(View.VISIBLE);
                    afterLoader.setVisibility(View.GONE);
                    clickedNext = true;
                    wallPaperChanged = false;
                    SharedPreferences sharedPreferencesAlbumSelected = getSharedPreferences("albumSelected", MODE_PRIVATE);
                    SharedPreferences sharedPreferencesAlbumId = getSharedPreferences("albumId", MODE_PRIVATE);
                    SharedPreferences sharedPreferencesPhotosList = getSharedPreferences("photosList", MODE_PRIVATE);
                    SharedPreferences sharedPreferencesSelectedPhotoPosition = getSharedPreferences("selectedPhotoPosition", MODE_PRIVATE);
                    albumId = sharedPreferencesAlbumId.getString("albumId", "");
                    selectedPhotoPosition = sharedPreferencesSelectedPhotoPosition.getInt("selectedPhotoPosition", 999999999);
                    selectedAlbum = AppController.getInstance().getPrefManger().getCategories().get(sharedPreferencesAlbumSelected.getInt("albumSelected", 0)).getId();
                    Gson gson = new Gson();
                    String json = sharedPreferencesPhotosList.getString("photosList", "");
                    Type type = new TypeToken<List<Wallpaper>>() {
                    }.getType();
                    photosList = gson.fromJson(json, type);

                    if (Objects.equals(albumId, selectedAlbum)) {
                        if (photosList.size() > selectedPhotoPosition + 1) {
                            nextImage = true;
                            fetchFullResolutionImage(photosList.get(selectedPhotoPosition + 1),target);
                            clickedNext = false;
                        } else {
                            nextImage = true;
                            fetchFullResolutionImage(photosList.get(0),target);
//                            SharedPreferences.Editor editor = sharedPreferencesSelectedPhotoPosition.edit();
//                            editor.putInt("selectedPhotoPosition", 0);
//                            editor.apply();
                            clickedNext = false;
                        }
                    } else {
                        nextImage = true;
                        fetchFullResolutionImage(photosList.get(0),target);
                        clickedNext = false;
                    }
                }
            }
        });

        ImageView previous = (ImageView) mFloatingWidget.findViewById(R.id.previous);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!clickedPrev) {
                    spinner.setVisibility(View.VISIBLE);
                    afterLoader.setVisibility(View.GONE);
                    clickedPrev = true;
                    wallPaperChanged = false;
                    SharedPreferences sharedPreferencesAlbumSelected = getSharedPreferences("albumSelected", MODE_PRIVATE);
                    SharedPreferences sharedPreferencesAlbumId = getSharedPreferences("albumId", MODE_PRIVATE);
                    SharedPreferences sharedPreferencesPhotosList = getSharedPreferences("photosList", MODE_PRIVATE);
                    SharedPreferences sharedPreferencesSelectedPhotoPosition = getSharedPreferences("selectedPhotoPosition", MODE_PRIVATE);
                    albumId = sharedPreferencesAlbumId.getString("albumId", "");
                    selectedPhotoPosition = sharedPreferencesSelectedPhotoPosition.getInt("selectedPhotoPosition", 999999999);
                    selectedAlbum = AppController.getInstance().getPrefManger().getCategories().get(sharedPreferencesAlbumSelected.getInt("albumSelected", 0)).getId();
                    Gson gson = new Gson();
                    String json = sharedPreferencesPhotosList.getString("photosList", "");
                    Type type = new TypeToken<List<Wallpaper>>() {
                    }.getType();
                    photosList = gson.fromJson(json, type);

                    if (Objects.equals(albumId, selectedAlbum)) {
                        if (photosList.size() > 0 && selectedPhotoPosition > 0) {
                            prevImage = true;
                            fetchFullResolutionImage(photosList.get(selectedPhotoPosition - 1),target);
                            clickedPrev = false;
                        } else {
                            prevImage = true;
                            fetchFullResolutionImage(photosList.get(photosList.size()-1),target);
//                            SharedPreferences.Editor editor = sharedPreferencesSelectedPhotoPosition.edit();
//                            editor.putInt("selectedPhotoPosition", 0);
//                            editor.apply();
                            clickedPrev = false;
                        }
                    } else {
                        prevImage = true;
                        fetchFullResolutionImage(photosList.get(0),target);
                        clickedPrev = false;
                    }
                }
            }


        });

        mFloatingWidget.findViewById(R.id.root_container).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        int Xdiff = (int) (event.getRawX() - initialTouchX);
                        int Ydiff = (int) (event.getRawY() - initialTouchY);
                        if (Xdiff < 10 && Ydiff < 10) {
                            if (isViewCollapsed()) {
                                collapsedView.setVisibility(View.GONE);
                                expandedView.setVisibility(View.VISIBLE);
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(mFloatingWidget, params);
                        return true;
                }
                return false;
            }
        });
    }

    private void fetchFullResolutionImage(Wallpaper selectedPhoto, final Target target) {
        String url = selectedPhoto.getPhotoJson();

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
                    Picasso.with(getApplicationContext())
                            .load(fullResolutionUrl)
                            .into(target);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.msg_unknown_error),
                            Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.msg_wall_fetch_error),
                        Toast.LENGTH_LONG).show();
            }
        });

        // Remove the url from cache
        AppController.getInstance().getRequestQueue().getCache().remove(url);

        // Disable the cache for this url, so that it always fetches updated
        // json
        jsonObjReq.setShouldCache(false);

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);

//        if(!wallPaperChanged){
//            fetchFullResolutionImage(selectedPhoto);
//        }
    }

    private boolean isViewCollapsed() {
        return mFloatingWidget == null || mFloatingWidget.findViewById(R.id.collapse_view).getVisibility() == View.VISIBLE;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingWidget != null) mWindowManager.removeView(mFloatingWidget);
    }
}
