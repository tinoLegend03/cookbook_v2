package com.project.roomdb_replica_ufficiale

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.navigation.NavigationView
import com.project.roomdb_replica_ufficiale.fragments.detail.DetailFragment


import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.project.roomdb_replica_ufficiale.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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