package com.project.roomdb_replica_ufficiale

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

import androidx.appcompat.widget.Toolbar
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


        // Trova la Toolbar e il DrawerLayout dal layout
        val toolbar: Toolbar = binding.toolbar
        drawerLayout = findViewById(R.id.drawer_layout)
//        val navigationView: NavigationView = findViewById(R.id.navigation_view)

        // Imposta la Toolbar come ActionBar
        setSupportActionBar(toolbar)

        // Configura NavController
        val navController = findNavController(R.id.nav_host_fragment)

        // Configura AppBarConfiguration per il drawer
        appBarConfiguration = AppBarConfiguration.Builder(
            setOf(
                R.id.homeFragment,
                R.id.listFragment,
                R.id.addFragment,
                R.id.detailFragment,
            )
        ).setOpenableLayout(drawerLayout).build()


        // Gestione selezione menu navigation drawer
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    navController.navigate(R.id.homeFragment)
                }
                R.id.nav_my_recipes -> {
                    navController.navigate(R.id.listFragment)
                }
                R.id.nav_settings -> {
                    // Apri impostazioni
                }
                R.id.nav_profile -> {
                    // Apri profilo
                }
            }

            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }


        // Collega ActionBar al NavController
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Collega NavigationView al NavController
        binding.navView.setupWithNavController(navController)
    }

    // Gestisce il click sull'icona hamburger o il tasto back
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}