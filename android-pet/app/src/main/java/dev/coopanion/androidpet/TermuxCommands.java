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
        String command;
        if ("start-core.sh".equals(scriptName)) {
            command = "cd \"$HOME/Coopanion\" || { echo 'ERROR: ~/Coopanion not found' >> \"$HOME/.coopanion-core.log\"; exit 1; }; "
                    + "LOG_FILE=\"$HOME/.coopanion-core.log\"; PID_FILE=\"$HOME/.coopanion-core.pid\"; "
                    + "if node -e 'fetch(\"http://127.0.0.1:7797/pet\").then(r=>process.exit(r.ok?0:1)).catch(()=>process.exit(1))' >/dev/null 2>&1; then "
                    + "echo external > \"$PID_FILE\"; echo ALREADY_RUNNING >> \"$LOG_FILE\"; exit 0; fi; "
                    + "if [ -f \"$PID_FILE\" ]; then state=$(cat \"$PID_FILE\" 2>/dev/null); case \"$state\" in managed:*) pid=${state#managed:}; "
                    + "if kill -0 \"$pid\" 2>/dev/null; then echo ALREADY_RUNNING >> \"$LOG_FILE\"; exit 0; fi;; esac; fi; "
                    + "nohup node --import tsx --expose-gc core/boot.ts >> \"$LOG_FILE\" 2>&1 </dev/null & "
                    + "pid=$!; echo managed:$pid > \"$PID_FILE\"; echo STARTED:$pid >> \"$LOG_FILE\"";
        } else if ("stop-core.sh".equals(scriptName)) {
            command = "PID_FILE=\"$HOME/.coopanion-core.pid\"; LOG_FILE=\"$HOME/.coopanion-core.log\"; "
                    + "if [ ! -f \"$PID_FILE\" ]; then echo NO_MANAGED_CORE >> \"$LOG_FILE\"; exit 0; fi; "
                    + "state=$(cat \"$PID_FILE\" 2>/dev/null); case \"$state\" in managed:*) pid=${state#managed:}; "
                    + "if kill -0 \"$pid\" 2>/dev/null; then kill \"$pid\" 2>/dev/null || true; echo STOP_REQUESTED:$pid >> \"$LOG_FILE\"; "
                    + "else echo ALREADY_STOPPED >> \"$LOG_FILE\"; fi;; external) echo CORE_WAS_ALREADY_RUNNING_NOT_STOPPED >> \"$LOG_FILE\";; "
                    + "*) echo UNKNOWN_PID_FILE_STATE >> \"$LOG_FILE\";; esac; rm -f \"$PID_FILE\"";
        } else {
            Toast.makeText(context, "未知的 Termux 命令", Toast.LENGTH_LONG).show();
            return false;
        }

        Intent intent = new Intent();
        intent.setClassName(TERMUX_PACKAGE, "com.termux.app.RunCommandService");
        intent.setAction("com.termux.RUN_COMMAND");
        intent.putExtra("com.termux.RUN_COMMAND_PATH", BASH);
        intent.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", new String[] { "-lc", command });
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
