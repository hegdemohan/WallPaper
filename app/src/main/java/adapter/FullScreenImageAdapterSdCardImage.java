package adapter;

/**
 * Created by mhegde on 04/25/2017.
 */


import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.github.chrisbanes.photoview.PhotoView;
import com.cipherScriptDevs.backDrop.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FullScreenImageAdapterSdCardImage extends PagerAdapter {

    private Activity _activity;
    private ArrayList<String> _imagePaths;
    private LayoutInflater inflater;

    // constructor
    public FullScreenImageAdapterSdCardImage(Activity activity,
                                  ArrayList<String> imagePaths) {
        this._activity = activity;
        this._imagePaths = imagePaths;
    }

    @Override
    public int getCount() {
        return this._imagePaths.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        PhotoView imgDisplay;

        inflater = (LayoutInflater) _activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.layout_fullscreen_sdcard_image, container,
                false);

        imgDisplay = (PhotoView) viewLayout.findViewById(R.id.imgDisplay);

//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Picasso.with(_activity).load("file://" + _imagePaths.get(position))
                .into(imgDisplay);
        //Bitmap bitmap = BitmapFactory.decodeFile(_imagePaths.get(position), options);
        //imgDisplay.setImageBitmap(bitmap);

        container.addView(viewLayout);

        return viewLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }
}
