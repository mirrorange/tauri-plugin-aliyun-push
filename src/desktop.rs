use serde::de::DeserializeOwned;
use tauri::{plugin::PluginApi, AppHandle, Runtime};

pub fn init<R: Runtime, C: DeserializeOwned>(
  app: &AppHandle<R>,
  _api: PluginApi<R, C>,
) -> crate::Result<AliyunPush<R>> {
  Ok(AliyunPush(app.clone()))
}

/// Access to the aliyun-push APIs.
pub struct AliyunPush<R: Runtime>(AppHandle<R>);

impl<R: Runtime> AliyunPush<R> {
  // Desktop platform doesn't support push notifications
  // All methods will return errors when called from desktop
}
