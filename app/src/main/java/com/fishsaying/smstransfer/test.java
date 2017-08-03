package com.fishsaying.smstransfer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.telephony.SmsMessage;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by dennis on 2017/8/2.
 * introduction:
 */

public class test extends Activity {
 SmsReceiver receiver=new SmsReceiver();

    public SmsReceiver getReceiver() {
        receiver.setListener(new SmsReceiver.MessageListener() {
            @Override
            public void OnReceived(@NotNull Message message) {

            }
        });
        return receiver;

    }
}
