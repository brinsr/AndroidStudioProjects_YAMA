package com.example.yama
//Yet Another Messaging App
//Note : Even if the dialog lets you choose this as default app, notifications do not show
//all changes specified should be made in manifest Even if Not being used , as per below:
//https://android-developers.googleblog.com/2013/10/getting-your-sms-apps-ready-for-kitkat.html
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.example.yama.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivity"
    private val REQUEST_SET_DEFAULT_SMS_APP = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        if (!isDefaultSmsApp()) {
//            requestSetDefaultSmsApp()
//        }
//        //not receiving notifications, trying to make this app default to receive,
        // so onreceive is triggered


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            navController.navigate(R.id.ComposeMessageFragment)
            //startActivity(Intent(this,ComposeMessageFragment::class.java))
        }
    }

    //to make this app the default SMS app
    override fun onResume() {
        super.onResume()
        if (Telephony.Sms.getDefaultSmsPackage(this) != packageName) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Make YAMA the default app")
            builder.setMessage("Do you want to set YAMA as your default SMS app?")
            builder.setPositiveButton("Yes") { dialog, which ->
                // User has given permission to make the app the default SMS app, do something with it
                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
                startActivity(intent)
                dialog.dismiss()
            }
            builder.setNegativeButton("No") { dialog, which ->
                // User has denied permission to make the app the default SMS app, do something with it
                dialog.dismiss()
            }
            builder.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                Log.d("TAG", "Settings menu pressed")
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun isDefaultSmsApp(): Boolean {
        val myPackage = packageName
        return (Telephony.Sms.getDefaultSmsPackage(this) == packageName)
    }

    private fun requestSetDefaultSmsApp() {
        // Check if the current Android version is Android 11 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
            if (!roleManager.isRoleAvailable(RoleManager.ROLE_SMS)) {
                // SMS role not available, cannot proceed with the request
                return
            }
            if (!roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                // Request the SMS role
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                startActivityForResult(intent, REQUEST_SET_DEFAULT_SMS_APP)
            }
        } else {
            //request to change the default SMS app
            val myPackageName = packageName // The package name of your app
            val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, myPackageName)
            startActivityForResult(intent, REQUEST_SET_DEFAULT_SMS_APP)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SET_DEFAULT_SMS_APP) {
            // Check if the user has set your app as the default SMS app
            if (isDefaultSmsApp()) {
                 // User has set your app as the default SMS app, continue with your app logic
                Log.v("TAG","default app!")
            } else {
                // User has not set your app as the default SMS app, show a message or take appropriate action
                Log.e(TAG,"not default")
            }
        }
    }
}
