package com.example.files.fragments;

import static com.example.files.MainActivity.instance;
import static com.example.files.Statics.dpToPixels;
import static com.example.files.Statics.hasNavigationBar;
import static com.example.files.utils.Animations.hide;
import static com.example.files.view.ViewUtils.refreshRecyclerPadding;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.files.database.DBHelper;
import com.example.files.R;
import com.example.files.utils.Animations;
import com.example.files.utils.CenterLayoutManager;
import com.example.files.utils.FavoritesAdapter;
import com.example.files.models.FavoriteItem;
import com.example.files.models.JFile;
import com.example.files.models.ViewHolder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class FavoritesFragment extends Fragment {

    CheckBox selectAll;
    ImageButton back;
    RecyclerView rvList;
    LinearLayout actionBar;
    TextView remove;
    SearchView search;

    DBHelper favsDb;
    FavoritesAdapter adapter;
    ArrayList<JFile> selected = new ArrayList<>();
    String chosen = "";

    public FavoritesFragment() {
        // Required empty public constructor
    }

    public FavoritesFragment(String chosen) {
        // Required empty public constructor
        this.chosen = chosen;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        favsDb = new DBHelper(requireActivity());

        actionBar = view.findViewById(R.id.bottom_action_bar);
        remove = view.findViewById(R.id.btn_remove);
        back = view.findViewById(R.id.btn_back);
        selectAll = view.findViewById(R.id.select_all);
        rvList = view.findViewById(R.id.rv_list);
        search = view.findViewById(R.id.search);
        rvList.setLayoutManager(new CenterLayoutManager(requireActivity()));

        if (hasNavigationBar()) actionBar.setPadding(dpToPixels(15),
                dpToPixels(20), dpToPixels(15), dpToPixels(25));
        else actionBar.setPadding(dpToPixels(15), dpToPixels(15),
                dpToPixels(15), dpToPixels(15));
        back.setClipToOutline(true);
        back.setOnClickListener(v -> requireActivity().onBackPressed());
        remove.setClipToOutline(true);
        remove.setOnClickListener(v -> {
            selected.sort(Comparator.comparingInt(JFile::getPosition).reversed());
            for (JFile jFile : selected) {
                for (FavoriteItem fav : favsDb.getAllPaths()) {
                    if (jFile.getPath().equals(fav.getPath())) {
                        favsDb.deletePath(fav.getId());
                        adapter.jFileList.remove(jFile.getPosition());
                        adapter.notifyItemRemoved(jFile.getPosition());
                        break;
                    }
                }
            }
            hide(actionBar);
            selected.clear();
            selectAll.setText(requireActivity().getString(R.string.items_chosen, String.valueOf(0)));
            new Handler().postDelayed(() ->
                    adapter.notifyDataSetChanged(), 2000);
        });

        selectAll.setOnClickListener(v -> {
            if (adapter != null) adapter.selectAll(selectAll.isChecked() && adapter.getItemCount() > 0);
        });

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);

                return false;
            }
        });

        new Handler().post(this::loadFavorites);

        return view;
    }

    public void loadFavorites() {
        ArrayList<JFile> objects = new ArrayList<>();
        for (FavoriteItem fav : favsDb.getAllPaths()) {
            objects.add(new JFile(fav, instance));
        }
        adapter = new FavoritesAdapter(objects, selected);
        adapter.setAdapterListener(new FavoritesAdapter.AdapterListener() {
            @Override
            public void onItemClick(ViewHolder holder, int position) {
                adapter.selected(position);

                if (selected.size() == 1 && adapter.jFileList.get(position).isSelected()) {
                    int layoutPosition = holder.getLayoutPosition();
                    if (layoutPosition > rvList.getChildCount()-3)
                        rvList.scrollToPosition(layoutPosition);
                }
            }

            @Override
            public void onSelectItemListener(int selectedSize) {
                selectAll.setText(requireActivity().getString(R.string.items_chosen, String.valueOf(selectedSize)));
                selectAll.setChecked(selectedSize == adapter.jFileList.size() && adapter.getItemCount() > 0);
                refreshRecyclerPadding(rvList, selectedSize != 0);
                if (selectedSize > 0) Animations.show(actionBar, () -> {});
                else hide(actionBar);
            }
        });

        rvList.setAdapter(adapter);
        if (!chosen.equals("")) {
            for (int position = 0; position < adapter.jFileList.size(); position++) {
                if (adapter.jFileList.get(position).getPath().equals(chosen)) {
                    adapter.selected(position);
                    Objects.requireNonNull((CenterLayoutManager) rvList.getLayoutManager())
                            .scrollToCenter(requireContext(), position);
                    break;
                }
            }
        }
    }

}