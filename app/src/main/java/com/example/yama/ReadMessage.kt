package com.example.yama

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.provider.Telephony.*
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleCursorAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.navigation.fragment.findNavController
//import com.example.yama.databinding.FragmentFirstBinding
import com.example.yama.databinding.FragmentReadmessageBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ReadMessage : Fragment() ,LoaderManager.LoaderCallbacks<Cursor>{
    private val TAG ="ReadMessageFragment"
    private var _binding: com.example.yama.databinding.FragmentReadmessageBinding? = null
    private lateinit var msgAdapter:SimpleCursorAdapter
    private val READ_SMS_REQUEST_CODE = 200
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReadmessageBinding.inflate(inflater, container, false)
       // supportLoaderManager.initLoader(0,null,this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //msg -model,get the data after getting permission
        //requesting dangerous permissions -this is done only once at runtime
        //From developers.android-https://developer.android.com/training/permissions/requesting#already-granted
        //register the permissions callback

        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) {isGranted:Boolean ->
                if(isGranted){
                    Log.v(TAG,"Permission granted")
                }else{
                    Log.e(TAG,"permission needed to proceed")
                }
            }

        //request permission from user
        when{
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_SMS
            ) == PackageManager.PERMISSION_GRANTED -> {
                getAllSmsFromProvider()
            }
            else -> {
                //ask for permission
                requestPermissionLauncher.launch(android.Manifest.permission.READ_SMS)
            }
        }
        //msg-model -the data
        //msg-view
        val listView = binding.msgList//this seems to directly get reference to item in layout

        //msg-controller
        msgAdapter = SimpleCursorAdapter(
                    requireContext(),
                    R.layout.list_item,
                    null,
                    arrayOf(Sms.Inbox.CREATOR,Sms.Inbox.BODY,Sms.Inbox.DATE),
                    intArrayOf(R.id.creator,R.id.msg_body,R.id.msg_date),
                    0
        )

        listView.adapter = msgAdapter

        //load the data
        LoaderManager.getInstance(this).initLoader(0,null,this)
     }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        //VERY VERY IMPORTANT TO INCLUDE id, started working ONLY after including id
        // author,body,date of message
        val projection = arrayOf(Sms.Inbox._ID,Sms.Inbox.CREATOR, Sms.Inbox.BODY, Sms.Inbox.DATE)
        val loader = CursorLoader(requireContext(), Sms.CONTENT_URI,projection,null,null,null)
        return loader
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
       //replace the data
        msgAdapter.swapCursor(data)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        //empty the data
        msgAdapter.swapCursor(null)
    }


    @SuppressLint("SuspiciousIndentation")
    fun getAllSmsFromProvider(){
        //columns
        val projection:Array<String> = arrayOf(Sms.Inbox.CREATOR, Sms.Inbox.BODY, Sms.Inbox.DATE)
        val cr:ContentResolver = requireContext().contentResolver
        val  cursor: Cursor? = cr.query(
            Telephony.Sms.Inbox.CONTENT_URI,
                            projection,
                            null,
                            null,
                            null)

     //   if (cursor != null && cursor.getCount()>0) {
            cursor!!.moveToFirst()
     //   }//move to first item
       if (cursor != null && cursor.getCount()>0) {
            cursor.moveToNext()
        } //go to next item
        cursor!!.close()
    }

}