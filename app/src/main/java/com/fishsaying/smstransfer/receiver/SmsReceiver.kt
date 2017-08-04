package com.fishsaying.smstransfer.receiver

import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.telephony.SmsMessage
import android.util.Log
import com.fishsaying.smstransfer.entity.Message


/**
 * Created by dennis on 2017/8/2.
 * introduction:
 *
 */
class SmsReceiver : BroadcastReceiver() {
    var listener: MessageListener? =null
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("TAG","onReceive")
        val bundle = intent.extras
        var msg: SmsMessage? = null
        if (null != bundle) {
            val objects = bundle.get("pdus") as Array<Any>
            for (`object` in objects) {
                msg = SmsMessage.createFromPdu(`object` as ByteArray)
                val body = msg!!.messageBody
                val address = msg.originatingAddress
                Log.d("TAG","body=$body,address=$address")
                listener?.OnReceived(Message(address, body))
            }
        }
    }

    interface MessageListener {
        fun OnReceived(message: Message)
    }
}