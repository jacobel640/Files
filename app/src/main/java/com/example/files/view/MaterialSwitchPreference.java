package com.example.files.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.materialswitch.MaterialSwitch;

public class MaterialSwitchPreference extends SwitchPreferenceCompat {

    public MaterialSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private boolean value;

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        MaterialSwitch widget = (MaterialSwitch) holder.findViewById(android.R.id.switch_widget);
        if (widget == null) return; // no custom widget provided
        // Clean listener before invoke SwitchPreference.onBindView
        ViewGroup viewGroup = (ViewGroup) holder.itemView;
        clearListenerInViewGroup(viewGroup);
        super.onBindViewHolder(holder);
        // Set initial value and main check-listener
        widget.setChecked(value);
        widget.setOnCheckedChangeListener((buttonView, isChecked) -> setChecked(isChecked));
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        value = checked;
    }

    /**
     * Clear listener in Switch for specify ViewGroup.
     *
     * @param viewGroup The ViewGroup that will need to clear the listener.
     */
    private void clearListenerInViewGroup(ViewGroup viewGroup) {
        if (null == viewGroup) {
            return;
        }

        int count = viewGroup.getChildCount();
        for (int n = 0; n < count; ++n) {
            View childView = viewGroup.getChildAt(n);
            if (childView instanceof MaterialSwitch switchView) {
                switchView.setOnCheckedChangeListener(null);
                return;
            } else if (childView instanceof ViewGroup childGroup) {
                clearListenerInViewGroup(childGroup);
            }
        }
    }

}
