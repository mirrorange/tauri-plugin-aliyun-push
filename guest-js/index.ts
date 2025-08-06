import { invoke } from '@tauri-apps/api/core'
import { listen, type UnlistenFn } from '@tauri-apps/api/event'

export interface InitializeConfig extends Record<string, unknown> {
  appKey: string
  appSecret: string
}

export interface InitializeResponse extends Record<string, unknown> {
  success: boolean
  deviceId: string
  response: string
}

export interface DeviceIdResponse extends Record<string, unknown> {
  deviceId: string
}

export interface OperationResponse extends Record<string, unknown> {
  success: boolean
  response: string
}

export interface TagRequest extends Record<string, unknown> {
  tags: string[]
  target?: number
  alias?: string
}

export interface PushNotification extends Record<string, unknown> {
  type: string
  title: string
  content: string
  extras?: Record<string, any>
}

export interface ChannelStatusResponse extends Record<string, unknown> {
  success: boolean
  status: string
}

export interface TagListResponse extends Record<string, unknown> {
  success: boolean
  tags: string
}

export const TARGET_DEVICE = 1
export const TARGET_ACCOUNT = 2
export const TARGET_ALIAS = 3

/**
 * Initialize Aliyun Push SDK with app credentials
 * @param config - Configuration containing appKey and appSecret
 * @returns Promise with initialization response including device ID
 */
export async function initialize(config: InitializeConfig): Promise<InitializeResponse> {
  return await invoke<InitializeResponse>('plugin:aliyun-push|initialize', config)
}

/**
 * Get the device ID assigned by Aliyun Push
 * @returns Promise with device ID
 */
export async function getDeviceId(): Promise<DeviceIdResponse> {
  return await invoke<DeviceIdResponse>('plugin:aliyun-push|get_device_id')
}

/**
 * Bind an account to receive targeted push notifications
 * @param account - Account identifier
 * @returns Promise with operation response
 */
export async function bindAccount(account: string): Promise<OperationResponse> {
  return await invoke<OperationResponse>('plugin:aliyun-push|bind_account', { account })
}

/**
 * Unbind the current account
 * @returns Promise with operation response
 */
export async function unbindAccount(): Promise<OperationResponse> {
  return await invoke<OperationResponse>('plugin:aliyun-push|unbind_account')
}

/**
 * Bind tags for targeted push notifications
 * @param request - Tag binding request
 * @returns Promise with operation response
 */
export async function bindTag(request: TagRequest): Promise<OperationResponse> {
  return await invoke<OperationResponse>('plugin:aliyun-push|bind_tag', request)
}

/**
 * Unbind tags
 * @param request - Tag unbinding request
 * @returns Promise with operation response
 */
export async function unbindTag(request: TagRequest): Promise<OperationResponse> {
  return await invoke<OperationResponse>('plugin:aliyun-push|unbind_tag', request)
}

/**
 * Bind an alias for targeted push notifications
 * @param alias - Alias identifier
 * @returns Promise with operation response
 */
export async function bindAlias(alias: string): Promise<OperationResponse> {
  return await invoke<OperationResponse>('plugin:aliyun-push|bind_alias', { alias })
}

/**
 * Unbind an alias
 * @param alias - Alias identifier
 * @returns Promise with operation response
 */
export async function unbindAlias(alias: string): Promise<OperationResponse> {
  return await invoke<OperationResponse>('plugin:aliyun-push|unbind_alias', { alias })
}

/**
 * List all tags bound to the target
 * @param target - Target type (device, account, or alias)
 * @returns Promise with tag list response
 */
export async function listTags(target?: number): Promise<TagListResponse> {
  return await invoke<TagListResponse>('plugin:aliyun-push|list_tags', { 
    target: target || TARGET_DEVICE 
  })
}

/**
 * Turn on the push channel
 * @returns Promise with operation response
 */
export async function turnOnPushChannel(): Promise<OperationResponse> {
  return await invoke<OperationResponse>('plugin:aliyun-push|turn_on_push_channel')
}

/**
 * Turn off the push channel
 * @returns Promise with operation response
 */
export async function turnOffPushChannel(): Promise<OperationResponse> {
  return await invoke<OperationResponse>('plugin:aliyun-push|turn_off_push_channel')
}

/**
 * Check the push channel status
 * @returns Promise with channel status response
 */
export async function checkPushChannelStatus(): Promise<ChannelStatusResponse> {
  return await invoke<ChannelStatusResponse>('plugin:aliyun-push|check_push_channel_status')
}

/**
 * Request notification permission (Android 13+)
 * @returns Promise with permission request result
 */
export async function requestNotificationPermission(): Promise<{ requested: boolean; reason?: string }> {
  return await invoke<{ requested: boolean; reason?: string }>('plugin:aliyun-push|request_notification_permission')
}

/**
 * Listen for push notifications
 * @param callback - Callback function to handle push notifications
 * @returns Promise with unlisten function
 */
export async function onPushNotification(
  callback: (notification: PushNotification) => void
): Promise<UnlistenFn> {
  return await listen<PushNotification>('pushNotification', (event) => {
    callback(event.payload)
  })
}

export default {
  initialize,
  getDeviceId,
  bindAccount,
  unbindAccount,
  bindTag,
  unbindTag,
  bindAlias,
  unbindAlias,
  listTags,
  turnOnPushChannel,
  turnOffPushChannel,
  checkPushChannelStatus,
  requestNotificationPermission,
  onPushNotification,
  TARGET_DEVICE,
  TARGET_ACCOUNT,
  TARGET_ALIAS
}
