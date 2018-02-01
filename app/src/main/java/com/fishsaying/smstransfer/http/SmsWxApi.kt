package com.fishsaying.smstransfer.http;

import com.fishsaying.smstransfer.entity.Message
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import rx.Observable

/**
 * @program: SMSTransfer
 *
 * @description: 微信机器人
 *
 * @author: taicheng
 *
 * @create: 2018-02-01 15:49
 **/

interface SmsWxApi {
    @FormUrlEncoded
    @POST("smstransfer/send")
    fun postMessage(
            @Field("name") name: String,
            @Field("tel") tel: String,
            @Field("content") content: String): Observable<Message>
}