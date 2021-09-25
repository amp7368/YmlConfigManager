package ycm.yml.manager.ycm;

/**
 * a static version of Ycm that just uses the default config
 *
 * @author Apple (amp7368)
 */
public class YcmDefault implements YcmHolder {
    private static final Ycm defaultYcm = new Ycm();
    private static final YcmDefault instance = new YcmDefault();

    @Override
    public Ycm getYcm() {
        return defaultYcm;
    }

    public static YcmDefault getInstance() {
        return instance;
    }
}
