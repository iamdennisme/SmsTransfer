package com.fishsaying.smstransfer.util

import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import com.fishsaying.smstransfer.entity.KEY_WORD
import com.fishsaying.smstransfer.entity.Message
import com.fishsaying.smstransfer.entity.TARGET_PHONE
import com.fishsaying.smstransfer.http.SmsWxApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tuonbondol.networkutil.isNetworkConnected
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observable
import rx.Observer
import rx.Scheduler
import rx.schedulers.Schedulers
import java.util.function.Consumer

/**
 * Created by ${dennis.huang} on 01/02/2018.
 */

class HandleMessage(private val context: Context) {

    var keyWord: ArrayList<String> = ArrayList()


    private var phoneNumber: String = ""
    private val sp = context.getSharedPreferences("smsTransfer", 0)

    fun handleMessage(message: Message) {
        if (!isNetworkConnected(context)) {
            sendToTargetPhone(message)
        } else {
            sendToWx(message)
        }
    }

    fun sendToWx(message: Message) {
        val baseurl = "*"
        val retrofit: Retrofit = Retrofit
                .Builder()
                .baseUrl(baseurl)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        val smsWxApi: SmsWxApi = retrofit.create(SmsWxApi::class.java)
        val obserable: Observable<Message> = smsWxApi.postMessage(message.name, message.tel, message.content)
        val observer: Observer<Message> = object : Observer<Message> {
            override fun onError(e: Throwable?) {
                sendToTargetPhone(message)
            }

            override fun onNext(t: Message?) {
                Log.d("TAG", "success")
            }

            override fun onCompleted() {

            }
        }
        obserable.subscribeOn(Schedulers.io())
                .subscribe(observer)
    }

    fun sendToTargetPhone(message: Message) {
        if (sp.contains(KEY_WORD)) {
            val dataSource: String = sp.getString(KEY_WORD, "")
            keyWord = Gson().fromJson(dataSource, object : TypeToken<List<String>>() {
            }.type)
        }
        val text = """form ${message.tel}(${message.name}):
        ${message.content}""".trim()
        if (text.isEmpty()) {
            return
        }
        keyWord.forEach {
            if (message.content.contains(it)) {
                sendSMS(text)
                return
            }
        }
    }

    private fun sendSMS(text: String) {
        if (sp.contains(TARGET_PHONE)) {
            phoneNumber = sp.getString(TARGET_PHONE, "")
        }
        if (phoneNumber.isEmpty()) {
            return
        }
        val smsManager = android.telephony.SmsManager.getDefault()
        val divideContents = smsManager.divideMessage(text)
        for (it in divideContents) {
            smsManager.sendTextMessage(this.phoneNumber, null, it, null, null)
        }
    }
}
