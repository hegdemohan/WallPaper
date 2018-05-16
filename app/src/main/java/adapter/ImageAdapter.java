package adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;
import com.cipherScriptDevs.backDrop.FullScreenActivitySdCardImage;
import com.cipherScriptDevs.backDrop.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by mhegde on 09/25/2017.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private final ArrayList<String> filePaths;
    private final Context context;
    private final int imageWidth;
    private MultiSelector mMultiSelector = new MultiSelector();
    private ActionMode mMode;

    public ImageAdapter(Context context, ArrayList<String> filePaths , int imageWidth) {
        this.context = context;
        this.filePaths = filePaths;
        this.imageWidth = imageWidth;
    }

    private ArrayList<String> selectedFilePaths = new ArrayList();
    private boolean success = false;
    private boolean multiChoice = false;
    private ModalMultiSelectorCallback mActionModeCallback = new ModalMultiSelectorCallback(mMultiSelector) {
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater menuInflater = mode.getMenuInflater();
            menuInflater.inflate(R.menu.context_delete_multiple, menu);
            mMode = mode;
            multiChoice = true;
            mode.setTitle(selectedFilePaths.size() + " items selected");
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                    case R.id.deleteId:
                        for (String path : selectedFilePaths) {
                            File deleteImage = new File(path);
                            if (deleteImage.exists()) {
                                success = deleteImage.delete();
                            }
                        }
                        if (success) {
                            for(int i = 0; i < selectedFilePaths.size(); i++){
                                filePaths.remove(filePaths.indexOf(selectedFilePaths.get(i)));
                            }
                            selectedFilePaths.clear();
                            notifyDataSetChanged();
                            Toast.makeText(context, "Selected wallpapers were deleted successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Error while deleting", Toast.LENGTH_SHORT).show();
                        }
                        mode.finish();
                        notifyDataSetChanged();
                        return true;
                    default:
                        return false;
                }
        }

        public void onDestroyActionMode(ActionMode mode) {
            selectedFilePaths.clear();
            notifyDataSetChanged();
            multiChoice = false;
        }
    };

    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_sdcard, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageAdapter.ViewHolder holder, int position) {
//        Bitmap image = decodeFile(filePaths.get(position), imageWidth,
//                imageWidth);
        Picasso.with(context).load("file://" + filePaths.get(position))
                .resize(imageWidth, imageWidth)
                .centerCrop()
                .into(holder.ivThumb);

        holder.selected.setVisibility(View.GONE);
        if(selectedFilePaths.size() > 0 && selectedFilePaths.contains(filePaths.get(position))){
            holder.selected.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        if (null != filePaths) {
            return filePaths.size();
        }

        return 0;
    }



    class ViewHolder extends SwappingHolder implements View.OnClickListener , View.OnLongClickListener{
        private final ImageView ivThumb;
        private final ImageView selected;

        public ViewHolder(View itemView) {
            super(itemView,mMultiSelector);
            ivThumb = (ImageView) itemView.findViewById(R.id.iv_thumb);
            selected = (ImageView) itemView.findViewById(R.id.selected);
            selected.setVisibility(View.GONE);
            ivThumb.setOnLongClickListener(this);
            ivThumb.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(multiChoice){
                if(!selectedFilePaths.contains(filePaths.get(getAdapterPosition()))){
                    selected.setVisibility(View.VISIBLE);
                    selectedFilePaths.add(filePaths.get(getAdapterPosition()));
                }else{
                    selected.setVisibility(View.GONE);
                    selectedFilePaths.remove(filePaths.get(getAdapterPosition()));
                }
                if(selectedFilePaths.size() == 0){
                    mMode.finish();
                }
                mMode.setTitle(selectedFilePaths.size() + " items selected");
            }else {
                Intent intent = new Intent(context, FullScreenActivitySdCardImage.class);
                intent.putExtra("position", getAdapterPosition());
                context.startActivity(intent);
            }
        }

        @Override
        public boolean onLongClick(View v) {
                selectedFilePaths.clear();
                selectedFilePaths.add(filePaths.get(getAdapterPosition()));
                selected.setVisibility(View.VISIBLE);
                ((AppCompatActivity) context).startSupportActionMode(mActionModeCallback);
                mMultiSelector.setSelectable(true);
                mMultiSelector.setSelected(ViewHolder.this, true);
                return true;
            }
        }
    }
