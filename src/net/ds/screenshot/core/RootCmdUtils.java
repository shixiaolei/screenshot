package net.ds.screenshot.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import net.ds.screenshot.BuildConfig;
import net.ds.screenshot.IOUtils;
import android.os.Looper;
import android.text.format.DateUtils;
import android.util.Log;

/**
 * 执行su命令工具类.
 * 
 * @author shixiaolei
 */
public class RootCmdUtils {
    
    private static final String TAG = "RootCmdUtils";
    private static final String EOF = "shell_eof";
    private static final String EOF_BIN = "\necho " + EOF + " \n";
    private static final long DEFAULT_TIMEOUT = DateUtils.SECOND_IN_MILLIS * 30;
    
    /**
     * 执行回调.
     * 
     * @author shixiaolei
     */
    public interface CmdCallback {
        public void onCmdSucceed();
        public void onCmdFailed();
    }
    
    /**
     * 执行cmd命令，并通过回调返回.
     * 
     * <p>此函数可能会耗时， 考虑放在<strong>非UI线程</strong>里</p>
     * 
     * @return 执行成功，返回true; 否则返回false。
     */
    public static void execute(final String[] cmd, final CmdCallback callback) {
        execute(cmd, DEFAULT_TIMEOUT, callback);
    }
    
    
    /**
     * 执行cmd命令，并通过回调返回.
     * 
     * <p>此函数可能会耗时， 考虑放在<strong>非UI线程</strong>里</p>
     * 
     * @return 执行成功，返回true; 否则返回false。
     */
    public static void execute(final String[] cmd, long maxWaitTime, final CmdCallback callback) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "you should call me in non-ui thread.");
            }
        }
        Process process = null;
        BufferedReader input = null;
        BufferedReader error = null;
        OutputStream output = null;
        Timer timer = null;
        final AtomicBoolean isWaiting = new AtomicBoolean(false);
        
        if (maxWaitTime > 0) {
            timer = new Timer();
            timer.schedule(new TimeoutCheck(process, input, error, output, timer, callback, isWaiting), maxWaitTime);
        }
        
        try {
            process = Runtime.getRuntime().exec("su");
            isWaiting.set(true);
            input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            output = process.getOutputStream();
            for (String c : cmd) {
                output.write(c.getBytes());
            }
            output.write(EOF_BIN.getBytes());
            output.flush();
            
            String response;
            while((response = input.readLine()) != null) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "stdin -- >" + response);
                }
                if (EOF.equals(response)) {
                    isWaiting.set(false);
                    if (callback != null) {
                        callback.onCmdSucceed();
                    }
                    return;
                }
            }
            
            isWaiting.set(false);
            if (callback != null) {
                callback.onCmdFailed();
            }
            
        } catch (Throwable e) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "exeception occurred.", e);
            }
            isWaiting.set(false);
            if (callback != null) {
                callback.onCmdFailed();
            }
            
        } finally {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "execute finalize");
            }
            recycle(process, input, error, output, timer);
        }
    }
    
    private static class TimeoutCheck extends TimerTask {

        private Process process;
        private BufferedReader input;
        private BufferedReader error;
        private OutputStream output;
        private Timer timer;
        private CmdCallback callback;
        private AtomicBoolean isWaiting;
        
        public TimeoutCheck(Process process, BufferedReader input, BufferedReader error,
                OutputStream output, Timer timer, CmdCallback callback, AtomicBoolean isWaiting) {
            super();
            this.process = process;
            this.input = input;
            this.error = error;
            this.output = output;
            this.callback = callback;
            this.timer = timer;
            this.isWaiting = isWaiting;
        }

        @Override
        public void run() {
            if (isWaiting.get()) {
                if (callback != null) {
                    callback.onCmdFailed();
                }
                isWaiting.set(false);
                recycle(process, input, error, output, timer);
            }
        }
        
    }

    private static void recycle(Process process, BufferedReader input, BufferedReader error,
            OutputStream output, Timer timer) {
        IOUtils.closeQuietly(output);
        IOUtils.closeQuietly(error);
        IOUtils.closeQuietly(input);
        try {
            if (timer != null) {
                timer.cancel();
            }
        } catch (Throwable e) {
        }
        try {
            if (process != null) {
                process.destroy();
            }
        } catch (Throwable e) {
        }
    }
    
}
