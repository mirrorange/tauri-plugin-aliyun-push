package com.mirrorange.plugin.aliyunpush

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import app.tauri.annotation.Command
import app.tauri.annotation.TauriPlugin
import app.tauri.plugin.JSObject
import app.tauri.plugin.Plugin
import app.tauri.plugin.Invoke
import app.tauri.plugin.JSArray
import com.alibaba.sdk.android.push.CloudPushService
import com.alibaba.sdk.android.push.CommonCallback
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory
import com.alibaba.sdk.android.push.noonesdk.PushInitConfig

@TauriPlugin
class AliyunPushPlugin(private val activity: Activity): Plugin(activity) {
    
    private val TAG = "AliyunPushPlugin"
    private var pushService: CloudPushService? = null
    private var isInitialized = false
    
    companion object {
        private var appKey: String? = null
        private var appSecret: String? = null
        private var deviceId: String? = null
        private var pushCallback: ((String, String?, Map<String, String>?) -> Unit)? = null
    }
    
    @Command
    fun initialize(invoke: Invoke) {
        appKey = invoke.getString("appKey")
        appSecret = invoke.getString("appSecret")
        
        if (appKey.isNullOrEmpty() || appSecret.isNullOrEmpty()) {
            invoke.reject("AppKey and AppSecret are required")
            return
        }
        
        val application = activity.application
        
        // Configure push initialization
        val pushInitConfig = PushInitConfig.Builder()
            .application(application)
            .appKey(appKey)
            .appSecret(appSecret)
            .disableChannelProcess(false)
            .disableChannelProcessHeartbeat(false)
            .build()
        
        try {
            // Initialize Push SDK
            PushServiceFactory.init(pushInitConfig)
            pushService = PushServiceFactory.getCloudPushService()
            
            // Enable debug logging
            pushService?.setLogLevel(CloudPushService.LOG_DEBUG)
            
            // Register with push service
            pushService?.register(activity.applicationContext, object : CommonCallback {
                override fun onSuccess(response: String?) {
                    Log.d(TAG, "Push registration successful: $response")
                    deviceId = pushService?.deviceId
                    isInitialized = true
                    
                    val result = JSObject()
                    result.put("success", true)
                    result.put("deviceId", deviceId ?: "")
                    result.put("response", response ?: "")
                    invoke.resolve(result)
                }
                
                override fun onFailed(errorCode: String?, errorMessage: String?) {
                    Log.e(TAG, "Push registration failed: $errorCode - $errorMessage")
                    invoke.reject("Registration failed: $errorCode - $errorMessage")
                }
            })
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize push service", e)
            invoke.reject("Failed to initialize: ${e.message}")
        }
    }
    
    @Command
    fun getDeviceId(invoke: Invoke) {
        if (!isInitialized) {
            invoke.reject("Push service not initialized")
            return
        }
        
        val result = JSObject()
        result.put("deviceId", deviceId ?: "")
        invoke.resolve(result)
    }
    
    @Command
    fun bindAccount(invoke: Invoke) {
        if (!isInitialized) {
            invoke.reject("Push service not initialized")
            return
        }
        
        val account = invoke.getString("account")
        if (account.isNullOrEmpty()) {
            invoke.reject("Account is required")
            return
        }
        
        pushService?.bindAccount(account, object : CommonCallback {
            override fun onSuccess(response: String?) {
                Log.d(TAG, "Account bound successfully: $response")
                val result = JSObject()
                result.put("success", true)
                result.put("response", response ?: "")
                invoke.resolve(result)
            }
            
            override fun onFailed(errorCode: String?, errorMessage: String?) {
                Log.e(TAG, "Failed to bind account: $errorCode - $errorMessage")
                invoke.reject("Failed to bind account: $errorCode - $errorMessage")
            }
        })
    }
    
    @Command
    fun unbindAccount(invoke: Invoke) {
        if (!isInitialized) {
            invoke.reject("Push service not initialized")
            return
        }
        
        pushService?.unbindAccount(object : CommonCallback {
            override fun onSuccess(response: String?) {
                Log.d(TAG, "Account unbound successfully: $response")
                val result = JSObject()
                result.put("success", true)
                result.put("response", response ?: "")
                invoke.resolve(result)
            }
            
            override fun onFailed(errorCode: String?, errorMessage: String?) {
                Log.e(TAG, "Failed to unbind account: $errorCode - $errorMessage")
                invoke.reject("Failed to unbind account: $errorCode - $errorMessage")
            }
        })
    }
    
