# Coopanion Android Floating Pet (prototype)

This is an Android overlay wrapper for the existing Coopanion Core pet page. It does not replace Core or bundle the Core server.

## Requirements

- Android 8.0 or newer.
- Coopanion Core running in Termux on the same phone and listening at `127.0.0.1:7797`.
- Overlay permission granted to this app.

## Build

The GitHub Actions workflow `.github/workflows/android-pet.yml` builds a debug APK when changes are pushed to `android-overlay-prototype`, or when manually dispatched from Actions.

Download the `coopanion-android-pet-debug` artifact from the completed workflow run. The APK is unsigned for Play Store distribution, but can be installed locally after allowing installation from the chosen source.

## Use

1. Start Coopanion Core in Termux.
2. Install and open this app.
3. Grant “Display over other apps” permission.
4. Tap “启动桌宠浮窗”.
5. Use the top bar to drag the floating window. Tap ↻ to reload and × to close.

The overlay loads `http://127.0.0.1:7797/pet`. If the Core server is stopped, the pet page cannot connect; restart Core and tap reload.

## Prototype limitations

- This wrapper only provides the floating window. Coopanion Core and its world/plugins still run separately in Termux.
- Android vendors may restrict background services or overlay behavior. Allow background activity for Termux and this app if your device offers such controls.
- The pet page uses the existing web client; behavior may differ from Coopanion's Electron desktop window.
