import SwiftRs
import Tauri
import UIKit
import WebKit
import CloudPushSDK
import UserNotifications

// MARK: - Supporting Structures

struct DeviceIdResult: Encodable {
    let deviceId: String?
}

struct AccountResult: Encodable {
    let success: Bool
    let error: String?
}

struct TagResult: Encodable {
    let success: Bool
    let error: String?
}

struct TagListResult: Encodable {
    let tags: [String]?
    let error: String?
}

struct AliasResult: Encodable {
    let success: Bool
    let error: String?
}

struct ChannelStatusResult: Encodable {
    let isOpen: Bool
}

struct ChannelOperationResult: Encodable {
    let success: Bool
    let error: String?
}

struct PermissionResult: Encodable {
    let granted: Bool
}

struct BindAccountArgs: Decodable {
    let account: String
}

struct TagOperationArgs: Decodable {
    let tags: [String]
    let target: Int
    let alias: String?
}

struct AliasOperationArgs: Decodable {
    let alias: String
}

// Notification event types matching Android implementation
enum NotificationEventType: String {
    case messageReceived = "onMessage"
    case notificationReceived = "onNotification"
    case notificationOpened = "onNotificationOpened"
    case notificationRemoved = "onNotificationRemoved"
    case notificationClickedWithNoAction = "onNotificationClickedWithNoAction"
    case notificationReceivedInApp = "onNotificationReceivedInApp"
}

// MARK: - Main Plugin Class

class AliyunPushPlugin: Plugin {
    private var isInitialized = false
    private var pendingDeviceToken: Data?
    
    // MARK: - Plugin Lifecycle
    