    @Command
    fun bindTag(invoke: Invoke) {
        if (!isInitialized) {
            invoke.reject("Push service not initialized")
            return
        }
        
        val tagsArray = invoke.getArray("tags")
        if (tagsArray == null || tagsArray.length() == 0) {
            invoke.reject("Tags are required")
            return
        }
        
        val tags = mutableListOf<String>()
        for (i in 0 until tagsArray.length()) {
            tags.add(tagsArray.getString(i))
        }
        
        val target = try {
            invoke.getInteger("target") ?: CloudPushService.DEVICE_TARGET
        } catch (e: Exception) {
            CloudPushService.DEVICE_TARGET
        }
        val alias = invoke.getString("alias")
        
        pushService?.bindTag(target, tags.toTypedArray(), alias, object : CommonCallback {
            override fun onSuccess(response: String?) {
                Log.d(TAG, "Tags bound successfully: $response")
                val result = JSObject()
                result.put("success", true)
                result.put("response", response ?: "")
                invoke.resolve(result)
            }
            
            override fun onFailed(errorCode: String?, errorMessage: String?) {
                Log.e(TAG, "Failed to bind tags: $errorCode - $errorMessage")
                invoke.reject("Failed to bind tags: $errorCode - $errorMessage")
            }
        })
    }
    
    @Command
    fun unbindTag(invoke: Invoke) {
        if (!isInitialized) {
            invoke.reject("Push service not initialized")
            return
        }
        
        val tagsArray = invoke.getArray("tags")
        if (tagsArray == null || tagsArray.length() == 0) {
            invoke.reject("Tags are required")
            return
        }
        
        val tags = mutableListOf<String>()
        for (i in 0 until tagsArray.length()) {
            tags.add(tagsArray.getString(i))
        }
        
        val target = try {
            invoke.getInteger("target") ?: CloudPushService.DEVICE_TARGET
        } catch (e: Exception) {
            CloudPushService.DEVICE_TARGET
        }
        val alias = invoke.getString("alias")
        
        pushService?.unbindTag(target, tags.toTypedArray(), alias, object : CommonCallback {
            override fun onSuccess(response: String?) {
                Log.d(TAG, "Tags unbound successfully: $response")
                val result = JSObject()
                result.put("success", true)
                result.put("response", response ?: "")
                invoke.resolve(result)
            }
            
            override fun onFailed(errorCode: String?, errorMessage: String?) {
                Log.e(TAG, "Failed to unbind tags: $errorCode - $errorMessage")
                invoke.reject("Failed to unbind tags: $errorCode - $errorMessage")
            }
        })
    }
    
    @Command
    fun bindAlias(invoke: Invoke) {
        if (!isInitialized) {
            invoke.reject("Push service not initialized")
            return
        }
        
        val alias = invoke.getString("alias")
        if (alias.isNullOrEmpty()) {
            invoke.reject("Alias is required")
            return
        }
        
        pushService?.addAlias(alias, object : CommonCallback {
            override fun onSuccess(response: String?) {
                Log.d(TAG, "Alias bound successfully: $response")
                val result = JSObject()
                result.put("success", true)
                result.put("response", response ?: "")
                invoke.resolve(result)
            }
            
            override fun onFailed(errorCode: String?, errorMessage: String?) {
                Log.e(TAG, "Failed to bind alias: $errorCode - $errorMessage")
                invoke.reject("Failed to bind alias: $errorCode - $errorMessage")
            }
        })
    }
    
    @Command
    fun unbindAlias(invoke: Invoke) {
        if (!isInitialized) {
            invoke.reject("Push service not initialized")
            return
        }
        
        val alias = invoke.getString("alias")
        if (alias.isNullOrEmpty()) {
            invoke.reject("Alias is required")
            return
        }
        
        pushService?.removeAlias(alias, object : CommonCallback {
            override fun onSuccess(response: String?) {
                Log.d(TAG, "Alias unbound successfully: $response")
                val result = JSObject()
                result.put("success", true)
                result.put("response", response ?: "")
                invoke.resolve(result)
            }
            
            override fun onFailed(errorCode: String?, errorMessage: String?) {
                Log.e(TAG, "Failed to unbind alias: $errorCode - $errorMessage")
                invoke.reject("Failed to unbind alias: $errorCode - $errorMessage")
            }
        })
    }
    
