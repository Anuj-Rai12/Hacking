package com.uptodd.uptoddapp.media

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.utilities.ChangeLanguage

private const val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1


class MusicPlayer : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ChangeLanguage(this).setLanguage()

        setContentView(R.layout.activity_music_player)

//        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val navController = this.findNavController(R.id.music_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController)
        
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.music_fragment)
        return navController.navigateUp()
    }

}