use tauri::{
  plugin::{Builder, TauriPlugin},
  Manager, Runtime,
};

pub use models::*;

#[cfg(desktop)]
mod desktop;
#[cfg(mobile)]
mod mobile;

mod commands;
mod error;
mod models;

pub use error::{Error, Result};

#[cfg(desktop)]
use desktop::AliyunPush;
#[cfg(mobile)]
use mobile::AliyunPush;

/// Extensions to [`tauri::App`], [`tauri::AppHandle`] and [`tauri::Window`] to access the aliyun-push APIs.
pub trait AliyunPushExt<R: Runtime> {
  fn aliyun_push(&self) -> &AliyunPush<R>;
}

impl<R: Runtime, T: Manager<R>> crate::AliyunPushExt<R> for T {
  fn aliyun_push(&self) -> &AliyunPush<R> {
    self.state::<AliyunPush<R>>().inner()
  }
}

/// Initializes the plugin.
pub fn init<R: Runtime>() -> TauriPlugin<R> {
  Builder::new("aliyun-push")
    .invoke_handler(tauri::generate_handler![
      commands::initialize,
      commands::get_device_id,
      commands::bind_account,
      commands::unbind_account,
      commands::bind_tag,
      commands::unbind_tag,
      commands::bind_alias,
      commands::unbind_alias,
      commands::list_tags,
      commands::turn_on_push_channel,
      commands::turn_off_push_channel,
      commands::check_push_channel_status,
      commands::request_notification_permission,
    ])
    .setup(|app, api| {
      #[cfg(mobile)]
      let aliyun_push = mobile::init(app, api)?;
      #[cfg(desktop)]
      let aliyun_push = desktop::init(app, api)?;
      app.manage(aliyun_push);
      Ok(())
    })
    .build()
}
