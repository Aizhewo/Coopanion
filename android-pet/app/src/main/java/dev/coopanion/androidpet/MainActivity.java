package dev.coopanion.androidpet;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private TextView status;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showScreen();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (status != null) updateStatus();
    }

    private void showScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 36, 28, 28);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(0xFFF6F7FB);

        TextView title = new TextView(this);
        title.setText("🐳 Coopanion 浮窗桌宠");
        title.setTextSize(24);
        title.setTextColor(0xFF172033);
        root.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView intro = new TextView(this);
        intro.setText("\n一键启动 Termux 中的 Coopanion Core，并打开桌宠浮窗。首次使用需按说明授权 Termux 命令执行权限。\n\n默认连接：http://127.0.0.1:7797/pet\n");
        intro.setTextSize(16);
        intro.setTextColor(0xFF3B465A);
        root.addView(intro);

        status = new TextView(this);
        status.setTextSize(14);
        status.setTextColor(0xFF334155);
        root.addView(status);

        Button permission = new Button(this);
        permission.setText("① 授予悬浮窗权限");
        permission.setOnClickListener(v -> {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(i);
        });
        root.addView(permission, fullWidth());

        Button autoStart = new Button(this);
        autoStart.setText("② 一键启动 Core + 桌宠");
        autoStart.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "请先授予悬浮窗权限", Toast.LENGTH_LONG).show();
                updateStatus();
                return;
            }
            if (TermuxCommands.runScript(this, "start-core.sh")) {
                Toast.makeText(this, "已请求 Termux 启动 Core，正在打开桌宠…", Toast.LENGTH_LONG).show();
                handler.postDelayed(this::startOverlay, 2500);
            } else {
                Toast.makeText(this, "无法调用 Termux。请检查 Termux 安装及授权设置。", Toast.LENGTH_LONG).show();
            }
        });
        root.addView(autoStart, fullWidth());

        Button overlayOnly = new Button(this);
        overlayOnly.setText("仅打开浮窗（Core 已运行）");
        overlayOnly.setOnClickListener(v -> startOverlay());
        root.addView(overlayOnly, fullWidth());

        Button stop = new Button(this);
        stop.setText("关闭桌宠 + 停止自动启动的 Core");
        stop.setOnClickListener(v -> {
            stopService(new Intent(this, OverlayService.class));
            TermuxCommands.runScript(this, "stop-core.sh");
            Toast.makeText(this, "已请求关闭桌宠并停止本应用启动的 Core", Toast.LENGTH_LONG).show();
        });
        root.addView(stop, fullWidth());

        TextView tip = new TextView(this);
        tip.setText("\n首次配置：在 Termux 的 ~/.termux/termux.properties 中设置 allow-external-apps=true，并在安卓设置中授予本应用“在 Termux 环境中运行命令”的权限。配置步骤见项目 README。\n\n如果启动较慢，可在浮窗中点 ↻ 重试。");
        tip.setTextSize(14);
        tip.setTextColor(0xFF64748B);
        root.addView(tip);

        setContentView(root);
        updateStatus();
    }

    private void startOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "请先授予悬浮窗权限", Toast.LENGTH_LONG).show();
            return;
        }
        Intent i = new Intent(this, OverlayService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(i);
        else startService(i);
    }

    private LinearLayout.LayoutParams fullWidth() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.topMargin = 10;
        return p;
    }

    private void updateStatus() {
        if (status == null) return;
        status.setText(Settings.canDrawOverlays(this)
                ? "悬浮窗权限：已允许"
                : "悬浮窗权限：尚未允许");
    }
}
