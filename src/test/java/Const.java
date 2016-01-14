import java.io.File;

/**
 * Created by xiaohe on 1/14/16.
 */
public class Const {
    public static final String SW_NAME = "RV-Log";

    public static final String releasePath = System.getProperty("user.dir") + File.separator +
            "target" + File.separator + "release" + File.separator + SW_NAME
            + File.separator + SW_NAME;

    public static final String libPath = releasePath + File.separator + "lib" + File.separator;

    public static final String binPath = releasePath + File.separator + "bin" + File.separator;

//    public static final String logicPluginPath = libPath + "plugins" + File.separator;
}
