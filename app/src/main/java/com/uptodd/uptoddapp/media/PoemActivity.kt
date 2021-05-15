package com.uptodd.uptoddapp.media

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.utilities.ChangeLanguage

class PoemActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ChangeLanguage(this).setLanguage()

        setContentView(R.layout.activity_poem)


//        askPermissions()

//        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val navController = this.findNavController(R.id.poem_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController)

        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.poem_fragment)
        return navController.navigateUp()
    }

}