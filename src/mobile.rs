use serde::de::DeserializeOwned;
use tauri::{
  plugin::{PluginApi, PluginHandle},
  AppHandle, Runtime,
};

use crate::models::*;

#[cfg(target_os = "ios")]
tauri::ios_plugin_binding!(init_plugin_aliyun_push);

// initializes the Kotlin or Swift plugin classes
pub fn init<R: Runtime, C: DeserializeOwned>(
  _app: &AppHandle<R>,
  api: PluginApi<R, C>,
) -> crate::Result<AliyunPush<R>> {
  #[cfg(target_os = "android")]
  let handle = api.register_android_plugin("com.mirrorange.plugin.aliyunpush", "AliyunPushPlugin")?;
  #[cfg(target_os = "ios")]
  let handle = api.register_ios_plugin(init_plugin_aliyun_push)?;
  Ok(AliyunPush(handle))
}

/// Access to the aliyun-push APIs.
pub struct AliyunPush<R: Runtime>(PluginHandle<R>);

impl<R: Runtime> AliyunPush<R> {
  pub fn get_device_id(&self) -> crate::Result<DeviceIdResponse> {
    self
      .0
      .run_mobile_plugin("getDeviceId", ())
      .map_err(Into::into)
  }
  
  pub fn bind_account(&self, account: String) -> crate::Result<OperationResponse> {
    self
      .0
      .run_mobile_plugin("bindAccount", AccountRequest { account })
      .map_err(Into::into)
  }
  
  pub fn unbind_account(&self) -> crate::Result<OperationResponse> {
    self
      .0
      .run_mobile_plugin("unbindAccount", ())
      .map_err(Into::into)
  }
  
  pub fn bind_tag(&self, request: TagRequest) -> crate::Result<OperationResponse> {
    self
      .0
      .run_mobile_plugin("bindTag", request)
      .map_err(Into::into)
  }
  
  pub fn unbind_tag(&self, request: TagRequest) -> crate::Result<OperationResponse> {
    self
      .0
      .run_mobile_plugin("unbindTag", request)
      .map_err(Into::into)
  }
  
  pub fn bind_alias(&self, alias: String) -> crate::Result<OperationResponse> {
    self
      .0
      .run_mobile_plugin("bindAlias", AliasRequest { alias })
      .map_err(Into::into)
  }
  
  pub fn unbind_alias(&self, alias: String) -> crate::Result<OperationResponse> {
    self
      .0
      .run_mobile_plugin("unbindAlias", AliasRequest { alias })
      .map_err(Into::into)
  }
  
  pub fn list_tags(&self, target: Option<i32>) -> crate::Result<TagListResponse> {
    #[derive(serde::Serialize)]
    struct ListTagsRequest {
      target: i32,
    }
    self
      .0
      .run_mobile_plugin("listTags", ListTagsRequest { target: target.unwrap_or(1) })
      .map_err(Into::into)
  }
  
  pub fn turn_on_push_channel(&self) -> crate::Result<OperationResponse> {
    self
      .0
      .run_mobile_plugin("turnOnPushChannel", ())
      .map_err(Into::into)
  }
  
  pub fn turn_off_push_channel(&self) -> crate::Result<OperationResponse> {
    self
      .0
      .run_mobile_plugin("turnOffPushChannel", ())
      .map_err(Into::into)
  }
  
  pub fn check_push_channel_status(&self) -> crate::Result<ChannelStatusResponse> {
    self
      .0
      .run_mobile_plugin("checkPushChannelStatus", ())
      .map_err(Into::into)
  }
  
  pub fn request_notification_permission(&self) -> crate::Result<serde_json::Value> {
    self
      .0
      .run_mobile_plugin("requestNotificationPermission", ())
      .map_err(Into::into)
  }
}
