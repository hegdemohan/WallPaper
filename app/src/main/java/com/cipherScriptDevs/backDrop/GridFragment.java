package com.cipherScriptDevs.backDrop;

/**
 * Created by mhegde on 04/20/2017.
 */


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import adapter.GridViewAdapter;
import picasa.Wallpaper;
import util.LPref;
import util.Utils;

import static android.content.Context.MODE_PRIVATE;

public class GridFragment extends Fragment {
    private static final String TAG = GridFragment.class.getSimpleName();
    private Utils utils;
    private GridViewAdapter adapter;
    public GridView gridView;
    private int columnWidth;
    private static final String bundleAlbumId = "albumId";
    private String selectedAlbumId;
    private List<Wallpaper> photosList;
    public RelativeLayout refreshPage;
    public ProgressBar pbLoader;
    private InterstitialAd interstitialAd;
    private boolean isCalledInterstitial = false;
    private PrefManager pref;
    private JSONObject cacheImages = new JSONObject();

    // Picasa JSON response node keys
    private static final String TAG_FEED = "feed", TAG_ENTRY = "entry",
            TAG_MEDIA_GROUP = "media$group",
            TAG_MEDIA_CONTENT = "media$content", TAG_IMG_URL = "url",
            TAG_IMG_WIDTH = "width", TAG_IMG_HEIGHT = "height", TAG_ID = "id",
            TAG_T = "$t";

    String albumId;
    private String selectedAlbum;
    private TextView downloadWallpaperText;

    public static GridFragment newInstance(Bundle bundle) {
        GridFragment gridFragment = new GridFragment();
        gridFragment.setArguments(bundle);
        return gridFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_grid, container,
                false);


