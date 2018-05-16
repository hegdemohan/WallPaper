package util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.cipherScriptDevs.backDrop.AppConst;
import com.cipherScriptDevs.backDrop.AppController;
import com.cipherScriptDevs.backDrop.PrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import picasa.Wallpaper;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by mhegde on 05/09/2017.
 */

public class SharedPreferencesHelper {
    Activity _activity;
    private static final String TAG_FEED = "feed", TAG_ENTRY = "entry",
            TAG_MEDIA_GROUP = "media$group",
            TAG_MEDIA_CONTENT = "media$content", TAG_IMG_URL = "url",
            TAG_IMG_WIDTH = "width", TAG_IMG_HEIGHT = "height", TAG_ID = "id",
            TAG_T = "$t";
    private List<Wallpaper> photosList = new ArrayList<>();
    private FirebaseRemoteConfig mFireBaseRemoteConfig;


    public SharedPreferencesHelper(Activity activity){
        _activity = activity;
    }

    public void fetchRemoteConfigParams(){
        mFireBaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        long cacheExpiration = 0;
        mFireBaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(_activity, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mFireBaseRemoteConfig.activateFetched();
                            SharedPreferences sharedPreferenceVideoAdDownloadFireBase = _activity.getSharedPreferences("videoAdDownloadFireBase",MODE_PRIVATE);
                            SharedPreferences.Editor editorDownload = sharedPreferenceVideoAdDownloadFireBase.edit();
                            editorDownload.putInt("videoAdDownloadFireBase",Integer.parseInt(mFireBaseRemoteConfig.getString("VIDEO_AD_DOWNLOAD")));
                            editorDownload.apply();

                            SharedPreferences sharedPreferenceVideoAdSetFireBaseFireBase= _activity.getSharedPreferences("VideoAdSetFireBase",MODE_PRIVATE);
                            SharedPreferences.Editor editorSet = sharedPreferenceVideoAdSetFireBaseFireBase.edit();
                            editorSet.putInt("VideoAdSetFireBase", Integer.parseInt(mFireBaseRemoteConfig.getString("VIDEO_AD_SET")));
                            editorSet.apply();

                            SharedPreferences sharedPreferenceVideoAdShareFireBaseFireBase = _activity.getSharedPreferences("VideoAdShareFireBase",MODE_PRIVATE);
                            SharedPreferences.Editor editorShare = sharedPreferenceVideoAdShareFireBaseFireBase.edit();
                            editorShare.putInt("VideoAdShareFireBase", Integer.parseInt(mFireBaseRemoteConfig.getString("VIDEO_AD_SHARE")));
                            editorShare.apply();

                            SharedPreferences sharedPreferenceInterstitialAdViewImageFireBase = _activity.getSharedPreferences("InterstitialAdViewImage",MODE_PRIVATE);
                            SharedPreferences.Editor editorInterstitial = sharedPreferenceInterstitialAdViewImageFireBase.edit();
                            editorInterstitial.putInt("InterstitialAdViewImage", Integer.parseInt(mFireBaseRemoteConfig.getString("INTERSTITIAL_AD_VIEW_IMAGE")));
                            editorInterstitial.apply();

                            LPref.putIntPref("newUpdate",Integer.parseInt(mFireBaseRemoteConfig.getString("NEW_UPDATE")));

                            LPref.putIntPref("InterstitialViewFullScreen",Integer.parseInt(mFireBaseRemoteConfig.getString("INTERSTITIAL_VIEW_FULLSCREEN")));

                            LPref.putIntPref("InterstitialGridFragmentSwipe",Integer.parseInt(mFireBaseRemoteConfig.getString("INTERSTITIAL_GRID_FRAGMENT_SWIPE")));

                            LPref.putIntPref("InterstitialFullScreenSwipe",Integer.parseInt(mFireBaseRemoteConfig.getString("INTERSTITIAL_FULLSCREEN_SWIPE")));
                        }else {
                            fetchRemoteConfigParams();
                        }
                    }
                });
    }

    public void getPhotosList(String selectedAlbumId){
        String url = null;
        photosList.clear();
        PrefManager pref = new PrefManager(_activity);
        if (selectedAlbumId == null) {
            // Recently added album url
            url = AppConst.URL_RECENTLY_ADDED.replace("_PICASA_USER_",
                    pref.getGoogleUserName());
        } else {
            // Selected an album, replace the Album Id in the url
            url = AppConst.URL_ALBUM_PHOTOS.replace("_PICASA_USER_",
                    pref.getGoogleUserName()).replace("_ALBUM_ID_",
                    selectedAlbumId);
        }

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    // Parsing the json response
                    JSONArray entry = response.getJSONObject(TAG_FEED)
                            .getJSONArray(TAG_ENTRY);

                    // looping through each photo and adding it to list
                    // data set
                    for (int i = 0; i < entry.length(); i++) {
                        JSONObject photoObj = (JSONObject) entry.get(i);
                        JSONArray mediaContentArray = photoObj
                                .getJSONObject(TAG_MEDIA_GROUP)
                                .getJSONArray(TAG_MEDIA_CONTENT);

                        if (mediaContentArray.length() > 0) {
                            JSONObject mediaObj = (JSONObject) mediaContentArray
                                    .get(0);

                            String url = mediaObj
                                    .getString(TAG_IMG_URL);

                            String photoJson = photoObj.getJSONObject(
                                    TAG_ID).getString(TAG_T)
                                    + "&imgmax=d";

                            int width = mediaObj.getInt(TAG_IMG_WIDTH);
                            int height = mediaObj
                                    .getInt(TAG_IMG_HEIGHT);

                            Wallpaper p = new Wallpaper(photoJson, url, width,
                                    height);

                            // Adding the photo to list data set
                            photosList.add(p);
                        }
                    }
                    SharedPreferences sharedPreferencesPhotosList = _activity.getSharedPreferences("photosList", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferencesPhotosList.edit();
                    Gson gson = new Gson();
                    String json = gson.toJson(photosList);
                    editor.putString("photosList",json);
                    editor.apply();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
               ///////////
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }

    public void setAlbumId(String albumId){
        SharedPreferences sharedPreferencesAlbumId = _activity.getSharedPreferences("albumId",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferencesAlbumId.edit();
        editor.putString("albumId", albumId);
        editor.apply();
    }
    public void setSharedPreferences(){

        SharedPreferences sharedPreferencesAlbumSelected = _activity.getSharedPreferences("albumSelected",MODE_PRIVATE);

        if(sharedPreferencesAlbumSelected.getInt("albumSelected", 0) == 0){
            SharedPreferences.Editor editor = sharedPreferencesAlbumSelected.edit();
            editor.putInt("albumSelected",0);
            editor.apply();
        }

        SharedPreferences sharedPreferencesPhotosList = _activity.getSharedPreferences("photosList",MODE_PRIVATE);
        if(sharedPreferencesPhotosList.getString("photosList", "").equals("")){
            getPhotosList(AppController.getInstance().getPrefManger().getCategories().get(sharedPreferencesAlbumSelected.getInt("albumSelected", 0)).getId());
        }else{
            getPhotosList(AppController.getInstance().getPrefManger().getCategories().get(sharedPreferencesAlbumSelected.getInt("albumSelected", 0)).getId());
        }


        SharedPreferences sharedPreferencesAlbumId = _activity.getSharedPreferences("albumId",MODE_PRIVATE);
        if(sharedPreferencesAlbumId.getString("albumId", "").equals("")){
            SharedPreferences.Editor editor = sharedPreferencesAlbumId.edit();
            editor.putString("albumId", AppController.getInstance().getPrefManger().getCategories().get(0).getId());
            editor.apply();
        }

        SharedPreferences sharedPreferencesSelectedPhotoPosition = _activity.getSharedPreferences("selectedPhotoPosition",MODE_PRIVATE);
        if(sharedPreferencesSelectedPhotoPosition.getInt("selectedPhotoPosition",999999999) == 999999999) {
            SharedPreferences.Editor editor = sharedPreferencesSelectedPhotoPosition.edit();
            editor.putInt("selectedPhotoPosition", -1);
            editor.apply();
        }

        SharedPreferences sharedPreferenceMaxLimitVideo = _activity.getSharedPreferences("maxLimitVideo",MODE_PRIVATE);
        if(sharedPreferenceMaxLimitVideo.getInt("maxLimitVideo", 0) > 99999 ) {
            SharedPreferences.Editor editor = sharedPreferenceMaxLimitVideo.edit();
            editor.putInt("maxLimitVideo", 0);
            editor.apply();
        }
        SharedPreferences sharedPreferenceMaxLimitVideoDownload = _activity.getSharedPreferences("maxLimitVideoDownload",MODE_PRIVATE);
        if(sharedPreferenceMaxLimitVideoDownload.getInt("maxLimitVideoDownload", 0) > 99999 ) {
            SharedPreferences.Editor editor = sharedPreferenceMaxLimitVideoDownload.edit();
            editor.putInt("maxLimitVideoDownload", 0);
            editor.apply();
        }

        SharedPreferences sharedPreferenceMaxLimitVideoShare = _activity.getSharedPreferences("maxLimitVideoShare",MODE_PRIVATE);
        if(sharedPreferenceMaxLimitVideoShare.getInt("maxLimitVideoShare", 0) > 99999 ) {
            SharedPreferences.Editor editor = sharedPreferenceMaxLimitVideoShare.edit();
            editor.putInt("maxLimitVideoShare", 0);
            editor.apply();
        }

        SharedPreferences sharedPreferenceInterstitialAd = _activity.getSharedPreferences("interstitialAd",MODE_PRIVATE);
        if(sharedPreferenceInterstitialAd.getInt("interstitialAd", 0) > 99999 ) {
            SharedPreferences.Editor editor = sharedPreferenceInterstitialAd.edit();
            editor.putInt("interstitialAd", 0);
            editor.apply();
        }

        SharedPreferences sharedPreferenceInterstitialAdOnFling = _activity.getSharedPreferences("interstitialAdOnFling",MODE_PRIVATE);
        if(sharedPreferenceInterstitialAdOnFling.getInt("interstitialAdOnFling", 0) > 99999 ) {
            SharedPreferences.Editor editor = sharedPreferenceInterstitialAdOnFling.edit();
            editor.putInt("interstitialAdOnFling", 0);
            editor.apply();
        }
    }
}

