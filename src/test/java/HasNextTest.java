import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;

/**
 * Created by hx312 on 1/12/2016.
 */
public class HasNextTest {
    private final TestHelper helper;
    private final String path;

    public HasNextTest(String path) {
        this.path = new File(path).getParent();
        String classpath = "." + File.pathSeparator + "CustomizedLogReader" + File.separator
                + File.pathSeparator + System.getProperty("java.class.path");

        classpath = Const.libPath + "*" + File.pathSeparator + classpath;

        HashMap<String, String> envMap = new HashMap<>();
        envMap.put("LOGICPLUGINPATH", Const.libPath);
        System.setProperty("java.class.path", classpath);

        helper = new TestHelper(path, envMap);
    }

    @Test
    public void test_RVM_Availability() throws Exception {
        final String testName = "Insert2";
        String command = Const.binPath + "rv-log";
        if (SystemUtils.IS_OS_WINDOWS) {
            command += ".bat";
        }
        helper.testCommand(null, false, true, command, testName + ".rvm");
    }
}
