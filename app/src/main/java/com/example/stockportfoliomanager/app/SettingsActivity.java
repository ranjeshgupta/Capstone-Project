package com.example.stockportfoliomanager.app;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.stockportfoliomanager.app.data.PortContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nish on 11-03-2016.
 */
public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {
    public static final String LOG_TAG = SettingsActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);

        ListPreference portList = (ListPreference)findPreference(getString(R.string.pref_port_key));

        Uri portListUri = PortContract.PortEntry.buildPortUri();
        Cursor cursor = getContentResolver().query(
                portListUri,
                null,
                null,
                null,
                PortContract.PortEntry.COLUMN_PORT_NAME + " ASC;");

        List<String> entries = new ArrayList<String>();
        List<String> entryValues = new ArrayList<String>();

        if (cursor == null) {
            Log.w(LOG_TAG, "cursor is null");
            return;
        } else if (!cursor.moveToFirst()) {
            Log.w(LOG_TAG, "unable to cursor to move first");
            cursor.close();
            return;
        } else {
            cursor.moveToFirst();

            do {
                entries.add(cursor.getString(1));
                entryValues.add(Integer.toString(cursor.getInt(0)));
            } while (cursor.moveToNext());

        }
        cursor.close();

        final CharSequence[] entryCharSeq = entries.toArray(new CharSequence[entries.size()]);
        final CharSequence[] entryValsChar = entryValues.toArray(new CharSequence[entryValues.size()]);
        Log.d(LOG_TAG, "Entries size: " + Integer.toString(entries.size()));
        Log.d(LOG_TAG, "Entry Values size:" + Integer.toString(entryValues.size()));

        portList.setEntries(entryCharSeq);
        portList.setEntryValues(entryValsChar);

        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_port_key)));
    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}
