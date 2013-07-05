package com.crystal.settings.beerbong;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import com.crystal.settings.R;
import com.crystal.settings.SettingsPreferenceFragment;

public class ExpandListPreference extends SettingsPreferenceFragment {

    private PreferenceCategory mExpandList;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();

        addPreferencesFromResource(R.xml.expand_list);

        final PreferenceScreen prefSet = getPreferenceScreen();

        mExpandList = (PreferenceCategory) prefSet.findPreference("expand_list");

        Applications.AppInfo[] items = Applications.getApplicationList(mContext);

        mExpandList.removeAll();

        for (int i = 0; i < items.length; i++) {
            Preference pref = new Preference(mContext);
            Applications.AppInfo bAppInfo = items[i];

            pref.setKey(bAppInfo.pack);
            pref.setTitle(bAppInfo.name);
            pref.setIcon(bAppInfo.icon);
            pref.setLayoutResource(R.layout.simple_preference);

            pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                public boolean onPreferenceClick(final Preference preference) {
                    Applications.addExpand(mContext, preference.getKey());
                    getActivity().getFragmentManager().popBackStackImmediate();
                    return false;
                }
            });
            mExpandList.addPreference(pref);
        }

    }

}
