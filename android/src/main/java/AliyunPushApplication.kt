package com.mirrorange.plugin.aliyunpush

import android.app.Application
import android.content.Context
import android.util.Log
import com.alibaba.sdk.android.push.CloudPushService
import com.alibaba.sdk.android.push.CommonCallback
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory
import com.alibaba.sdk.android.push.noonesdk.PushInitConfig

class AliyunPushApplication : Application() {
    
    companion object {
        private const val TAG = "AliyunPushApplication"
        private var pushService: CloudPushService? = null
        private var isInitialized = false
        private var deviceId: String? = null
        
        // 在这里配置您的 AppKey 和 AppSecret
        // TODO: 请替换为您的实际 AppKey 和 AppSecret
        private const val APP_KEY = "YOUR_APP_KEY"
        private const val APP_SECRET = "YOUR_APP_SECRET"
        
        fun getPushService(): CloudPushService? = pushService
        fun isInitialized(): Boolean = isInitialized
        fun getDeviceId(): String? = deviceId
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application onCreate")
        
        // Check if we are in the main process or channel process
        val processName = getProcessName()
        Log.d(TAG, "Process name: $processName")
        
        // Initialize Push SDK for both main process and channel process
        if (processName == packageName || processName?.contains(":channel") == true) {
            initPushService()
        }
    }
    
    private fun initPushService() {
        try {
            Log.d(TAG, "Initializing Push SDK")
            
            // Check if APP_KEY and APP_SECRET are configured
            if (APP_KEY == "YOUR_APP_KEY" || APP_SECRET == "YOUR_APP_SECRET") {
                Log.e(TAG, "Please configure APP_KEY and APP_SECRET in AliyunPushApplication.kt")
                return
            }
            
            // Configure push initialization
            val pushInitConfig = PushInitConfig.Builder()
                .application(this)
                .appKey(APP_KEY)
                .appSecret(APP_SECRET)
                .disableChannelProcess(false)
                .disableChannelProcessHeartbeat(false)
                .build()
            
            // Initialize Push SDK
            PushServiceFactory.init(pushInitConfig)
            pushService = PushServiceFactory.getCloudPushService()
            
            // Enable debug logging
            pushService?.setLogLevel(CloudPushService.LOG_DEBUG)
            
            Log.d(TAG, "Push SDK initialized successfully")
            
            // Register with push service
            pushService?.register(applicationContext, object : CommonCallback {
                override fun onSuccess(response: String?) {
                    Log.d(TAG, "Push registration successful: $response")
                    deviceId = pushService?.deviceId
                    isInitialized = true
                    Log.i(TAG, "Device ID: $deviceId")
                }
                
                override fun onFailed(errorCode: String?, errorMessage: String?) {
                    Log.e(TAG, "Push registration failed: $errorCode - $errorMessage")
                    isInitialized = false
                }
            })
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize push service", e)
        }
    }
    
    private fun getProcessName(): String? {
        return try {
            val pid = android.os.Process.myPid()
            val manager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            manager.runningAppProcesses?.find { it.pid == pid }?.processName
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get process name", e)
            null
        }
    }
}