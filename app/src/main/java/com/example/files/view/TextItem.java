package com.example.files.view;

import static com.example.files.MainActivity.instance;
import static com.example.files.utils.MainActivityUtils.Storages.storageItems;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.files.R;
import com.example.files.models.StorageItem;

@SuppressLint("ViewConstructor")
public class TextItem extends LinearLayout {

    TextView textView;
    int itemId;

    @SuppressLint("InflateParams")
    public TextItem(String text, int itemId) {
        super(instance);
        inflate(instance, R.layout.item_text, this);
        this.textView = findViewById(R.id.btnPath);
        this.textView.setClipToOutline(true);
        this.textView.setText(formatText(text));
        this.itemId = itemId;
        setEnabled(false);
    }

    @SuppressLint({"StringFormatMatches", "ResourceType"})
    private String formatText(String text) {
        if (text.equals("0")) return getContext().getString(R.string.internal_storage);
        else if (isExternalStorage(text)) {
            int storageNumber = 0;
            for (StorageItem si : storageItems) {
                if (si.getFile().getName().equals(text)) {
                    storageNumber = si.getItemId()-1;
                    break;
                }
            }
            return getContext().getString(R.string.external_storage, storageNumber);
        }
        return text;
    }

    private boolean isExternalStorage(String text) {
        for (StorageItem si : storageItems) {
            if (text.equals(si.getFile().getName()) && !si.isShortcut()) return true;
        }
        return false;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        textView.setOnClickListener(onClickListener);
    }

    public String getText() {
        return textView.getText().toString();
    }

    public void setEnabled(boolean enabled) {
        textView.setSelected(enabled);
    }

    public boolean isEnabled() {
        return textView.isSelected();
    }

    public int getItemId() {
        return itemId;
    }
}
