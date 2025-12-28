package com.example.files.view;

import static com.example.files.utils.MainActivityUtils.Storages.storageItems;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.files.R;
import com.example.files.models.StorageItem;

@SuppressLint("ViewConstructor")
public class ItemText extends androidx.appcompat.widget.AppCompatTextView {

    int itemId;

    @SuppressLint({"UseCompatLoadingForDrawables", "ResourceType"})
    public ItemText(@NonNull Context context, String text, int itemId) {
        super(context);
        inflate(context, R.layout.item_text, null);
        this.itemId = itemId;
        setText(formatText(text));
        setEnabled(false);
    }

    public int getItemId() {
        return itemId;
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled();
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
            if (text.equals(si.getFile().getName())) return true;
        }
        return false;
    }

}
