package com.example.files.view;

import static com.example.files.utils.MainActivityUtils.Storages.storageItems;

import android.annotation.SuppressLint;
import android.content.Context;

import com.example.files.R;
import com.example.files.models.JFile;
import com.example.files.models.StorageItem;
import com.google.android.material.chip.Chip;

@SuppressLint("ViewConstructor")
public class JChip extends Chip {
    long dateLimit;
    JFile.Type type;
    public JChip(Context context, String text) {
        super(context);

        setGravity(TEXT_ALIGNMENT_CENTER);
//        setEnsureMinTouchTargetSize(Statics.dpToPixels(40));

        setCheckable(true);
        setText(formatText(text));
//        setId(ID);
    }

    public long getDateLimit() {
        return dateLimit;
    }

    public void setDateLimit(long dateLimit) {
        this.dateLimit = dateLimit;
    }

    public JFile.Type getType() {
        return type;
    }

    public void setType(JFile.Type type) {
        this.type = type;
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

}
