package com.example.files.models;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.files.R;

public class ViewHolder extends RecyclerView.ViewHolder {

    public final CheckBox selected;
    public TextView fileName;
    public TextView info;
    public TextView size;
    public TextView ext;
    public View iconView;
    public ImageView image;
    public ImageView icon;
    public ImageView indicator;
    public ImageView divider;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
//        setIsRecyclable(false);
        selected = itemView.findViewById(R.id.selected);
        fileName = itemView.findViewById(R.id.file_name);
        info = itemView.findViewById(R.id.file_info);
        size = itemView.findViewById(R.id.file_size);
        iconView = itemView.findViewById(R.id.icon_view_root);
        image = itemView.findViewById(R.id.icon_view_image);
        icon = itemView.findViewById(R.id.icon_view_placeholder);
        ext = itemView.findViewById(R.id.icon_view_ext);
        indicator = itemView.findViewById(R.id.icon_view_indicator);
        divider = itemView.findViewById(R.id.divider);

        iconView.setClipToOutline(true);
    }
}
