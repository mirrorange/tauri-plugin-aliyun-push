const COMMANDS: &[&str] = &[
  "initialize",
  "get_device_id",
  "bind_account",
  "unbind_account",
  "bind_tag",
  "unbind_tag",
  "bind_alias",
  "unbind_alias",
  "list_tags",
  "turn_on_push_channel",
  "turn_off_push_channel",
  "check_push_channel_status",
  "request_notification_permission"
];

fn main() {
  tauri_plugin::Builder::new(COMMANDS)
    .android_path("android")
    .ios_path("ios")
    .build();
}