        return rootView;
    }


    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        albumId = getArguments().getString("albumId", "");
        selectedAlbum = albumId;
        LoadInterstitialAd();
        utils = new Utils(getContext());

        AdView mAdView = view.findViewById(R.id.adViewGrid);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("B43FCE5959423443AF7C9BFA9DAF18C3").build();
        if(LPref.getIntPref(AppConst.PRODUCT_AD_ID, AppConst.PRODUCT_NOT_BOUGHT) == 2){
            mAdView.loadAd(adRequest);
        }

        photosList = new ArrayList<>();
        pref = new PrefManager(getActivity());

        // if Album Id is null, user is selected recently added option
        if (getArguments().getString(bundleAlbumId) != null) {
            selectedAlbumId = getArguments().getString(bundleAlbumId);
        } else {
            selectedAlbumId = null;
        }

        // Preparing the request url
        String url = null;
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
        // Hiding the gridView and showing loader image before making the http
        // request
        gridView = view.findViewById(R.id.grid_view);
        pbLoader = (ProgressBar) view.findViewById(R.id.pbLoader);
        downloadWallpaperText = (TextView) view.findViewById(R.id.downloadWallpaperText);
        refreshPage = (RelativeLayout) view.findViewById(R.id.refreshPage);
        downloadWallpaperText.setVisibility(View.GONE);

            gridView.setVisibility(View.GONE);
            pbLoader.setVisibility(View.VISIBLE);

            /**
             * Making volley's json object request to fetch list of photos of an
             * album
             * */

        if(cacheImages.has(albumId)){
            try {
                setGridAdapter((List<Wallpaper>) cacheImages.get(albumId));
            } catch (JSONException e) {
                jsonRequest(url);
            }
        }else {
            jsonRequest(url);
        }

    }

    private void jsonRequest(final String url) {
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.GET, url,
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

                    // Notify list adapter about dataSet changes. So
                    // that it renders grid again
                    setGridAdapter(photosList);
                    adapter.notifyDataSetChanged();
                    if(!cacheImages.has(albumId)){
                        if (photosList.size()>0){
                            cacheImages.put(albumId,photosList);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(),
                            getString(R.string.msg_unknown_error),
                            Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                refreshPage.setVisibility(View.VISIBLE);
                jsonRequest(url);
            }
        });


        // Remove the url from cache
        AppController.getInstance().getRequestQueue().getCache().remove(url);

        // Disable the cache for this url, so that it always fetches updated
        // json
        jsonObjReq.setShouldCache(false);

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);

        // Initializing Grid View
        InitializeGridLayout();
    }

    public void setGridAdapter(final java.util.List<picasa.Wallpaper> photosList){
        // Gridview adapter
        // Hide the loader, make grid visible
        int count = LPref.getIntPref("interstitialAdGridTracker",0);
        if(LPref.getIntPref(AppConst.PRODUCT_AD_ID, AppConst.PRODUCT_NOT_BOUGHT) == 2){
            if (interstitialAd.isLoaded() && count >= LPref.getIntPref("InterstitialGridFragmentSwipe",10)){
                interstitialAd.show();
                isCalledInterstitial = false;
                LoadInterstitialAd();
                LPref.putIntPref("interstitialAdGridTracker",0);
            }else {
                LPref.putIntPref("interstitialAdGridTracker",count+1);
            }
        }
        pbLoader.setVisibility(View.GONE);
        refreshPage.setVisibility(View.GONE);
        gridView.setVisibility(View.VISIBLE);
        adapter = new GridViewAdapter(getActivity(), photosList, columnWidth,albumId);

        // setting grid view adapter
        gridView.setAdapter(adapter);
        // Grid item select listener
        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                // On selecting the grid image, we launch fullscreen activity
                Intent i = new Intent(getActivity(),
                        ViewImageActivity.class);

                // Passing selected image to fullscreen activity
                Wallpaper photo = photosList.get(position);
                SharedPreferences sharedPreferencesPhotosListForSlider = getActivity().getSharedPreferences("photosListSlider", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferencesPhotosListForSlider.edit();
                Gson gson = new Gson();
                String json = gson.toJson(photosList);
                editor.putString("photosListSlider", json);
                editor.apply();
                i.putExtra(ViewImageActivity.TAG_SEL_IMAGE, photo);
                i.putExtra(ViewImageActivity.TAG_SEL_AlBUM, selectedAlbum);
                i.putExtra("selectedPhotoPosition", position);
                SharedPreferences sharedPreferenceInterstitialAd = getContext().getSharedPreferences("interstitialAd",Context.MODE_PRIVATE);
                SharedPreferences.Editor editorInterstitialAd = sharedPreferenceInterstitialAd.edit();
                editorInterstitialAd.putInt("interstitialAd", sharedPreferenceInterstitialAd.getInt("interstitialAd",0)+1);
                editorInterstitialAd.apply();
                SharedPreferences sharedPreferenceInterstitialAdViewImageFireBase = getContext().getSharedPreferences("InterstitialAdViewImage",MODE_PRIVATE);
                int INTERSTITIAL_AD_VIEW_IMAGE = sharedPreferenceInterstitialAdViewImageFireBase.getInt("InterstitialAdViewImage",10);
                if (sharedPreferenceInterstitialAd.getInt("interstitialAd",0)>=INTERSTITIAL_AD_VIEW_IMAGE){
                    if (interstitialAd.isLoaded() && LPref.getIntPref(AppConst.PRODUCT_AD_ID, AppConst.PRODUCT_NOT_BOUGHT) == 2){
                        interstitialAd.show();
                        isCalledInterstitial = false;
                        LoadInterstitialAd();
                        editorInterstitialAd.putInt("interstitialAd",0);
                        editorInterstitialAd.apply();
                    }
                }
                startActivity(i);
            }
        });
    }

    public void LoadInterstitialAd(){
        if (!isCalledInterstitial){
            isCalledInterstitial = true;
            LPref.putBooleanPref("loadAdCalledGrid",true);
            interstitialAd = new InterstitialAd(getContext());
            interstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_unit_id));
            AdRequest request = new AdRequest.Builder().addTestDevice("B43FCE5959423443AF7C9BFA9DAF18C3").build();
            interstitialAd.loadAd(request);
        }
    }

    /**
     * Method to calculate the grid dimensions Calculates number columns and
     * columns width in grid
     */
    private void InitializeGridLayout() {
        Resources r = getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                AppConst.GRID_PADDING, r.getDisplayMetrics());

        // Column width
        columnWidth = (int) ((utils.getScreenWidth() - ((pref
                .getNoOfGridColumns()) * padding)) / pref
                .getNoOfGridColumns());

        // Setting number of grid columns
        gridView.setNumColumns(pref.getNoOfGridColumns());
        gridView.setColumnWidth(columnWidth);
        gridView.setStretchMode(GridView.NO_STRETCH);
        gridView.setPadding((int) padding, (int) padding, (int) padding,
                (int) padding);

        // Setting horizontal and vertical padding
        gridView.setHorizontalSpacing((int) padding);
        gridView.setVerticalSpacing((int) padding);
    }
}