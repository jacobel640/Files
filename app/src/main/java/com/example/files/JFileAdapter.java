package com.example.files;

import static com.example.files.MainActivity.hideKeyboard;
import static com.example.files.MainActivity.instance;
import static com.example.files.Statics.copyMode;
import static com.example.files.Statics.currentFragment;
import static com.example.files.Statics.isSingleLine;
import static com.example.files.Statics.multiSelected;
import static com.example.files.Statics.openFile;
import static com.example.files.Statics.openFolder;
import static com.example.files.Statics.openRecent;
import static com.example.files.Statics.selectedJFiles;
import static com.example.files.Statics.showFileSize;
import static com.example.files.fragments.FragmentBase.FragmentType.MAIN;
import static com.example.files.fragments.FragmentBase.FragmentType.SEARCH;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.files.actions.DialogSort;
import com.example.files.utils.FileIcon;
import com.example.files.fragments.FragmentBase.FragmentType;
import com.example.files.models.JFile;
import com.example.files.models.ViewHolder;
import com.example.files.utils.Filters;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class JFileAdapter extends RecyclerView.Adapter<ViewHolder> implements SectionIndexer {
    Vibrator vibrator;
    long[] pattern = {0, 3, 4, 0};

    public String searchChr = "";
    private AdapterListener adapterListener;
    SearchAdapterListener searchAdapterListener;
    public ArrayList<JFile> jFileList;
    private final List<JFile> getJFileListFiltered;
    private Context context;

    public enum ViewType {ROW, GRID}

    public ViewType viewType;
    FragmentType parent;

    public JFileAdapter(ArrayList<JFile> jFileList, ViewType viewType, FragmentType parent) {
        this.jFileList = jFileList;
        this.getJFileListFiltered = jFileList;
        this.viewType = viewType;
        this.parent = parent;
    }

    public boolean isParentType(FragmentType type) {
        return this.parent == type;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int typeView) {
        context = parent.getContext();
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (viewType == ViewType.GRID) {
            if (isParentType(MAIN))
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_recent, parent, false));
            else
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_grid_type, parent, false));
        } else
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_file_card, parent, false));
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n", "MissingPermission"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        JFile currentJFile = jFileList.get(position);
        if (currentJFile != null) currentJFile.setPosition(position);

        if (position == 0) holder.divider.setVisibility(View.GONE);
        else holder.divider.setVisibility(View.VISIBLE);

        holder.selected.setVisibility(multiSelected && !isParentType(MAIN) ? View.VISIBLE : View.GONE);
        if (currentJFile != null) {
            if (viewType == ViewType.GRID)
                holder.iconView.setBackgroundColor(currentJFile.isSelected() ?
                        context.getColor(R.color.selected) : context.getColor(R.color.background));
            else holder.itemView.setBackgroundColor(currentJFile.isSelected() ?
                    context.getColor(R.color.selected) : context.getColor(R.color.background));
            holder.selected.setChecked(currentJFile.isSelected());
            holder.fileName.setText(currentJFile.getName());
            if (isParentType(SEARCH)) {
                String[] parts = searchChr.split(" ");
                for (String part : parts) {
                    setHighLightedText(holder.fileName, part);
                }
                holder.size.setText(currentJFile.getCountItems());
            } else {
                holder.size.setText(currentJFile.getStringSize());
            }
            holder.info.setText(currentJFile.getStringDate());


            if (!currentJFile.isIconReady()) {
                currentJFile.setIconReadyListener(object ->
                        holder.itemView.post(() -> FileIcon.setIcon(holder, viewType, currentJFile, context)));
            }
            FileIcon.setIcon(holder, viewType, currentJFile, context);

//            if (isParentType(MAIN))
//            holder.itemView.findViewById(R.id.count_files).setVisibility(position == getItemCount()-1 ? View.VISIBLE : View.GONE);
        }

        holder.fileName.setSingleLine(isSingleLine || viewType == ViewType.GRID);
        holder.image.setClipToOutline(true);

        holder.itemView.setOnClickListener(view -> clickItem(holder, position, true));

        holder.itemView.setOnLongClickListener(view -> {

            if (!multiSelected) {
                vibrator.vibrate(pattern, -1);

                switch (parent) {
                    case MAIN:
                        if (copyMode) break;
                        openRecent();
                        if (currentJFile != null) currentFragment.select(currentJFile.getPath());
                        break;
                    case SEARCH:
                        openFolder(Objects.requireNonNull(jFileList.get(position).getParentFile()));
                        currentFragment.select(jFileList.get(position).getPath());
                        break;
                    case FILES:
                    case CATEGORY:
                    case RECENT:
                        instance.eventListener.onMultiSelectedChange(true);
                        break;
                }
            }

            clickItem(holder, position, false); // when multiSelected is true

            return true;
        });
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        Glide.with(holder.image).clear(holder.image);
        super.onViewRecycled(holder);
    }

    private final AtomicBoolean sortPending = new AtomicBoolean(false);

    public void onSizeBatchUpdated() {
        if (!showFileSize) return;

        if (sortPending.compareAndSet(false, true)) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                DialogSort.sort(jFileList);
                notifyDataSetChanged();
                sortPending.set(false);
            }, 300);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 0; // 0 = fast scrolling | position = slower scrolling
    }

    @Override
    public int getItemCount() {
        if (jFileList != null) return jFileList.size();
        else return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void deselectAll() {
        if (!multiSelected) {
            for (int j = 0; j < jFileList.size(); j++) //deselect all
                if (jFileList.get(j).isSelected()) selected(j);
        }
        notifyDataSetChanged();
        adapterListener.onSelectItemListener(selectedJFiles.size());
    }

    public void clickItem(ViewHolder holder, int position, boolean shortClick) {
        hideKeyboard(context);
        JFile jFile = jFileList.get(position);
        Log.d("##### Adapter.clickItem #####", "\nisShortClick= " + shortClick
                + "\nmultiSelected= " + multiSelected + "\nparent= " + parent);
        if (multiSelected && !isParentType(MAIN)) {
            if (adapterListener != null)
                // send an event to trigger select mode
                if (shortClick) adapterListener.onItemClick(holder, position);
                else adapterListener.onItemLongClick(holder, position);
        } else if (jFile.isDirectory() && shortClick) openFolder(jFile);
        else if (jFile.getExtension() != null && shortClick) openFile(jFile, context);
    }

    public void selected(int position) {
        //vibrator.vibrate(pattern, -1); // conflict with backPressed
        JFile jFile = jFileList.get(position);
        jFile.setSelected(!jFile.isSelected());
        if (jFile.isSelected()) selectedJFiles.add(jFile);
        else selectedJFiles.remove(jFile);
        notifyItemChanged(position);
        // fragment ui stuff
        adapterListener.onSelectItemListener(selectedJFiles.size());
    }

    public void selectRange(int start, int end) {
        for (int i = start; i <= end; i++) selected(i);
        notifyItemRangeChanged(start, end - start + 1);
        adapterListener.onSelectItemListener(selectedJFiles.size());
    }

    @SuppressLint("NotifyDataSetChanged")
    public void selectAll(boolean select) {
        if (select) {
            for (JFile jFile : jFileList) {
                if (!jFile.isSelected()) {
                    jFile.setSelected(true);
                    selectedJFiles.add(jFile);
                }
            }

        } else
            for (JFile jFile : jFileList)
                if (jFile.isSelected()) {
                    jFile.setSelected(false);
                    selectedJFiles.remove(jFile);
                }
        notifyDataSetChanged();
        adapterListener.onSelectItemListener(selectedJFiles.size());
    }

    public interface AdapterListener {
        void onItemClick(ViewHolder holder, int position);

        void onItemLongClick(ViewHolder holder, int position);

        void onSelectItemListener(int selectedSize);
    }

    public interface SearchAdapterListener {
        void filterResultStarted();

        void filterResultFinished();
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    public void setSearchAdapterListener(SearchAdapterListener searchAdapterListener) {
        this.searchAdapterListener = searchAdapterListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void notify(ArrayList<JFile> jFiles) {
        jFileList.clear();
        jFileList = jFiles;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearFiltering() {
        notify((ArrayList<JFile>) getJFileListFiltered);
    }

    public boolean isFiltered() {
        return jFileList.size() != getJFileListFiltered.size();
    }

    public Filter getFilter() {

        return new Filter() {

            ArrayList<JFile> resultData;

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults filterResults = new FilterResults();

                Objects.requireNonNull(charSequence).length();
                searchChr = charSequence.toString().toLowerCase();

                resultData = new ArrayList<>();

                for (JFile jFile : getJFileListFiltered) {
                    //if (jFile.toString().toLowerCase().contains(searchChr)) {
                    if (jFile.getName().toLowerCase().contains(searchChr)) {
                        resultData.add(jFile);
                    }
                }

                String[] parts = searchChr.split(" ");

                for (String part : parts) {
                    if (part.isEmpty()) continue;
                    for (JFile jFile : getJFileListFiltered) {
                        if (jFile.getName().toLowerCase().contains(part)) {
                            if (!exist(jFile.getPath())) resultData.add(jFile);
                        }
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

    public void multiFilter(Filters filters) {

        if (searchAdapterListener != null) searchAdapterListener.filterResultStarted();

        new Filter() {

            ArrayList<JFile> resultData;

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults filterResults = new FilterResults();

                searchChr = filters.getTextFilter().trim();
                String[] parts = searchChr.split(" ");

                resultData = new ArrayList<>();

                for (JFile jFile : getJFileListFiltered) {

                    boolean filter = jFile.getName().toLowerCase().contains(searchChr)
                            && (filters.getDateFilter() == 0 || jFile.lastModified() >= filters.getDateFilter())
                            && (filters.getTypeFilter() == null || jFile.getType() == filters.getTypeFilter());

                    if (filter) resultData.add(jFile);
                    else {
                        if (parts.length <= 1) continue;
                        for (String part : parts) {
                            if (part.isEmpty()) continue;

                            filter = jFile.getName().toLowerCase().contains(part)
                                    && (filters.getDateFilter() == 0 || jFile.lastModified() >= filters.getDateFilter())
                                    && (filters.getTypeFilter() == null || jFile.getType() == filters.getTypeFilter())
                                    && !exist(jFile.getPath());

                            if (filter) resultData.add(jFile);
                        }
                    }
                }


                filterResults.count = resultData.size();
                filterResults.values = resultData;

                return filterResults;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

                jFileList = (ArrayList<JFile>) filterResults.values;
                notifyDataSetChanged();

                if (searchAdapterListener != null) searchAdapterListener.filterResultFinished();
            }

            boolean exist(String path) {
                for (JFile jFile : resultData) {
                    if (jFile.getPath().equals(path)) return true;
                }
                return false;
            }
        }.filter("");
    }

    public void setHighLightedText(TextView tv, String textToHighlight) {
        String tvt = tv.getText().toString().toLowerCase();
        int ofe = tvt.indexOf(textToHighlight);
        Spannable wordToSpan = new SpannableString(tv.getText());
        for (int ofs = 0; ofs < tvt.length() && ofe != -1; ofs = ofe + 1) {
            ofe = tvt.indexOf(textToHighlight, ofs);
            if (ofe == -1) {
                break;
            } else {
                // set color here
                wordToSpan.setSpan(new ForegroundColorSpan(context.getColor(R.color.app_theme)), ofe, ofe + textToHighlight.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv.setText(wordToSpan, TextView.BufferType.SPANNABLE);
            }
        }
    }

    @Override
    public Object[] getSections() {
        return jFileList.toArray();
    }

    @Override
    public int getPositionForSection(int i) {
        return i;
    }

    @Override
    public int getSectionForPosition(int i) {
        return 0;
    }

}