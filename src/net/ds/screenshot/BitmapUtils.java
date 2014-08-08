package net.ds.screenshot;

import java.io.File;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;

/**
 * 所有位图Bitmpa对象相关的处理，暂时放在这个类<br>
 * 加载/转换/保存/等等（图形变换、图像算法不在此）
 * @author songzhaochun
 *
 */
public class BitmapUtils {

    public static boolean savePicToPath(Bitmap b, File path, CompressFormat format) {
        return savePicToPath(b, path, 90, format);
    }

    public static boolean savePicToPath(Bitmap b, File path, int quality, CompressFormat format) {
        if (b == null || path == null) {
            return false;
        }
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (!sdCardExist) {
            return false;
        }

        if (!path.getParentFile().exists()) {
            path.getParentFile().mkdirs();
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            boolean success = b.compress(format, quality, fos);
            fos.flush();
            return success;
        } catch (Throwable e) {
            return false;
        } finally {
            IOUtils.closeQuietly(fos);
        }
    }
}
