package com.crystal.settings.widgets;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.crystal.settings.fragments.QuickToggles;
import com.crystal.settings.R;

public class CustomTogglePref extends Preference {

    private QuickToggles mParent;

    public CustomTogglePref(Context context) {
        super(context);
        setLayoutResource(R.layout.custom_toggle_pref);
    }
    public CustomTogglePref(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.custom_toggle_pref);
    }

    public void setParent(QuickToggles parent){
        mParent = parent;
    }

    @Override
    public void onBindView (View view) {
        super.onBindView(view);
        LinearLayout ll = (LinearLayout) view.findViewById(R.id.widget_frame);
        mParent.setupToggleViews(ll);
    }
}
