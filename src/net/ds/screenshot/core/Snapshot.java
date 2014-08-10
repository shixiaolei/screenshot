package net.ds.screenshot.core;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import net.ds.screenshot.App;
import net.ds.screenshot.BitmapUtils;
import net.ds.screenshot.BuildConfig;
import net.ds.screenshot.FileUtils;
import net.ds.screenshot.IOUtils;
import net.ds.screenshot.core.RootCmdUtils.CmdCallback;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

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
    private static final  String SCREENSHOT_PATH = new File(App.getApp().getFilesDir(), "screen_capture_tmp.png").getAbsolutePath();
    
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
    public interface SnapshotToFileCallBack extends SnapshotCallBack {
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
    
    
    private static void captureAsync(SnapshotCallBack callback) {
        if ((Build.VERSION.SDK_INT >= 14) && (new File(BIN_SCREEN_CAP).exists())) {
            captureByScreenCapBin(callback);
        } else {
            captureByFb0(callback);
        }
    }
    
    private static void captureByScreenCapBin(final SnapshotCallBack callback) {
        if (callback == null) {
            return;
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "captureByScreenCapBin called");
        }
        
        final File picture = new File(SCREENSHOT_PATH);
        FileUtils.deleteQuietly(picture);
        FileUtils.ensureDirectory(picture.getParentFile());
        
        String[] cmd = new String[] {BIN_SCREEN_CAP + " -p " + SCREENSHOT_PATH};
        RootCmdUtils.execute(cmd, new CmdCallback() {
            
            @Override
            public void onCmdSucceed() {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onCmdSucceed");
                }
                
                if (callback instanceof SnapshotToBitmapCallBack) {
                    Bitmap bitmap = BitmapFactory.decodeFile(SCREENSHOT_PATH); //TODO 待优化，避免OOM
                    boolean isValidBmp = isValidBitmap(bitmap);
                    
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
        final File dev = getFb0Dev();
        if (dev == null) {
            callback.onFailed();
        }
        
        if (dev.canRead() && dev.canWrite()) {
            readScreenshotFromFb0(callback);
            return;
        }
        
        String[] cmd = new String[] {"chmod 666 " + dev.getAbsolutePath()};
        RootCmdUtils.execute(cmd, new CmdCallback() {
            @Override
            public void onCmdSucceed() {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onCmdSucceed");
                }
                readScreenshotFromFb0(callback);
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

    private static void readScreenshotFromFb0(final SnapshotCallBack callback) {
        DataInputStream input = null;
        try {
            WindowManager wm = (WindowManager) App.getApp().getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics dm = new DisplayMetrics();
            Display display = wm.getDefaultDisplay();
            display.getMetrics(dm);
            int format = display.getPixelFormat();
            int width = dm.widthPixels;
            int height = dm.heightPixels;
            PixelFormat pixelFormat = new PixelFormat();
            PixelFormat.getPixelFormatInfo(format, pixelFormat);
            byte[] pixelBytes = new byte[pixelFormat.bytesPerPixel * width * height];
            int[] pixelInts = new int[width * height];
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "readScreenshotFromFb0 format = " + format + ", width = " + width + ", height = " + height + ", pixelBytes size = " + pixelBytes.length + ", pixelInts size = " + pixelInts.length);
            }
            
            File dev = getFb0Dev();
            input = new DataInputStream(new FileInputStream(dev));
            input.readFully(pixelBytes);
            
            boolean convertSucceed = PixelUtils.bytesToInts(pixelBytes, pixelInts, format);
            Bitmap bitmap = Bitmap.createBitmap(pixelInts, width, height, Bitmap.Config.ARGB_8888);
            boolean isValidBitmap = isValidBitmap(bitmap);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "readScreenshotFromFb0 convertSucceed = " + convertSucceed + ", isValidBitmap = " + isValidBitmap);
            }
            
            if (isValidBitmap) {
                if (callback instanceof SnapshotToBitmapCallBack) {
                    ((SnapshotToBitmapCallBack) callback).onSucceed(bitmap);
                } else if (callback instanceof SnapshotToFileCallBack) {
                    final File picture = new File(SCREENSHOT_PATH);
                    FileUtils.deleteQuietly(picture);
                    FileUtils.ensureDirectory(picture.getParentFile());
                    BitmapUtils.savePicToPath(bitmap, new File(SCREENSHOT_PATH), CompressFormat.PNG);
                }
            } else {
                callback.onFailed();
            }
            
        } catch (Throwable e) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "readScreenshotFromFb0 failed.", e);
            }
            callback.onFailed();
            
        } finally {
            IOUtils.closeQuietly(input);
        }
    }


    private static boolean isValidBitmap(Bitmap bitmap) {
        return bitmap != null && !bitmap.isRecycled() && bitmap.getWidth() > 0 && bitmap.getHeight() > 0;
    }
    
}
