package com.example.files.adapters;

import static com.example.files.Statics.folder;
import static com.example.files.Statics.openFolder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.files.R;
import com.example.files.models.JFile;

import java.io.File;
import java.util.List;

public class PathAdapter extends BaseAdapter {

    private final Activity activity;
    private final List<JFile> mDataSource;
    private final LayoutInflater layoutInflater;
    private ClickListener clickListener;

    public PathAdapter(Activity activity, List<JFile> dataSource) {
        this.activity = activity;
        this.mDataSource = dataSource;
        this.layoutInflater = LayoutInflater.from(activity);
    }

    @Override
    public int getCount() {
        return mDataSource.size();
    }

    @Override
    public JFile getItem(int position) {
        return mDataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;  // שינוי לזהות ייחודית יותר
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_simple_text, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        File currentFile = getItem(position);
        holder.itemText.setText(currentFile.getName());

        convertView.setOnClickListener(v -> {
            if (!folder.getPath().equals(currentFile.getPath())) {
                openFolder(currentFile);
            } else {
                Toast.makeText(activity, "Already there", Toast.LENGTH_SHORT).show();
            }

            if (clickListener != null) {
                clickListener.onPostClick();
            }
        });

        return convertView;
    }

    public interface ClickListener {
        void onPostClick();
    }

    public void setPostClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    private static class ViewHolder {
        final TextView itemText;

        ViewHolder(View view) {
            itemText = view.findViewById(R.id.item_text);
        }
    }
}
