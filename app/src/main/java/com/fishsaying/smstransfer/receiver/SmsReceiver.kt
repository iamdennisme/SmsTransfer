package com.fishsaying.smstransfer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log
import com.fishsaying.smstransfer.entity.CONTACT
import com.fishsaying.smstransfer.entity.Contact
import com.fishsaying.smstransfer.entity.Message
import com.fishsaying.smstransfer.util.HandleMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


/**
 * Created by dennis on 2017/8/2.
 * introduction:
 *
 */
class SmsReceiver : BroadcastReceiver() {
    var contact: ArrayList<Contact> = ArrayList()
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("TAG", "onReceive")
        initContact(context)
        val bundle = intent.extras
        var msg: SmsMessage
        if (null == bundle) {
            return
        }
        val objects = bundle.get("pdus") as Array<*>
        for (dataObject in objects) {
            msg = SmsMessage.createFromPdu(dataObject as ByteArray)
            val body = msg.messageBody
            val address = msg.originatingAddress
            HandleMessage(context).handleMessage(sortMessage(body, address))
        }
    }

    private fun sortMessage(body: String, address: String): Message {
        Log.d("TAG", "body=$body,address=$address")
        contact.forEach {
            if (address.contains(it.phoneNumber)) {
                return Message(address, body, it.name)
            }
        }
        return Message(address, body, "unknown")
    }

    private fun initContact(context: Context) {
        val sp = context.getSharedPreferences("smsTransfer", 0)
        if (sp.contains(CONTACT)) {
            val contactSource: String = sp.getString(CONTACT, "")
            contact = Gson().fromJson(contactSource, object : TypeToken<List<Contact>>() {
            }.type)
        }
    }
}