package com.cipherScriptDevs.backDrop;

import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import adapter.ImageAdapter;
import picasa.Wallpaper;
import util.Utils;

/**
 * Created by mhegde on 09/25/2017.
 */

public class RecyclerFragment extends Fragment {

    private static final String TAG = GridFragment.class.getSimpleName();
    private Utils utils;

    private static final String bundleAlbumId = "albumId";
    private String selectedAlbumId;
    private List<Wallpaper> photosList;
    private ProgressBar pbLoader;
    private static final int ITEMS_PER_AD = 8;
    private static final int NATIVE_AD_HEIGHT = 150;
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";

    private PrefManager pref;
    ArrayList<String> imagePaths = new ArrayList<>();

    String albumId;
    private String selectedAlbum;
    private TextView downloadWallpaperText;

    private RecyclerView recyclerView;
    private int imageWidth = 0;

    public static RecyclerFragment newInstance(Bundle bundle) {
        RecyclerFragment recyclerFragment = new RecyclerFragment();
        recyclerFragment.setArguments(bundle);
        return recyclerFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_recyler, container,
                false);

        return rootView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        albumId = getArguments().getString("albumId", "");
        selectedAlbum = albumId;

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

        utils = new Utils(getActivity());

        // Hiding the gridView and showing loader image before making the http
        // request
        recyclerView = (RecyclerView) view.findViewById(R.id.rv_downloads);
        pbLoader = (ProgressBar) view.findViewById(R.id.pbLoader);
        downloadWallpaperText = (TextView) view.findViewById(R.id.downloadWallpaperText);
        downloadWallpaperText.setVisibility(View.GONE);
    }

    public void initialiseRecyclerView() {
        if (utils.getFilePaths().size() == 0) {
            downloadWallpaperText.setVisibility(View.VISIBLE);
            pbLoader.setVisibility(View.GONE);
        } else {
            downloadWallpaperText.setVisibility(View.GONE);
        }
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        imageWidth = size.x;
        imagePaths = utils.getFilePaths();
        ImageAdapter adapterDownloads = new ImageAdapter(getActivity(), imagePaths , imageWidth);
        recyclerView.setAdapter(adapterDownloads);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        recyclerView.setVisibility(View.VISIBLE);
        pbLoader.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        if (selectedAlbumId.equalsIgnoreCase("Downloads")) {
            initialiseRecyclerView();
        }
        super.onResume();
    }
}
