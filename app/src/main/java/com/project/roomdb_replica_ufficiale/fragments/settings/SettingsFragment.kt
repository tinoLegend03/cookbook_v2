package com.project.roomdb_replica_ufficiale.fragments.settings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.project.roomdb_replica_ufficiale.R
import com.project.roomdb_replica_ufficiale.fragments.svolgiricetta.TimerService

/*
PreferenceFragmentCompat è un tipo specifico di fragment che salva le informazioni in DefaultSharedPreferences
 */
class SettingsFragment : PreferenceFragmentCompat() {

    //listener il tema: quando viene impostato è necessario riavviare l'activity per vedere il cambio di tema
    private var listenerList: Preference.OnPreferenceChangeListener = object : Preference.OnPreferenceChangeListener {
        override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
            requireActivity().recreate()
            return true
        }
    }

    /*
    listneer che fondamentalmente crea un intent per accedere all'app impostazioni del dispositivo
    e gestire le notifiche
     */

    private var listenerPreference: Preference.OnPreferenceClickListener = object :
        Preference.OnPreferenceClickListener {
        override fun onPreferenceClick(preference: Preference): Boolean {
            val ctx = requireContext()
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                // Identifica la tua app nella schermata di sistema
                putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)
            }
            startActivity(intent)
            return true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        //ottengo la reference a themePreference
        val themePreference = findPreference<ListPreference>("theme")
        themePreference?.onPreferenceChangeListener = listenerList

        //Non ha senso porre l'opzione settings nei dispositivi che non hanno la dark mode
        //come impostazione di sistema
        if(Build.VERSION.SDK_INT < 29){
            themePreference?.entries = resources.getStringArray(R.array.choose_themev28)
            themePreference?.entryValues = resources.getStringArray(R.array.choose_themev28)
            themePreference?.setDefaultValue("Light mode")
        } else {
            themePreference?.entries = resources.getStringArray(R.array.choose_theme)
            themePreference?.entryValues = resources.getStringArray(R.array.choose_theme)
        }


        val notifyPreference = findPreference<Preference>("pref_system_notification")
        notifyPreference?.onPreferenceClickListener = listenerPreference



    }

}