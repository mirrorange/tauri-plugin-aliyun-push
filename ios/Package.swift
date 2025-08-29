// swift-tools-version:5.3
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
    name: "tauri-plugin-aliyun-push",
    platforms: [
        .iOS(.v13)
    ],
    products: [
        // Products define the executables and libraries a package produces, and make them visible to other packages.
        .library(
            name: "tauri-plugin-aliyun-push",
            type: .static,
            targets: ["tauri-plugin-aliyun-push"])
    ],
    dependencies: [
        .package(name: "Tauri", path: "../.tauri/tauri-api")
    ],
    targets: [
        // Targets are the basic building blocks of a package. A target can define a module or a test suite.
        // Targets can depend on other targets in this package, and on products in packages this package depends on.
        .binaryTarget(
            name: "CloudPushSDK",
            path: "Frameworks/CloudPushSDK.xcframework"),
        .binaryTarget(
            name: "AlicloudELS",
            path: "Frameworks/AlicloudELS.xcframework"),
        .binaryTarget(
            name: "UTDID",
            path: "Frameworks/UTDID.xcframework"),
        .target(
            name: "tauri-plugin-aliyun-push",
            dependencies: [
                .byName(name: "Tauri"),
                "CloudPushSDK",
                "AlicloudELS",
                "UTDID",
            ],
            path: "Sources",
            swiftSettings: [
                .unsafeFlags([
                    "-F", "Frameworks/CloudPushSDK.xcframework/ios-arm64",
                    "-F", "Frameworks/AlicloudELS.xcframework/ios-arm64",
                    "-F", "Frameworks/UTDID.xcframework/ios-arm64",
                ])
            ]),
    ]
)
