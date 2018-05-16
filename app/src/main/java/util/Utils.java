package util;

/**
 * Created by mhegde on 04/19/2017.
 */


import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.cipherScriptDevs.backDrop.AppConst;
import com.cipherScriptDevs.backDrop.GridFragment;
import com.cipherScriptDevs.backDrop.PrefManager;
import com.cipherScriptDevs.backDrop.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;


public class Utils {
    private String TAG = Utils.class.getSimpleName();
    private Context _context;
    private PrefManager pref;
    SharedPreferencesHelper sharedPreferencesHelper;
    private GridFragment gridFragment = new GridFragment();

    // constructor
    public Utils(Context context) {
        this._context = context;
        pref = new PrefManager(_context);
    }

    public ArrayList<String> getFilePaths() {
        ArrayList<String> filePaths = new ArrayList<>();
        File directory = new File(
                android.os.Environment.getExternalStorageDirectory()
                        + File.separator + AppConst.PHOTO_ALBUM);

        // check for directory
        if (directory.isDirectory()) {
            // getting list of file paths
            File[] listFiles = directory.listFiles();

            // Check for count
            if (listFiles.length > 0) {

                // loop through all files
                for (int i = 0; i < listFiles.length; i++) {

                    // get file path
                    String filePath = listFiles[i].getAbsolutePath();

                    // check for supported file extension
                    if (IsSupportedFile(filePath)) {
                        // Add image path to array list
                        filePaths.add(filePath);
                    }
                }
            }

        } else {
//            AlertDialog.Builder alert = new AlertDialog.Builder(_context);
//            alert.setTitle("Error!");
//            alert.setMessage(AppConst.PHOTO_ALBUM
//                    + " directory path is not valid! Please set the image directory name AppConstant.java class");
//            alert.setPositiveButton("OK", null);
//            alert.show();
        }

        return filePaths;
    }

    // Check supported file extensions
    private boolean IsSupportedFile(String filePath) {
        String ext = filePath.substring((filePath.lastIndexOf(".") + 1),
                filePath.length());

        if (AppConst.FILE_EXTN
                .contains(ext.toLowerCase(Locale.getDefault())))
            return true;
        else
            return false;

    }

    /*
     * getting screen width
     */
    @SuppressWarnings("deprecation")
    public int getScreenWidth() {
        int columnWidth;
        WindowManager wm = (WindowManager) _context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        final Point point = new Point();
        try {
            display.getSize(point);
        } catch (java.lang.NoSuchMethodError ignore) {
            // Older device
            point.x = display.getWidth();
            point.y = display.getHeight();
        }
        columnWidth = point.x;
        return columnWidth;
    }

    public void saveImageToSDCard(Bitmap bitmap) {
        SharedPreferences sharedPreferenceMaxLimitVideoDownload = _context.getSharedPreferences("maxLimitVideoDownload",Context.MODE_PRIVATE);
        SharedPreferences.Editor editorMaxLimit = sharedPreferenceMaxLimitVideoDownload.edit();
        editorMaxLimit.putInt("maxLimitVideoDownload",sharedPreferenceMaxLimitVideoDownload.getInt("maxLimitVideoDownload",0)+1);
        editorMaxLimit.apply();

        File filePath = Environment.getExternalStorageDirectory();
        File myDir = new File(filePath.getAbsoluteFile() + "/" + pref.getGalleryName());

        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Wallpaper-" + n + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            Toast.makeText(
                    _context,
                    _context.getString(R.string.toast_saved).replace("#",
                            "\"" + pref.getGalleryName() + "\""),
                    Toast.LENGTH_SHORT).show();
            gridFragment.onResume();
            Log.d(TAG, "Wallpaper saved to: " + file.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(_context,
                    _context.getString(R.string.toast_saved_failed),
                    Toast.LENGTH_SHORT).show();
        }

    }

    public boolean setAsWallpaper(final Bitmap bitmap, final String albumId , final int selectedPhotoPosition , final Activity activity) {
        try {
            new Thread(new Runnable() {
                public void run() {
                    WallpaperManager wm = WallpaperManager.getInstance(_context);
                    try {
                        wm.setBitmap(bitmap);
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(_context,
                                        _context.getString(R.string.toast_wallpaper_set),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    SharedPreferences sharedPreferencesSelectedPhotoPosition = _context.getSharedPreferences("selectedPhotoPosition",Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferencesSelectedPhotoPosition.edit();
                    editor.putInt("selectedPhotoPosition",selectedPhotoPosition);
                    editor.apply();
                    SharedPreferences sharedPreferenceMaxLimitVideo = _context.getSharedPreferences("maxLimitVideo",Context.MODE_PRIVATE);
                    SharedPreferences.Editor editorMaxLimit = sharedPreferenceMaxLimitVideo.edit();
                    editorMaxLimit.putInt("maxLimitVideo",sharedPreferenceMaxLimitVideo.getInt("maxLimitVideo",0)+1);
                    editorMaxLimit.apply();


                    sharedPreferencesHelper = new SharedPreferencesHelper(activity);
                    sharedPreferencesHelper.setAlbumId(albumId);
                }
            }).start();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(_context,
                    _context.getString(R.string.toast_wallpaper_set_failed),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void shareWallpaper(Bitmap bitmap) {
        SharedPreferences sharedPreferenceMaxLimitVideoShare = _context.getSharedPreferences("maxLimitVideoShare",Context.MODE_PRIVATE);
        SharedPreferences.Editor editorMaxLimit = sharedPreferenceMaxLimitVideoShare.edit();
        editorMaxLimit.putInt("maxLimitVideoShare",sharedPreferenceMaxLimitVideoShare.getInt("maxLimitVideoShare",0)+1);
        editorMaxLimit.apply();

        File filePath = Environment.getExternalStorageDirectory();
        File myDir = new File(filePath.getAbsoluteFile() + "/" + pref.getGalleryName());

        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Wallpaper-" + n + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
//            Log.d(TAG, "Wallpaper saved to: " + file.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(_context,
                    _context.getString(R.string.toast_share_failed),
                    Toast.LENGTH_SHORT).show();
        }



        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        File shareFile = new File(filePath + File.separator + fname);
        try{
            shareFile.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(shareFile);
            fileOutputStream.write(byteArrayOutputStream.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(file.getAbsolutePath()));
        Intent chooserIntent = Intent.createChooser(shareIntent, "Share via");
        chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(chooserIntent);
    }
}