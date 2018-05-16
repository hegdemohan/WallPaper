package com.cipherScriptDevs.backDrop;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import picasa.Category;

public class SplashScreenActivity extends Activity {
    private static final String TAG = SplashScreenActivity.class.getSimpleName();
    private static final String TAG_FEED = "feed", TAG_ENTRY = "entry",
            TAG_GPHOTO_ID = "gphoto$id", TAG_T = "$t",
            TAG_ALBUM_TITLE = "title";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        final ProgressBar retryPb = findViewById(R.id.retry_pb);
        final Button retry = findViewById(R.id.retry_button);
        final TextView retryText = findViewById(R.id.no_internet_warning);

        // Picasa request to get list of albums
        String url = AppConst.URL_PICASA_ALBUMS
                .replace("_PICASA_USER_", AppController.getInstance()
                        .getPrefManger().getGoogleUserName());

        // Preparing volley's json object request
        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.GET, url,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                List<Category> albums = new ArrayList<>();
                try {

                    retryText.setVisibility(View.GONE);
                    retry.setVisibility(View.GONE);
                    retryPb.setVisibility(View.GONE);
                    // Parsing the json response
                    JSONArray entry = response.getJSONObject(TAG_FEED)
                            .getJSONArray(TAG_ENTRY);

                    // loop through albums nodes and add them to album
                    // list
                    for (int i = 0; i < entry.length(); i++) {
                        JSONObject albumObj = (JSONObject) entry.get(i);
                        // album id
                        String albumId = albumObj.getJSONObject(
                                TAG_GPHOTO_ID).getString(TAG_T);

                        // album title
                        String albumTitle = albumObj.getJSONObject(
                                TAG_ALBUM_TITLE).getString(TAG_T);

                        Category album = new Category();
                        album.setId(albumId);
                        album.setTitle(albumTitle);

                        // add album to list
                        albums.add(album);
                    }

                    // Store albums in shared pref
                    AppController.getInstance().getPrefManger()
                                .storeCategories(albums);

                    // String the main activity
                    Intent intent = new Intent(getApplicationContext(),
                            MainActivity.class);
                    startActivity(intent);
                    // closing splash activity
                    finish();

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
                Log.e(TAG, "Volley Error: " + error.getMessage());

                // Unable to fetch albums
                // check for existing Albums data in Shared Preferences
                if (AppController.getInstance().getPrefManger()
                        .getCategories() != null && AppController.getInstance().getPrefManger()
                        .getCategories().size() > 0) {
                    // String the main activity
                    Intent intent = new Intent(getApplicationContext(),
                            MainActivity.class);
                    startActivity(intent);
                    // closing spalsh activity
                    finish();
                } else {
                    retryText.setVisibility(View.VISIBLE);
                    retryPb.setVisibility(View.GONE);
                    retry.setVisibility(View.VISIBLE);
//                    Intent i = new Intent(SplashScreenActivity.this,
//                            SettingsActivity.class);
//                    // clear all the activities
//                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(i);
                }
            }
        });

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retry.setVisibility(View.GONE);
                retryPb.setVisibility(View.VISIBLE);
                AppController.getInstance().addToRequestQueue(jsonObjReq);
            }
        });



        // disable the cache for this request, so that it always fetches updated
        // json
        jsonObjReq.setShouldCache(false);

        // Making the request
        AppController.getInstance().addToRequestQueue(jsonObjReq);

    }

}