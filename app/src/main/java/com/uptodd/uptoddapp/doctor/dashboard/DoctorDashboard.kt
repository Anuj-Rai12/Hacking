package com.uptodd.uptoddapp.doctor.dashboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.onNavDestinationSelected
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.uptodd.uptoddapp.R


class DoctorDashboard : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doctor_dashboard_activity)


        drawerLayout = findViewById(R.id.doctor_drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view_doctor)
        navController = findNavController(R.id.doctor_dashboard_fragment)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.doctorDashboardFragment,
                R.id.referADoctor,
                R.id.referredListPatient,
                R.id.referredListDoctor,
                R.id.doctorAccount,
                R.id.webinarsFragment,
                R.id.blogsFragment,
                R.id.resourcesFragment
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        setupBottomNav()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<NavigationView>(R.id.nav_view_doctor).menu.getItem(0).isChecked = true
        bottomNavigationView.menu.getItem(0).isChecked = true
    }

    private fun setupBottomNav() {
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.itemIconTintList = null
        val bottomNavController = Navigation.findNavController(this, R.id.doctor_dashboard_fragment)
        bottomNavigationView.setupWithNavController(bottomNavController)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            onNavDestinationSelected(
                item,
                Navigation.findNavController(this, R.id.doctor_dashboard_fragment)
            )
        }
    }
}