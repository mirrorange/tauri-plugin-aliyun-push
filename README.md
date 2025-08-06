# Tauri Plugin Aliyun Push

Tauri V2 plugin for integrating Aliyun Push SDK on Android, enabling push notifications for mobile applications.

## Features

- Initialize Aliyun Push SDK with AppKey and AppSecret
- Get device ID for push targeting
- Bind/unbind account, tags, and aliases for targeted push
- Manage push channel status
- Handle push notifications and messages
- Android 13+ notification permission support

## Installation

### Rust

Add the plugin to your `Cargo.toml`:

```toml
[dependencies]
tauri-plugin-aliyun-push = { path = "../tauri-plugin-aliyun-push" }
```

### JavaScript/TypeScript

Install the JavaScript bindings:

```bash
npm install tauri-plugin-aliyun-push-api
# or
yarn add tauri-plugin-aliyun-push-api
# or
pnpm add tauri-plugin-aliyun-push-api
```

## Usage

### Rust

Register the plugin in your Tauri app:

```rust
use tauri_plugin_aliyun_push;

fn main() {
    tauri::Builder::default()
        .plugin(tauri_plugin_aliyun_push::init())
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
```

### JavaScript/TypeScript

```typescript
import { 
  initialize, 
  getDeviceId, 
  bindAccount,
  bindTag,
  onPushNotification 
} from 'tauri-plugin-aliyun-push-api';

// Initialize the SDK with your Aliyun app credentials
await initialize({
  appKey: 'YOUR_APP_KEY',
  appSecret: 'YOUR_APP_SECRET'
});

// Get the device ID
const { deviceId } = await getDeviceId();
console.log('Device ID:', deviceId);

// Bind an account for targeted push
await bindAccount('user123');

// Bind tags for group targeting
await bindTag({
  tags: ['vip', 'beijing'],
  target: 1 // TARGET_DEVICE
});

// Listen for push notifications
const unlisten = await onPushNotification((notification) => {
  console.log('Received notification:', notification);
  console.log('Title:', notification.title);
  console.log('Content:', notification.content);
  console.log('Extras:', notification.extras);
});

// Cleanup when done
// unlisten();
```

## Configuration

### Android Setup

The plugin automatically configures the necessary Android permissions and dependencies. However, ensure your Android app has:

1. Internet access for push service
2. Proper ProGuard rules (automatically included)
3. Aliyun Maven repository (automatically configured)

### Required Permissions

The following permissions are automatically added:

- `android.permission.INTERNET`
- `android.permission.ACCESS_NETWORK_STATE`
- `android.permission.ACCESS_WIFI_STATE`
- `android.permission.WAKE_LOCK`
- `android.permission.POST_NOTIFICATIONS` (Android 13+)
- `android.permission.VIBRATE` (optional)

## API Reference

### Functions

#### `initialize(config: InitializeConfig): Promise<InitializeResponse>`
Initialize the Aliyun Push SDK with app credentials.

#### `getDeviceId(): Promise<DeviceIdResponse>`
Get the device ID assigned by Aliyun Push.

#### `bindAccount(account: string): Promise<OperationResponse>`
Bind an account for targeted push notifications.

#### `unbindAccount(): Promise<OperationResponse>`
Unbind the current account.

#### `bindTag(request: TagRequest): Promise<OperationResponse>`
Bind tags for targeted push notifications.

#### `unbindTag(request: TagRequest): Promise<OperationResponse>`
Unbind tags.

#### `bindAlias(alias: string): Promise<OperationResponse>`
Bind an alias for targeted push notifications.

#### `unbindAlias(alias: string): Promise<OperationResponse>`
Unbind an alias.

#### `listTags(target?: number): Promise<TagListResponse>`
List all tags bound to the target.

#### `turnOnPushChannel(): Promise<OperationResponse>`
Turn on the push channel.

#### `turnOffPushChannel(): Promise<OperationResponse>`
Turn off the push channel.

#### `checkPushChannelStatus(): Promise<ChannelStatusResponse>`
Check the push channel status.

#### `requestNotificationPermission(): Promise<{ requested: boolean; reason?: string }>`
Request notification permission (Android 13+).

#### `onPushNotification(callback: (notification: PushNotification) => void): Promise<UnlistenFn>`
Listen for push notifications.

### Constants

- `TARGET_DEVICE = 1` - Target device for tags
- `TARGET_ACCOUNT = 2` - Target account for tags
- `TARGET_ALIAS = 3` - Target alias for tags

## Platform Support

- ✅ Android
- ❌ iOS (not implemented)
- ❌ Desktop (returns error on desktop platforms)

## Notes

1. This plugin is designed for mobile applications using Tauri V2
2. The push service requires network connectivity
3. Initialize the SDK as early as possible in your app lifecycle
4. For Android 13+, request notification permissions explicitly
5. The plugin uses a "channel process" for better push reliability

## License

This plugin follows the Tauri plugin license structure.