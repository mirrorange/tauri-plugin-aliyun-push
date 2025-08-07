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
        private var appKey: String? = null
        private var appSecret: String? = null
        
        fun getPushService(): CloudPushService? = pushService
        fun isInitialized(): Boolean = isInitialized
        fun getDeviceId(): String? = deviceId
        
        fun initializeWithConfig(context: Context, key: String, secret: String, callback: CommonCallback?) {
            if (isInitialized && appKey == key && appSecret == secret) {
                // Already initialized with same config
                callback?.onSuccess("Already initialized")
                return
            }
            
            appKey = key
            appSecret = secret
            
            // Register with push service
            pushService?.register(context, object : CommonCallback {
                override fun onSuccess(response: String?) {
                    Log.d(TAG, "Push registration successful: $response")
                    deviceId = pushService?.deviceId
                    isInitialized = true
                    callback?.onSuccess(response)
                }
                
                override fun onFailed(errorCode: String?, errorMessage: String?) {
                    Log.e(TAG, "Push registration failed: $errorCode - $errorMessage")
                    isInitialized = false
                    callback?.onFailed(errorCode, errorMessage)
                }
            })
        }
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
            
            // First check if AppKey and AppSecret are provided in AndroidManifest
            val appInfo = packageManager.getApplicationInfo(packageName, android.content.pm.PackageManager.GET_META_DATA)
            val metaData = appInfo.metaData
            
            if (metaData != null) {
                val manifestAppKey = metaData.getString("com.alibaba.app.appkey")
                val manifestAppSecret = metaData.getString("com.alibaba.app.appsecret")
                
                if (!manifestAppKey.isNullOrEmpty() && !manifestAppSecret.isNullOrEmpty()) {
                    // Use manifest configuration
                    appKey = manifestAppKey
                    appSecret = manifestAppSecret
                    
                    val pushInitConfig = PushInitConfig.Builder()
                        .application(this)
                        .appKey(manifestAppKey)
                        .appSecret(manifestAppSecret)
                        .disableChannelProcess(false)
                        .disableChannelProcessHeartbeat(false)
                        .build()
                    
                    PushServiceFactory.init(pushInitConfig)
                    Log.d(TAG, "Push SDK initialized with manifest config")
                } else {
                    // Initialize without config, will configure later
                    PushServiceFactory.init(this)
                    Log.d(TAG, "Push SDK initialized without config")
                }
            } else {
                // Initialize without config, will configure later
                PushServiceFactory.init(this)
                Log.d(TAG, "Push SDK initialized without config")
            }
            
            pushService = PushServiceFactory.getCloudPushService()
            
            // Enable debug logging
            // Note: You can control this based on your app's build configuration
            pushService?.setLogLevel(CloudPushService.LOG_DEBUG)
            
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