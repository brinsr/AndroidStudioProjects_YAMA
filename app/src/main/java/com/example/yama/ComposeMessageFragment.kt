package com.example.yama

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SmsManager.getDefault
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.example.yama.databinding.FragmentSecondBinding
import com.google.android.material.snackbar.Snackbar

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ComposeMessageFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val ACTION_SMS_STATUS = "package com.example.yama.ACTION_SMS_STATUS"
    private val TAG = "ComposeMessageFragment"
    private val REQUEST_SELECT_PHONE_NUMBER = 1


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //requesting dangerous permissions -this is done only once at runtime
        //From developers.android-https://developer.android.com/training/permissions/requesting#already-granted
        //register the permissions callback

        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    Log.v(TAG, "Permission granted")
                } else {
                    Log.e(TAG, "permission needed to proceed")
                }
            }

        //request permission from user
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED -> {

                selectContact()
            }
            else -> {
                //ask for permission
                requestPermissionLauncher.launch(android.Manifest.permission.SEND_SMS)
            }
        }

//        //register broadcast receiver
//
//        val broadcastReceiver = object :BroadcastReceiver(){
//            override fun onReceive(context: Context?, intent: Intent?) {
//                if (intent != null) {
//                    if(intent.action == ACTION_SMS_STATUS){
//                        if(resultCode == Activity.RESULT_OK){
//                            Toast.makeText(requireContext(),"Message sent!",Toast.LENGTH_LONG)
//                        } else{
//                            Toast.makeText(requireContext(),"Error sending message!",Toast.LENGTH_LONG)
//                        }
//                    }
//                }
//            }
//        }
//        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiver, IntentFilter(ACTION_SMS_STATUS)

        binding.btnPrevious.setOnClickListener {
            findNavController().navigate(R.id.action_ComposeMessageFragment_to_ReadMessageFragment)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    fun selectContact() {
        //sending text using pendingIntent
        // val intent = Intent(ACTION_SMS_STATUS)
        // val pendingIntent = PendingIntent.getBroadcast(requireContext(),0,intent,0)

        // this did not work
        // val smsManager = requireContext().getSystemService<SmsManager>(SmsManager::class.java)
        //this worked
        //val smsManager = SmsManager.getDefault()
        // smsManager.sendTextMessage("5554",null,"Test message",null,null)

        //Search for and select a recipient through Contacts and display destination phone number
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = CommonDataKinds.Phone.CONTENT_TYPE
        }
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivityForResult(intent, REQUEST_SELECT_PHONE_NUMBER)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_SELECT_PHONE_NUMBER && resultCode == Activity.RESULT_OK) {
            //Get the URI and query the content provider for the phone number
            val contactUri: Uri = data!!.data!!
            val cr: ContentResolver = requireContext().contentResolver
            val projection: Array<String> = arrayOf(CommonDataKinds.Phone.NUMBER)
            cr.query(contactUri, projection, null, null, null).use { cursor ->
                //if the cursor returned is valid ,get the phone number
                if (cursor!!.moveToFirst()) {
                    val numberIndex = cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER)
                    val number = cursor.getString(numberIndex)
                    val smsManager = SmsManager.getDefault()

                    binding.btnSend.setOnClickListener {
                        val msg = binding.msgCompose.text.toString()
                        smsManager.sendTextMessage(number, null, msg, null, null)
                        binding.msgCompose.setText("")
                        if (resultCode == Activity.RESULT_OK) {

                            Toast.makeText(requireContext(), "Message sent!", Toast.LENGTH_LONG)
                                .show()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Error sending message!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }

//    override fun onResume() {
//        super.onResume()
//
//        val myPackageName = getPackageName()
//        if(!Telephony.Sms.getDefaultSmsPackage(requireContext()).equals(myPackageName)){
//            //app is not default
//            //show the "not currently set as the default SMS app" interface
//            val viewGroup = findViewById(R.id.not_default_app)
//            viewGroup.setVisibility(View.VISIBLE)
//
//            //Set up a button that allows the user to change the default SMS app
//            val btn_change_default = binding.btnChangeDefault
//            btn_change_default.setOnClickListener{
//                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
//                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, requireContext().packageName)
//                startActivity(intent)
//            }
//        } else{
//            //app is the default
//            //Hide the "not currently set as the dafault SMS app"
//            val viewGroup = findViewById(R.id.not_default_app)
//            viewGroup.setVisibility(View.GONE)
//        }
//    }
}