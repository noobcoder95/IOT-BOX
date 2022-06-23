package com.smartiotdevices.iotbox;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import androidx.core.app.NavUtils;

import com.smartiotdevices.iotbox.mousepadcomponent.AppCompatPreferenceActivity;

public class ActivityMousepadSettings extends AppCompatPreferenceActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("deprecation")
    private void setupSimplePreferencesScreen()
    {
        addPreferencesFromResource(R.xml.pref_connection);
        PreferenceCategory fakeHeader = new PreferenceCategory(this);;
        fakeHeader.setTitle("Keyboard");
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_keyboard);
        bindPreferenceSummaryToValue(findPreference("sensitivity_list"));
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (preference, value) ->
    {
        String stringValue = value.toString();
        if (preference instanceof ListPreference)
        {
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
        }

        else
        {
            preference.setSummary(stringValue);
        }
        return true;
    };

    private static void bindPreferenceSummaryToValue(Preference preference)
    {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }
}
