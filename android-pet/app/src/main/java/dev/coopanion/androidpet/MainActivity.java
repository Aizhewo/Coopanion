package dev.coopanion.androidpet;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private TextView status;

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
        intro.setText("\n让 Coopanion 的桌宠浮在其他应用上。先启动 Termux 里的 Coopanion Core，再回来开启浮窗。\n\n默认连接： http://127.0.0.1:7797/pet\n");
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

        Button start = new Button(this);
        start.setText("② 启动桌宠浮窗");
        start.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                updateStatus();
                return;
            }
            Intent i = new Intent(this, OverlayService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(i);
            else startService(i);
            updateStatus();
        });
        root.addView(start, fullWidth());

        Button stop = new Button(this);
        stop.setText("关闭浮窗");
        stop.setOnClickListener(v -> stopService(new Intent(this, OverlayService.class)));
        root.addView(stop, fullWidth());

        TextView tip = new TextView(this);
        tip.setText("\n提示：浮窗需要 Core 保持运行。若显示连接失败，请先在 Termux 启动 Core，然后在浮窗内点“重试”。");
        tip.setTextSize(14);
        tip.setTextColor(0xFF64748B);
        root.addView(tip);

        setContentView(root);
        updateStatus();
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
