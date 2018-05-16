package util;

import android.app.WallpaperManager;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.cipherScriptDevs.backDrop.AppController;

import java.util.ArrayList;

/**
 * Created by mhegde on 05/08/2017.
 */
public class GCMService extends GcmTaskService {
    private ArrayList<String> imagePaths = new ArrayList<>();
    AppController appController = new AppController();
    Utils utils = new Utils(appController.getAppContext());
    @Override
    public int onRunTask(TaskParams taskParams) {
        SharedPreferences sharedPreferences = getSharedPreferences("position",MODE_APPEND);
        int position = sharedPreferences.getInt("currentPosition",0);
        imagePaths = utils.getFilePaths();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePaths.get(position), options);
        setAsWallpaperGCM(bitmap);
        SharedPreferences.Editor editorHour = sharedPreferences.edit();
        if(position < imagePaths.toArray().length-1){
            editorHour.putInt("currentPosition", position+1);
            editorHour.apply();
            return 0;
        }else {
            editorHour.putInt("currentPosition", 0);
            editorHour.apply();
        }
        return GcmNetworkManager.RESULT_SUCCESS;
    }

    public void setAsWallpaperGCM(Bitmap bitmap) {
        try {
            WallpaperManager wm = WallpaperManager.getInstance(this);
            wm.setBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
