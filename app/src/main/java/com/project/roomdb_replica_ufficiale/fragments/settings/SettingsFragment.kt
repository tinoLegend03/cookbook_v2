package com.project.roomdb_replica_ufficiale.fragments.settings

import android.os.Bundle
import android.preference.ListPreference
import android.preference.PreferenceManager
import androidx.preference.PreferenceFragmentCompat
import com.project.roomdb_replica_ufficiale.R


class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val context = preferenceManager.context

        val themePreference = androidx.preference.ListPreference(context)
//        themePreference.onPreferenceChangeListener =

    }
}