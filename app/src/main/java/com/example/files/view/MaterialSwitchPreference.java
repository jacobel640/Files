package com.example.files.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreference;

public class MaterialSwitchPreference extends SwitchPreference {

    /**
     * Construct a new SwitchPreference with the given style options.
     *
     * @param context  The Context that will style this preference
     * @param attrs    Style attributes that differ from the default
     * @param defStyle Theme attribute defining the default style options
     */
    public MaterialSwitchPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Construct a new SwitchPreference with the given style options.
     *
     * @param context The Context that will style this preference
     * @param attrs   Style attributes that differ from the default
     */
    public MaterialSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Construct a new SwitchPreference with default style options.
     *
     * @param context The Context that will style this preference
     */
    public MaterialSwitchPreference(Context context) {
        super(context, null);
    }

    public MaterialSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private boolean value;

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        CompoundButton widget = (CompoundButton) holder.findViewById(android.R.id.switch_widget);
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
            if (childView instanceof CompoundButton) {
                final CompoundButton switchView = (CompoundButton) childView;
                switchView.setOnCheckedChangeListener(null);
                return;
            } else if (childView instanceof ViewGroup) {
                ViewGroup childGroup = (ViewGroup) childView;
                clearListenerInViewGroup(childGroup);
            }
        }
    }

}
