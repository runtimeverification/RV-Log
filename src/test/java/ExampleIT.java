import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;

/**
 * Created by hx312 on 1/12/2016.
 */
public class ExampleIT {
    private final TestHelper helper;

    private final String path;

    private final String outputDir = "CustomizedLogReader";

    public ExampleIT() {
        this.path = System.getProperty("user.dir") + File.separator + "examples" + File.separator +
                "SimpleFormula" + File.separator + "Insert" + File.separator + "Insert2.rvm";

        String classpath = "." + File.pathSeparator + outputDir + File.separator
                + File.pathSeparator + System.getProperty("java.class.path");

        classpath = Const.libPath + "*" + File.pathSeparator + classpath;

        HashMap<String, String> envMap = new HashMap<>();
        envMap.put("LOGICPLUGINPATH", Const.libPath + "plugins");
        System.setProperty("java.class.path", classpath);

        helper = new TestHelper(this.path, envMap);
    }

    @Test
    public void test_RVM_Availability() throws Exception {
        final String testName = "/home/xiaohe/Projects/RV-Log/examples/SimpleFormula/Insert/Insert2";
        String command = Const.binPath + "rv-log";
        if (SystemUtils.IS_OS_WINDOWS) {
            command += ".bat";
        }
        helper.testCommand(null, false, true, command, testName + ".rvm");

        //generate monitor library code
        helper.testCommand("", null, false, true, true, "java",
                "com.runtimeverification.rvmonitor.java.rvj.Main", "-d",
                outputDir + File.separator + "rvm" + File.separator,
                testName + ".rvm");
    }
}
