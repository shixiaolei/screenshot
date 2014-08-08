
package net.ds.screenshot.core;

import android.graphics.PixelFormat;

/**
 * 像素工具类.
 * 
 * @author shixiaolei
 */
public class PixelUtils {

    /**
     * 根据图片格式，将byte数组转化成int数组.
     * 
     * @param byteArray 输入参数，byte数组
     * @param intArray 输出参数，int数组
     * @param format 图片格式 {@link #PixelFormat}
     * @return 是否转化成功
     */
    public static boolean bytesToInts(byte[] byteArray, int[] intArray, int format) {
        if (format == PixelFormat.RGBA_8888) {
            return bytesToIntAsRgba8888(byteArray, intArray);
        } else if (format == PixelFormat.RGBX_8888) {
            return bytesToIntAsRgbx8888(byteArray, intArray);
        } else if (format == PixelFormat.RGB_888) {
            return bytesToIntAsRgb888(byteArray, intArray);
        } else if (format == PixelFormat.RGB_565) {
            return bytesToIntAsRgb565(byteArray, intArray);
        } else if (format == PixelFormat.RGBA_5551) {
            return bytesToIntAsRgba5551(byteArray, intArray);
        } else if (format == PixelFormat.RGBA_4444) {
            return bytesToIntAsRgba4444(byteArray, intArray);
        } else {
            return false;
        }
    }

    private static boolean bytesToIntAsRgba8888(byte[] paramArrayOfByte, int[] paramArrayOfInt) {
        boolean bool = true;
        int i = paramArrayOfInt.length;
        for (int j = 0; j < i; j++) {
            int k = b(paramArrayOfByte, j * 4);
            int m = 0xFF000000 & k;
            int n = (k & 0xFF) << 16;
            int i1 = 0xFF00 & k;
            int i2 = (k & 0xFF0000) >> 16 | (i1 | (m | n));
            paramArrayOfInt[j] = i2;
            if (i2 != 0)
                bool = false;
        }
        return bool;
    }

    private static boolean bytesToIntAsRgbx8888(byte[] paramArrayOfByte, int[] paramArrayOfInt) {
        boolean bool = true;
        int i = paramArrayOfInt.length;
        for (int j = 0; j < i; j++) {
            int k = b(paramArrayOfByte, j * 4);
            int m = (k & 0xFF) << 16;
            int n = 0xFF00 & k;
            int i1 = (k & 0xFF0000) >> 16 | (n | (0xFF000000 | m));
            paramArrayOfInt[j] = i1;
            if (i1 != 0)
                bool = false;
        }
        return bool;

    }

    private static boolean bytesToIntAsRgb888(byte[] paramArrayOfByte, int[] paramArrayOfInt) {
        boolean bool = true;
        int i = paramArrayOfInt.length;
        for (int j = 0; j < i; j++) {
            int k = c(paramArrayOfByte, j * 3);
            int m = (k & 0xFF) << 16;
            int n = 0xFF00 & k;
            int i1 = (k & 0xFF0000) >> 16 | (n | (0xFF000000 | m));
            paramArrayOfInt[j] = i1;
            if (i1 != 0)
                bool = false;
        }
        return bool;
    }

    private static boolean bytesToIntAsRgb565(byte[] paramArrayOfByte, int[] paramArrayOfInt) {
        boolean bool = true;
        int i = paramArrayOfInt.length;
        for (int j = 0; j < i; j++) {
            int k = d(paramArrayOfByte, j * 2);
            int m = (0xF800 & k) << 8;
            int n = (k & 0x7E0) << 5;
            int i1 = (k & 0x1F) << 3 | (n | (0xFF000000 | m));
            paramArrayOfInt[j] = i1;
            if (i1 != 0)
                bool = false;
        }
        return bool;
    }

    private static boolean bytesToIntAsRgba5551(byte[] paramArrayOfByte, int[] paramArrayOfInt) {
        return false; // 暂不支持Rgba5551
    }

    private static boolean bytesToIntAsRgba4444(byte[] paramArrayOfByte, int[] paramArrayOfInt) {
        boolean bool = true;
        int i = paramArrayOfInt.length;
        for (int j = 0; j < i; j++) {
            int k = d(paramArrayOfByte, j * 2);
            int m = (0xF000 & k) << 16;
            int n = (k & 0xF00) << 12;
            int i1 = (k & 0xF0) << 8;
            int i2 = (k & 0xF) << 4 | (i1 | (m | n));
            paramArrayOfInt[j] = i2;
            if (i2 != 0)
                bool = false;
        }
        return bool;
    }

    private static int b(byte[] paramArrayOfByte, int paramInt) {
        int i = 0xFF & paramArrayOfByte[paramInt];
        int j = 0xFF & paramArrayOfByte[(paramInt + 1)];
        int k = 0xFF & paramArrayOfByte[(paramInt + 2)];
        return i | ((0xFF & paramArrayOfByte[(paramInt + 3)]) << 24 | k << 16 | j << 8);
    }

    private static int c(byte[] paramArrayOfByte, int paramInt) {
        int i = 0xFF & paramArrayOfByte[paramInt];
        int j = 0xFF & paramArrayOfByte[(paramInt + 1)];
        return i | (0xFF000000 | (0xFF & paramArrayOfByte[(paramInt + 2)]) << 16 | j << 8);
    }

    private static short d(byte[] paramArrayOfByte, int paramInt) {
        return (short) (0xFF & paramArrayOfByte[paramInt] | (0xFF & paramArrayOfByte[(paramInt + 1)]) << 8);
    }

}
