package com.example.files.utils;

import static com.example.files.MainActivity.hideKeyboard;
import static com.example.files.Statics.isSingleLine;
import static com.example.files.utils.FileIcon.setIcon;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.files.JFileAdapter;
import com.example.files.R;
import com.example.files.models.JFile;
import com.example.files.models.ViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FavoritesAdapter extends RecyclerView.Adapter<ViewHolder> implements Filterable
{
    public String searchChr;
    private AdapterListener adapterListener;
    public ArrayList<JFile> jFileList;
    public ArrayList<JFile> selected;
    private final List<JFile> getJFileListFiltered;
    private Context context;

    public FavoritesAdapter(ArrayList<JFile> jFileList, ArrayList<JFile> selected) {
        this.jFileList = jFileList;
        this.selected = selected;
        this.getJFileListFiltered = jFileList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_file_card, parent, false));
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n", "MissingPermission"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        JFile currentJFile = jFileList.get(position);
        if (currentJFile != null) currentJFile.setPosition(position);

        if (position == 0) holder.divider.setVisibility(View.GONE);
        else holder.divider.setVisibility(View.VISIBLE);

        holder.selected.setVisibility(View.VISIBLE);
        if (currentJFile != null) {
            holder.itemView.setBackgroundColor(currentJFile.isSelected() ?
                    context.getColor(R.color.selected) : context.getColor(R.color.background));
            holder.selected.setChecked(currentJFile.isSelected());
            holder.fileName.setText(currentJFile.getName());
            holder.fileName.setSingleLine(isSingleLine);
            holder.info.setSingleLine(false);
            holder.info.setText(new PathFormatter(context).format(currentJFile.getPath()));
            if (searchChr != null) {
                String[] parts = searchChr.split(" ");
                for (String part : parts) {
                    setHighLightedText(holder.fileName, part);
                    setHighLightedText(holder.info, part);
                }
            }
            holder.size.setVisibility(View.GONE);
            holder.size.setText(currentJFile.getCountItems());
            holder.image.setClipToOutline(true);

            setIcon(holder, JFileAdapter.ViewType.ROW, currentJFile, context);
        }

        holder.itemView.setOnClickListener(view -> clickItem(holder, position));
        holder.itemView.setOnLongClickListener(v -> {
            clickItem(holder, position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        if (jFileList!=null) return jFileList.size();
        else return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void deselectAll() {
        for (int j = 0; j < jFileList.size(); j++) //deselect all
            if (jFileList.get(j).isSelected()) selected(j);
        notifyDataSetChanged();
        adapterListener.onSelectItemListener(selected.size());
    }

    public void clickItem(ViewHolder holder, int position){
        hideKeyboard(context);
        adapterListener.onItemClick(holder, position);
        //selected(position);
    }

    public void selected(int position) {
        JFile jFile = jFileList.get(position);
        jFile.setSelected(!jFile.isSelected());
        if (jFile.isSelected()) selected.add(jFile);
        else selected.remove(jFile);
        notifyItemChanged(position);
        adapterListener.onSelectItemListener(selected.size());
    }

    public void selectRange(int start, int end) {
        for (int i = start; i <= end; i++) selected(i);
        notifyItemRangeChanged(start, end - start + 1);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void selectAll(boolean select) {
        if (select) {
            for (JFile jFile : jFileList)
                if (!jFile.isSelected()) {
                    jFile.setSelected(true);
                    selected.add(jFile);
                }

        } else
            for (JFile jFile : jFileList)
                if (jFile.isSelected()) {
                    jFile.setSelected(false);
                    selected.remove(jFile);
                }

        adapterListener.onSelectItemListener(selected.size());
        notifyDataSetChanged();
    }

    public interface AdapterListener {
        void onItemClick( ViewHolder holder, int position);
        void onSelectItemListener(int selectedSize);
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void notify(ArrayList<JFile> jFiles){
        jFileList.clear();
        jFileList = jFiles;
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {

        return new Filter() {

            List<JFile> resultData;

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults filterResults = new FilterResults();

                Objects.requireNonNull(charSequence).length();
                searchChr = charSequence.toString().toLowerCase();

                resultData = new ArrayList<>();

                for (JFile jFile: getJFileListFiltered) {
                    //if (jFile.toString().toLowerCase().contains(searchChr)) {
                    if (jFile.getName().toLowerCase().contains(searchChr)) {
                        resultData.add(jFile);
                    }
                }

                String[] parts = searchChr.split(" ");

                for (String part : parts) {
                    if (part.equals("")) continue;
                    for (JFile jFile: getJFileListFiltered) {
                        if (jFile.getName().toLowerCase().contains(part)) {
                            if (!exist(jFile.getPath())) resultData.add(jFile);
                        }
                    }
                }

                for (JFile jFile: getJFileListFiltered) {
                    if (new PathFormatter(context).format(jFile.getPath()).toLowerCase().contains(searchChr)) {
                        if (!exist(jFile.getPath())) resultData.add(jFile);
                    }
                }

                filterResults.count = resultData.size();
                filterResults.values = resultData;

                return filterResults;
            }

            boolean exist(String path) {
                for (JFile jFile : resultData) {
                    if (jFile.getPath().equals(path)) return true;
                }
                return false;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

                jFileList = (ArrayList<JFile>) filterResults.values;
                notifyDataSetChanged();

            }
        };
    }

    public void setHighLightedText(TextView tv, String textToHighlight) {
        String tvt = tv.getText().toString().toLowerCase();
        int ofe = tvt.indexOf(textToHighlight);
        Spannable wordToSpan = new SpannableString(tv.getText());
        for (int ofs = 0; ofs < tvt.length() && ofe != -1; ofs = ofe + 1) {
            ofe = tvt.indexOf(textToHighlight, ofs);
            if (ofe == -1) {
                break;
            }
            else {
                // set color here
                wordToSpan.setSpan(new ForegroundColorSpan(context.getColor(R.color.app_theme)), ofe, ofe + textToHighlight.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv.setText(wordToSpan, TextView.BufferType.SPANNABLE);
            }
        }
    }

}