import UIKit
import CloudPushSDK
import UserNotifications
import Tauri

// MARK: - AliyunPushAppDelegate

class AliyunPushAppDelegate: NSObject {
    
    static let shared = AliyunPushAppDelegate()
    
    private override init() {
        super.init()
    }
    
    // MARK: - Setup and Configuration
    
    func setupWithApplication(_ application: UIApplication, launchOptions: [UIApplication.LaunchOptionsKey: Any]?) {
        // Set notification center delegate
        UNUserNotificationCenter.current().delegate = self
        
        // Check if app was launched from notification
        if let remoteNotification = launchOptions?[.remoteNotification] as? [AnyHashable: Any] {
            handleNotificationLaunch(remoteNotification)
        }
    }
    
    // MARK: - APNs Registration Handlers
    
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Logger.info("AliyunPushAppDelegate: Received device token")
        
        // Convert token to string format
        let tokenParts = deviceToken.map { data in String(format: "%02.2hhx", data) }
        let token = tokenParts.joined()
        Logger.info("AliyunPushAppDelegate: Device token: \(token)")
        
        // Post notification to plugin
        NotificationCenter.default.post(
            name: NSNotification.Name("UIApplicationDidRegisterForRemoteNotificationsWithDeviceToken"),
            object: deviceToken
        )
        
        // Register with CloudPushSDK
        CloudPushSDK.registerDevice(deviceToken) { result in
            if result.success {
                Logger.info("AliyunPushAppDelegate: Device token registered with Aliyun Push")
            } else {
                Logger.error("AliyunPushAppDelegate: Failed to register device token: \(result.error?.localizedDescription ?? "Unknown error")")
            }
        }
    }
    
    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        Logger.error("AliyunPushAppDelegate: Failed to register for remote notifications: \(error.localizedDescription)")
        
        // Post notification to plugin
        NotificationCenter.default.post(
            name: NSNotification.Name("UIApplicationDidFailToRegisterForRemoteNotificationsWithError"),
            object: error
        )
    }
    
    // MARK: - Remote Notification Handlers
    
    func application(_ application: UIApplication, 
                    didReceiveRemoteNotification userInfo: [AnyHashable: Any],
                    fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        Logger.info("AliyunPushAppDelegate: Received remote notification (silent)")
        
        // Handle notification
        handleRemoteNotification(userInfo, isSilent: true)
        
        // Report notification ack to Aliyun
        CloudPushSDK.sendNotificationAck(userInfo)
        
        completionHandler(.newData)
    }
    
    // MARK: - Private Helper Methods
    
    private func handleNotificationLaunch(_ userInfo: [AnyHashable: Any]) {
        Logger.info("AliyunPushAppDelegate: App launched from notification")
        
        // Emit event for notification launch
        emitNotificationEvent(type: "onNotificationOpened", userInfo: userInfo)
        
        // Report notification ack
        CloudPushSDK.sendNotificationAck(userInfo)
    }
    
    private func handleRemoteNotification(_ userInfo: [AnyHashable: Any], isSilent: Bool = false) {
        // Extract notification data
        let aps = userInfo["aps"] as? [String: Any]
        let alert = aps?["alert"] as? [String: String]
        let title = alert?["title"] ?? ""
        let body = alert?["body"] ?? ""
        
        Logger.info("AliyunPushAppDelegate: Notification - Title: \(title), Body: \(body)")
        
        // Emit appropriate event
        if isSilent {
            emitNotificationEvent(type: "onNotification", userInfo: userInfo)
        } else {
            emitNotificationEvent(type: "onNotificationReceivedInApp", userInfo: userInfo)
        }
    }
    
    private func emitNotificationEvent(type: String, userInfo: [AnyHashable: Any]) {
        // Convert userInfo to proper format
        var eventData: JSObject = [:]
        eventData["type"] = type
        
        // Extract notification content
        if let aps = userInfo["aps"] as? [String: Any],
           let alert = aps["alert"] as? [String: String] {
            eventData["title"] = alert["title"] ?? ""
            eventData["content"] = alert["body"] ?? ""
        }
        
        // Add custom extras
        var extras: JSObject = [:]
        for (key, value) in userInfo {
            if let keyStr = key as? String, keyStr != "aps" {
                // Convert value to JSValue-compatible type
                if let stringValue = value as? String {
                    extras[keyStr] = stringValue
                } else if let boolValue = value as? Bool {
                    extras[keyStr] = boolValue
                } else if let intValue = value as? Int {
                    extras[keyStr] = intValue
                } else if let doubleValue = value as? Double {
                    extras[keyStr] = NSNumber(value: doubleValue)
                } else if let dictValue = value as? [String: Any] {
                    // For nested dictionaries, convert to string representation
                    if let jsonData = try? JSONSerialization.data(withJSONObject: dictValue),
                       let jsonString = String(data: jsonData, encoding: .utf8) {
                        extras[keyStr] = jsonString
                    }
                } else {
                    // Fall back to string description for other types
                    extras[keyStr] = String(describing: value)
                }
            }
        }
        eventData["extras"] = extras
        
        // Trigger event through plugin
        if let plugin = PluginManager.shared.getPlugin(withName: "AliyunPushPlugin") as? AliyunPushPlugin {
            plugin.trigger("aliyun-push-event", data: eventData)
        }
    }
}

// MARK: - UNUserNotificationCenterDelegate

extension AliyunPushAppDelegate: UNUserNotificationCenterDelegate {
    
    // Called when notification arrives while app is in foreground
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                               willPresent notification: UNNotification,
                               withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        Logger.info("AliyunPushAppDelegate: Notification received in foreground")
        
        let userInfo = notification.request.content.userInfo
        handleRemoteNotification(userInfo, isSilent: false)
        
        // Report notification ack
        CloudPushSDK.sendNotificationAck(userInfo)
        
        // Show notification even in foreground
        if #available(iOS 14.0, *) {
            completionHandler([.banner, .sound, .badge, .list])
        } else {
            completionHandler([.alert, .sound, .badge])
        }
    }
    
    // Called when user taps on notification
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                               didReceive response: UNNotificationResponse,
                               withCompletionHandler completionHandler: @escaping () -> Void) {
        Logger.info("AliyunPushAppDelegate: User tapped notification")
        
        let userInfo = response.notification.request.content.userInfo
        
        // Determine if app was in background
        let isBackground = UIApplication.shared.applicationState != .active
        let eventType = isBackground ? "onNotificationOpened" : "onNotificationClickedWithNoAction"
        
        emitNotificationEvent(type: eventType, userInfo: userInfo)
        
        // Report notification click
        CloudPushSDK.sendNotificationAck(userInfo)
        
        completionHandler()
    }
}

// MARK: - UIApplicationDelegate Extension

extension AliyunPushAppDelegate: UIApplicationDelegate {
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        setupWithApplication(application, launchOptions: launchOptions)
        return true
    }
}

// MARK: - Helper for Plugin Manager Access

// Note: This is a placeholder for accessing the plugin manager
// The actual implementation would depend on how Tauri manages plugins
struct PluginManager {
    static let shared = PluginManager()
    
    func getPlugin(withName name: String) -> Plugin? {
        // This would be implemented by the Tauri plugin system
        return nil
    }
}