    @Command
    fun listTags(invoke: Invoke) {
        if (!isInitialized) {
            invoke.reject("Push service not initialized")
            return
        }
        
        val target = try {
            invoke.getInteger("target") ?: CloudPushService.DEVICE_TARGET
        } catch (e: Exception) {
            CloudPushService.DEVICE_TARGET
        }
        
        pushService?.listTags(target, object : CommonCallback {
            override fun onSuccess(response: String?) {
                Log.d(TAG, "Tags listed successfully: $response")
                val result = JSObject()
                result.put("success", true)
                result.put("tags", response ?: "")
                invoke.resolve(result)
            }
            
            override fun onFailed(errorCode: String?, errorMessage: String?) {
                Log.e(TAG, "Failed to list tags: $errorCode - $errorMessage")
                invoke.reject("Failed to list tags: $errorCode - $errorMessage")
            }
        })
    }
    
    @Command
    fun turnOnPushChannel(invoke: Invoke) {
        if (!isInitialized) {
            invoke.reject("Push service not initialized")
            return
        }
        
        pushService?.turnOnPushChannel(object : CommonCallback {
            override fun onSuccess(response: String?) {
                Log.d(TAG, "Push channel turned on: $response")
                val result = JSObject()
                result.put("success", true)
                result.put("response", response ?: "")
                invoke.resolve(result)
            }
            
            override fun onFailed(errorCode: String?, errorMessage: String?) {
                Log.e(TAG, "Failed to turn on push channel: $errorCode - $errorMessage")
                invoke.reject("Failed to turn on push channel: $errorCode - $errorMessage")
            }
        })
    }
    
    @Command
    fun turnOffPushChannel(invoke: Invoke) {
        if (!isInitialized) {
            invoke.reject("Push service not initialized")
            return
        }
        
        pushService?.turnOffPushChannel(object : CommonCallback {
            override fun onSuccess(response: String?) {
                Log.d(TAG, "Push channel turned off: $response")
                val result = JSObject()
                result.put("success", true)
                result.put("response", response ?: "")
                invoke.resolve(result)
            }
            
            override fun onFailed(errorCode: String?, errorMessage: String?) {
                Log.e(TAG, "Failed to turn off push channel: $errorCode - $errorMessage")
                invoke.reject("Failed to turn off push channel: $errorCode - $errorMessage")
            }
        })
    }
    
    @Command
    fun checkPushChannelStatus(invoke: Invoke) {
        if (!isInitialized) {
            invoke.reject("Push service not initialized")
            return
        }
        
        pushService?.checkPushChannelStatus(object : CommonCallback {
            override fun onSuccess(response: String?) {
                Log.d(TAG, "Push channel status: $response")
                val result = JSObject()
                result.put("success", true)
                result.put("status", response ?: "")
                invoke.resolve(result)
            }
            
            override fun onFailed(errorCode: String?, errorMessage: String?) {
                Log.e(TAG, "Failed to check push channel status: $errorCode - $errorMessage")
                invoke.reject("Failed to check push channel status: $errorCode - $errorMessage")
            }
        })
    }
    
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Command
    fun requestNotificationPermission(invoke: Invoke) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val activity = activity
            activity.requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
            val result = JSObject()
            result.put("requested", true)
            invoke.resolve(result)
        } else {
            val result = JSObject()
            result.put("requested", false)
            result.put("reason", "Not required for this Android version")
            invoke.resolve(result)
        }
    }
    
    // Method to handle push notifications from MessageReceiver
    fun handlePushNotification(type: String, title: String?, content: String?, extraMap: Map<String, String>?) {
        val data = JSObject()
        data.put("type", type)
        data.put("title", title ?: "")
        data.put("content", content ?: "")
        
        if (extraMap != null && extraMap.isNotEmpty()) {
            val extras = JSObject()
            for ((key, value) in extraMap) {
                extras.put(key, value)
            }
            data.put("extras", extras)
        }
        
        trigger("pushNotification", data)
    }
}