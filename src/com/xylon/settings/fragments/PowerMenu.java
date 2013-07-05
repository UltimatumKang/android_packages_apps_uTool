package com.crystal.settings.fragments;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.crystal.settings.R;
import com.crystal.settings.R.xml;
import com.crystal.settings.SettingsPreferenceFragment;

public class PowerMenu extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String PREF_EXPANDED_DESKTOP = "show_expanded_desktop";
    private static final String PREF_PROFILES = "show_profiles";
    private static final String PREF_SCREENSHOT = "show_screenshot";
    private static final String PREF_TORCH_TOGGLE = "show_torch_toggle";
    private static final String PREF_AIRPLANE_TOGGLE = "show_airplane_toggle";
    private static final String PREF_NAVBAR_HIDE = "show_navbar_hide";
    private static final String PREF_VOLUME_STATE_TOGGLE = "show_volume_state_toggle";
    private static final String PREF_REBOOT_KEYGUARD = "show_reboot_keyguard";
    private static final String PREF_POWER_KEYGUARD = "show_power_keyguard";

    SwitchPreference mExpandedDesktopPref;
    SwitchPreference mShowProfiles;
    SwitchPreference mShowScreenShot;
    SwitchPreference mShowTorchToggle;
    SwitchPreference mShowAirplaneToggle;
    SwitchPreference mShowNavBarHide;
    SwitchPreference mShowVolumeStateToggle;
    SwitchPreference mShowPowerKeyguard;
    SwitchPreference mShowRebootKeyguard;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.power_menu_settings);

        mShowProfiles = (SwitchPreference) findPreference(PREF_PROFILES);
        mShowProfiles.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.SYSTEM_PROFILES_ENABLED, 1) == 1));
        mShowProfiles.setOnPreferenceChangeListener(this);

        mShowScreenShot = (SwitchPreference) findPreference(PREF_SCREENSHOT);
        mShowScreenShot.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_SCREENSHOT,
                0) == 1);
        mShowScreenShot.setOnPreferenceChangeListener(this);

        mShowTorchToggle = (SwitchPreference) findPreference(PREF_TORCH_TOGGLE);
        mShowTorchToggle.setChecked(Settings.System.getBoolean(mContentRes,
                Settings.System.POWER_DIALOG_SHOW_TORCH_TOGGLE, false));
        mShowTorchToggle.setOnPreferenceChangeListener(this);

        mShowAirplaneToggle = (SwitchPreference) findPreference(PREF_AIRPLANE_TOGGLE);
        mShowAirplaneToggle.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_AIRPLANE_TOGGLE,
                1) == 1);
        mShowAirplaneToggle.setOnPreferenceChangeListener(this);

        mExpandedDesktopPref = (SwitchPreference) findPreference(PREF_EXPANDED_DESKTOP);
        mExpandedDesktopPref.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.POWER_MENU_EXPANDED_DESKTOP_ENABLED,
                0) == 1);
        // Only enable if Expanded desktop support is also enabled
        boolean enabled = Settings.System.getInt(getContentResolver(),
                Settings.System.EXPANDED_DESKTOP_STYLE, 0) != 0;
        mExpandedDesktopPref.setEnabled(enabled);
        mExpandedDesktopPref.setOnPreferenceChangeListener(this);

        mShowNavBarHide = (SwitchPreference) findPreference(PREF_NAVBAR_HIDE);
        mShowNavBarHide.setChecked(Settings.System.getBoolean(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_NAVBAR_HIDE, false));
        mShowNavBarHide.setOnPreferenceChangeListener(this);

        mShowVolumeStateToggle = (SwitchPreference) findPreference(PREF_VOLUME_STATE_TOGGLE);
        mShowVolumeStateToggle.setChecked(Settings.System.getBoolean(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_VOLUME_STATE_TOGGLE, true));
        mShowVolumeStateToggle.setOnPreferenceChangeListener(this);

        mShowRebootKeyguard = (SwitchPreference) findPreference(PREF_REBOOT_KEYGUARD);
        mShowRebootKeyguard.setChecked(Settings.System.getBoolean(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_REBOOT_KEYGUARD, true));
        mShowRebootKeyguard.setOnPreferenceChangeListener(this);

        mShowPowerKeyguard = (SwitchPreference) findPreference(PREF_POWER_KEYGUARD);
        mShowPowerKeyguard.setChecked(Settings.System.getBoolean(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_POWER_KEYGUARD, true));
        mShowPowerKeyguard.setOnPreferenceChangeListener(this);

    }

    public boolean onPreferenceChange(Preference preference, Object value) {
        boolean newValue;

        if (preference == mExpandedDesktopPref) {
            Settings.System.putInt(mContentRes,
                    Settings.System.POWER_MENU_EXPANDED_DESKTOP_ENABLED,
                    (Boolean) value ? 1 : 0);
            return true;
        } else if (preference == mShowProfiles) {
            Settings.System.putInt(mContentRes,
                    Settings.System.SYSTEM_PROFILES_ENABLED,
                    (Boolean) value ? 1 : 0);
        } else if (preference == mShowScreenShot) {
            Settings.System.putInt(mContentRes,
                    Settings.System.POWER_DIALOG_SHOW_SCREENSHOT,
                    (Boolean) value ? 1 : 0);
            return true;
        } else if (preference == mShowTorchToggle) {
            Settings.System.putBoolean(mContentRes,
                    Settings.System.POWER_DIALOG_SHOW_TORCH_TOGGLE,
                    (Boolean) value);
            return true;
        } else if (preference == mShowAirplaneToggle) {
            Settings.System.putInt(mContentRes,
                    Settings.System.POWER_DIALOG_SHOW_AIRPLANE_TOGGLE,
                    (Boolean) value ? 1 : 0);
            return true;
        } else if (preference == mShowNavBarHide) {
            Settings.System.putBoolean(mContentRes,
                    Settings.System.POWER_DIALOG_SHOW_NAVBAR_HIDE,
                    (Boolean) value);
            return true;
        } else if (preference == mShowVolumeStateToggle) {
            Settings.System.putBoolean(mContentRes,
                    Settings.System.POWER_DIALOG_SHOW_VOLUME_STATE_TOGGLE,
                    (Boolean) value);
            return true;
        } else if (preference == mShowRebootKeyguard) {
            Settings.System.putBoolean(mContentRes,
                    Settings.System.POWER_DIALOG_SHOW_REBOOT_KEYGUARD,
                    (Boolean) value);
            return true;
        } else if (preference == mShowPowerKeyguard) {
            Settings.System.putBoolean(mContentRes,
                    Settings.System.POWER_DIALOG_SHOW_POWER_KEYGUARD,
                    (Boolean) value);
            return true;
        }
        return false;
    }
}
