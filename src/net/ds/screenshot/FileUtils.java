
package net.ds.screenshot;

import java.io.File;

public class FileUtils {

    /**
     * Deletes a file, never throwing an exception. If file is a directory,
     * delete it and all sub-directories.
     * <p>
     * The difference between File.delete() and this method are:
     * <ul>
     * <li>A directory to be deleted does not have to be empty.</li>
     * <li>No exceptions are thrown when a file or directory cannot be deleted.</li>
     * </ul>
     * 
     * @param file file or directory to delete, can be <code>null</code>
     * @return <code>true</code> if the file or directory was deleted, otherwise
     *         <code>false</code>
     * @since Commons IO 1.4
     */
    public static boolean deleteQuietly(File file) {
        if (file == null) {
            return false;
        }

        try {
            return file.delete();
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * 检查目录是否存在，如果不存在，则创建; 如果path是一个文件而不是文件夹, 先删除它再创建.
     * 
     * @param path
     */
    public static void ensureDirectory(File dir) {
        if (dir == null) {
            return;
        }
        if (!dir.isDirectory()) {
            deleteQuietly(dir);
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

}
