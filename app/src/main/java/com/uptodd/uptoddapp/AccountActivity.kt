package com.uptodd.uptoddapp

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.uptodd.uptoddapp.databinding.ActivityAccountBinding
import com.uptodd.uptoddapp.ui.todoScreens.TodosListActivity


class AccountActivity : AppCompatActivity() {

    private lateinit var navCtrl: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        DataBindingUtil.setContentView<ActivityAccountBinding>(
            this,
            R.layout.activity_account
        )

        navCtrl = findNavController(R.id.AccountNavHost)
        NavigationUI.setupActionBarWithNavController(this, navCtrl)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navCtrl.navigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this, TodosListActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}