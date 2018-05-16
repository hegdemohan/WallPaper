package com.cipherScriptDevs.backDrop;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by mhegde on 02/02/2018.
 */

public class RewardedVideoAds implements RewardedVideoAdListener{
    private static final String TAG = "RewardedVideoAds";

    private static RewardedVideoAds instance;
    private RewardedVideoAd mRewardedVideoAd;
    private boolean watchedCompleteAd = false;
    private SharedPreferences sharedPreferenceMaxLimitVideo = AppController.getAppContext().getSharedPreferences("maxLimitVideo",MODE_PRIVATE);

    public static RewardedVideoAds getInstance() {
        if (null == instance) {
            instance = new RewardedVideoAds();
        }
        return instance;
    }


    public void loadAd(Context context){
        MobileAds.initialize(context,AppController.getAppContext().getResources().getString(R.string.app_id));
        watchedCompleteAd = false;
        // Use an activity context to get the rewarded video instance.
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(context);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        mRewardedVideoAd.loadAd(AppController.getAppContext().getResources().getString(R.string.rewarded_video_ad_unit_id),new AdRequest.Builder().addTestDevice("B43FCE5959423443AF7C9BFA9DAF18C3").build());
    }

    public void showAd(){
        if(mRewardedVideoAd.isLoaded()){
            mRewardedVideoAd.show();
        }else{
            Toast.makeText(AppController.getAppContext(),"No video ad is available currently, please try again after 1 or 2 minutes", Toast.LENGTH_LONG).show();
            mRewardedVideoAd.loadAd(AppController.getAppContext().getResources().getString(R.string.rewarded_video_ad_unit_id),new AdRequest.Builder().addTestDevice("B43FCE5959423443AF7C9BFA9DAF18C3").build());
        }
    }

    @Override
    public void onRewardedVideoAdLoaded() {

    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {
        if(!watchedCompleteAd){
            Toast.makeText(AppController.getAppContext(),"Sorry, You have not watched the video completely",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        watchedCompleteAd = true;
        SharedPreferences.Editor editorIsVideoWatched = sharedPreferenceMaxLimitVideo.edit();
        editorIsVideoWatched.putInt("maxLimitVideo", 0);
        editorIsVideoWatched.apply();
        Toast.makeText(AppController.getAppContext(),"Congrats!! You have successfully renewed the limit on this feature!",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
        loadAd(AppController.getAppContext());
    }
}
