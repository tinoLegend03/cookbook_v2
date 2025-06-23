package com.project.roomdb_replica_ufficiale.fragments.settings

import android.os.Build
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.project.roomdb_replica_ufficiale.R


class SettingsFragment : PreferenceFragmentCompat() {

    private var listener: Preference.OnPreferenceChangeListener = object : Preference.OnPreferenceChangeListener {
        override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
            requireActivity().recreate()
            return true
        }

    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val themePreference = findPreference<ListPreference>("theme")
        themePreference?.onPreferenceChangeListener = listener

        if(Build.VERSION.SDK_INT < 29){
            themePreference?.entries = resources.getStringArray(R.array.choose_themev28)
            themePreference?.entryValues = resources.getStringArray(R.array.choose_themev28)
            themePreference?.setDefaultValue("Light mode")
        } else {
            themePreference?.entries = resources.getStringArray(R.array.choose_theme)
            themePreference?.entryValues = resources.getStringArray(R.array.choose_theme)
        }

    }

}