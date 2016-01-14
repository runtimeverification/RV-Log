import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by hx312 on 1/12/2016.
 */
@RunWith(Parameterized.class)
public class ExampleIT {
    private final TestHelper helper;

    private final String path;

    private final String testName;

    private final String outputDir = "CustomizedLogReader";

    public ExampleIT(String folder, String testName) {
        this.path = System.getProperty("user.dir") + File.separator + "examples" + File.separator +
                folder + File.separator + testName + File.separator + testName + ".rvm";

        this.testName = testName;

        String classpath = "." + File.pathSeparator + outputDir + File.separator
                + File.pathSeparator + System.getProperty("java.class.path");

        classpath = Const.libPath + "*" + File.pathSeparator + classpath;

        HashMap<String, String> envMap = new HashMap<>();
        envMap.put("LOGICPLUGINPATH", Const.libPath + "plugins");
        System.setProperty("java.class.path", classpath);

        helper = new TestHelper(this.path, envMap);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        ArrayList<Object[]> data = new ArrayList<>();
        //the examples
        data.add(new Object[]{"SimpleFormula", "Insert2"});

        return data;
    }

    @Test
    public void test_RVM_Availability() throws Exception {
        String command = Const.binPath + "rv-log";
        if (SystemUtils.IS_OS_WINDOWS) {
            command += ".bat";
        }
        helper.testCommand(null, false, true, command, this.path);

        //generate monitor library code
        helper.testCommand("", null, false, true, true, "java",
                "com.runtimeverification.rvmonitor.java.rvj.Main", "-d",
                outputDir + File.separator + "rvm" + File.separator, this.path);


        //delete the output files
        helper.deleteFiles(true, this.outputDir);
    }
}
