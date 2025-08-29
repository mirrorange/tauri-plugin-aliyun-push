use std::path::PathBuf;

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
    "request_notification_permission",
    "registerListener",
    "remove_listener",
];

fn main() {
    // Configure iOS framework linking
    let frameworks = vec!["CloudPushSDK", "AlicloudELS", "UTDID"];

    for framework_name in frameworks {
        link_framework(framework_name);
    }

    tauri_plugin::Builder::new(COMMANDS)
        .android_path("android")
        .ios_path("ios")
        .build();
}

fn link_framework(framework_name: &str) {
    let target_os = std::env::var("CARGO_CFG_TARGET_OS").unwrap();
    let is_simulator = false;
    if target_os != "ios" {
        // Skip linking for non-ios targets
        return;
    }

    let arch_name = if is_simulator {
        "ios-arm64_x86_64-simulator"
    } else {
        "ios-arm64"
    };

    let manifest_dir = std::env::var("CARGO_MANIFEST_DIR").unwrap();
    let framework_dir = PathBuf::from(&manifest_dir)
        .join("ios/Frameworks")
        .join(format!("{}.xcframework", framework_name))
        .join(arch_name);
    let framework_path = framework_dir.join(format!("{}.framework", framework_name));
    println!(
        "cargo:rustc-link-search=framework={}",
        framework_dir.display()
    );
    println!("cargo:rustc-link-lib=framework={}", framework_name);

    let headers: PathBuf = PathBuf::from(&framework_path).join("Headers");
    println!("cargo:include={}", headers.display());
}
