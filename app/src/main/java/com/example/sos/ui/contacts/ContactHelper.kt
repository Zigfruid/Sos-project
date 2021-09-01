package com.example.sos.ui.contacts

import android.database.Cursor
import android.provider.ContactsContract
import androidx.fragment.app.Fragment

abstract class ContactHelper(viewResId: Int) : Fragment(viewResId) {

    private lateinit var storeContacts:ArrayList<Map<String,String>>

   fun getContactsIntoArrayList() : ArrayList<Map<String,String>> {
       storeContacts = ArrayList()
       val cursor: Cursor? =context?.contentResolver!!.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
        while (cursor!!.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            val map:MutableMap<String,String> = mutableMapOf()
            map["Name"]=name
            map["Number"]=phoneNumber
            storeContacts.add(map)
        }
        cursor.close()
       return storeContacts
    }
}