    @objc override public func load(webview: WKWebView) {
        super.load(webview: webview)
        Logger.debug("AliyunPushPlugin: Loading plugin")
        
        // Set CloudPushSDK log level
        CloudPushSDK.setLogLevel(MPLogLevel.info)
        
        // Register for APNs notifications
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleRemoteNotificationRegistration(_:)),
            name: NSNotification.Name("UIApplicationDidRegisterForRemoteNotificationsWithDeviceToken"),
            object: nil
        )
        
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleRemoteNotificationRegistrationError(_:)),
            name: NSNotification.Name("UIApplicationDidFailToRegisterForRemoteNotificationsWithError"),
            object: nil
        )
        
        // Register for push message notifications
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(onMessageReceived(_:)),
            name: NSNotification.Name("CCPDidReceiveMessageNotification"),
            object: nil
        )
    }
    
    // MARK: - Initialization
    
    @objc public func initialize(_ invoke: Invoke) throws {
        CloudPushSDK.start(withAppkey: "335576918", appSecret: "8765dfdf57ce4650bc11479529b07bbb") { [weak self] result in
            guard let self = self else { return }
            
            if result.success {
                self.isInitialized = true
                Logger.info("AliyunPushPlugin: SDK initialized successfully")
                Logger.info("AliyunPushPlugin: DeviceId: \(CloudPushSDK.getDeviceId() ?? "N/A")")
                
                // Register device token if we have one pending
                if let token = self.pendingDeviceToken {
                    self.registerDeviceToken(token)
                    self.pendingDeviceToken = nil
                }
                
                // Request notification permissions
                self.requestNotificationPermissions()
                
                invoke.resolve(["success": true])
            } else {
                Logger.error("AliyunPushPlugin: SDK initialization failed: \(result.error?.localizedDescription ?? "Unknown error")")
                invoke.reject("SDK initialization failed: \(result.error?.localizedDescription ?? "Unknown error")")
            }
        }
    }
    
    // MARK: - Device ID
    
    @objc public func getDeviceId(_ invoke: Invoke) {
        let deviceId = CloudPushSDK.getDeviceId()
        invoke.resolve(DeviceIdResult(deviceId: deviceId))
    }
    
    // MARK: - Account Binding
    
    @objc public func bindAccount(_ invoke: Invoke) throws {
        let args = try invoke.parseArgs(BindAccountArgs.self)
        
        CloudPushSDK.bindAccount(args.account) { result in
            if result.success {
                invoke.resolve(AccountResult(success: true, error: nil))
            } else {
                let error = result.error?.localizedDescription ?? "Unknown error"
                invoke.resolve(AccountResult(success: false, error: error))
            }
        }
    }
    
    @objc public func unbindAccount(_ invoke: Invoke) {
        CloudPushSDK.unbindAccount { result in
            if result.success {
                invoke.resolve(AccountResult(success: true, error: nil))
            } else {
                let error = result.error?.localizedDescription ?? "Unknown error"
                invoke.resolve(AccountResult(success: false, error: error))
            }
        }
    }
    
    // MARK: - Tag Operations
    
    @objc public func bindTag(_ invoke: Invoke) throws {
        let args = try invoke.parseArgs(TagOperationArgs.self)
        
        CloudPushSDK.bindTag(Int32(args.target), withTags: args.tags, withAlias: args.alias, withCallback: { result in
            if result.success {
                invoke.resolve(TagResult(success: true, error: nil))
            } else {
                let error = result.error?.localizedDescription ?? "Unknown error"
                invoke.resolve(TagResult(success: false, error: error))
            }
        })
    }
    
    @objc public func unbindTag(_ invoke: Invoke) throws {
        let args = try invoke.parseArgs(TagOperationArgs.self)
        
        CloudPushSDK.unbindTag(Int32(args.target), withTags: args.tags, withAlias: args.alias, withCallback: { result in
            if result.success {
                invoke.resolve(TagResult(success: true, error: nil))
            } else {
                let error = result.error?.localizedDescription ?? "Unknown error"
                invoke.resolve(TagResult(success: false, error: error))
            }
        })
    }
    
    @objc public func listTags(_ invoke: Invoke) {
        CloudPushSDK.listTags(1) { result in
            if result.success {
                let tags = result.data as? [String] ?? []
                invoke.resolve(TagListResult(tags: tags, error: nil))
            } else {
                let error = result.error?.localizedDescription ?? "Unknown error"
                invoke.resolve(TagListResult(tags: nil, error: error))
            }
        }
    }
    
    // MARK: - Alias Operations
    
    @objc public func bindAlias(_ invoke: Invoke) throws {
        let args = try invoke.parseArgs(AliasOperationArgs.self)
        
        CloudPushSDK.addAlias(args.alias) { result in
            if result.success {
                invoke.resolve(AliasResult(success: true, error: nil))
            } else {
                let error = result.error?.localizedDescription ?? "Unknown error"
                invoke.resolve(AliasResult(success: false, error: error))
            }
        }
    }
    
    @objc public func unbindAlias(_ invoke: Invoke) throws {
        let args = try invoke.parseArgs(AliasOperationArgs.self)
        
        CloudPushSDK.removeAlias(args.alias) { result in
            if result.success {
                invoke.resolve(AliasResult(success: true, error: nil))
            } else {
                let error = result.error?.localizedDescription ?? "Unknown error"
                invoke.resolve(AliasResult(success: false, error: error))
            }
        }
    }
    
    // MARK: - Push Channel Operations
    
    @objc public func turnOnPushChannel(_ invoke: Invoke) {
        // iOS doesn't have a direct equivalent to Android's channel on/off
        // We can use notification permissions as a proxy
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
            if let error = error {
                invoke.resolve(ChannelOperationResult(success: false, error: error.localizedDescription))
            } else {
                invoke.resolve(ChannelOperationResult(success: granted, error: granted ? nil : "Permission denied"))
            }
        }
    }
    
    @objc public func turnOffPushChannel(_ invoke: Invoke) {
        // iOS doesn't allow programmatically disabling notifications
        // Return success but note that user must disable in Settings
        invoke.resolve(ChannelOperationResult(
            success: true,
            error: "Please disable notifications in iOS Settings"
        ))
    }
    
    @objc public func checkPushChannelStatus(_ invoke: Invoke) {
        UNUserNotificationCenter.current().getNotificationSettings { settings in
            let isOpen = settings.authorizationStatus == .authorized || 
                        settings.authorizationStatus == .provisional
            invoke.resolve(ChannelStatusResult(isOpen: isOpen))
        }
    }
    
    // MARK: - Permission Request
    
    @objc public func requestNotificationPermission(_ invoke: Invoke) {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
            DispatchQueue.main.async {
                if granted {
                    UIApplication.shared.registerForRemoteNotifications()
                }
                invoke.resolve(PermissionResult(granted: granted))
            }
        }
    }
    
    // MARK: - Private Helper Methods
    
    private func requestNotificationPermissions() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
            DispatchQueue.main.async {
                if granted {
                    UIApplication.shared.registerForRemoteNotifications()
                    Logger.info("AliyunPushPlugin: Notification permissions granted")
                } else {
                    Logger.info("AliyunPushPlugin: Notification permissions denied")
                }
            }
        }
    }
    
    private func registerDeviceToken(_ deviceToken: Data) {
        CloudPushSDK.registerDevice(deviceToken) { result in
            if result.success {
                Logger.info("AliyunPushPlugin: Device token registered successfully")
            } else {
                Logger.error("AliyunPushPlugin: Failed to register device token: \(result.error?.localizedDescription ?? "Unknown error")")
            }
        }
    }
    
    // MARK: - Notification Handlers
    
    @objc private func handleRemoteNotificationRegistration(_ notification: Notification) {
        guard let deviceToken = notification.object as? Data else { return }
        
        if isInitialized {
            registerDeviceToken(deviceToken)
        } else {
            // Store token for later registration
            pendingDeviceToken = deviceToken
        }
    }
    
    @objc private func handleRemoteNotificationRegistrationError(_ notification: Notification) {
        if let error = notification.object as? Error {
            Logger.error("AliyunPushPlugin: Failed to register for remote notifications: \(error.localizedDescription)")
        }
    }
    
    @objc private func onMessageReceived(_ notification: Notification) {
        guard let data = notification.object as? [String: Any],
              let title = data["title"] as? String,
              let content = data["content"] as? String else {
            return
        }
        
        Logger.info("AliyunPushPlugin: Message received - Title: \(title), Content: \(content)")
        
        // Emit event to JavaScript
        var event: JSObject = [
            "type": NotificationEventType.messageReceived.rawValue,
            "title": title,
            "content": content
        ]
        
        // Convert extras to JSObject-compatible format
        var extras: JSObject = [:]
        for (key, value) in data {
            let keyStr = key as String
            if let strValue = value as? String {
                extras[keyStr] = strValue
            } else if let boolValue = value as? Bool {
                extras[keyStr] = boolValue
            } else if let intValue = value as? Int {
                extras[keyStr] = intValue
            } else if let doubleValue = value as? Double {
                extras[keyStr] = doubleValue
            } else {
                extras[keyStr] = String(describing: value)
            }
        }
        event["extras"] = extras
        
        trigger("pushNotification", data: event)
    }
}

@_cdecl("init_plugin_aliyun_push")
func initPlugin() -> Plugin {
    return AliyunPushPlugin()
}