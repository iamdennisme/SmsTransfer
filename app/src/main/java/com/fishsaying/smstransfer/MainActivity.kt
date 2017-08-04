package com.fishsaying.smstransfer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import com.fishsaying.smstransfer.service.SmsService
import com.fishsaying.smstransfer.util.PermissionsUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton
import android.provider.ContactsContract
import android.content.ContentResolver
import android.content.AsyncQueryHandler
import android.database.Cursor
import com.fishsaying.smstransfer.entity.Contact


class MainActivity : AppCompatActivity() {
    val contactIdMap: HashMap<Int, Contact> = HashMap()
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
        initContact()
        initService()
    }

    private fun initContact() {
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.DATA1, ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
        val handler = MyAsyncQueryHandler(contentResolver)
        val array: Array<String>? = null
        handler.startQuery(0, "", uri, projection, "", array,
                "sort_key COLLATE LOCALIZED asc")
    }

    @SuppressLint("HandlerLeak")
    private inner class MyAsyncQueryHandler(cr: ContentResolver) : AsyncQueryHandler(cr) {
        override fun onQueryComplete(token: Int, cookie: Any, cursor: Cursor?) {
            if (cursor != null && cursor.count > 0) {
                val list = ArrayList<Contact>()
                cursor.moveToFirst()
                for (i in 0..cursor.count - 1) {
                    cursor.moveToPosition(i)
                    val name = cursor.getString(0)
                    val number = cursor.getString(1)
                    val contactId = cursor.getInt(2)
                    if (contactIdMap.containsKey(contactId)) {
                        // 无操作
                    } else {
                        // 创建联系人对象
                        val contact = Contact(name, number.trim().replace("-","").replace(" ",""))
                        list.add(contact)
                       contactIdMap.put(contactId, contact)
                    }
                }
                if (list.size > 0) {
                    val edit = sp.edit()
                    edit.putString("contact", Gson().toJson(list))
                    edit.apply()
                }
            }

            super.onQueryComplete(token, cookie, cursor)
        }

    }

    private fun initService() {
        val startIntent = Intent(this, SmsService::class.java)
        startService(startIntent)
    }

    private fun initPermission() {
        val permission = ArrayList<String>()
        permission.add(Manifest.permission.RECEIVE_SMS)
        permission.add(Manifest.permission.READ_SMS)
        permission.add(Manifest.permission.SEND_SMS)
        permission.add(Manifest.permission.READ_CONTACTS)
        permission.add(Manifest.permission.WRITE_CONTACTS)
        permission.add(Manifest.permission.CALL_PHONE)
        val util = PermissionsUtil
        val unGranted = util.checkPermissions(this, permission)
        if (unGranted.isNotEmpty()) {
            util.requestPermissions(this, permission, 520)
        }
    }

    fun initView() {
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
