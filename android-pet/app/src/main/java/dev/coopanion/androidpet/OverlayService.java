package dev.coopanion.androidpet;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.drawable.GradientDrawable;

public class OverlayService extends Service {
    private static final String CHANNEL_ID = "coopanion_pet_overlay";
    private WindowManager windowManager;
    private View overlay;
    private WindowManager.LayoutParams params;
    private WebView webView;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(7, buildNotification());
        if (!Settings.canDrawOverlays(this)) {
            stopSelf();
            return;
        }
        showOverlay();
    }

    private Notification buildNotification() {
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(this, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);
        return builder.setContentTitle("Coopanion 桌宠正在运行")
                .setContentText("点击返回桌宠控制页")
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .setContentIntent(pending)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Coopanion 浮窗桌宠", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    private void showOverlay() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LinearLayout frame = new LinearLayout(this);
        frame.setOrientation(LinearLayout.VERTICAL);
        frame.setBackground(roundRect(0x00FFFFFF, 22));

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(10, 3, 5, 3);
        bar.setBackground(roundRect(0xEE182033, 16));

        TextView label = new TextView(this);
        label.setText("🐳 鲸鲸桌宠");
        label.setTextColor(Color.WHITE);
        label.setTextSize(12);
        bar.addView(label, new LinearLayout.LayoutParams(0, dp(34), 1));

        Button retry = new Button(this);
        retry.setText("↻");
        retry.setTextSize(14);
        retry.setMinWidth(dp(30));
        retry.setPadding(0, 0, 0, 0);
        retry.setOnClickListener(v -> loadPetPage());
        bar.addView(retry, new LinearLayout.LayoutParams(dp(32), dp(34)));


        Button smaller = new Button(this);
        smaller.setText("−");
        smaller.setTextSize(16);
        smaller.setMinWidth(dp(30));
        smaller.setPadding(0, 0, 0, 0);
        smaller.setOnClickListener(v -> resizeOverlay(-30));
        bar.addView(smaller, new LinearLayout.LayoutParams(dp(30), dp(34)));

        Button larger = new Button(this);
        larger.setText("+");
        larger.setTextSize(14);
        larger.setMinWidth(dp(30));
        larger.setPadding(0, 0, 0, 0);
        larger.setOnClickListener(v -> resizeOverlay(30));
        bar.addView(larger, new LinearLayout.LayoutParams(dp(30), dp(34)));

        Button close = new Button(this);
        close.setText("×");
        close.setTextSize(18);
        close.setMinWidth(dp(30));
        close.setPadding(0, 0, 0, 0);
        close.setOnClickListener(v -> stopSelf());
        bar.addView(close, new LinearLayout.LayoutParams(dp(30), dp(34)));

        bar.setOnTouchListener(new View.OnTouchListener() {
            private int startX, startY;
            private float downX, downY;
            @Override public boolean onTouch(View v, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    startX = params.x;
                    startY = params.y;
                    downX = e.getRawX();
                    downY = e.getRawY();
                    return true;
                } else if (e.getAction() == MotionEvent.ACTION_MOVE) {
                    params.x = startX + (int)(e.getRawX() - downX);
                    params.y = startY + (int)(e.getRawY() - downY);
                    windowManager.updateViewLayout(overlay, params);
                    return true;
                }
                return e.getAction() == MotionEvent.ACTION_UP;
            }
        });

        webView = new WebView(this);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) {
                String css = "document.documentElement.style.background='transparent';" +
                    "document.body.style.background='transparent';" +
                    "document.body.style.margin='0';" +
                    "document.body.style.overflow='hidden';" +
                    "document.body.style.width='100%';" +
                    "document.body.style.height='100%';" +
                    "document.body.style.zoom='0.72';" +
                    "document.querySelectorAll('.floor').forEach(e=>e.style.display='none');" +
                    "document.querySelectorAll('.stage').forEach(e=>{e.style.background='transparent';e.style.minHeight='0';e.style.height='auto';});";
                view.evaluateJavascript(css, null);
            }
        });
        frame.addView(bar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(36)));
        frame.addView(webView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        overlay = frame;
        params = new WindowManager.LayoutParams(
                dp(200), dp(220),
                Build.VERSION.SDK_INT >= 26
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.END;
        params.x = dp(12);
        params.y = dp(120);
        windowManager.addView(overlay, params);
        loadPetPage();
    }

    private void resizeOverlay(int deltaDp) {
        if (windowManager == null || overlay == null || params == null) return;
        int minWidth = dp(150), maxWidth = dp(300);
        int minHeight = dp(160), maxHeight = dp(360);
        params.width = Math.max(minWidth, Math.min(maxWidth, params.width + dp(deltaDp)));
        params.height = Math.max(minHeight, Math.min(maxHeight, params.height + dp(deltaDp)));
        windowManager.updateViewLayout(overlay, params);
    }

    private void loadPetPage() {
        if (webView == null) return;
        webView.loadUrl("http://127.0.0.1:7797/pet");
    }

    private GradientDrawable roundRect(int color, int radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radiusDp));
        return d;
    }

    private int dp(float value) {
        return (int)(value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override public void onDestroy() {
        if (windowManager != null && overlay != null) {
            try { windowManager.removeView(overlay); } catch (Exception ignored) {}
        }
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }
        overlay = null;
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
