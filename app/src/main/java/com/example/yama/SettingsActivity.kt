package com.example.yama

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //action bar "back"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content,SettingsFragment())
            .commit()
    }

    class SettingsFragment:PreferenceFragmentCompat(){
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

            setPreferencesFromResource(R.xml.preferences,rootKey)//sets the layout
        }
    }

}