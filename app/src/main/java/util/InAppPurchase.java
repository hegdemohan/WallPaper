package util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;

import com.android.vending.billing.IInAppBillingService;
import com.cipherScriptDevs.backDrop.AppConst;
import com.cipherScriptDevs.backDrop.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by mhegde on 03/06/2018.
 */

public class InAppPurchase extends AppCompatActivity{

    private IInAppBillingService mServiceBuyProduct;
    private IInAppBillingService mServiceCheckPurchases;
    private boolean mManualPause = false;
    Activity _activity;
    private NavigationView navigationView;

    public InAppPurchase(Activity activity){
        _activity = activity;
    }
    public void removeAds() {

        if (null == mServiceBuyProduct) {
            Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
            serviceIntent.setPackage("com.android.vending");
            _activity.bindService(serviceIntent, mServiceConnectionBuyProduct, Context.BIND_AUTO_CREATE);
        } else {
            showBuyDialog();
        }
    }

    public void checkAppPurchase() {

        int purchaseStatus = LPref.getIntPref(AppConst.PRODUCT_AD_ID, AppConst.PRODUCT_UNKNOWN);

        if ((AppConst.PRODUCT_UNKNOWN == purchaseStatus ||
                AppConst.PRODUCT_NOT_BOUGHT == purchaseStatus)
                && null == mServiceCheckPurchases) {
            Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
            serviceIntent.setPackage("com.android.vending");
            _activity.bindService(serviceIntent, mServiceConnectionCheckPurchases, Context.BIND_AUTO_CREATE);

            navigationView = _activity.findViewById(R.id.nav_view);
            navigationView.getMenu().findItem(R.id.premium).setVisible(true);
        }else {
            LPref.putIntPref(AppConst.PRODUCT_AD_ID, AppConst.PRODUCT_BOUGHT);
            navigationView = _activity.findViewById(R.id.nav_view);
            navigationView.getMenu().findItem(R.id.premium).setVisible(false);
        }
    }

    ServiceConnection mServiceConnectionBuyProduct = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBuyProduct = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServiceBuyProduct = IInAppBillingService.Stub.asInterface(service);
            showBuyDialog();
        }
    };

    private void showBuyDialog() {

        mManualPause = true;

        if (AppConst.PRODUCT_BOUGHT == LPref.getIntPref(AppConst.PRODUCT_AD_ID, AppConst.PRODUCT_UNKNOWN)) {
            return;
        }

        ArrayList<String> skuList = new ArrayList<String>();
        skuList.add(AppConst.PRODUCT_AD_ID);
        Bundle querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST", skuList);

        Bundle skuDetails = null;

        try {
            skuDetails = mServiceBuyProduct.getSkuDetails(3, _activity.getPackageName(), "inapp", querySkus);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        int response = skuDetails.getInt("RESPONSE_CODE");
        if (response == 0) {
            ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");
            String            sku          = "";

            for (String thisResponse : responseList) {
                JSONObject object = null;

                try {
                    object = new JSONObject(thisResponse);
                    sku = object.getString("productId");
//                    String price = object.getString("price");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            Bundle buyIntentBundle = null;

            try {
                buyIntentBundle = mServiceBuyProduct.getBuyIntent(3, _activity.getPackageName(),
                        sku, "inapp", "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            try {
                PendingIntent pendingIntent = null;
                if (buyIntentBundle != null) {
                    pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                }
                if (pendingIntent != null) {
                    _activity.startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), 0, 0, 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1001) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);

            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");

            reCheckAppPurchase();
        }
    }

    private void reCheckAppPurchase() {

        if (null != mServiceCheckPurchases) {
            try {
                _activity.unbindService(mServiceConnectionCheckPurchases);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        _activity.bindService(serviceIntent, mServiceConnectionCheckPurchases, Context.BIND_AUTO_CREATE);
    }



    ServiceConnection mServiceConnectionCheckPurchases = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceCheckPurchases = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServiceCheckPurchases = IInAppBillingService.Stub.asInterface(service);

            int    response   = -1;
            Bundle ownedItems = null;

            try {
                ownedItems = mServiceCheckPurchases.getPurchases(3, _activity.getPackageName(), "inapp", null);
                response = ownedItems.getInt("RESPONSE_CODE");
            } catch (RemoteException e) {
//                e.printStackTrace();
            }

            if (0 == response) {
                ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                ArrayList<String> purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");

                LPref.putIntPref(AppConst.PRODUCT_AD_ID, AppConst.PRODUCT_NOT_BOUGHT);

                assert purchaseDataList != null;
                for (int i = 0; i < purchaseDataList.size(); ++i) {
//                    String purchaseData = purchaseDataList.get(i);
//                    String signature = signatureList.get(i);
                    assert ownedSkus != null;
                    String sku = ownedSkus.get(i);

                    if (sku.equals(AppConst.PRODUCT_AD_ID)) {

                        LPref.putIntPref(AppConst.PRODUCT_AD_ID, AppConst.PRODUCT_BOUGHT);
                        navigationView = _activity.findViewById(R.id.nav_view);
                        navigationView.getMenu().findItem(R.id.premium).setVisible(false);
                        break;
                    }
                }
            }
        }
    };
}
