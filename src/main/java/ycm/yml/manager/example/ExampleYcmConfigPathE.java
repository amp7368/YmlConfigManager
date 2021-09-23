package ycm.yml.manager.example;

import ycm.yml.manager.fields.YcmField;

/**
 * Just an example of a ConfigObject
 *
 * @author Apple (amp7368)
 */
public class ExampleYcmConfigPathE {
    @YcmField
    public String pathI;
    @YcmField
    public String pathJ;
    @YcmField
    public int pathK;

    @Override
    public String toString() {
        return "ExampleYcmConfigPathE{" +
                "pathI='" + pathI + '\'' +
                ", pathJ='" + pathJ + '\'' +
                ", pathK=" + pathK +
                '}';
    }
}
