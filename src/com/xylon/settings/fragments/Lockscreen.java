/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.crystal.settings.fragments;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.widget.Toast;

import com.crystal.settings.R;
import com.crystal.settings.SettingsPreferenceFragment;
import com.crystal.settings.Utils;
import com.crystal.settings.widgets.SeekBarPreference;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import net.margaritov.preference.colorpicker.ColorPickerView;

public class Lockscreen extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "Lockscreen";

    private static final String PREF_LOCKSCREEN_AUTO_ROTATE = "lockscreen_auto_rotate";
    private static final String PREF_LOCKSCREEN_ALL_WIDGETS = "lockscreen_all_widgets";
    private static final String PREF_LOCKSCREEN_UNLIMITED_WIDGETS = "lockscreen_unlimited_widgets";
    private static final String PREF_LOCKSCREEN_MAXIMIZE_WIDGETS = "lockscreen_maximize_widgets";
    private static final String PREF_LOCKSCREEN_HIDE_INITIAL_PAGE_HINTS = "lockscreen_hide_initial_page_hints";
    private static final String PREF_LOCKSCREEN_LONGPRESS_CHALLENGE = "lockscreen_longpress_challenge";
    private static final String PREF_LOCKSCREEN_USE_CAROUSEL = "lockscreen_use_widget_container_carousel";
    private static final String PREF_ALWAYS_BATTERY_PREF = "lockscreen_battery_status";
    private static final String PREF_DASH_CLOCK = "dash_clock";
    private static final String PREF_SEE_TRHOUGH = "see_through";
    private static final String BACKGROUND_PREF = "lockscreen_background";
    private static final String PREF_LS_COLOR_ALPHA = "lock_color_alpha";

    // CyanogenMod Additions
    private static final String SLIDE_LOCK_DELAY_TOGGLE = "slide_lock_delay_toggle";
    private static final String SLIDE_LOCK_TIMEOUT_DELAY = "slide_lock_timeout_delay";
    private static final String SLIDE_LOCK_SCREENOFF_DELAY = "slide_lock_screenoff_delay";
    private static final String MENU_UNLOCK_PREF = "menu_unlock";
    private static final String HOME_UNLOCK_PREF = "home_unlock";
    private static final String LOCKSCREEN_QUICK_UNLOCK_CONTROL = "quick_unlock_control";
    private static final String KEY_VIBRATE_PREF = "lockscreen_vibrate";


    private static final int REQUEST_CODE_BG_WALLPAPER = 199;

    private static final int LOCKSCREEN_BACKGROUND_COLOR_FILL = 0;
    private static final int LOCKSCREEN_BACKGROUND_CUSTOM_IMAGE = 1;
    private static final int LOCKSCREEN_BACKGROUND_DEFAULT_WALLPAPER = 2;

    ColorPickerPreference mLsColorAlpha;
    CheckBoxPreference mLockscreenAutoRotate;
    CheckBoxPreference mLockscreenAllWidgets;
    CheckBoxPreference mLockscreenUnlimitedWidgets;
    CheckBoxPreference mMaximizeWidgets;
    CheckBoxPreference mLockscreenHideInitialPageHints;
    CheckBoxPreference mLockscreenLongpressChallenge;
    CheckBoxPreference mLockscreenUseCarousel;
    CheckBoxPreference mSeeThrough;
    ListPreference mCustomBackground;
    ListPreference mBatteryStatus;

    CheckBoxPreference mSlideLockDelayToggle;
    ListPreference mSlideLockTimeoutDelay;
    ListPreference mSlideLockScreenOffDelay;
    CheckBoxPreference mVibratePref;
    CheckBoxPreference mMenuUnlock;
    CheckBoxPreference mHomeUnlock;
    CheckBoxPreference mQuickUnlockScreen;

    private File mWallpaperImage;
    private File mWallpaperTemporary;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.lock_screen_settings);
        PreferenceScreen prefSet = getPreferenceScreen();

        // Dont display the lock clock preference if its not installed
        removePreferenceIfPackageNotInstalled(findPreference(PREF_DASH_CLOCK));

        mLockscreenAutoRotate = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_AUTO_ROTATE);
        mLockscreenAutoRotate.setChecked(Settings.System.getBoolean(mContext
                .getContentResolver(), Settings.System.LOCKSCREEN_AUTO_ROTATE, false));

        mLockscreenAllWidgets = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_ALL_WIDGETS);
        mLockscreenAllWidgets.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_ALL_WIDGETS, false));

        mLockscreenUnlimitedWidgets = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_UNLIMITED_WIDGETS);
        mLockscreenUnlimitedWidgets.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_UNLIMITED_WIDGETS, false));

        mMaximizeWidgets = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_MAXIMIZE_WIDGETS);
        if (!Utils.isPhone(getActivity())) {
            getPreferenceScreen().removePreference(mMaximizeWidgets);
            mMaximizeWidgets = null;
        } else {
            mMaximizeWidgets.setOnPreferenceChangeListener(this);
        }

        mLockscreenHideInitialPageHints = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_HIDE_INITIAL_PAGE_HINTS);
        mLockscreenHideInitialPageHints.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_HIDE_INITIAL_PAGE_HINTS, false));

        mLockscreenLongpressChallenge = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_LONGPRESS_CHALLENGE);
        mLockscreenLongpressChallenge.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_LONGPRESS_CHALLENGE, false));

        mLockscreenUseCarousel = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_USE_CAROUSEL);
        mLockscreenUseCarousel.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_USE_WIDGET_CONTAINER_CAROUSEL, false));

        mBatteryStatus = (ListPreference) findPreference(PREF_ALWAYS_BATTERY_PREF);
        mBatteryStatus.setOnPreferenceChangeListener(this);

        mSeeThrough = (CheckBoxPreference) prefSet.findPreference(PREF_SEE_TRHOUGH);

        mCustomBackground = (ListPreference) prefSet.findPreference(BACKGROUND_PREF);
        mCustomBackground.setOnPreferenceChangeListener(this);

        mLsColorAlpha = (ColorPickerPreference) findPreference(PREF_LS_COLOR_ALPHA);
        mLsColorAlpha.setOnPreferenceChangeListener(this);

        mWallpaperImage = new File(getActivity().getFilesDir() + "/lockwallpaper");
        mWallpaperTemporary = new File(getActivity().getCacheDir() + "/lockwallpaper.tmp");

        mSlideLockDelayToggle = (CheckBoxPreference) prefSet.findPreference(SLIDE_LOCK_DELAY_TOGGLE);
        mSlideLockDelayToggle.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.SCREEN_LOCK_SLIDE_DELAY_TOGGLE, 0) == 1);

        mSlideLockTimeoutDelay = (ListPreference) prefSet.findPreference(SLIDE_LOCK_TIMEOUT_DELAY);
        int slideTimeoutDelay = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.SCREEN_LOCK_SLIDE_TIMEOUT_DELAY, 5000);
        mSlideLockTimeoutDelay.setValue(String.valueOf(slideTimeoutDelay));
        updateSlideAfterTimeoutSummary();
        mSlideLockTimeoutDelay.setOnPreferenceChangeListener(this);

        mSlideLockScreenOffDelay = (ListPreference) prefSet.findPreference(SLIDE_LOCK_SCREENOFF_DELAY);
        int slideScreenOffDelay = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.SCREEN_LOCK_SLIDE_SCREENOFF_DELAY, 0);
        mSlideLockScreenOffDelay.setValue(String.valueOf(slideScreenOffDelay));
        updateSlideAfterScreenOffSummary();
        mSlideLockScreenOffDelay.setOnPreferenceChangeListener(this);

        // Quick Unlock Screen Control
        mQuickUnlockScreen = (CheckBoxPreference) prefSet.findPreference(LOCKSCREEN_QUICK_UNLOCK_CONTROL);
        mQuickUnlockScreen.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_QUICK_UNLOCK_CONTROL, 0) == 1);

        // Menu Unlock
        mMenuUnlock = (CheckBoxPreference) prefSet.findPreference(MENU_UNLOCK_PREF);
        mMenuUnlock.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.MENU_UNLOCK_SCREEN, 0) == 1);

        // Home Unlock
        mHomeUnlock = (CheckBoxPreference) prefSet.findPreference(HOME_UNLOCK_PREF);
        mHomeUnlock.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.HOME_UNLOCK_SCREEN, 0) == 1);

        // Vibrate on unlock
        mVibratePref = (CheckBoxPreference) prefSet.findPreference(KEY_VIBRATE_PREF);
        mVibratePref.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_VIBRATE_ENABLED, 1) == 1);

        setBatteryStatusSummary();
        updateCustomBackgroundSummary();
    }

    private void updateSlideAfterTimeoutSummary() {
        // Update summary message with current value
        long currentTimeout = Settings.System.getInt(getActivity().getApplicationContext()
                .getContentResolver(),
                Settings.System.SCREEN_LOCK_SLIDE_TIMEOUT_DELAY, 5000);
        final CharSequence[] entries = mSlideLockTimeoutDelay.getEntries();
        final CharSequence[] values = mSlideLockTimeoutDelay.getEntryValues();
        int best = 0;
        for (int i = 0; i < values.length; i++) {
            long timeout = Long.valueOf(values[i].toString());
            if (currentTimeout >= timeout) {
                best = i;
            }
        }
        mSlideLockTimeoutDelay.setSummary(entries[best]);
    }

    private void updateSlideAfterScreenOffSummary() {
        // Update summary message with current value
        long currentTimeout = Settings.System.getInt(getActivity().getApplicationContext()
                .getContentResolver(),
                Settings.System.SCREEN_LOCK_SLIDE_SCREENOFF_DELAY, 0);
        final CharSequence[] entries = mSlideLockScreenOffDelay.getEntries();
        final CharSequence[] values = mSlideLockScreenOffDelay.getEntryValues();
        int best = 0;
        for (int i = 0; i < values.length; i++) {
            long timeout = Long.valueOf(values[i].toString());
            if (currentTimeout >= timeout) {
                best = i;
            }
        }
        mSlideLockScreenOffDelay.setSummary(entries[best]);
    }

    private void updateCustomBackgroundSummary() {
        int resId;
        boolean seeThroughState = true;
        boolean colorState = true;
        String value = Settings.System.getString(getContentResolver(),
                Settings.System.LOCKSCREEN_BACKGROUND);
        if (value == null) {
            resId = R.string.lockscreen_background_default_wallpaper;
            mCustomBackground.setValueIndex(LOCKSCREEN_BACKGROUND_DEFAULT_WALLPAPER);
        } else if (value.isEmpty()) {
            resId = R.string.lockscreen_background_custom_image;
            mCustomBackground.setValueIndex(LOCKSCREEN_BACKGROUND_CUSTOM_IMAGE);
            seeThroughState = false;
            colorState = false;
        } else {
            resId = R.string.lockscreen_background_color_fill;
            mCustomBackground.setValueIndex(LOCKSCREEN_BACKGROUND_COLOR_FILL);
            colorState = false;            
        }
        mCustomBackground.setSummary(getResources().getString(resId));
        mSeeThrough.setEnabled(seeThroughState);
        mLsColorAlpha.setEnabled(colorState);
    }

    @Override
    public void onResume() {
        super.onResume();

        ContentResolver cr = getActivity().getContentResolver();

        if (mMaximizeWidgets != null) {
            mMaximizeWidgets.setChecked(Settings.System.getInt(cr,
                    Settings.System.LOCKSCREEN_MAXIMIZE_WIDGETS, 0) == 1);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mLockscreenAutoRotate) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.LOCKSCREEN_AUTO_ROTATE,
                    ((CheckBoxPreference) preference).isChecked());
            return true; 
        } else if (preference == mLockscreenAllWidgets) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.LOCKSCREEN_ALL_WIDGETS,
                    ((CheckBoxPreference) preference).isChecked());
            return true;
        } else if (preference == mLockscreenUnlimitedWidgets) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.LOCKSCREEN_UNLIMITED_WIDGETS,
                    ((CheckBoxPreference) preference).isChecked());
            return true;
        } else if (preference == mLockscreenHideInitialPageHints) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_HIDE_INITIAL_PAGE_HINTS,
                    ((CheckBoxPreference)preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mLockscreenLongpressChallenge) {
            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_LONGPRESS_CHALLENGE,
                    ((CheckBoxPreference)preference).isChecked());
            return true;
        } else if (preference == mLockscreenUseCarousel) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_USE_WIDGET_CONTAINER_CAROUSEL,
                    ((CheckBoxPreference)preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mSeeThrough) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_SEE_THROUGH,
                    mSeeThrough.isChecked() ? 1 : 0);
        } else if (preference == mSlideLockDelayToggle) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.SCREEN_LOCK_SLIDE_DELAY_TOGGLE,
                    isToggled(preference) ? 1 : 0);
        } if (preference == mQuickUnlockScreen) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_QUICK_UNLOCK_CONTROL,
                    isToggled(preference) ? 1 : 0);
        } else if (preference == mMenuUnlock) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.MENU_UNLOCK_SCREEN,
                    isToggled(preference) ? 1 : 0);
        } else if (preference == mHomeUnlock) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.HOME_UNLOCK_SCREEN,
                    isToggled(preference) ? 1 : 0);
        }  else if (preference == mVibratePref) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_VIBRATE_ENABLED,
                    isToggled(preference) ? 1 : 0);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private boolean isToggled(Preference pref) {
        return ((CheckBoxPreference) pref).isChecked();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_BG_WALLPAPER) {
            int hintId;

            if (resultCode == Activity.RESULT_OK) {
                if (mWallpaperTemporary.exists()) {
                    mWallpaperTemporary.renameTo(mWallpaperImage);
                }
                mWallpaperImage.setReadOnly();
                hintId = R.string.lockscreen_background_result_successful;
                Settings.System.putString(getContentResolver(),
                        Settings.System.LOCKSCREEN_BACKGROUND, "");
                updateCustomBackgroundSummary();
            } else {
                if (mWallpaperTemporary.exists()) {
                    mWallpaperTemporary.delete();
                }
                hintId = R.string.lockscreen_background_result_not_successful;
            }
            Toast.makeText(getActivity(),
                    getResources().getString(hintId), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
       ContentResolver cr = getActivity().getContentResolver();
        if (preference == mBatteryStatus) {
            int value = Integer.valueOf((String) objValue);
            int index = mBatteryStatus.findIndexOfValue((String) objValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_ALWAYS_SHOW_BATTERY, value);
            mBatteryStatus.setSummary(mBatteryStatus.getEntries()[index]);
            return true;
        } else if (preference == mMaximizeWidgets) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_MAXIMIZE_WIDGETS, value ? 1 : 0);
            return true;
        } else if (preference == mCustomBackground) {
            int selection = mCustomBackground.findIndexOfValue(objValue.toString());
            return handleBackgroundSelection(selection);
        } else if (preference == mLsColorAlpha) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(objValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_COLOR_ALPHA, intHex);
            return true;
        } else if (preference == mSlideLockTimeoutDelay) {
            int slideTimeoutDelay = Integer.valueOf((String) objValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.SCREEN_LOCK_SLIDE_TIMEOUT_DELAY,
                    slideTimeoutDelay);
            updateSlideAfterTimeoutSummary();
            return true;
        } else if (preference == mSlideLockScreenOffDelay) {
            int slideScreenOffDelay = Integer.valueOf((String) objValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.SCREEN_LOCK_SLIDE_SCREENOFF_DELAY, slideScreenOffDelay);
            updateSlideAfterScreenOffSummary();
            return true;
        }
        return false;
    }

     private boolean handleBackgroundSelection(int selection) {
        if (selection == LOCKSCREEN_BACKGROUND_COLOR_FILL) {
            final ColorPickerView colorView = new ColorPickerView(getActivity());
            int currentColor = Settings.System.getInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_BACKGROUND, -1);

            if (currentColor != -1) {
                colorView.setColor(currentColor);
            }
            colorView.setAlphaSliderVisible(true);

            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.lockscreen_custom_background_dialog_title)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.System.putInt(getContentResolver(),
                                    Settings.System.LOCKSCREEN_BACKGROUND, colorView.getColor());
                            updateCustomBackgroundSummary();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setView(colorView)
                    .show();
        } else if (selection == LOCKSCREEN_BACKGROUND_CUSTOM_IMAGE) {
            final Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
            intent.setType("image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("scale", true);
            intent.putExtra("scaleUpIfNeeded", false);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());

            final Display display = getActivity().getWindowManager().getDefaultDisplay();
            final Rect rect = new Rect();
            final Window window = getActivity().getWindow();

            window.getDecorView().getWindowVisibleDisplayFrame(rect);

            int statusBarHeight = rect.top;
            int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
            int titleBarHeight = contentViewTop - statusBarHeight;
            boolean isPortrait = getResources().getConfiguration().orientation ==
                    Configuration.ORIENTATION_PORTRAIT;

            int width = display.getWidth();
            int height = display.getHeight() - titleBarHeight;

            intent.putExtra("aspectX", isPortrait ? width : height);
            intent.putExtra("aspectY", isPortrait ? height : width);

            try {
                mWallpaperTemporary.createNewFile();
                mWallpaperTemporary.setWritable(true, false);
                mWallpaperTemporary.setReadable(true, false);
                intent.putExtra(MediaStore.EXTRA_OUTPUT ,Uri.fromFile(mWallpaperTemporary));
                intent.putExtra("return-data", false);
                getActivity().startActivityFromFragment(this, intent, REQUEST_CODE_BG_WALLPAPER);
                //Ignored would be preferable to nothing
            } catch (IOException e) {
            } catch (ActivityNotFoundException e) {
            }
        } else if (selection == LOCKSCREEN_BACKGROUND_DEFAULT_WALLPAPER) {
            Settings.System.putString(getContentResolver(),
                    Settings.System.LOCKSCREEN_BACKGROUND, null);
            updateCustomBackgroundSummary();
            return true;
        }

        return false;
    }

    private void setBatteryStatusSummary() {
        // Set the battery status description text
        if (mBatteryStatus != null) {
            int batteryStatus = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_ALWAYS_SHOW_BATTERY, 0);
            mBatteryStatus.setValueIndex(batteryStatus);
            mBatteryStatus.setSummary(mBatteryStatus.getEntries()[batteryStatus]);
        }
    }

    private boolean removePreferenceIfPackageNotInstalled(Preference preference) {
        String intentUri = ((PreferenceScreen) preference).getIntent().toUri(1);
        Pattern pattern = Pattern.compile("component=([^/]+)/");
        Matcher matcher = pattern.matcher(intentUri);

        String packageName = matcher.find() ? matcher.group(1) : null;
        if (packageName != null) {
            try {
                getPackageManager().getPackageInfo(packageName, 0);
            } catch (NameNotFoundException e) {
                Log.e(TAG, "package " + packageName + " not installed, hiding preference.");
                getPreferenceScreen().removePreference(preference);
                return true;
            }
        }
        return false;
    }

}
