use serde::de::DeserializeOwned;
use tauri::{plugin::PluginApi, AppHandle, Runtime};

use crate::models::*;

pub fn init<R: Runtime, C: DeserializeOwned>(
  app: &AppHandle<R>,
  _api: PluginApi<R, C>,
) -> crate::Result<AliyunPush<R>> {
  Ok(AliyunPush(app.clone()))
}

/// Access to the aliyun-push APIs.
pub struct AliyunPush<R: Runtime>(AppHandle<R>);

impl<R: Runtime> AliyunPush<R> {
  pub fn ping(&self, payload: PingRequest) -> crate::Result<PingResponse> {
    Ok(PingResponse {
      value: payload.value,
    })
  }
}
