package com.crystal.settings.beerbong;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import com.crystal.settings.R;
import com.crystal.settings.SettingsPreferenceFragment;

public class Expand extends SettingsPreferenceFragment {

    private PreferenceCategory mExpandList;
    private Context mContext;

    public Expand() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        addPreferencesFromResource(R.xml.expand);

        PreferenceScreen prefSet = getPreferenceScreen();

        mExpandList = (PreferenceCategory) prefSet.findPreference("expand_list");

        updateList();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
    }

    private void updateList() {

        Applications.AppInfo[] items = Applications.getExpandList(mContext);

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
                    AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                    alert.setTitle(R.string.expand_alert_remove_application);

                    String title = (String) preference.getTitle();

                    String summary = mContext.getResources().getString(R.string.expand_remove_application,
                            new Object[] { title });

                    alert.setMessage(summary);

                    alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                            Applications.removeExpand(mContext, preference.getKey());
                            updateList();
                        }
                    });
                    alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                        }
                    });

                    alert.show();

                    return false;
                }
            });
            mExpandList.addPreference(pref);
        }
    }
}
