
package com.xylon.settings.fragments;

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
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
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

import com.xylon.settings.R;
import com.xylon.settings.SettingsPreferenceFragment;
import com.xylon.settings.Utils;
import com.xylon.settings.util.Helpers;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class PieColor extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String PIE_COLOR_STYLE = "pie_color_style";
    private static final String PIE_JUICE = "pie_juice";
    private static final String PIE_BACKGROUND = "pie_background";
    private static final String PIE_SELECT = "pie_select";
    private static final String PIE_OUTLINES = "pie_outlines";
    private static final String PIE_STATUS_CLOCK = "pie_status_clock";
    private static final String PIE_STATUS = "pie_status";
    private static final String PIE_CHEVRON_LEFT = "pie_chevron_left";
    private static final String PIE_CHEVRON_RIGHT = "pie_chevron_right";
    private static final String PIE_BUTTON_COLOR = "pie_button_color";

    ListPreference mColorStyle;
    ColorPickerPreference mPieBg;
    ColorPickerPreference mJuice;
    ColorPickerPreference mSelect;
    ColorPickerPreference mOutlines;
    ColorPickerPreference mStatusClock;
    ColorPickerPreference mStatus;
    ColorPickerPreference mChevronLeft;
    ColorPickerPreference mChevronRight;
    ColorPickerPreference mBtnColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pie_color);

        mColorStyle = (ListPreference) findPreference(PIE_COLOR_STYLE);
        mColorStyle.setOnPreferenceChangeListener(this);

        mPieBg = (ColorPickerPreference) findPreference(PIE_BACKGROUND);
        mPieBg.setOnPreferenceChangeListener(this);

        mJuice = (ColorPickerPreference) findPreference(PIE_JUICE);
        mJuice.setOnPreferenceChangeListener(this);

        mSelect = (ColorPickerPreference) findPreference(PIE_SELECT);
        mSelect.setOnPreferenceChangeListener(this);

        mOutlines = (ColorPickerPreference) findPreference(PIE_OUTLINES);
        mOutlines.setOnPreferenceChangeListener(this);

        mStatusClock = (ColorPickerPreference) findPreference(PIE_STATUS_CLOCK);
        mStatusClock.setOnPreferenceChangeListener(this);

        mStatus = (ColorPickerPreference) findPreference(PIE_STATUS);
        mStatus.setOnPreferenceChangeListener(this);

        mChevronLeft = (ColorPickerPreference) findPreference(PIE_CHEVRON_LEFT);
        mChevronLeft.setOnPreferenceChangeListener(this);

        mChevronRight = (ColorPickerPreference) findPreference(PIE_CHEVRON_RIGHT);
        mChevronRight.setOnPreferenceChangeListener(this);

        mBtnColor = (ColorPickerPreference) findPreference(PIE_BUTTON_COLOR);
        mBtnColor.setOnPreferenceChangeListener(this);

        updateVisibility();
    }

    private void updateVisibility() {
        int visible = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.PIE_COLOR_STYLE, 1);
        if (visible == 1) {
            mJuice.setEnabled(false);
            mPieBg.setEnabled(false);
            mSelect.setEnabled(false);
            mOutlines.setEnabled(false);
            mStatusClock.setEnabled(false);
            mStatus.setEnabled(false);
            mChevronLeft.setEnabled(false);
            mChevronRight.setEnabled(false);
            mBtnColor.setEnabled(false);
        } else {
            mJuice.setEnabled(true);
            mPieBg.setEnabled(true);
            mSelect.setEnabled(true);
            mOutlines.setEnabled(true);
            mStatusClock.setEnabled(true);
            mStatus.setEnabled(true);
            mChevronLeft.setEnabled(true);
            mChevronRight.setEnabled(true);
            mBtnColor.setEnabled(true);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mColorStyle) {
            int value = Integer.valueOf((String) newValue);
            int index = mColorStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_COLOR_STYLE, value);
            preference.setSummary(mColorStyle.getEntries()[index]);
            updateVisibility();
            if (value == 1)
                Helpers.restartSystemUI();
            return true;
        } else if (preference == mPieBg) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_BACKGROUND, intHex);
            return true;
        } else if (preference == mSelect) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_SELECT, intHex);
            return true;
        } else if (preference == mOutlines) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_OUTLINES, intHex);
            return true;
        } else if (preference == mStatusClock) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_STATUS_CLOCK, intHex);
            return true;
        } else if (preference == mStatus) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_STATUS, intHex);
            return true;
        } else if (preference == mChevronLeft) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_CHEVRON_LEFT, intHex);
            return true;
        } else if (preference == mChevronRight) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_CHEVRON_RIGHT, intHex);
            return true;
        } else if (preference == mBtnColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_BUTTON_COLOR, intHex);
            return true;
        } else if (preference == mJuice) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_JUICE, intHex);
            return true;
        }
        return false;
    }
}
