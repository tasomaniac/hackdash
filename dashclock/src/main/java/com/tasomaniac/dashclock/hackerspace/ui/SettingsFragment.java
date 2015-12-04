/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tasomaniac.dashclock.hackerspace.ui;

import android.app.AlertDialog;
import android.app.backup.BackupManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.Toast;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.tasomaniac.android.widget.IntegrationPreference;
import com.tasomaniac.dashclock.hackerspace.Analytics;
import com.tasomaniac.dashclock.hackerspace.App;
import com.tasomaniac.dashclock.hackerspace.R;
import com.tasomaniac.dashclock.hackerspace.SpaceApiService;
import com.tasomaniac.dashclock.hackerspace.StatusService;
import com.tasomaniac.dashclock.hackerspace.data.HackerSpacePreference;
import com.tasomaniac.dashclock.hackerspace.data.model.Directory;
import com.tasomaniac.dashclock.hackerspace.data.model.HackerSpace;

import java.util.ArrayList;
import java.util.Collections;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LAUNCHER_ACTIVITY_NAME = "com.tasomaniac.dashclock.hackerspace.ui.MainActivity";

    @Inject SpaceApiService spaceApiService;

    @Inject HackerSpacePreference chosenSpacePref;

    @Inject Analytics analytics;

    private ArrayList<HackerSpace> spaces;

    private IntegrationPreference dashclockPref;
    private ListPreference spacesListPreference;

    public SettingsFragment() {
    }

    public static SettingsFragment newInstance(boolean fromDashClock) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putBoolean(DashClockExtension.EXTRA_FROM_DASHCLOCK_SETTINGS, fromDashClock);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.get(getActivity()).component().inject(this);

        if (savedInstanceState == null) {
            analytics.sendScreenView("Settings Page");
        }

        addPreferencesFromResource(R.xml.pref_general);

        spacesListPreference = (ListPreference) findPreference(R.string.pref_key_space_name);
        bindPreferenceSummaryToValue(
                spacesListPreference, chosenSpacePref.getHackerSpace().space);

        dashclockPref = (IntegrationPreference) findPreference(R.string.pref_key_dashclock_integration);

        PreferenceCategory integrations = (PreferenceCategory) findPreference(R.string.pref_key_integrations);
        boolean fromDashClock = getArguments().getBoolean(DashClockExtension.EXTRA_FROM_DASHCLOCK_SETTINGS, false);
        if (integrations != null && fromDashClock) {
            integrations.removePreference(dashclockPref);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Timber.d("Directories requested");
        spaceApiService.directory().enqueue(new Callback<Directory>() {
            @Override
            public void onResponse(Response<Directory> response, Retrofit retrofit) {
                Timber.d("Directories response");
                if (!response.isSuccess()) {
                    showError();
                    return;
                }

                spaces = response.body();
                Collections.sort(spaces);

                spacesListPreference.setEnabled(true);

                String[] entries = new String[spaces.size()];
                for (int i = 0; i < spaces.size(); i++) {
                    HackerSpace space = spaces.get(i);
                    entries[i] = space.space;
                }
                spacesListPreference.setEntries(entries);
                spacesListPreference.setEntryValues(entries);

                bindPreferenceSummaryToValue(spacesListPreference, chosenSpacePref.getHackerSpace().space);
            }

            @Override
            public void onFailure(Throwable t) {
                showError();
            }
        });
    }

    private void showError() {
        try {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.error_title)
                    .setMessage(R.string.error_message)
                    .setNegativeButton(android.R.string.ok, null)
                    .show();
        } catch (Exception e) {
            try {
                Toast.makeText(getActivity(), R.string.error_message, Toast.LENGTH_SHORT).show();
            } catch (Exception ignored) {
            }
        }
    }

    @Nullable
    public Preference findPreference(@StringRes int keyResource) {
        return findPreference(getString(keyResource));
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        dashclockPref.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        dashclockPref.pause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        new BackupManager(getActivity()).dataChanged();


        if (getString(R.string.pref_key_space_name).equals(s)) {
            int chosenIndex = spaces.indexOf(chosenSpacePref.getHackerSpace());
            chosenSpacePref.saveHackerSpace(chosenIndex > 0 ? spaces.get(chosenIndex) : null);

            getActivity().sendBroadcast(new Intent(StatusService.SETTINGS_CHANGED_EVENT));
            analytics.sendEvent("Settings", "Chosen Space",
                    chosenSpacePref.getHackerSpace().toString());
        }

        // Potentially enable/disable the launcher activity if the settings button
        // preference has changed.
        final String launcherIntentKey = getString(R.string.pref_key_launcher_intent);
        if (isAdded() && launcherIntentKey.equals(s)) {

            final boolean hideLauncher = sharedPreferences.getBoolean(launcherIntentKey, false);
            getActivity().getPackageManager().setComponentEnabledSetting(
                    new ComponentName(
                            getActivity().getPackageName(),
                            LAUNCHER_ACTIVITY_NAME),
                    hideLauncher
                            ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                            : PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }

    @NonNull
    @Override
    @SuppressWarnings("ConstantConditions")
    public View getView() {
        return super.getView();
    }

    /**
     * A preference value change listener that updates the preference's summary to reflect its new
     * value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener
            = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            if (value == null) {
                return false;
            }
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? (listPreference.getEntries()[index])
                                .toString().replaceAll("%", "%%")
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the preference's value is
     * changed, its summary (line of text below the preference title) is updated to reflect the
     * value. The summary is also immediately updated upon calling this method. The exact display
     * format is dependent on the type of preference.
     */
    public static void bindPreferenceSummaryToValue(Preference preference, String pref) {
        setAndCallPreferenceChangeListener(preference, sBindPreferenceSummaryToValueListener, pref);
    }

    /**
     * When the preference's value is changed, trigger the given listener. The listener is also
     * immediately called with the preference's current value upon calling this method.
     */
    public static void setAndCallPreferenceChangeListener(Preference preference,
                                                          Preference.OnPreferenceChangeListener listener,
                                                          String pref) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(listener);

        // Trigger the listener immediately with the preference's
        // current value.
        listener.onPreferenceChange(preference, pref);
    }

}
