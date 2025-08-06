package com.mirrorange.plugin.aliyunpush

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import app.tauri.annotation.Command
import app.tauri.annotation.InvokeArg
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
        try {
            // In Tauri V2, arguments are passed as properties on the invoke object
            val args = invoke.parseArgs(InitializeArgs::class.java)
            appKey = args?.appKey
            appSecret = args?.appSecret
            
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
        
        try {
            val account = invoke.parseArgs(AccountArgs::class.java)?.account
            
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
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse arguments", e)
            invoke.reject("Invalid arguments: ${e.message}")
        }
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
        
        try {
            val args = invoke.parseArgs(TagsArgs::class.java)
            
            if (args?.tags.isNullOrEmpty()) {
                invoke.reject("Tags are required")
                return
            }
            
            val target = args.target ?: CloudPushService.DEVICE_TARGET
            val alias = args.alias
            
            pushService?.bindTag(target, args.tags.toTypedArray(), alias, object : CommonCallback {
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
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse arguments", e)
            invoke.reject("Invalid arguments: ${e.message}")
        }
    }
    
    @Command
    fun unbindTag(invoke: Invoke) {
        if (!isInitialized) {
            invoke.reject("Push service not initialized")
            return
        }
        
        try {
            val args = invoke.parseArgs(TagsArgs::class.java)
            
            if (args?.tags.isNullOrEmpty()) {
                invoke.reject("Tags are required")
                return
            }
            
            val target = args.target ?: CloudPushService.DEVICE_TARGET
            val alias = args.alias
            
            pushService?.unbindTag(target, args.tags.toTypedArray(), alias, object : CommonCallback {
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
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse arguments", e)
            invoke.reject("Invalid arguments: ${e.message}")
        }
    }
    
    @Command
    fun bindAlias(invoke: Invoke) {
        if (!isInitialized) {
            invoke.reject("Push service not initialized")
            return
        }
        
        try {
            val alias = invoke.parseArgs(AliasArgs::class.java)?.alias
            
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
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse arguments", e)
            invoke.reject("Invalid arguments: ${e.message}")
        }
    }
    
    @Command
    fun unbindAlias(invoke: Invoke) {
        if (!isInitialized) {
            invoke.reject("Push service not initialized")
            return
        }
        
        try {
            val alias = invoke.parseArgs(AliasArgs::class.java)?.alias
            
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
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse arguments", e)
            invoke.reject("Invalid arguments: ${e.message}")
        }
    }
    
    @Command
    fun listTags(invoke: Invoke) {
        if (!isInitialized) {
            invoke.reject("Push service not initialized")
            return
        }
        
        try {
            val target = invoke.parseArgs(TargetArgs::class.java)?.target ?: CloudPushService.DEVICE_TARGET
            
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
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse arguments", e)
            invoke.reject("Invalid arguments: ${e.message}")
        }
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

// Data classes for command arguments
@InvokeArg
data class InitializeArgs(
    val appKey: String,
    val appSecret: String
)

@InvokeArg
data class AccountArgs(
    val account: String
)

@InvokeArg
data class AliasArgs(
    val alias: String
)

@InvokeArg
data class TagsArgs(
    val tags: List<String>,
    val target: Int? = null,
    val alias: String? = null
)

@InvokeArg
data class TargetArgs(
    val target: Int? = null
)