package com.example.files.fragments;

import static com.example.files.MainActivity.actionBarVisibility;
import static com.example.files.MainActivity.enableTextButton;
import static com.example.files.MainActivity.instance;
import static com.example.files.MainActivity.sp;
import static com.example.files.MainActivity.textBtnState;
import static com.example.files.Statics.CATEGORY_VIEW_TYPE;
import static com.example.files.Statics.FOLDER_VIEW_TYPE;
import static com.example.files.Statics.OpenSearch;
import static com.example.files.Statics.TAG_FOLDER;
import static com.example.files.Statics.actions;
import static com.example.files.Statics.copyMode;
import static com.example.files.Statics.currentFragment;
import static com.example.files.Statics.favorites;
import static com.example.files.Statics.folder;
import static com.example.files.Statics.isSingleLine;
import static com.example.files.Statics.multiSelected;
import static com.example.files.Statics.openFileWith;
import static com.example.files.Statics.order;
import static com.example.files.Statics.prepareAction;
import static com.example.files.Statics.removeCurrentAction;
import static com.example.files.Statics.selectedJFiles;
import static com.example.files.Statics.setFastScrollBar;
import static com.example.files.Statics.showFileSize;
import static com.example.files.Statics.showHiddenFiles;
import static com.example.files.Statics.sort;
import static com.example.files.Statics.takeCardUriPermission;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.files.JFileAdapter;
import com.example.files.R;
import com.example.files.Statics;
import com.example.files.databinding.FragmentFilesBinding;
import com.example.files.databinding.PathNavigatorBinding;
import com.example.files.databinding.ToolbarFilesBinding;
import com.example.files.models.FavoriteItem;
import com.example.files.view.ViewUtils;
import com.example.files.actions.DialogBase;
import com.example.files.actions.DialogCopy;
import com.example.files.actions.DialogCreateNew;
import com.example.files.actions.DialogDelete;
import com.example.files.actions.DialogDetails;
import com.example.files.actions.DialogMove;
import com.example.files.actions.DialogRename;
import com.example.files.actions.DialogSort;
import com.example.files.actions.Share;
import com.example.files.activities.SettingsActivity;
import com.example.files.listeners.OnMultiSelectedChange;
import com.example.files.models.JFile;
import com.example.files.models.ViewHolder;
import com.michaelflisar.dragselectrecyclerview.DragSelectTouchListener;
import com.michaelflisar.dragselectrecyclerview.DragSelectionProcessor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class FragmentBase extends Fragment {

    FragmentFilesBinding binding;
    ToolbarFilesBinding toolbar;
    PathNavigatorBinding pathNavigator;

    public enum FragmentType {MAIN, FILES, CATEGORY, RECENT, ARCHIVE, SEARCH}

    FragmentType fragmentType;

    public FragmentBase(FragmentType fragmentType) {
        this.fragmentType = fragmentType;
    }

    protected abstract void refresh();

    protected abstract void loadList();

    public abstract boolean notVisible();

    DragSelectionProcessor.Mode mode = DragSelectionProcessor.Mode.Simple;
    DragSelectionProcessor dragSelectionProcessor;
    DragSelectTouchListener.OnDragSelectListener onDragSelectionListener;
    DragSelectTouchListener dragSelectTouchListener;
    @SuppressLint("StaticFieldLeak")
    JFileAdapter jFileAdapter;
    ArrayList<JFile> objects;
    Drawable star = null;
    public File parent = new File(""); // TODO make null
    String itemPath;
    boolean running, selected, isArchive, canceled;
    public boolean bool = false;
    int position = 0, fragmentPosition, zipPosition;
    long lastRefresh;

    // ZippedFragment
    Handler refresh;
    JFile parentJFile;
    int sumLoad = 0;
    File zip;
    
    // Category
    Uri uri = null;
    String category, type = null;
    StaggeredGridLayoutManager staggeredGridLayoutManager;

    @SuppressLint("UseCompatLoadingForDrawables")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFilesBinding.inflate(inflater, container, false);
        toolbar = binding.toolbarFiles;
        pathNavigator = toolbar.pathNavigator;

        toolbar.layout.setClipToOutline(true);
        toolbar.selectAll.setOnClickListener(view -> jFileAdapter.selectAll(toolbar.selectAll.isChecked()));
        binding.emptyFolder.setSelected(true); // to prevent transparency touches
        toolbar.favorite.setClipToOutline(true);
        star = requireActivity().getDrawable(R.drawable.star);
        toolbar.favorite.setOnClickListener(view -> {
            if (isFavoriteChecked()) {
                for (JFile jFile : selectedJFiles) {
                    for (FavoriteItem fav : favorites.getAllPaths()) {
                        if (jFile.getPath().equals(fav.getPath())) {
                            favorites.deletePath(fav.getId());
                            break;
                        }
                    }
                }
            } else for (JFile jFile : selectedJFiles) favorites.addToFavorites(jFile);

            setFavoriteChecked(favorites.allExist(selectedJFiles));
//            instance.eventListener.onMultiSelectedChange(false);
        });
        toolbar.search.setClipToOutline(true);
        if (isType(FragmentType.CATEGORY) || isType(FragmentType.RECENT))
            toolbar.search.setOnClickListener(view -> {
                if (jFileAdapter != null) OpenSearch(category, jFileAdapter.jFileList);
            });
        else toolbar.search.setOnClickListener(v1 -> OpenSearch(TAG_FOLDER));
        binding.rvFiles.setLayoutManager(new LinearLayoutManager(requireActivity()));
        setFastScrollBar(binding.rvFiles, (view, position) ->
                String.valueOf(jFileAdapter.jFileList.get(position).getName().charAt(0)));

        setViewType();
        // has to be before the other methods
        toolbarSetup();

        onCreateView(binding.getRoot());

        if (isFilesType() || isType(FragmentType.ARCHIVE)) setPathsList();

        loadList();

        new Handler().postDelayed(() -> fragmentPosition = instance.getSupportFragmentManager()
                .getBackStackEntryCount(), 1000);

        sp.registerOnSharedPreferenceChangeListener((sharedPreferences, s) -> {
            showHiddenFiles = sp.getBoolean("SHOW_HIDDEN_FILES", false);
            showFileSize = sp.getBoolean("SHOW_FILE_SIZE", false);
            isSingleLine = !sp.getBoolean("SHOW_FULL_FILE_NAME", false);
            sort = sp.getInt("SORT", 0);
            order = sp.getInt("ORDER", 0);
        });

        return binding.getRoot();
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "NotifyDataSetChanged"})
    private void setViewType() {
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(
                calculateNoOfColumns(requireContext()), //number of grid columns
                GridLayoutManager.VERTICAL);

        toolbar.layout.setOnClickListener(v1 -> {
            if (jFileAdapter == null) return;
            JFileAdapter tempAdapter = jFileAdapter;
            binding.rvFiles.setAdapter(null);
            binding.rvFiles.setLayoutManager(null);
            jFileAdapter = null;
            if (getViewType() == JFileAdapter.ViewType.GRID) {
                toolbar.layout.setImageDrawable(requireActivity().getDrawable(R.drawable.view_list));
                binding.rvFiles.setLayoutManager(new LinearLayoutManager(getContext()));
                setViewType(JFileAdapter.ViewType.ROW);
            } else {
                toolbar.layout.setImageDrawable(requireActivity().getDrawable(R.drawable.view_grid));
                binding.rvFiles.setLayoutManager(staggeredGridLayoutManager);
                setViewType(JFileAdapter.ViewType.GRID);
            }
            jFileAdapter = tempAdapter;
            jFileAdapter.viewType = getViewType();
            binding.rvFiles.setAdapter(jFileAdapter);
            setListeners();
            setListListener();
//            jFileAdapter = new JFileAdapter(objects, CATEGORY_VIEW_TYPE, CATEGORY);
//            rvFiles.setAdapter(jFileAdapter);
        });

        if (getViewType() == JFileAdapter.ViewType.ROW) {
            toolbar.layout.setImageDrawable(requireActivity().getDrawable(R.drawable.view_list));
            binding.rvFiles.setLayoutManager(new LinearLayoutManager(requireActivity()));
        } else {
            toolbar.layout.setImageDrawable(requireActivity().getDrawable(R.drawable.view_grid));
            binding.rvFiles.setLayoutManager(staggeredGridLayoutManager);
        }

        ViewUtils.refreshRecyclerPadding(binding.rvFiles, !selectedJFiles.isEmpty());
    }

    JFileAdapter.ViewType getViewType() {
        return isFilesType() || isType(FragmentType.ARCHIVE) ? FOLDER_VIEW_TYPE : CATEGORY_VIEW_TYPE;
    }

    void setViewType(JFileAdapter.ViewType viewType) {
        if (isFilesType() || isType(FragmentType.ARCHIVE)) FOLDER_VIEW_TYPE = viewType;
        else CATEGORY_VIEW_TYPE = viewType;
    }

    public abstract void onCreateView(View v);

    void toolbarSetup() {
        // Home button
        pathNavigator.home.setClipToOutline(true);
        pathNavigator.home.setOnClickListener(view -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
            for (Fragment ignored : requireActivity().getSupportFragmentManager().getFragments())
                requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Back button

        // Menu
        toolbar.toolbar.setNavigationOnClickListener(view -> requireActivity().onBackPressed());
        toolbar.toolbar.inflateMenu(R.menu.menu);
        toolbar.toolbar.getMenu().findItem(R.id.add).setVisible(isFilesType());
        toolbar.toolbar.getMenu().findItem(R.id.open_with).setVisible(false);
        toolbar.toolbar.getMenu().findItem(R.id.rename).setVisible(false);
        toolbar.toolbar.getMenu().findItem(R.id.copy).setVisible(false);
        toolbar.toolbar.getMenu().findItem(R.id.move).setVisible(false);
        toolbar.toolbar.getMenu().findItem(R.id.delete).setVisible(false);
        toolbar.toolbar.getMenu().findItem(R.id.details).setVisible(false);
        toolbar.toolbar.getMenu().findItem(R.id.add_fav).setVisible(false);
        toolbar.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.rename) {
                new DialogRename(instance);
            }
            if (item.getItemId() == R.id.move){
                prepareAction(new DialogMove(selectedJFiles));
            }
            if (item.getItemId() == R.id.copy){
                prepareAction(new DialogCopy(selectedJFiles));
            }
            if (item.getItemId() == R.id.delete){
                prepareAction(new DialogDelete(selectedJFiles));
            }
            if (item.getItemId() == R.id.add) {
                new DialogCreateNew(instance, false);
            }
            if (item.getItemId() == R.id.add_fav) {
                for (JFile jFile : selectedJFiles) favorites.addToFavorites(jFile);
                instance.eventListener.onMultiSelectedChange(false);
            }
            if (item.getItemId() == R.id.open_with) {
                openFileWith(selectedJFiles.get(0), requireActivity());
            }
            if(item.getItemId() == R.id.share) {
                new Share(instance);
            }
            if(item.getItemId() == R.id.details){
                new DialogDetails(instance, false);
            }
            if(item.getItemId() == R.id.sort){
                new DialogSort(jFileAdapter).show();
            }
            if (item.getItemId() == R.id.settings){
                if (multiSelected) instance.eventListener.onMultiSelectedChange(false);
                Intent settings = new Intent(requireActivity(), SettingsActivity.class);
                startActivity(settings);
            }
            if(item.getItemId() == R.id.refresh){
                running = false;
                refresh();
            }
            if (item.getItemId() == R.id.uri) {
                takeCardUriPermission(this.parent);
            }
            return true;
        });
    }

    protected void setListeners() {
        onDragSelectionListener = (start, end, isSelected) -> {
            // update your selection
            // range is inclusive start/end positions
        };

        dragSelectTouchListener = new DragSelectTouchListener()
                // check region OnDragSelectListener for more info
                .withSelectListener(onDragSelectionListener)
                // following is all optional
                .withMaxScrollDistance(16)               // default: 16; 	    defines the speed of the auto scrolling
                .withTopOffset(0)                // default: 0; 		set an offset for the touch region on top of the RecyclerView
                .withBottomOffset(0)             // default: 0; 		set an offset for the touch region on bottom of the RecyclerView
                .withScrollAboveTopRegion(true)  // default: true; 	    enable auto scrolling, even if the finger is moved above the top region
                .withScrollBelowTopRegion(true)  // default: true; 	    enable auto scrolling, even if the finger is moved below the top region
                .withDebug(false)                // default: false;
        ;

        jFileAdapter.setAdapterListener(new JFileAdapter.AdapterListener() {

            @Override
            public void onItemClick(ViewHolder holder, int position) {
                jFileAdapter.selected(position);

                if (selectedJFiles.size() == 1 && jFileAdapter.jFileList.get(position).isSelected()) {
                    int layoutPosition = holder.getLayoutPosition();
                    if (layoutPosition > binding.rvFiles.getChildCount()-3)
                        binding.rvFiles.scrollToPosition(layoutPosition);
                }
            }

            @Override
            public void onItemLongClick(ViewHolder holder, int position) {
                dragSelectTouchListener.startDragSelection(position);

                if (selectedJFiles.size() == 1 && jFileAdapter.jFileList.get(position).isSelected()) {
                    int layoutPosition = holder.getLayoutPosition();
                    if (layoutPosition > binding.rvFiles.getChildCount()-3)
                        binding.rvFiles.scrollToPosition(layoutPosition);
                }
            }

            @Override
            public void onSelectItemListener(int selectedSize) {
//                Toast.makeText(instance, "fragment " + currentFragment.parent.getName(), Toast.LENGTH_SHORT).show();
                toolbar.toolbar.getMenu().findItem(R.id.rename).setVisible(selectedSize == 1);
                toolbar.toolbar.getMenu().findItem(R.id.open_with).setVisible(
                        selectedSize == 1 && !selectedJFiles.get(0).isDirectory());
                toolbar.toolbar.getMenu().findItem(R.id.add_fav).setVisible(selectedSize > 0);
                toolbar.toolbar.getMenu().findItem(R.id.add_fav).setTitle(requireActivity()
                        .getString(R.string.add_to_favorites));
                toolbar.toolbar.getMenu().findItem(R.id.share).setVisible(selectedSize == 0 && !multiSelected);
                toolbar.selectAll.setChecked(selectedSize == jFileAdapter.getItemCount()
                        && jFileAdapter.getItemCount() > 0);
                toolbar.selectAll.setText(requireActivity().getString(R.string.items_chosen, String.valueOf(selectedSize)));
                setFavoriteChecked(favorites.allExist(selectedJFiles));
                toolbar.favorite.setVisibility(selectedSize == 0 ? View.GONE : View.VISIBLE);
                binding.folderName.setText(requireActivity().getString(R.string.items_chosen, String.valueOf(selectedSize)));
                if (!copyMode) actionBarVisibility(selectedSize > 0 ? View.VISIBLE : View.GONE);
                else actionBarVisibility(View.VISIBLE);
                if (selectedSize != 0) refreshRecyclerPadding(true);
            }
        });
        dragSelectionProcessor = new DragSelectionProcessor(new DragSelectionProcessor.ISelectionHandler() {
            @Override
            public HashSet<Integer> getSelection() {
                return null;
            }

            @Override
            public boolean isSelected(int index) {
                return jFileAdapter.jFileList.get(index).isSelected();
            }

            @Override
            public void updateSelection(int start, int end, boolean isSelected, boolean calledFromOnStart) {
                jFileAdapter.selectRange(start, end);
            }
        })
                .withMode(mode);
        dragSelectTouchListener = new DragSelectTouchListener()
                .withSelectListener(dragSelectionProcessor);
        binding.rvFiles.addOnItemTouchListener(dragSelectTouchListener);
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.RIGHT
        ) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (copyMode) {
                    removeCurrentAction();
//                    actionBarVisibility(View.GONE);
//                    copyMode = false;
//                    cleanCache(instance);
                }
                int position = viewHolder.getAbsoluteAdapterPosition();
                selectedJFiles.add(jFileAdapter.jFileList.get(position));
                prepareAction(new DialogCopy(selectedJFiles));
            }

            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    int iconMargin = Statics.dpToPixels(16);//(int) getResources().getDimension(R.dimen.ic_margin);
                    int iconSize = Statics.dpToPixels(24);//(int) getResources().getDimension(R.dimen.ic_size);

                    Drawable icon = ContextCompat.getDrawable(requireContext(), R.drawable.action_copy);
                    ColorDrawable background = new ColorDrawable(Color.BLUE);

                    int itemViewHeight = viewHolder.itemView.getBottom() - viewHolder.itemView.getTop();

                    background.setBounds(viewHolder.itemView.getRight() + (int) dX, viewHolder.itemView.getTop(), viewHolder.itemView.getRight(), viewHolder.itemView.getBottom());
                    background.draw(c);

                    int iconTop = viewHolder.itemView.getTop() + (itemViewHeight - iconSize) / 2;
                    int iconMarginVertical = (itemViewHeight - iconSize) / 2;
                    int iconLeft = viewHolder.itemView.getRight() - iconMargin - iconSize;
                    int iconRight = viewHolder.itemView.getRight() - iconMargin;
                    int iconBottom = iconTop + iconSize;

                    assert icon != null;
                    icon.setBounds(iconLeft, iconTop + iconMarginVertical, iconRight, iconBottom - iconMarginVertical);
                    icon.draw(c);
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.rvFiles);
    }

    public void refreshRecyclerPadding(boolean addSpace){
        if (binding != null) ViewUtils.refreshRecyclerPadding(binding.rvFiles, addSpace);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void setFavoriteChecked(boolean checked) {
        toolbar.favorite.setImageDrawable(checked ? star : requireActivity().getDrawable(R.drawable.star_outline));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public boolean isFavoriteChecked() {
        return toolbar.favorite.getDrawable() == star;
    }

    @SuppressLint({"ResourceType", "UseCompatLoadingForDrawables"})
    protected void setPathsList() {

        ToolbarPathList.setPathList(pathNavigator.hlsLL, pathNavigator.hScrollView, pathNavigator.tvPath, parent, isArchive);
        binding.folderName.setText(pathNavigator.tvPath.getText());
    }

    protected void setListListener() {
        instance.addMultiSelectedChangeListener(new OnMultiSelectedChange() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onMultiSelectedChange(boolean multiSelected) {

                Log.d("##### ff.onMultiSelected #####", ""+multiSelected);

                Statics.multiSelected = multiSelected;

                if (!multiSelected) {
                    selectedJFiles.clear();
                    toolbar.toolbar.setNavigationIcon(R.drawable.back);
                    toolbar.selectAll.setVisibility(View.GONE);
                    jFileAdapter.deselectAll();
                    toolbar.toolbar.getMenu().findItem(R.id.share).setVisible(true);
                    binding.folderName.setText(pathNavigator.tvPath.getText());
                } else if (!copyMode) {
                    selectedJFiles.clear();
                    toolbar.toolbar.setNavigationIcon(null);
                    toolbar.selectAll.setVisibility(View.VISIBLE);
                    jFileAdapter.deselectAll();
                } else {
                    Statics.multiSelected = false; // TODO be careful!
                }

                jFileAdapter.notifyDataSetChanged();

            }


            @Override
            public void onRefresh() {
                refresh();
            }

            @Override
            public void onRefreshActionsList() {
                refreshActionsList();
            }
        });
    }

    public void applySettings(){
        new Handler().post(() -> {
            if (multiSelected) instance.eventListener.onMultiSelectedChange(false);
            refresh();
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
                ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

    }

    public void animate() {
        binding.rvLayout.animate();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (notVisible()) return;
        currentFragment = this;

        if (isFilesType()) {
            folder = parent;
            if (!parent.exists()) {
                new Handler().post(() -> instance.onBackPressed());
                return;
            }
        }
        setListListener();
        new Handler().post(this::refreshActionsList);
        new Handler().postDelayed(() -> textBtnState(enableTextButton()),100);
        if (fragmentPosition == requireActivity().getSupportFragmentManager().getBackStackEntryCount()-1) {
            if (bool) {
                setListeners();
                refresh();
            }

        }
        bool = true;
        assert this.getTag() != null;
        Log.d("##### onResume() #####", this.getTag());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (isFilesType()) {
            folder = parent;
            if (!parent.exists()) {
                new Handler().post(() -> instance.onBackPressed());
                return;
            }
        }

        textBtnState(enableTextButton());
    }

    public void refreshGrid() {
        if (staggeredGridLayoutManager != null && isAdded()) {
            staggeredGridLayoutManager.setSpanCount(calculateNoOfColumns(requireContext()));
        }
    }

    public boolean isType(FragmentType TYPE) {
        return fragmentType == TYPE;
    }

    public boolean isFilesType() {
        return fragmentType == FragmentType.FILES;
    }

    protected void refreshActionsList() {

        toolbar.hsvActions.post(() -> {
            toolbar.hsvActions.removeAllViews();
            if (actions.isEmpty()) return;

            for (DialogBase action : actions) {
                if (!action.operating) continue;

                LayoutInflater secInflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                @SuppressLint("InflateParams") View view = secInflater.inflate(R.layout.item_action_in_progress, null);
                FrameLayout ll = view.findViewById(R.id.action_view);
                ll.setClipToOutline(true);
                TextView id = view.findViewById(R.id.id);
                TextView type = view.findViewById(R.id.action_type);
                ImageView finished = view.findViewById(R.id.finished);
                id.setText(String.valueOf(action.getDialogID()+1));
                type.setText(action.resTitle);
                view.setOnClickListener(v -> action.reshow());
                toolbar.hsvActions.addView(view);
                action.addEventListener((result) ->
                        toolbar.hsvActions.post(() -> {
                            if (result != DialogBase.Result.SUCCESS) {
                                toolbar.hsvActions.removeView(view);
                                return;
                            }
                            AnimatedVectorDrawable finishAvd = getAnimatedVectorDrawable(finished, view);
                            finished.setVisibility(View.VISIBLE);
                            finishAvd.start();
                        }));
            }
        });
    }

    private @NonNull AnimatedVectorDrawable getAnimatedVectorDrawable(ImageView finished, View view) {
        AnimatedVectorDrawable finishAvd = (AnimatedVectorDrawable) finished.getDrawable();
        finishAvd.registerAnimationCallback(new Animatable2.AnimationCallback() {
            @Override
            public void onAnimationEnd(Drawable drawable) {
                super.onAnimationEnd(drawable);
                toolbar.hsvActions.removeView(view);
            }
        });
        return finishAvd;
    }

    protected void triggerSizeCounting() {
        trigger(0);
    }

    protected void trigger(int position) {
        if (position >= jFileAdapter.getItemCount()) return;

        int end = Math.min(position + 3, jFileAdapter.getItemCount());

        AtomicInteger pending = new AtomicInteger(0);

        for (int i = position; i < end; i++) {
            JFile file = jFileAdapter.jFileList.get(i);

            if (!file.isDirectory()) continue;
            if (file.isSizeReady()) continue;

            pending.incrementAndGet();

            file.setSizeLoadListener(size -> {
                if (pending.decrementAndGet() == 0) {
                    onBatchFinished(end);
                }
            });

            file.loadSizeIfNeeded();
        }

        if (pending.get() == 0) {
            onBatchFinished(end);
        }
    }

    private void onBatchFinished(int nextPosition) {
        if (!isVisible()) return;
        if (sort == 1) {
            jFileAdapter.onSizeBatchUpdated();
        }

        new Handler(Looper.getMainLooper())
                .postDelayed(() -> trigger(nextPosition), 50);
    }

    public void select(String itemPath) {
        this.selected = true;
        this.itemPath = itemPath;
    }

    public void highlightSelectedItem(boolean select) {
        position = 0;
        for (JFile jFile : jFileAdapter.jFileList) {
            if (jFile.getPath().equals(itemPath)) {
                Objects.requireNonNull(binding.rvFiles.getLayoutManager()).scrollToPosition(position);
                if (select) {
                    instance.eventListener.onMultiSelectedChange(true);
                    jFileAdapter.selected(position);
                    break;
                } else jFileAdapter.jFileList.get(position).setSelected(true);
                int finalPosition = position;
                new Handler().postDelayed(() -> {
                    if (multiSelected) jFileAdapter.selected(finalPosition);
                    jFileAdapter.jFileList.get(finalPosition).setSelected(false);
                    selected = false;
                }, 1000);
                break;
            }
            position++;
        }
    }

    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int scalingFactor = 90; // You can vary the value held by the scalingFactor
        // variable. The smaller it is the more no. of columns you can display, and the
        // larger the value the less no. of columns will be calculated. It is the scaling
        // factor to tweak to your needs.
        int columnCount = (int) (dpWidth / scalingFactor);
        return (Math.max(columnCount, 3)); // if column no. is less than 3, we still display 3 columns
    }

}

