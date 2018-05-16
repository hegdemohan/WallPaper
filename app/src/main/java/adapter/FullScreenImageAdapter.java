package adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cipherScriptDevs.backDrop.AppConst;
import com.cipherScriptDevs.backDrop.AppController;
import com.cipherScriptDevs.backDrop.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;
import java.util.List;

import picasa.Wallpaper;
import util.LPref;
import util.Utils;

/**
 * Created by mhegde on 03/30/2018.
 */

public class FullScreenImageAdapter extends PagerAdapter{

    private Activity _activity;
    private List<Wallpaper> imagePaths;
    private Boolean isCalled = false;
    private static final String TAG_ENTRY = "entry",
            TAG_MEDIA_GROUP = "media$group",
            TAG_MEDIA_CONTENT = "media$content", TAG_IMG_URL = "url";
    private JSONObject mediaObj;
    private String selectedAlbumId;
    private Hashtable<Integer,Bitmap> bitmaps = new Hashtable<>();
    private boolean isLoadedInterstitial = false;
    private InterstitialAd interstitialAd;

    public FullScreenImageAdapter(Activity activity, List<Wallpaper> imagePaths,String selectedAlbumId) {
        this._activity= activity;
        this.imagePaths = imagePaths;
        this.selectedAlbumId = selectedAlbumId;
    }

    @Override
    public int getCount() {
        return imagePaths.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == (object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        LayoutInflater inflater = (LayoutInflater) _activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;

        final View viewLayout = inflater.inflate(R.layout.image_fullscreen_view_pager_layout, container, false);

        final ImageView imgDisplay = viewLayout.findViewById(R.id.viewFullImage);

        final ProgressBar pbLoader = viewLayout.findViewById(R.id.progressBarViewFullImage);

        pbLoader.setVisibility(View.VISIBLE);

        String url = imagePaths.get(position).getPhotoJson();

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

                    mediaObj = (JSONObject) mediaContentArray.get(0);
                    LPref.putIntPref("fullScreenSwipeCount",LPref.getIntPref("fullScreenSwipeCount",0)+1);
                    final String fullResolutionUrl = mediaObj.getString(TAG_IMG_URL);
                    int INTERSTITIAL_AD_FULL_SCREEN = LPref.getIntPref("InterstitialFullScreenSwipe",10);
                    if (LPref.getIntPref("fullScreenSwipeCount",0)>=INTERSTITIAL_AD_FULL_SCREEN){
                        if (interstitialAd.isLoaded() && LPref.getIntPref(AppConst.PRODUCT_AD_ID, AppConst.PRODUCT_NOT_BOUGHT) == 2){
                            interstitialAd.show();
                            isLoadedInterstitial = false;
                            LoadInterstitialAd();
                            LPref.putIntPref("fullScreenSwipeCount",0);
                        }
                    }
                    Picasso.with(AppController.getAppContext()).load(fullResolutionUrl).fetch(new Callback() {
                        @Override
                        public void onSuccess() {
                            Picasso.with(AppController.getAppContext()).load(fullResolutionUrl).into(new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap fullBitmap, Picasso.LoadedFrom from) {
                                    imgDisplay.setImageBitmap(fullBitmap);
                                    bitmaps.put(position,fullBitmap);
                                    pbLoader.setVisibility(View.GONE);
                                }

                                @Override
                                public void onBitmapFailed(Drawable errorDrawable) {

                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {

                                }
                            });
                        }

                        @Override
                        public void onError() {

                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(AppController.getAppContext(), AppController.getAppContext().getString(R.string.msg_unknown_error),Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AppController.getAppContext(),
                        AppController.getAppContext().getString(R.string.msg_wall_fetch_error),
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

        (container).addView(viewLayout);

        return viewLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        (container).removeView((RelativeLayout) object);
    }

    public void setWallpaper(int pos){
        Utils utils = new Utils(_activity);
        if(bitmaps.containsKey(pos) && !isCalled){
            isCalled = true;
            Toast.makeText(_activity,"Processing request, please wait",Toast.LENGTH_LONG).show();
            if (utils.setAsWallpaper(bitmaps.get(pos),selectedAlbumId,pos,_activity)){
                isCalled = false;
            }
        }else {
            Toast.makeText(_activity,"Please wait till the wallpaper is loaded",Toast.LENGTH_LONG).show();
        }
    }

    public void LoadInterstitialAd(){
        if (!isLoadedInterstitial){
            isLoadedInterstitial = true;
            interstitialAd = new InterstitialAd(_activity);
            interstitialAd.setAdUnitId(_activity.getResources().getString(R.string.interstitial_ad_unit_id));
            AdRequest request = new AdRequest.Builder().addTestDevice("B43FCE5959423443AF7C9BFA9DAF18C3").build();
            interstitialAd.loadAd(request);
        }
    }

    public void saveToSdCard(int pos){
        Utils utils = new Utils(_activity);
        if (bitmaps.containsKey(pos)){
            Toast.makeText(_activity,"Processing request, please wait..",Toast.LENGTH_LONG).show();
            utils.saveImageToSDCard(bitmaps.get(pos));
        }else {
            Toast.makeText(_activity,"Please wait till the wallpaper is loaded",Toast.LENGTH_LONG).show();
        }
    }
}
