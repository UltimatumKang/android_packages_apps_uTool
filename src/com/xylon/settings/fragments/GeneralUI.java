package com.crystal.settings.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
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
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.Spannable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

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
import com.crystal.settings.widgets.AlphaSeekBar;
import com.crystal.settings.widgets.SeekBarPreference;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class GeneralUI extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "General User Interface";

    private static final String PREF_SHOW_OVERFLOW = "show_overflow";
    private static final String PREF_WAKEUP_WHEN_PLUGGED_UNPLUGGED = "wakeup_when_plugged_unplugged";
    private static final String PREF_FULLSCREEN_KEYBOARD = "fullscreen_keyboard";
    private static final String PREF_RECENTS_RAM_BAR = "recents_ram_bar";
    private static final String PREF_HIDE_EXTRAS = "hide_extras";
    private static final String PREF_POWER_CRT_MODE = "system_power_crt_mode";
    private static final String PREF_POWER_CRT_SCREEN_OFF = "system_power_crt_screen_off";
    private static final String PREF_KEYBOARD_ROTATION_TOGGLE = "keyboard_rotation_toggle";
    private static final String PREF_KEYBOARD_ROTATION_TIMEOUT = "keyboard_rotation_timeout";

    private static final int TIMEOUT_DEFAULT = 5000; // 5s

    Preference mRamBar;
    CheckBoxPreference mShowActionOverflow;
    CheckBoxPreference mHideExtras;
    CheckBoxPreference mWakeUpWhenPluggedOrUnplugged;
    CheckBoxPreference mFullscreenKeyboard;
    CheckBoxPreference mCrtOff;
    ListPreference mCrtMode;
    CheckBoxPreference mKeyboardRotationToggle;
    ListPreference mKeyboardRotationTimeout;

    private boolean mIsCrtOffChecked = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_general_ui);

        addPreferencesFromResource(R.xml.general_ui_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver cr = mContext.getContentResolver();

        mShowActionOverflow = (CheckBoxPreference) findPreference(PREF_SHOW_OVERFLOW);
        mShowActionOverflow.setChecked(Settings.System.getBoolean(getActivity().
                        getApplicationContext().getContentResolver(),
                        Settings.System.UI_FORCE_OVERFLOW_BUTTON, false));

        // respect device default configuration
        // true fades while false animates
        boolean electronBeamFadesConfig = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_animateScreenLights);

        // use this to enable/disable crt on feature
        mIsCrtOffChecked = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.SYSTEM_POWER_ENABLE_CRT_OFF,
                electronBeamFadesConfig ? 0 : 1) == 1;

        mCrtOff = (CheckBoxPreference) findPreference(PREF_POWER_CRT_SCREEN_OFF);
        mCrtOff.setChecked(mIsCrtOffChecked);

        mCrtMode = (ListPreference) prefSet.findPreference(PREF_POWER_CRT_MODE);
        int crtMode = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.SYSTEM_POWER_CRT_MODE, 0);
        mCrtMode.setValue(String.valueOf(crtMode));
        mCrtMode.setSummary(mCrtMode.getEntry());
        mCrtMode.setOnPreferenceChangeListener(this);

        mWakeUpWhenPluggedOrUnplugged = (CheckBoxPreference) findPreference(PREF_WAKEUP_WHEN_PLUGGED_UNPLUGGED);
        if (mWakeUpWhenPluggedOrUnplugged != null) {
            mWakeUpWhenPluggedOrUnplugged.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                            Settings.System.WAKEUP_WHEN_PLUGGED_UNPLUGGED, true));

            // hide option if device is already set to never wake up
            PreferenceGroup miscPrefs = (PreferenceGroup) findPreference("misc");
            if(!mContext.getResources().getBoolean(
                    com.android.internal.R.bool.config_unplugTurnsOnScreen) && (miscPrefs != null)) {
                miscPrefs.removePreference(mWakeUpWhenPluggedOrUnplugged);
            }
        }
        
        mFullscreenKeyboard = (CheckBoxPreference) findPreference(PREF_FULLSCREEN_KEYBOARD);
        mFullscreenKeyboard.setChecked(Settings.System.getInt(cr,
                Settings.System.FULLSCREEN_KEYBOARD, 0) == 1);

        mKeyboardRotationToggle = (CheckBoxPreference) findPreference(PREF_KEYBOARD_ROTATION_TOGGLE);
        mKeyboardRotationToggle.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.KEYBOARD_ROTATION_TIMEOUT, 0) > 0);

        mKeyboardRotationTimeout = (ListPreference) findPreference(PREF_KEYBOARD_ROTATION_TIMEOUT);
        mKeyboardRotationTimeout.setOnPreferenceChangeListener(this);
        updateRotationTimeout(Settings.System.getInt(getActivity()
                    .getContentResolver(), Settings.System.KEYBOARD_ROTATION_TIMEOUT, TIMEOUT_DEFAULT));


        mHideExtras = (CheckBoxPreference) findPreference(PREF_HIDE_EXTRAS);
        mHideExtras.setChecked(Settings.System.getBoolean(cr,
                       Settings.System.HIDE_EXTRAS_SYSTEM_BAR, false));

        mRamBar = findPreference(PREF_RECENTS_RAM_BAR);
        updateRamBar();

        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateRamBar();
    }

    @Override
    public void onPause() {
        super.onResume();
        updateRamBar();
    }

    private void updateRamBar() {
        int ramBarMode = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.RECENTS_RAM_BAR_MODE, 0);
        if (ramBarMode != 0)
            mRamBar.setSummary(getResources().getString(R.string.ram_bar_color_enabled));
        else
            mRamBar.setSummary(getResources().getString(R.string.ram_bar_color_disabled));
    }

    public void updateRotationTimeout(int timeout) {
        if (timeout == 0)
            timeout = TIMEOUT_DEFAULT;
        mKeyboardRotationTimeout.setValue(Integer.toString(timeout));
        mKeyboardRotationTimeout.setSummary(getString(R.string.keyboard_rotation_timeout_summary, mKeyboardRotationTimeout.getEntry()));
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mShowActionOverflow) {
            boolean enabled = mShowActionOverflow.isChecked();
            Settings.System.putBoolean(getContentResolver(),
                    Settings.System.UI_FORCE_OVERFLOW_BUTTON, enabled ? true : false);
            // Show toast appropriately
            if (enabled) {
                Toast.makeText(getActivity(), R.string.show_overflow_toast_enable,
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), R.string.show_overflow_toast_disable,
                        Toast.LENGTH_LONG).show();
            }
            return true;
        } else if (preference == mHideExtras) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.HIDE_EXTRAS_SYSTEM_BAR,
                    ((CheckBoxPreference) preference).isChecked());
            return true;
        } else if (preference == mWakeUpWhenPluggedOrUnplugged) {
            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.WAKEUP_WHEN_PLUGGED_UNPLUGGED,
                    ((CheckBoxPreference) preference).isChecked());
            return true;
        } else if (preference == mFullscreenKeyboard) {
            Settings.System.putInt(getActivity().getContentResolver(), Settings.System.FULLSCREEN_KEYBOARD,
                    mFullscreenKeyboard.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mCrtOff) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SYSTEM_POWER_ENABLE_CRT_OFF,
                    mCrtOff.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mKeyboardRotationToggle) {
            boolean isAutoRotate = (Settings.System.getInt(getContentResolver(),
                        Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
            if (isAutoRotate && mKeyboardRotationToggle.isChecked())
                mKeyboardRotationDialog();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.KEYBOARD_ROTATION_TIMEOUT,
                    mKeyboardRotationToggle.isChecked() ? TIMEOUT_DEFAULT : 0);
            updateRotationTimeout(TIMEOUT_DEFAULT);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mCrtMode) {
            int crtMode = Integer.valueOf((String) newValue);
            int index = mCrtMode.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SYSTEM_POWER_CRT_MODE, crtMode);
            mCrtMode.setSummary(mCrtMode.getEntries()[index]);
            return true;
        } else if (preference == mKeyboardRotationTimeout) {
            int timeout = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.KEYBOARD_ROTATION_TIMEOUT, timeout);
            updateRotationTimeout(timeout);
            return true;
        }
        return false;
    }

    public void mKeyboardRotationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.keyboard_rotation_dialog);
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(com.android.internal.R.string.ok), null);
        AlertDialog alert = builder.create();
        alert.show();
    }
}
