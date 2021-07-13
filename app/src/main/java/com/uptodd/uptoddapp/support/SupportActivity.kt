package com.uptodd.uptoddapp.support

import android.app.Dialog
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.UpToddDialogs

class SupportActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support)

        val navController = this.findNavController(R.id.support_fragment_activity)
        NavigationUI.setupActionBarWithNavController(this, navController)

//        supportActionBar!!.setHomeButtonEnabled(true);
//        supportActionBar!!.setDisplayHomeAsUpEnabled(true);

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.support_fragment_activity)
        return navController.navigateUp()
    }
}