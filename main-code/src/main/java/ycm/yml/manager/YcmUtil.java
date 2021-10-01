package ycm.yml.manager;

import java.io.File;

/**
 * @author Apple (amp7368)
 */
public class YcmUtil {
    /**
     * creates a File from file and the specified children
     *
     * @param file     the parent file
     * @param children the children of the file
     * @return the new subFile
     */
    public static File fileWithChildren(File file, String... children) {
        for (String child : children) {
            file = new File(file, child);
        }
        return file;
    }
}
