use tauri::{AppHandle, command, Runtime};

use crate::models::*;
use crate::Result;
#[cfg(mobile)]
use crate::AliyunPushExt;

#[cfg(mobile)]
#[command]
pub(crate) async fn initialize<R: Runtime>(
    app: AppHandle<R>,
    config: InitializeConfig,
) -> Result<InitializeResponse> {
    app.aliyun_push().initialize(config)
}

#[cfg(mobile)]
#[command]
pub(crate) async fn get_device_id<R: Runtime>(
    app: AppHandle<R>,
) -> Result<DeviceIdResponse> {
    app.aliyun_push().get_device_id()
}

#[cfg(mobile)]
#[command]
pub(crate) async fn bind_account<R: Runtime>(
    app: AppHandle<R>,
    account: String,
) -> Result<OperationResponse> {
    app.aliyun_push().bind_account(account)
}

#[cfg(mobile)]
#[command]
pub(crate) async fn unbind_account<R: Runtime>(
    app: AppHandle<R>,
) -> Result<OperationResponse> {
    app.aliyun_push().unbind_account()
}

#[cfg(mobile)]
#[command]
pub(crate) async fn bind_tag<R: Runtime>(
    app: AppHandle<R>,
    request: TagRequest,
) -> Result<OperationResponse> {
    app.aliyun_push().bind_tag(request)
}

#[cfg(mobile)]
#[command]
pub(crate) async fn unbind_tag<R: Runtime>(
    app: AppHandle<R>,
    request: TagRequest,
) -> Result<OperationResponse> {
    app.aliyun_push().unbind_tag(request)
}

#[cfg(mobile)]
#[command]
pub(crate) async fn bind_alias<R: Runtime>(
    app: AppHandle<R>,
    alias: String,
) -> Result<OperationResponse> {
    app.aliyun_push().bind_alias(alias)
}

#[cfg(mobile)]
#[command]
pub(crate) async fn unbind_alias<R: Runtime>(
    app: AppHandle<R>,
    alias: String,
) -> Result<OperationResponse> {
    app.aliyun_push().unbind_alias(alias)
}

// Desktop stub implementations
#[cfg(desktop)]
#[command]
pub(crate) async fn initialize<R: Runtime>(
    _app: AppHandle<R>,
    _config: InitializeConfig,
) -> Result<InitializeResponse> {
    Err(crate::Error::Msg("Aliyun Push is only available on mobile platforms".to_string()))
}

#[cfg(desktop)]
#[command]
pub(crate) async fn get_device_id<R: Runtime>(
    _app: AppHandle<R>,
) -> Result<DeviceIdResponse> {
    Err(crate::Error::Msg("Aliyun Push is only available on mobile platforms".to_string()))
}

#[cfg(desktop)]
#[command]
pub(crate) async fn bind_account<R: Runtime>(
    _app: AppHandle<R>,
    _account: String,
) -> Result<OperationResponse> {
    Err(crate::Error::Msg("Aliyun Push is only available on mobile platforms".to_string()))
}

#[cfg(desktop)]
#[command]
pub(crate) async fn unbind_account<R: Runtime>(
    _app: AppHandle<R>,
) -> Result<OperationResponse> {
    Err(crate::Error::Msg("Aliyun Push is only available on mobile platforms".to_string()))
}

#[cfg(desktop)]
#[command]
pub(crate) async fn bind_tag<R: Runtime>(
    _app: AppHandle<R>,
    _request: TagRequest,
) -> Result<OperationResponse> {
    Err(crate::Error::Msg("Aliyun Push is only available on mobile platforms".to_string()))
}

#[cfg(desktop)]
#[command]
pub(crate) async fn unbind_tag<R: Runtime>(
    _app: AppHandle<R>,
    _request: TagRequest,
) -> Result<OperationResponse> {
    Err(crate::Error::Msg("Aliyun Push is only available on mobile platforms".to_string()))
}

#[cfg(desktop)]
#[command]
pub(crate) async fn bind_alias<R: Runtime>(
    _app: AppHandle<R>,
    _alias: String,
) -> Result<OperationResponse> {
    Err(crate::Error::Msg("Aliyun Push is only available on mobile platforms".to_string()))
}

#[cfg(desktop)]
#[command]
pub(crate) async fn unbind_alias<R: Runtime>(
    _app: AppHandle<R>,
    _alias: String,
) -> Result<OperationResponse> {
    Err(crate::Error::Msg("Aliyun Push is only available on mobile platforms".to_string()))
}