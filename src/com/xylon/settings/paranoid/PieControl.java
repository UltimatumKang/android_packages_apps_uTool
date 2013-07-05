
package com.crystal.settings.paranoid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SwitchPreference;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.Spannable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.crystal.settings.R;
import com.crystal.settings.SettingsPreferenceFragment;
import com.crystal.settings.Utils;
import com.crystal.settings.util.Helpers;
import com.crystal.settings.widgets.SeekBarPreference;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class PieControl extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "Paranoid Android PIE";

    private static final String PIE_CONTROLS = "pie_controls";
    private static final String PIE_GRAVITY = "pie_gravity";
    private static final String PIE_MODE = "pie_mode";
    private static final String PIE_SIZE = "pie_size";
    private static final String PIE_TRIGGER = "pie_trigger";
    private static final String PIE_ANGLE = "pie_angle";
    private static final String PIE_GAP = "pie_gap";
    private static final String PIE_LASTAPP = "pie_lastapp";
    private static final String PIE_MENU = "pie_menu";
    private static final String PIE_SEARCH = "pie_search";
    private static final String PIE_CENTER = "pie_center";
    private static final String PIE_STICK = "pie_stick";

    SwitchPreference mPieControls;
    ListPreference mPieMode;
    ListPreference mPieGravity;
    ListPreference mPieAngle;
    ListPreference mPieTrigger;
    ListPreference mPieGap;
    CheckBoxPreference mPieMenu;
    CheckBoxPreference mPieLastApp;
    CheckBoxPreference mPieSearch;
    CheckBoxPreference mPieCenter;
    CheckBoxPreference mPieStick;
    SeekBarPreference mPieSize;

    private static final float PIE_SIZE_MIN = 0.7f;
    private static final float PIE_SIZE_MAX = 1.4f;
    private static final float PIE_SIZE_DEFAULT = 1.0f;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pa_pie_control);

        PreferenceScreen prefSet = getPreferenceScreen();

        mPieControls = (SwitchPreference) findPreference(PIE_CONTROLS);
        mPieControls.setChecked((Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_CONTROLS, 0) == 1));
        mPieControls.setOnPreferenceChangeListener(this);

        mPieGravity = (ListPreference) prefSet.findPreference(PIE_GRAVITY);
        int pieGravity = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_GRAVITY, 3);
        mPieGravity.setValue(String.valueOf(pieGravity));
        mPieGravity.setOnPreferenceChangeListener(this);

        mPieMode = (ListPreference) prefSet.findPreference(PIE_MODE);
        int pieMode = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_MODE, 2);
        mPieMode.setValue(String.valueOf(pieMode));
        mPieMode.setOnPreferenceChangeListener(this);

        mPieTrigger = (ListPreference) prefSet.findPreference(PIE_TRIGGER);
        try {
            float pieTrigger = Settings.System.getFloat(mContext.getContentResolver(),
                    Settings.System.PIE_TRIGGER);
            mPieTrigger.setValue(String.valueOf(pieTrigger));
        } catch(Settings.SettingNotFoundException ex) {
            // So what
        }
        mPieTrigger.setOnPreferenceChangeListener(this);

        mPieSize = (SeekBarPreference) prefSet.findPreference(PIE_SIZE);
        mPieSize.setOnPreferenceChangeListener(this);

        mPieGap = (ListPreference) prefSet.findPreference(PIE_GAP);
        int pieGap = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_GAP, 3);
        mPieGap.setValue(String.valueOf(pieGap));
        mPieGap.setOnPreferenceChangeListener(this);

        mPieAngle = (ListPreference) prefSet.findPreference(PIE_ANGLE);
        int pieAngle = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_ANGLE, 12);
        mPieAngle.setValue(String.valueOf(pieAngle));
        mPieAngle.setOnPreferenceChangeListener(this);

        mPieMenu = (CheckBoxPreference) prefSet.findPreference(PIE_MENU);
        mPieMenu.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_MENU, 1) == 1);

        mPieLastApp = (CheckBoxPreference) prefSet.findPreference(PIE_LASTAPP);
        mPieLastApp.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_LAST_APP, 1) == 1);

        mPieSearch = (CheckBoxPreference) prefSet.findPreference(PIE_SEARCH);
        mPieSearch.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_SEARCH, 1) == 1);

        mPieCenter = (CheckBoxPreference) prefSet.findPreference(PIE_CENTER);
        mPieCenter.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_CENTER, 1) == 1);

        mPieStick = (CheckBoxPreference) prefSet.findPreference(PIE_STICK);
        mPieStick.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_STICK, 1) == 1);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateValues();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mPieMenu) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_MENU, mPieMenu.isChecked() ? 1 : 0);
        } else if (preference == mPieLastApp) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_LAST_APP, mPieLastApp.isChecked() ? 1 : 0);
        } else if (preference == mPieSearch) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_SEARCH, mPieSearch.isChecked() ? 1 : 0);
        } else if (preference == mPieCenter) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_CENTER, mPieCenter.isChecked() ? 1 : 0);
        } else if (preference == mPieStick) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_STICK, mPieStick.isChecked() ? 1 : 0);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mPieControls) {
            Settings.System.putInt(getActivity().getApplicationContext()
                    .getContentResolver(), Settings.System.PIE_CONTROLS,
                (Boolean) newValue ? 1 : 0);
            return true;
        } else if (preference == mPieMode) {
            int pieMode = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_MODE, pieMode);
            return true;
        } else if (preference == mPieSize) {
            float val = Float.parseFloat((String) newValue);
            float value = (val * ((PIE_SIZE_MAX - PIE_SIZE_MIN) /
                100)) + PIE_SIZE_MIN;
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.PIE_SIZE,
                    value);
            return true;
        } else if (preference == mPieGravity) {
            int pieGravity = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_GRAVITY, pieGravity);
            return true;
        } else if (preference == mPieGap) {
            int pieGap = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_GAP, pieGap);
            return true;
        } else if (preference == mPieTrigger) {
            float pieTrigger = Float.valueOf((String) newValue);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.PIE_TRIGGER, pieTrigger);
            return true;
        } else if (preference == mPieAngle) {
            int pieAngle = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_ANGLE, pieAngle);
            return true;
        }
        return false;
    }

    private void updateValues() {
        float pieSize;
        try{
            pieSize = Settings.System.getFloat(getActivity()
                    .getContentResolver(), Settings.System.PIE_SIZE);
        } catch (Exception e) {
            pieSize = PIE_SIZE_DEFAULT;
            Settings.System.putFloat(getActivity().getContentResolver(),
                Settings.System.PIE_SIZE, pieSize);
        }
        float pieSizeValue = ((pieSize - PIE_SIZE_MIN) /
                    ((PIE_SIZE_MAX - PIE_SIZE_MIN) / 100)) / 100;
        mPieSize.setInitValue((int) (pieSizeValue * 100));
        mPieSize.disablePercentageValue(true);
    }
}
