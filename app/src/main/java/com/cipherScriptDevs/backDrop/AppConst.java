package com.cipherScriptDevs.backDrop;

import android.os.Build;

import java.util.Arrays;
import java.util.List;

/**
 * Created by mhegde on 04/19/2017.
 */

public class AppConst {
    // Number of columns of Grid View
    // by default 2 but user can configure this in dialog_settings activity
    public static final int NUM_OF_COLUMNS = 2;

    // GridView image padding
    public static final int GRID_PADDING = 1; // in dp

    // Gallery directory name to save wallpapers
    public static final String SDCARD_DIR_NAME = "Wallpapers";

    // Picasa/Google web album username
    public static final String PICASA_USER = "mywallpaperapplication";

    // Public albums list url
    public static final String URL_PICASA_ALBUMS = "https://picasaweb.google.com/data/feed/api/user/mywallpaperapplication?kind=album&alt=json";

    // Picasa album photos url
    public static final String URL_ALBUM_PHOTOS = "https://picasaweb.google.com/data/feed/api/user/mywallpaperapplication/albumid/_ALBUM_ID_?alt=json";

    // Picasa recently added photos url
    public static final String URL_RECENTLY_ADDED = "https://picasaweb.google.com/data/feed/api/user/mywallpaperapplication?kind=photo&alt=json";


    // SD card image directory
    public static final String PHOTO_ALBUM = "Wallpapers";

    // supported file formats
    public static final List<String> FILE_EXTN = Arrays.asList("jpg", "jpeg", "png");

    //In-app purchase constants
    public static final int PRODUCT_BOUGHT = 0;

    public static final int PRODUCT_UNKNOWN = 1;

    public static final int PRODUCT_NOT_BOUGHT = 2;

    public static final String PRODUCT_AD_ID = "removeads";

    public static final String APPLICATION_ID = BuildConfig.APPLICATION_ID;
}
