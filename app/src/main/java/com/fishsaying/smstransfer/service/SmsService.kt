package com.fishsaying.smstransfer.service

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import com.fishsaying.smstransfer.entity.Contact
import com.fishsaying.smstransfer.entity.Message
import com.fishsaying.smstransfer.receiver.SmsReceiver
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jetbrains.anko.toast


@Suppress("NAME_SHADOWING")
/**
 * Created by dennis on 2017/8/2.
 * introduction:
 */

class SmsService : Service() {
    var isTransfer: Boolean = true;
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val sp = getSharedPreferences("smsTransfer", 0)
        val receiver = SmsReceiver()
        var phoneNumber: String = ""
        var keyWord: ArrayList<String> = ArrayList()
        var contact: ArrayList<Contact> = ArrayList()
        receiver.listener = object : SmsReceiver.MessageListener {
            override fun OnReceived(message: Message) {
                if (message.address.contains("your main phone number")) {
                    when (message.content) {
                        "open" -> {
                            isTransfer = true
                            toast("转发已开启")
                            return
                        }
                        "close" -> {
                            isTransfer = false
                            toast("转发已关闭")
                            return
                        }
                    }
                }

                if (!isTransfer) {
                    return
                }
                val text = """form ${message.address}:
${message.content}""".trim()
                if (text.isNullOrEmpty()) {
                    return
                }
                if (sp.contains("data")) {
                    val dataSource: String = sp.getString("data", "")
                    keyWord = Gson().fromJson(dataSource, object : TypeToken<List<String>>() {
                    }.type)
                }

                if (sp.contains("contact")) {
                    val contactSource: String = sp.getString("contact", "")
                    contact = Gson().fromJson(contactSource, object : TypeToken<List<Contact>>() {
                    }.type)
                }

                if (sp.contains("target")) {
                    phoneNumber = sp.getString("target", "")
                }
                if (phoneNumber.isEmpty()) {
                    return
                }
                if (keyWord.isEmpty()) {
                    sendSMS(phoneNumber, text)
                    return
                }
                if (contact.isNotEmpty()) {
                    contact.forEach {
                        if (message.address.contains(it.phoneNumber)) {
                            val text = """form ${message.address}(${it.name}):
${message.content}""".trim()
                            sendSMS(phoneNumber, text)
                            return
                        }
                    }
                }
                keyWord.forEach {
                    if (text.contains(it)) {
                        sendSMS(phoneNumber, text)
                        return
                    }
                }

            }
        }
        val filter = IntentFilter("android.provider.Telephony.SMS_RECEIVED")
        registerReceiver(receiver, filter)
    }

    fun sendSMS(phoneNumber: String, message: String) {
        val smsManager = android.telephony.SmsManager.getDefault()
        val divideContents = smsManager.divideMessage(message)
        for (text in divideContents) {
            smsManager.sendTextMessage(phoneNumber, null, text, null, null)
        }

    }

}
