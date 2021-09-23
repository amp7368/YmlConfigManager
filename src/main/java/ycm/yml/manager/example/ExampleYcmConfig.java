package ycm.yml.manager.example;

import ycm.yml.manager.fields.YcmField;
import ycm.yml.manager.fields.YcmInlineComment;
import ycm.yml.manager.fields.YcmNewlineComment;
import ycm.yml.manager.ycm.YcmFileNameable;

import java.util.UUID;

/**
 * Just an example of a ConfigObject
 *
 * @author Apple (amp7368)
 */
public class ExampleYcmConfig implements YcmFileNameable {
    /**
     * comments on the line where path is specified
     * override the name of the path
     */
    @YcmInlineComment("a comment for A")
    @YcmField(pathname = "pathA")
    public String valueA;

    /**
     * comments on the line before where the path is specified
     * the name will default to the name of the variable
     */
    @YcmNewlineComment("a comment for B")
    @YcmField
    public String pathB;

    /**
     * no comment provided, and the path is "pathC"
     */
    @YcmField
    public int pathC;

    /**
     * creates a Section under the path "pathD"
     */
    @YcmField
    public PathD pathD;

    /**
     * creates a Section under the path "pathE"
     */
    @YcmInlineComment("a comment for pathE")
    @YcmField
    public ExampleYcmConfigPathE pathE;
    /**
     * a field that will not be converted to yml
     */
    public String randomField;

    /**
     * the name the file will be saved under
     */
    private String filename = UUID.randomUUID().toString();


    @Override
    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public String toString() {
        return "ExampleYcmConfig{" +
                "valueA='" + valueA + '\'' +
                ", pathB='" + pathB + '\'' +
                ", pathC=" + pathC +
                ", pathD=" + pathD +
                ", pathE=" + pathE +
                ", randomField='" + randomField + '\'' +
                '}';
    }

    /**
     * all properly annotated fields under this class will be represented
     * as a subPath of any
     */
    public static class PathD {
        @YcmField
        @YcmNewlineComment("These can be commented too")
        public String pathF;
        @YcmField
        public String pathG;
        @YcmField
        public int pathH;

        @Override
        public String toString() {
            return "PathD{" +
                    "pathF='" + pathF + '\'' +
                    ", pathG='" + pathG + '\'' +
                    ", pathH=" + pathH +
                    '}';
        }
    }
}
