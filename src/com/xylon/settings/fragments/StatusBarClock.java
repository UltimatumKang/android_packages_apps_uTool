
package com.crystal.settings.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.EditText;

import com.crystal.settings.SettingsPreferenceFragment;
import com.crystal.settings.R;
import com.crystal.settings.util.ShortcutPickerHelper;
import com.crystal.settings.util.Helpers;

import java.util.Date;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBarClock extends SettingsPreferenceFragment implements
                ShortcutPickerHelper.OnPickListener, OnPreferenceChangeListener {

    private static final String PREF_CLOCK = "enable_clock";
    private static final String PREF_ENABLE = "clock_style";
    private static final String PREF_AM_PM_STYLE = "clock_am_pm_style";
    private static final String PREF_CLOCK_DATE_DISPLAY = "clock_date_display";
    private static final String PREF_CLOCK_DATE_STYLE = "clock_date_style";
    private static final String PREF_CLOCK_DATE_FORMAT = "clock_date_format";
    private static final String PREF_CLOCK_SHORTCLICK = "clock_shortclick";
    private static final String PREF_CLOCK_LONGCLICK = "clock_longclick";
    private static final String PREF_CLOCK_DOUBLECLICK = "clock_doubleclick";

    public static final int CLOCK_DATE_STYLE_LOWERCASE = 1;
    public static final int CLOCK_DATE_STYLE_UPPERCASE = 2;
    private static final int CUSTOM_CLOCK_DATE_FORMAT_INDEX = 18;

    private int shortClick = 0;
    private int longClick = 1;
    private int doubleClick = 2;

    private ShortcutPickerHelper mPicker;
    private Preference mPreference;
    private String mString;

    ListPreference mClockStyle;
    ListPreference mClockAmPmstyle;
    ListPreference mClockDateDisplay;
    ListPreference mClockDateStyle;
    ListPreference mClockDateFormat;
    ListPreference mClockShortClick;
    ListPreference mClockLongClick;
    ListPreference mClockDoubleClick;
    SwitchPreference mEnableClock;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_statusbar_clock);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.status_bar_clock_settings);

        PreferenceScreen prefs = getPreferenceScreen();

        mPicker = new ShortcutPickerHelper(this, this);

        mEnableClock = (SwitchPreference) prefs.findPreference(PREF_CLOCK);
        mEnableClock.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUS_BAR_CLOCK,
                0) == 1);
        mEnableClock.setOnPreferenceChangeListener(this);

        mClockStyle = (ListPreference) findPreference(PREF_ENABLE);
        mClockStyle.setOnPreferenceChangeListener(this);
        mClockStyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_CLOCK_STYLE,
                1)));

        mClockAmPmstyle = (ListPreference) findPreference(PREF_AM_PM_STYLE);
        mClockAmPmstyle.setOnPreferenceChangeListener(this);
        mClockAmPmstyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_CLOCK_AM_PM_STYLE,
                2)));

        mClockDateDisplay = (ListPreference) findPreference(PREF_CLOCK_DATE_DISPLAY);
        mClockDateDisplay.setOnPreferenceChangeListener(this);
        mClockDateDisplay.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_CLOCK_DATE_DISPLAY,
                0)));
        mClockDateDisplay.setSummary(mClockDateDisplay.getEntry());

        mClockDateStyle = (ListPreference) findPreference(PREF_CLOCK_DATE_STYLE);
        mClockDateStyle.setOnPreferenceChangeListener(this);
        mClockDateStyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_CLOCK_DATE_STYLE,
                2)));
        mClockDateStyle.setSummary(mClockDateStyle.getEntry());

        mClockDateFormat = (ListPreference) findPreference(PREF_CLOCK_DATE_FORMAT);
        mClockDateFormat.setOnPreferenceChangeListener(this);
        if (mClockDateFormat.getValue() == null) {
            mClockDateFormat.setValue("EEE");
        }

        mClockShortClick = (ListPreference) findPreference(PREF_CLOCK_SHORTCLICK);
        mClockShortClick.setOnPreferenceChangeListener(this);
        mClockShortClick.setSummary(getProperSummary(mClockShortClick));

        mClockLongClick = (ListPreference) findPreference(PREF_CLOCK_LONGCLICK);
        mClockLongClick.setOnPreferenceChangeListener(this);
        mClockLongClick.setSummary(getProperSummary(mClockLongClick));

        mClockDoubleClick = (ListPreference) findPreference(PREF_CLOCK_DOUBLECLICK);
        mClockDoubleClick.setOnPreferenceChangeListener(this);
        mClockDoubleClick.setSummary(getProperSummary(mClockDoubleClick));

        boolean mClockDateToggle = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_DATE_DISPLAY, 0) != 0;
        if (!mClockDateToggle) {
            mClockDateStyle.setEnabled(false);
            mClockDateFormat.setEnabled(false);
        }

        parseClockDateFormats();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean result = false;

        AlertDialog dialog;

        if (preference == mEnableClock) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_CLOCK,
                    (Boolean) newValue ? 1 : 0);
            return true;
        } else if (preference == mClockAmPmstyle) {
            int val = Integer.parseInt((String) newValue);
            result = Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_AM_PM_STYLE, val);
        } else if (preference == mClockStyle) {
            int val = Integer.parseInt((String) newValue);
            result = Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_STYLE, val);
        } else if (preference == mClockDateDisplay) {
            int val = Integer.parseInt((String) newValue);
            int index = mClockDateDisplay.findIndexOfValue((String) newValue);
            result = Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_DATE_DISPLAY, val);
            mClockDateDisplay.setSummary(mClockDateDisplay.getEntries()[index]);
            if (val == 0) {
                mClockDateStyle.setEnabled(false);
                mClockDateFormat.setEnabled(false);
            } else {
                mClockDateStyle.setEnabled(true);
                mClockDateFormat.setEnabled(true);
            }
            return true;
        } else if (preference == mClockDateStyle) {
            int val = Integer.parseInt((String) newValue);
            int index = mClockDateStyle.findIndexOfValue((String) newValue);
            result = Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_DATE_STYLE, val);
            mClockDateStyle.setSummary(mClockDateStyle.getEntries()[index]);
            parseClockDateFormats();
            return true;
        } else if (preference == mClockDateFormat) {
            int index = mClockDateFormat.findIndexOfValue((String) newValue);

            if (index == CUSTOM_CLOCK_DATE_FORMAT_INDEX) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle(R.string.clock_date_string_edittext_title);
                alert.setMessage(R.string.clock_date_string_edittext_summary);

                final EditText input = new EditText(getActivity());
                String oldText = Settings.System.getString(getActivity().getContentResolver(), Settings.System.STATUSBAR_CLOCK_DATE_FORMAT);
                if (oldText != null) {
                    input.setText(oldText);
                }
                alert.setView(input);

                alert.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int whichButton) {
                        String value = input.getText().toString();
                        if (value.equals("")) {
                            return;
                        }
                        Settings.System.putString(getActivity().getContentResolver(), Settings.System.STATUSBAR_CLOCK_DATE_FORMAT, value);

                        return;
                    }
                });

                alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int which) {
                        return;
                    }
                });
                dialog = alert.create();
                dialog.show();
            } else {
                if ((String) newValue != null) {
                    Settings.System.putString(getActivity().getContentResolver(), Settings.System.STATUSBAR_CLOCK_DATE_FORMAT, (String) newValue);
                }
            }
            return true;
        } else if (preference == mClockShortClick) {
            mPreference = preference;
            mString = Settings.System.NOTIFICATION_CLOCK[shortClick];
            if (newValue.equals("**app**")) {
                mPicker.pickShortcut();
            } else {
                result = Settings.System.putString(getContentResolver(), Settings.System.NOTIFICATION_CLOCK[shortClick], (String) newValue);
                mClockShortClick.setSummary(getProperSummary(mClockShortClick));
            }
        } else if (preference == mClockLongClick) {
            mPreference = preference;
            mString = Settings.System.NOTIFICATION_CLOCK[longClick];
            if (newValue.equals("**app**")) {
                mPicker.pickShortcut();
            } else {
                result = Settings.System.putString(getContentResolver(), Settings.System.NOTIFICATION_CLOCK[longClick], (String) newValue);
                mClockLongClick.setSummary(getProperSummary(mClockLongClick));
            }
        } else if (preference == mClockDoubleClick) {
            mPreference = preference;
            mString = Settings.System.NOTIFICATION_CLOCK[doubleClick];
            if (newValue.equals("**app**")) {
                mPicker.pickShortcut();
            } else {
                result = Settings.System.putString(getContentResolver(), Settings.System.NOTIFICATION_CLOCK[doubleClick], (String) newValue);
                mClockDoubleClick.setSummary(getProperSummary(mClockDoubleClick));
            }
        }
        return result;
    }
    public void shortcutPicked(String uri, String friendlyName, Bitmap bmp, boolean isApplication) {
          mPreference.setSummary(friendlyName);
          Settings.System.putString(getContentResolver(), mString, (String) uri);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ShortcutPickerHelper.REQUEST_PICK_SHORTCUT
                    || requestCode == ShortcutPickerHelper.REQUEST_PICK_APPLICATION
                    || requestCode == ShortcutPickerHelper.REQUEST_CREATE_SHORTCUT) {
                mPicker.onActivityResult(requestCode, resultCode, data);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getProperSummary(Preference preference) {
        if (preference == mClockDoubleClick) {
            mString = Settings.System.NOTIFICATION_CLOCK[doubleClick];
        } else if (preference == mClockLongClick) {
            mString = Settings.System.NOTIFICATION_CLOCK[longClick];
        } else if (preference == mClockShortClick) {
            mString = Settings.System.NOTIFICATION_CLOCK[shortClick];
        }

        String uri = Settings.System.getString(getActivity().getContentResolver(),mString);
        String empty = "";

        if (uri == null)
            return empty;

        if (uri.startsWith("**")) {
            if (uri.equals("**alarm**"))
                return getResources().getString(R.string.alarm);
            else if (uri.equals("**event**"))
                return getResources().getString(R.string.event);
            else if (uri.equals("**voiceassist**"))
                return getResources().getString(R.string.voiceassist);
            else if (uri.equals("**clockoptions**"))
                return getResources().getString(R.string.clock_options);
            else if (uri.equals("**today**"))
                return getResources().getString(R.string.today);
            else if (uri.equals("**null**"))
                return getResources().getString(R.string.nothing);
        } else {
            return mPicker.getFriendlyNameForUri(uri);
        }
        return null;
    }

    private void parseClockDateFormats() {
        // Parse and repopulate mClockDateFormats's entries based on current date.
        String[] dateEntries = getResources().getStringArray(R.array.clock_date_format_entries);
        CharSequence parsedDateEntries[];
        parsedDateEntries = new String[dateEntries.length];
        Date now = new Date();

        int lastEntry = dateEntries.length - 1;
        int dateFormat = Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_CLOCK_DATE_STYLE, 2);
        for (int i = 0; i < dateEntries.length; i++) {
            if (i == lastEntry) {
                parsedDateEntries[i] = dateEntries[i];
            } else {
                String newDate;
                CharSequence dateString = DateFormat.format(dateEntries[i], now);
                if (dateFormat == CLOCK_DATE_STYLE_LOWERCASE) {
                    newDate = dateString.toString().toLowerCase();
                } else if (dateFormat == CLOCK_DATE_STYLE_UPPERCASE) {
                    newDate = dateString.toString().toUpperCase();
                } else {
                    newDate = dateString.toString();
                }

                parsedDateEntries[i] = newDate;
            }
        }
        mClockDateFormat.setEntries(parsedDateEntries);
    }
}
