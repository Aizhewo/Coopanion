# Coopanion Android Floating Pet

An Android floating-window wrapper for the existing Coopanion Core pet page. Core still runs inside Termux; the app can now request Termux to start and stop it.

## Requirements

- Android 8.0 or newer.
- Termux installed, with the existing Coopanion checkout at `~/Coopanion`.
- Node.js and the project's dependencies installed in that checkout.
- Overlay permission granted to this app.

## First-time Termux setup

1. Open Termux and run:
   ```bash
   mkdir -p ~/.termux
   touch ~/.termux/termux.properties
   grep -q '^allow-external-apps *= *true$' ~/.termux/termux.properties || echo 'allow-external-apps = true' >> ~/.termux/termux.properties
   ```
   If the file already has `allow-external-apps = false`, edit that existing line to `true` rather than leaving two conflicting settings.
2. Fully close and reopen Termux so the setting takes effect.
3. In Android Settings, open **Apps → Coopanion 浮窗桌宠 → Permissions / Additional permissions**, and allow **Run commands in Termux environment**. The exact labels can vary by Android version.
4. Open the pet app and grant **Display over other apps**.

Only grant this command permission to an app you trust. It allows the app to execute commands in your Termux user environment.

## Build

The GitHub Actions workflow `.github/workflows/android-pet.yml` builds a debug APK on pushes to `android-overlay-prototype`.

Download `coopanion-android-pet-debug` from the latest completed workflow run's **Artifacts** section, unzip it, and install the APK.

## Use

- Tap **一键启动 Core + 桌宠** to launch Core in the background and open the floating window.
- Tap **仅打开浮窗（Core 已运行）** if you started Core yourself.
- Close the floating window with × or use the app's stop button to request shutdown.
- The app writes Core logs to `~/.coopanion-core.log` and tracks a process it launched in `~/.coopanion-core.pid`.
- If Core was already running before automation, the stop script leaves that process alone.

The start script runs `node --import tsx --expose-gc core/boot.ts` from `~/Coopanion` and expects the pet page at `http://127.0.0.1:7797/pet`.

## Limitations

- Android may prevent Termux from being launched after it has been force-stopped. Open Termux manually again in that case.
- Some device vendors aggressively restrict background processes. If startup is unreliable, allow background activity and relax battery restrictions for Termux and the pet app.
- The current prototype waits 2.5 seconds before opening the overlay and retries the page load after 5 seconds. If Core takes longer to boot, tap ↻.
