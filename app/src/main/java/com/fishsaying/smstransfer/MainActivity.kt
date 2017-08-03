package com.fishsaying.smstransfer

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton

class MainActivity : AppCompatActivity() {

    val data = ArrayList<String>()
    val sp: SharedPreferences by lazy {
        getSharedPreferences("smsTransfer", 0)
    }
    val adapter: BaseAdapter by lazy {
        ArrayAdapter(this, android.R.layout.simple_list_item_1, data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initPermission()
        initView()
        val startIntent = Intent(this, SmsService::class.java)
        startService(startIntent)
        Log.d("TAG", "startService")
        val edit = sp.edit()
        listView.adapter = adapter

        btnTarget.setOnClickListener {
            val target = editTarget.text.toString().trim()
            if (target.isNullOrEmpty()) {
                toast("请输入正确的电话号码")
                return@setOnClickListener
            }
            edit.putString("target", target)
            edit.apply();
            toast("设置电话号码成功")
        }
        btnKeyWord.setOnClickListener {
            val keyWord = editKeyWord.text.toString().trim()
            if (keyWord.isNullOrEmpty()) {
                toast("请输入正确的关键词")
                return@setOnClickListener
            }
            if (data.contains(keyWord)) {
                toast("重复关键词")
                return@setOnClickListener
            }
            data.add(keyWord)
            adapter.notifyDataSetChanged()
            edit.putString("data", Gson().toJson(data))
            edit.apply()
            toast("添加关键词成功")
        }
        listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, i, _ ->
            alert("确认移除关键词吗") {
                yesButton {
                    data.removeAt(i)
                    adapter.notifyDataSetChanged()
                    edit.putString("data", Gson().toJson(data))
                    edit.apply()
                }
                noButton {

                }
            }.show()

            false
        }
    }

    private fun initPermission() {
        val permission = ArrayList<String>()
        permission.add(Manifest.permission.RECEIVE_SMS)
        permission.add(Manifest.permission.READ_SMS)
        permission.add(Manifest.permission.SEND_SMS)
        val util = PermissionsUtil
        val unGranted = util.checkPermissions(this, permission)
        if (unGranted.isNotEmpty()) {
            util.requestPermissions(this, permission, 520)
        }
    }

    fun initView() {
        if (sp.contains("target")) {
            editTarget.setText(sp.getString("target", ""))
        }
        if (sp.contains("data")) {
            val dataSource: String = sp.getString("data", "")
            val data: ArrayList<String> = Gson().fromJson(dataSource, object : TypeToken<List<String>>() {
            }.type)
            if (data.isEmpty()) {
                return
            }
            this.data.addAll(data)
            adapter.notifyDataSetChanged()
        }
    }
}
