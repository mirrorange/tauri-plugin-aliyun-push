package com.mirrorange.plugin.aliyunpush

import android.content.Context
import android.util.Log
import com.alibaba.sdk.android.push.MessageReceiver
import com.alibaba.sdk.android.push.notification.CPushMessage
import app.tauri.plugin.PluginManager

class AliyunPushMessageReceiver : MessageReceiver() {
    
    private val TAG = "AliyunPushMessageReceiver"
    
    override fun onNotification(
        context: Context?,
        title: String?,
        summary: String?,
        extraMap: MutableMap<String, String>?
    ) {
        Log.d(TAG, "Received notification - Title: $title, Summary: $summary")
        notifyPlugin("notification", title, summary, extraMap)
    }
    
    override fun onMessage(context: Context?, cPushMessage: CPushMessage?) {
        Log.d(TAG, "Received message - Title: ${cPushMessage?.title}, Content: ${cPushMessage?.content}")
        val extraMap = mutableMapOf<String, String>()
        cPushMessage?.let {
            extraMap["messageId"] = it.messageId ?: ""
            extraMap["appId"] = it.appId ?: ""
        }
        notifyPlugin("message", cPushMessage?.title, cPushMessage?.content, extraMap)
    }
    
    override fun onNotificationOpened(
        context: Context?,
        title: String?,
        summary: String?,
        extraMap: String?
    ) {
        Log.d(TAG, "Notification opened - Title: $title, Summary: $summary")
        val map = parseExtraMap(extraMap)
        notifyPlugin("notificationOpened", title, summary, map)
    }
    
    override fun onNotificationClickedWithNoAction(
        context: Context?,
        title: String?,
        summary: String?,
        extraMap: String?
    ) {
        Log.d(TAG, "Notification clicked with no action - Title: $title, Summary: $summary")
        val map = parseExtraMap(extraMap)
        notifyPlugin("notificationClicked", title, summary, map)
    }
    
    override fun onNotificationReceivedInApp(
        context: Context?,
        title: String?,
        summary: String?,
        extraMap: MutableMap<String, String>?,
        openType: Int,
        openActivity: String?,
        openUrl: String?
    ) {
        Log.d(TAG, "Notification received in app - Title: $title, Summary: $summary")
        val map = extraMap?.toMutableMap() ?: mutableMapOf()
        map["openType"] = openType.toString()
        map["openActivity"] = openActivity ?: ""
        map["openUrl"] = openUrl ?: ""
        notifyPlugin("notificationInApp", title, summary, map)
    }
    
    override fun onNotificationRemoved(context: Context?, messageId: String?) {
        Log.d(TAG, "Notification removed - MessageId: $messageId")
        val map = mutableMapOf<String, String>()
        map["messageId"] = messageId ?: ""
        notifyPlugin("notificationRemoved", null, null, map)
    }
    
    override fun showNotificationNow(context: Context?, map: MutableMap<String, String>?): Boolean {
        // Return true to show notification immediately, false to intercept
        // We'll let the SDK handle notification display by default
        return true
    }
    
    private fun parseExtraMap(extraMapString: String?): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()
        if (!extraMapString.isNullOrEmpty()) {
            try {
                // Parse JSON string to map
                // Simple parsing for key-value pairs
                val pairs = extraMapString.split(",")
                for (pair in pairs) {
                    val keyValue = pair.split(":")
                    if (keyValue.size == 2) {
                        val key = keyValue[0].trim().replace("\"", "").replace("{", "").replace("}", "")
                        val value = keyValue[1].trim().replace("\"", "").replace("{", "").replace("}", "")
                        map[key] = value
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse extra map", e)
            }
        }
        return map
    }
    
    private fun notifyPlugin(type: String, title: String?, content: String?, extraMap: Map<String, String>?) {
        try {
            // Get the plugin instance and notify it
            AliyunPushPlugin.getInstance()?.handlePushNotification(type, title, content, extraMap)
                ?: Log.w(TAG, "Plugin instance not available yet")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to notify plugin", e)
        }
    }
}