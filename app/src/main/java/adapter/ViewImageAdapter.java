package adapter;

/**
 * Created by mhegde on 04/24/2017.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cipherScriptDevs.backDrop.AppConst;
import com.cipherScriptDevs.backDrop.AppController;
import com.cipherScriptDevs.backDrop.FullScreenActivity;
import com.cipherScriptDevs.backDrop.R;
import com.cipherScriptDevs.backDrop.RewardedVideoAds;
import com.cipherScriptDevs.backDrop.ViewImageActivity;
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
import util.InAppPurchase;
import util.LPref;
import util.Utils;

import static android.content.Context.MODE_PRIVATE;

public class ViewImageAdapter extends PagerAdapter {

    private Activity _activity;
    private List<Wallpaper> _imagePaths;
    private LayoutInflater inflater;
    private JSONObject mediaObj;
    private String message;
    private InAppPurchase inAppPurchase;
    private Hashtable<Integer,Bitmap> bitmaps = new Hashtable<>();
    private Hashtable<Integer,String> fullResolutionUrls = new Hashtable<>();
    private boolean isProcessed;
    private static final String TAG_ENTRY = "entry",
            TAG_MEDIA_GROUP = "media$group",
            TAG_MEDIA_CONTENT = "media$content", TAG_IMG_URL = "url";
    private String url;
    private ViewPager viewPager;
    private String selectedAlbum;
    private InterstitialAd interstitialAd;
    private boolean isLoadedInterstitial = false;

    // constructor
    public ViewImageAdapter(Activity activity,
                            List<Wallpaper> imagePaths,ViewPager viewPager,String selectedAlbum,InterstitialAd interstitialAd) {
        this._activity = activity;
        this._imagePaths = imagePaths;
        this.viewPager = viewPager;
        this.selectedAlbum = selectedAlbum;
        this.interstitialAd = interstitialAd;
    }

    @Override
    public int getCount() {
        return this._imagePaths.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == (object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        inflater = (LayoutInflater) _activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        final View viewLayout = inflater.inflate(R.layout.image_viewpager_layout, container, false);

        final ImageView imgDisplay = viewLayout.findViewById(R.id.viewImage);

        imgDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullScreenImage(viewPager.getCurrentItem(),selectedAlbum);
            }
        });

        final ProgressBar pbLoader = viewLayout.findViewById(R.id.progressBarViewImage);

        pbLoader.setVisibility(View.VISIBLE);

        url = _imagePaths.get(position).getPhotoJson();

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

                    final String fullResolutionUrl = mediaObj
                            .getString(TAG_IMG_URL);
                    SharedPreferences sharedPreferenceInterstitialAdOnFling = _activity.getSharedPreferences("interstitialAdOnFling", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editorInterstitialAd = sharedPreferenceInterstitialAdOnFling.edit();
                    editorInterstitialAd.putInt("interstitialAd", sharedPreferenceInterstitialAdOnFling.getInt("interstitialAd",0)+1);
                    editorInterstitialAd.apply();

                    SharedPreferences sharedPreferenceInterstitialAdViewImageFireBase = _activity.getSharedPreferences("InterstitialAdViewImage",MODE_PRIVATE);
                    int INTERSTITIAL_AD_VIEW_IMAGE = sharedPreferenceInterstitialAdViewImageFireBase.getInt("InterstitialAdViewImage",10);
                    if (sharedPreferenceInterstitialAdOnFling.getInt("interstitialAd",0)>=INTERSTITIAL_AD_VIEW_IMAGE){
                        if (interstitialAd.isLoaded() && LPref.getIntPref(AppConst.PRODUCT_AD_ID, AppConst.PRODUCT_NOT_BOUGHT) == 2){
                            interstitialAd.show();
                            isLoadedInterstitial = false;
                            LoadInterstitialAd();
                            editorInterstitialAd.putInt("interstitialAd",0);
                            editorInterstitialAd.apply();
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
                                    fullResolutionUrls.put(position,fullResolutionUrl);
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

    public void performClickOperations(View view, final String selectedAlbum, final int selectedPosition , final LinearLayout processing , final LinearLayout llSetWallpaper){
        if(bitmaps.containsKey(selectedPosition)){
            if (bitmaps.get(selectedPosition) != null){
                final Utils utils = new Utils(_activity);
                switch (view.getId()) {
                    // button Download Wallpaper tapped
                    case R.id.llDownloadWallpaper:
                        SharedPreferences sharedPreferenceVideoAdDownloadFireBase = _activity.getSharedPreferences("videoAdDownloadFireBase", MODE_PRIVATE);
                        int VIDEO_AD_DOWNLOAD = sharedPreferenceVideoAdDownloadFireBase.getInt("videoAdDownloadFireBase",10);
                        SharedPreferences sharedPreferenceMaxLimitVideoDownload = _activity.getSharedPreferences("maxLimitVideoDownload", MODE_PRIVATE);
                        if (sharedPreferenceMaxLimitVideoDownload.getInt("maxLimitVideoDownload",0) >= VIDEO_AD_DOWNLOAD && LPref.getIntPref(AppConst.PRODUCT_AD_ID, AppConst.PRODUCT_NOT_BOUGHT) == 2){
                            message = _activity.getResources().getString(R.string.max_limit);
                            openDialog(message);
                        }else {
                            utils.saveImageToSDCard(bitmaps.get(selectedPosition));
                        }
                        break;
                    // button Set As Wallpaper tapped
                    case R.id.llSetWallpaper:
                        SharedPreferences sharedPreferenceVideoAdSetFireBase = _activity.getSharedPreferences("VideoAdSetFireBase", MODE_PRIVATE);
                        int VIDEO_AD_SET = sharedPreferenceVideoAdSetFireBase.getInt("VideoAdSetFireBase",30);
                        SharedPreferences sharedPreferenceMaxLimitVideo = _activity.getSharedPreferences("maxLimitVideo", MODE_PRIVATE);
                        if (sharedPreferenceMaxLimitVideo.getInt("maxLimitVideo",0) >= VIDEO_AD_SET && LPref.getIntPref(AppConst.PRODUCT_AD_ID, AppConst.PRODUCT_NOT_BOUGHT) == 2){
                            message = _activity.getResources().getString(R.string.max_limit);
                            openDialog(message);
                        }else {
                            processing.setVisibility(View.VISIBLE);
                            llSetWallpaper.setVisibility(View.GONE);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    isProcessed = utils.setAsWallpaper(bitmaps.get(selectedPosition) , selectedAlbum ,selectedPosition , _activity);
                                    if(isProcessed){
                                        processing.setVisibility(View.GONE);
                                        llSetWallpaper.setVisibility(View.VISIBLE);
                                    }
                                }
                            },2000);
                        }
                        break;
                    case R.id.fab:
                        SharedPreferences sharedPreferenceVideoAdShareFireBaseFireBase = _activity.getSharedPreferences("VideoAdShareFireBase",MODE_PRIVATE);
                        int VIDEO_AD_SHARE = sharedPreferenceVideoAdShareFireBaseFireBase.getInt("VideoAdShareFireBase",10);
                        SharedPreferences sharedPreferenceMaxLimitVideoShare = _activity.getSharedPreferences("maxLimitVideoShare", MODE_PRIVATE);
                        if (sharedPreferenceMaxLimitVideoShare.getInt("maxLimitVideoShare",0) >= VIDEO_AD_SHARE && LPref.getIntPref(AppConst.PRODUCT_AD_ID, AppConst.PRODUCT_NOT_BOUGHT) == 2){
                            message = _activity.getResources().getString(R.string.max_limit);
                            openDialog(message);
                        }else {
                            utils.shareWallpaper(bitmaps.get(selectedPosition));
                        }
                    default:
                        break;
                }
            }
        }
    }

    public void openDialog(String message){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(_activity);
        final View dialogBox = _activity.getLayoutInflater().inflate(R.layout.dialog_max_limit, null);
        TextView messageText = dialogBox.findViewById(R.id.message);
        messageText.setText(message);

        dialogBuilder.setView(dialogBox);
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

        Button removeAds = dialogBox.findViewById(R.id.premium);
        removeAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inAppPurchase = new InAppPurchase(_activity);
                inAppPurchase.removeAds();
                dialog.dismiss();
            }
        });
    }

    public void fullScreenImage(int currentPosition,String selectedAlbum){
        if(bitmaps.containsKey(currentPosition)){
                    int count = LPref.getIntPref("interstitialAdViewFullScreenTracker",0);
                    if (interstitialAd.isLoaded() && count >= LPref.getIntPref("InterstitialViewFullScreen",10)){
                        interstitialAd.show();
                        isLoadedInterstitial = false;
                        LPref.putIntPref("interstitialAdViewFullScreenTracker",0);
                    }else {
                        LPref.putIntPref("interstitialAdViewFullScreenTracker",count+1);
                    }
                    Intent intent = new Intent(_activity, FullScreenActivity.class);
                    intent.putExtra("url", fullResolutionUrls.get(currentPosition));
                    intent.putExtra("selectedAlbumId",selectedAlbum);
                    intent.putExtra("selectedPhotoPosition",currentPosition);
                    _activity.startActivity(intent);
                }else {
                    Toast.makeText(_activity,"Please wait till the image is loaded",Toast.LENGTH_LONG).show();
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

}