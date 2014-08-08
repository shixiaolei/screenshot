package net.ds.screenshot.core;

import java.io.File;

import net.ds.screenshot.App;
import net.ds.screenshot.BuildConfig;
import net.ds.screenshot.FileUtils;
import net.ds.screenshot.core.RootCmdUtils.CmdCallback;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * 截屏工具类.
 * 
 * @author shixiaolei
 */
public class Snapshot {
    
    private static final String TAG = "Snapshot";
    private static final String BIN_SCREEN_CAP = "/system/bin/screencap";
    private static final String DEV_GRAPHICS_FB0 = "/dev/graphics/fb0";
    private static final String DEV_FB0 = "/dev/fb0";
    
    /**
     * 截屏回调基类.
     * 
     * @author shixiaolei
     */
    public interface SnapshotCallBack {
        public void onFailed();
    }
    
    /**
     * 截屏回调， 成功后返回一张bitmap.
     * 
     * @author shixiaolei
     */
    public interface SnapshotToBitmapCallBack extends SnapshotCallBack {
        public void onSucceed(Bitmap b);
    }
    
    /**
     * 截屏回调， 成功后将截屏保存到文件并返回.
     * 
     * @author shixiaolei
     */
    public interface SnapshotToFileCallBack {
        public void onSucceed(File f);
    }
    
    
    /**
     * 异步截屏, 并回调结果.
     * 
     * @param callback 请使用子类{@link #SnapshotToBitmapCallBack} 或 {@link #SnapshotToFileCallBack}
     */
    public static void capture(final SnapshotCallBack callback) {
        new Thread() {
            @Override
            public void run() {
                
                try {
                    Thread.sleep(5000);//FIXME 方便测试效果，延迟5秒截图
                } catch (InterruptedException e) {
                }
                
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_FOREGROUND);
                captureAsync(callback);
            }
        }.start();
    }
    
    
    public static void captureAsync(SnapshotCallBack callback) {
        captureByScreenCapBin(callback);
    }
    
    private static void captureByScreenCapBin(final SnapshotCallBack callback) {
        if (callback == null) {
            return;
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "captureByScreenCapBin called");
        }
        
        final String tmpPath = new File(App.getApp().getFilesDir(), "screen_capture_tmp.png").getAbsolutePath();
        final File picture = new File(tmpPath);
        FileUtils.deleteQuietly(picture);
        FileUtils.ensureDirectory(picture.getParentFile());
        
        String[] cmd = new String[] {BIN_SCREEN_CAP + " -p " + tmpPath};
        RootCmdUtils.execute(cmd, new CmdCallback() {
            
            @Override
            public void onCmdSucceed() {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onCmdSucceed");
                }
                
                if (callback instanceof SnapshotToBitmapCallBack) {
                    Bitmap bitmap = BitmapFactory.decodeFile(tmpPath);
                    boolean isValidBmp = bitmap != null && !bitmap.isRecycled() && bitmap.getWidth() > 0 && bitmap.getHeight() > 0;
                    
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "isValidBmp = " + isValidBmp);
                    }
                    
                    if (isValidBmp) {
                        ((SnapshotToBitmapCallBack) callback).onSucceed(bitmap);
                    } else {
                        callback.onFailed();
                    }
                    
                } else if (callback instanceof SnapshotToFileCallBack) {
                    boolean isFileExist = picture.exists();
                    
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "isFileExist = " + isFileExist);
                    }
                    
                    
                    if (isFileExist) {
                        ((SnapshotToFileCallBack) callback).onSucceed(picture);
                    } else {
                        callback.onFailed();
                    }
                }
                
            }
            
            @Override
            public void onCmdFailed() {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onCmdSucceed");
                }
                callback.onFailed();
            }
        });
    }
    
    private static void captureByFb0(final SnapshotCallBack callback) {
        if (callback == null) {
            return;
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "captureByFb0 called");
        }
        File dev = getFb0Dev();
        if (dev == null) {
            callback.onFailed();
        }
        
        if (!dev.canRead() || !dev.canWrite()) {
            String[] cmd = new String[] {"chmod 666 " + dev.getAbsolutePath()};
            RootCmdUtils.execute(cmd, new CmdCallback() {

                @Override
                public void onCmdSucceed() {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "onCmdSucceed");
                    }
                }

                @Override
                public void onCmdFailed() {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "onCmdFailed");
                    }
                    callback.onFailed();
                }
                
            });
        }
    }
    
    private static File getFb0Dev() {
        File file = new File(DEV_GRAPHICS_FB0);
        if (file.exists()) {
            return file;
        }
        file = new File(DEV_FB0);
        if (file.exists()) {
            return file;
        }
        return null;
    }

}
