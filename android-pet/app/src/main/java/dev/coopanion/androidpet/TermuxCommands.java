package dev.coopanion.androidpet;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

final class TermuxCommands {
    private static final String TERMUX_PACKAGE = "com.termux";
    private static final String TERMUX_HOME = "/data/data/com.termux/files/home";
    private static final String BASH = "/data/data/com.termux/files/usr/bin/bash";

    private TermuxCommands() {}

    static boolean runScript(Context context, String scriptName) {
        Intent intent = new Intent();
        intent.setClassName(TERMUX_PACKAGE, "com.termux.app.RunCommandService");
        intent.setAction("com.termux.RUN_COMMAND");
        intent.putExtra("com.termux.RUN_COMMAND_PATH", BASH);
        intent.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", new String[] {
                "-lc", "bash \"$HOME/Coopanion/android-pet/scripts/" + scriptName + "\""
        });
        intent.putExtra("com.termux.RUN_COMMAND_WORKDIR", TERMUX_HOME);
        intent.putExtra("com.termux.RUN_COMMAND_BACKGROUND", true);
        try {
            context.startService(intent);
            return true;
        } catch (Exception e) {
            Toast.makeText(context,
                    "Termux 调用失败：" + e.getClass().getSimpleName()
                            + "。请确认已开启 allow-external-apps 并授予运行命令权限。",
                    Toast.LENGTH_LONG).show();
            return false;
        }
    }
}
