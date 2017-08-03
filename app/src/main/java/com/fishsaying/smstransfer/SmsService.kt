package com.fishsaying.smstransfer

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


@Suppress("NAME_SHADOWING")
/**
 * Created by dennis on 2017/8/2.
 * introduction:
 */

class SmsService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val sp = getSharedPreferences("smsTransfer", 0)
        val receiver = SmsReceiver()
        var phoneNumber: String = ""
        var keyWord: ArrayList<String> = ArrayList()
        receiver.listener = object : SmsReceiver.MessageListener {
            override fun OnReceived(message: Message) {
                val message = "你收到了从${message.address}发送来的消息内容:${message.content}"
                if (message.isNullOrEmpty()) {
                    return
                }
                if (sp.contains("data")) {
                    val dataSource: String = sp.getString("data", "")
                    keyWord = Gson().fromJson(dataSource, object : TypeToken<List<String>>() {
                    }.type)
                }
                if (sp.contains("target")) {
                    phoneNumber = sp.getString("target", "")
                }
                if (phoneNumber.isEmpty()) {
                    return
                }
                if (keyWord.isEmpty()) {
                    sendSMS(phoneNumber, message)
                    return
                }
                keyWord.forEach {
                    if (message.contains(it)) {
                        sendSMS(phoneNumber, message)
                        return
                    }
                }

            }
        }
        val filter = IntentFilter("android.provider.Telephony.SMS_RECEIVED")
        registerReceiver(receiver, filter)
    }

    fun sendSMS(phoneNumber: String, message: String) {
        // 获取短信管理器
        val smsManager = android.telephony.SmsManager.getDefault()
        // 拆分短信内容（手机短信长度限制）
        val divideContents = smsManager.divideMessage(message)
        for (text in divideContents) {
            smsManager.sendTextMessage(phoneNumber, null, text, null, null)
        }
    }

}
