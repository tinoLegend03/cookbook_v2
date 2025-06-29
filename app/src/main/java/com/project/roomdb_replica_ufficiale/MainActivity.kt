package com.project.roomdb_replica_ufficiale

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.drawerlayout.widget.DrawerLayout

import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.project.roomdb_replica_ufficiale.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //GESTIONE TEMA SCUSO
        //non so se contesto corretto
        val preference = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        var theme: String
        if(Build.VERSION.SDK_INT < 29){
            theme = preference.getString("theme", "Light mode") ?: "Light mode"

        } else {
            theme = preference.getString("theme", "System") ?: "System"
        }
        Log.d("THEME VALUE", theme)
        if(theme == "Dark mode"){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else if (theme == "Light mode") {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        //FINE GESTIONE TEMA SCURO


        // Toolbar + Drawer
        val toolbar: Toolbar = binding.toolbar
        drawerLayout = findViewById(R.id.drawer_layout)
        // Imposta la Toolbar come ActionBar
        setSupportActionBar(toolbar)

        // Configura NavController
        navController = findNavController(R.id.nav_host_fragment)

        //Top-Level: fragment con icono hamburger
        val topLevel= setOf(
            R.id.homeFragment
        )

        // AppBarConfiguration
        appBarConfiguration = AppBarConfiguration(topLevel, drawerLayout)

        //  Toolbar + NavigationView
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        // 2. Listener per bloccare il drawer nei secondari
        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d("NAV", "dest = ${destination.label} (${destination.id})")
            if (destination.id in topLevel) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }
    }

    // Gestisce il click sull'icona hamburger o il tasto back
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}