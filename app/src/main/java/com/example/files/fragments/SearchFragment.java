package com.example.files.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.files.MainActivity.enableTextButton;
import static com.example.files.MainActivity.hideKeyboard;
import static com.example.files.MainActivity.instance;
import static com.example.files.MainActivity.showKeyboard;
import static com.example.files.MainActivity.textBtnState;
import static com.example.files.Statics.folder;
import static com.example.files.Statics.multiSelected;
import static com.example.files.Statics.setFastScrollBar;
import static com.example.files.fragments.FragmentBase.FragmentType.SEARCH;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.example.files.JFileAdapter;
import com.example.files.R;
import com.example.files.actions.DialogSort;
import com.example.files.models.JFile;
import com.example.files.utils.Filters;
import com.example.files.view.JChip;
import com.google.android.material.chip.ChipGroup;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class SearchFragment extends Fragment {

    boolean cancel;
    ArrayList<JFile> objects, jFiles;
    @SuppressLint("StaticFieldLeak")
    JFileAdapter jFileAdapter;
    boolean isFolder;
    ImageButton back, clear;
    EditText search;
    RecyclerView rvFiles;
    TextView noResult, tvCount, filterTypeTitle;
    ImageView arrow;
    ProgressBar progress;
    CardView expand;
    LinearLayout layout;
    ChipGroup filterDateChips, filterTypeChips;
    JChip folderChip;
    String category;
    Filters filters;

    public SearchFragment() {
        // Required empty public constructor
    }

    public SearchFragment(String category) {
        this.category = category;
        this.objects = new ArrayList<>();
        Log.d("##### newSFragment #####", "noJFiles");
    }

    public SearchFragment(String category, ArrayList<JFile> jFiles) {
        this.category = category;
        this.objects = new ArrayList<>();
        this.jFiles = new ArrayList<>();
        this.jFiles.addAll(jFiles);
        Log.d("##### newSFragment #####", "withJFiles");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (multiSelected) instance.eventListener.onMultiSelectedChange(false);
        if (multiSelected) multiSelected = false;
        new Handler().postDelayed(() -> textBtnState(enableTextButton()), 100);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_search, container, false);

        progress = v.findViewById(R.id.progress);
        arrow = v.findViewById(R.id.expand_arrow);
//        arrow.animate().rotation(90f);
//        exp = v.findViewById(R.id.exp_filters);
        expand = v.findViewById(R.id.button_expand);
        expand.setClipToOutline(true);
        expand.setOnClickListener(view -> {
            toggleFiltersExpand();
//            expand.setText(expand(!exp.isExpanded()));
        });

        layout = v.findViewById(R.id.filters);
        layout.setVisibility(GONE);
        filterDateChips = v.findViewById(R.id.filter_date_chips);
        filterTypeChips = v.findViewById(R.id.filter_type_chips);
        filterTypeTitle = v.findViewById(R.id.filter_type_title);
        filters = new Filters();

        noResult = v.findViewById(R.id.no_results);
//        noResult.setSelected(true); // to prevent transparency touches

        tvCount = v.findViewById(R.id.tv_count);
        tvCount.setText(requireContext().getString(R.string.items, "0"));

        back = v.findViewById(R.id.btnBack);
        back.setClipToOutline(true);
        back.setOnClickListener(view -> requireActivity().onBackPressed());

        search = v.findViewById(R.id.search);
        search.setClipToOutline(true);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // if(jFileAdapter!=null) jFileAdapter.getFilter().filter(s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(jFileAdapter!=null) jFileAdapter.multiFilter(filters.setTextFilter(s.toString()));
                clear.setVisibility(s.length() == 0 ? GONE : View.VISIBLE);
                expandFilters(s.length() == 0);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        search.setOnFocusChangeListener((v1, hasFocus) -> {
            if (!hasFocus) hideKeyboard(instance);
        });

        clear = v.findViewById(R.id.btnClear);
        clear.setClipToOutline(true);
        clear.setOnClickListener(view -> {
            search.setText("");
            search.setHint(R.string.search);
        });

        rvFiles = v.findViewById(R.id.rv_files);
        rvFiles.setClipToOutline(true);
        rvFiles.setLayoutManager(new LinearLayoutManager(requireActivity()));
        rvFiles.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    hideKeyboard(recyclerView.getContext());
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        setFastScrollBar(rvFiles, position ->
                String.valueOf(jFileAdapter.jFileList.get(position).getName().charAt(0)));

        showKeyboard(requireActivity());
        search.requestFocus();

        new scanner().execute();

        return v;
    }

    public long getTimeFromNow(int type, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(type, amount);

        return calendar.getTime().getTime();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initDateFilters() {
        JChip today = new JChip(requireContext(), stringFrom(R.string.filter_today));
        today.setDateLimit(getTimeFromNow(Calendar.HOUR_OF_DAY, -24));

        JChip last3Days = new JChip(requireContext(), stringFrom(R.string.filter_three_days_ago));
        last3Days.setDateLimit(getTimeFromNow(Calendar.DAY_OF_MONTH, -3));

        JChip lastWeek = new JChip(requireContext(), stringFrom(R.string.filter_this_week));
        lastWeek.setDateLimit(getTimeFromNow(Calendar.DAY_OF_MONTH, -7));

        JChip lastMonth = new JChip(requireContext(), stringFrom(R.string.filter_this_month));
        lastMonth.setDateLimit(getTimeFromNow(Calendar.DAY_OF_MONTH, -30));

        ArrayList<JChip> jChips = new ArrayList<>();
        jChips.add(today);
        jChips.add(last3Days);
        jChips.add(lastWeek);
        jChips.add(lastMonth);

        for (JChip item : jChips) {
            filterDateChips.addView(item);
        }

        filterDateChips.setSingleSelection(true);
        filterDateChips.setOnCheckedChangeListener((group, checkedId) -> {

            JChip checkedJChip = getChip(group, checkedId);

            if (checkedJChip == null) {
                filters.clearDateFilter();
                jFileAdapter.multiFilter(filters);
//                Toast.makeText(requireContext(), "null | id:" + checkedId, Toast.LENGTH_SHORT).show();
                return;
            }

//            Toast.makeText(requireContext(), "checkedId:" + checkedId + " | itemCLicked:" + checkedJChip.getText(), Toast.LENGTH_SHORT).show();

            jFileAdapter.multiFilter(filters.setDateFilter(getChip(group, checkedId).getDateLimit()));

//            new Handler().post(() -> new DialogSort().compareRecentFile(jFileAdapter.jFileList));
        });
    }

    private void initTypeFilters() {
        JChip pictures = new JChip(requireContext(), stringFrom(R.string.pictures));
        pictures.setType(JFile.Type.IMAGE);

        JChip audio = new JChip(requireContext(), stringFrom(R.string.audio));
        audio.setType(JFile.Type.AUDIO);

        JChip video = new JChip(requireContext(), stringFrom(R.string.video));
        video.setType(JFile.Type.VIDEO);

        JChip apks = new JChip(requireContext(), stringFrom(R.string.installations));
        apks.setType(JFile.Type.APK);

        JChip documents = new JChip(requireContext(), stringFrom(R.string.documents));
        documents.setType(JFile.Type.DOCUMENT);

        ArrayList<JChip> jChips = new ArrayList<>();
        jChips.add(audio);
        jChips.add(video);
        jChips.add(pictures);
        jChips.add(documents);
        jChips.add(apks);

        for (JChip item : jChips) {
            filterTypeChips.addView(item);
        }

        filterTypeChips.setSingleSelection(true);
        filterTypeChips.setOnCheckedChangeListener((group, checkedId) -> {

            JChip checkedJChip = getChip(group, checkedId);

            if (checkedJChip == null) {
                filters.clearTypeFilter();
                jFileAdapter.multiFilter(filters);
//                Toast.makeText(requireContext(), "null | id:" + checkedId, Toast.LENGTH_SHORT).show();
                return;
            }

//            Toast.makeText(requireContext(), "checkedId:" + checkedId + " | itemCLicked:" + checkedJChip.getText(), Toast.LENGTH_SHORT).show();

            jFileAdapter.multiFilter(filters.setTypeFilter(getChip(group, checkedId).getType()));

            new Handler().post(() -> new DialogSort().compareRecentFile(jFileAdapter.jFileList));
        });
    }

    public JChip getChip(ChipGroup group, int ID) {
        for(int i = 0; i < group.getChildCount(); i++) {
            JChip jChip = (JChip) group.getChildAt(i);
            if (jChip.getId() == ID) return jChip;
        }
        return null;
    }

    public void setAdapter(int s) {

        ContentResolver contentResolver = instance.getContentResolver();

        Uri uri = MediaStore.Files.getContentUri(s==0 ? "external" : "internal");

        String sortOrder = MediaStore.Audio.Media.DATE_MODIFIED + " ASC";
        @SuppressLint("Recycle")
        Cursor cursor = contentResolver.query(uri, null, null, null, sortOrder);
        if (cursor != null && cursor.moveToLast()) {
            do {
                if (cancel) break;
                @SuppressLint("Range")
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Downloads.DATA));

                JFile jFile = new JFile(new File(String.valueOf(Uri.parse(data))), instance);
                objects.add(jFile);

            } while (cursor.moveToPrevious());
        }
    }

    @SuppressLint("StaticFieldLeak")
    class scanner extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (category.equals("folder")) progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            if (category.equals("search")) {
                setAdapter(0);
                setAdapter(1);
                if (cancel) cancel(true);
            } else if (category.equals("folder")) {
                folderChip = new JChip(requireContext(), folder.getName());
                folderChip.setOnCheckedChangeListener((compoundButton, checked) -> {
                    hideKeyboard(instance);
                    if (!checked) {
                        isFolder = false;
                        new searchScan().execute();
                    } else {
                        isFolder = true;
                        recreateList(jFiles);
                    }
                });

                jFiles = new ArrayList<>();
                iterateFolder(folder);
                if (cancel) cancel(true);
            }

            if (!category.equals("search") && !category.equals("folder")) {
                objects = new ArrayList<>(jFiles);

                if (!category.equals("downloads") && !category.equals("recent")) {
                    filterTypeChips.setVisibility(GONE);
                    filterTypeTitle.setVisibility(GONE);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            progress.setVisibility(GONE);
//            if (!isHintEnabled()) {

//            }
            jFileAdapter = new JFileAdapter(category.equals("folder") ? jFiles : objects, JFileAdapter.ViewType.ROW, SEARCH);
            jFileAdapter.multiFilter(filters.setTextFilter(String.valueOf(search.getText())));
            rvFiles.setAdapter(jFileAdapter);
            search.requestFocus();
            registerAfterInit();

            initDateFilters();
            initTypeFilters();
            expandFilters(true);

            if (folderChip != null) {
                folderChip.setChecked(true);
                ((LinearLayout) requireView().findViewById(R.id.folder_chip)).addView(folderChip);
            }
        }

//        public void cancel(boolean isCanceled, String nothing) {
//            super.cancel(true);
//        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void recreateList(ArrayList<JFile> arrayList) {

        jFileAdapter = null;
        if (arrayList.size()==0) arrayList = jFiles;
        jFileAdapter = new JFileAdapter(arrayList, JFileAdapter.ViewType.ROW, SEARCH);
        jFileAdapter.multiFilter(filters.setTextFilter(String.valueOf(search.getText())));
        rvFiles.setAdapter(jFileAdapter);

        registerAfterInit();
        search.requestFocus();
    }

    public void iterateFolder(File parent) {
        if (parent.listFiles() != null) {
            for (File file : Objects.requireNonNull(parent.listFiles())) {
                if (cancel) break; // about 3 second later for 150,000 iteration
                if (file.isDirectory()) iterateFolder(file);
                jFiles.add(new JFile(file, instance));
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    class searchScan extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setVisibility(View.VISIBLE); }
        @Override
        protected Void doInBackground(Void... arg0) {

            if (objects.size() != 0) return null;

            setAdapter(0);
            setAdapter(1);
            if (cancel) cancel(true);
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            recreateList(objects);
            progress.setVisibility(GONE); }
    }

    public void setAdapter(String type, Uri uri) {

        objects.clear();

        String sortOrder = MediaStore.Audio.Media.DATE_MODIFIED + " ASC";
        ContentResolver contentResolver = instance.getContentResolver();

        @SuppressLint("Recycle")
        Cursor cursor = contentResolver.query(uri, null, null, null, sortOrder);
        int i = 0;
        if (type.equals("recent")) {
            if (cursor != null && cursor.moveToLast()) {
                do {
                    @SuppressLint("Range")
                    String data = cursor.getString(cursor.getColumnIndex(MediaStore.Downloads.DATA));
                    File file = new File(String.valueOf(Uri.parse(data)));
                    if (filter(file)) {
                        i++;
                        objects.add(new JFile(new File(String.valueOf(Uri.parse(data))), instance));
                    }

                } while (cursor.moveToPrevious() && i <= 300);
            }
        }
    }

    public boolean filter(File file) {
        return  !file.isDirectory() && file.length() != 0 &&
                !file.getAbsolutePath().startsWith("//Android") &&
                !Objects.requireNonNull(file.getParentFile()).getPath().endsWith("WhatsApp/Databases") &&
                !file.getParentFile().getPath().endsWith("WhatsApp/Backups") &&
                isRecent(file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase());
    }

    public static boolean isRecent(String fileName) {
        switch (fileName) {
            case "": // folder
            case "m3u": // playlist file
            case "log": // logs...
            case "bak": // expressions of backup files
            case "bkup":
            case "backup":
            case "crypt1": // whatsapp backup files
            case "crypt12":
                return false;
            default: // else...
                return true;
        }

    }

    @Override
    public void onDestroy() {
        hideKeyboard(instance);
        super.onDestroy();
    }

    public String getExpendedText(boolean isExpanded){
        if (isExpanded) return getString(R.string.close);
        else return getString(R.string.filters);
    }

    public String stringFrom(int resId) {
        return instance.getString(resId);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
        if (jFileAdapter != null) jFileAdapter.notifyDataSetChanged();
    }

    public boolean isExpanded() {
        return layout.getVisibility() == VISIBLE;
    }

    public void toggleFiltersExpand() {
        expandFilters(!isExpanded());
    }

    public void expandFilters(boolean expand) {
        TransitionManager.beginDelayedTransition((ViewGroup) requireView(), new AutoTransition());

        layout.setVisibility(expand ? View.VISIBLE : GONE);
        arrow.animate().rotation(expand ? 90f : -90f);
    }

    public void registerAfterInit() {
        updateResultCount();

        jFileAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();

                updateResultCount();
            }
        });

        jFileAdapter.setSearchAdapterListener(new JFileAdapter.SearchAdapterListener() {
            @Override
            public void filterResultStarted() {
                progress.setVisibility(VISIBLE);
            }

            @Override
            public void filterResultFinished() {
                progress.setVisibility(GONE);
                noResult.setVisibility(jFileAdapter.getItemCount() == 0 ? View.VISIBLE : GONE);
            }
        });
    }

    public void updateResultCount() {
        new Handler().post(() ->
                tvCount.setText(requireContext().getString(R.string.items, String.valueOf(jFileAdapter.getItemCount()))));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        cancel = true;
    }
}