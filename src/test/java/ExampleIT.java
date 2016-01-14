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

    private final String testFolder;
    private final String testName;

    private final String testPath;

    private final String outputDir = "CustomizedLogReader";

    private String optionsOfRVLog = "";

    public ExampleIT(String folder, String testName) {
        this.testFolder = System.getProperty("user.dir") + File.separator +
                "examples" + File.separator + folder + File.separator + testName + File.separator;

        this.testName = testName;

        this.testPath = this.testFolder + testName + ".rvm";

        String classpath = "." + File.pathSeparator + testFolder +
                File.separator + outputDir + File.separator
                + File.pathSeparator + System.getProperty("java.class.path");

        classpath = Const.libPath + "*" + File.pathSeparator + classpath;

        HashMap<String, String> envMap = new HashMap<>();
        envMap.put("LOGICPLUGINPATH", Const.libPath + "plugins");
        System.setProperty("java.class.path", classpath);

        helper = new TestHelper(this.testPath, envMap);

        switch (this.testName) {
            case "Insert2":
                this.optionsOfRVLog = "--format=monpoly";
                break;

            default:
        }
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        ArrayList<Object[]> data = new ArrayList<>();
        //the examples
        data.add(new Object[]{"SimpleFormula", "Insert2"});

        return data;
    }

    @Test
    public void testEG() throws Exception {
        String command = Const.binPath + "rv-log";
        if (SystemUtils.IS_OS_WINDOWS) {
            command += ".bat";
        }

        helper.testCommand(null, false, true, command, this.optionsOfRVLog, this.testPath);

        //generate monitor library code
        helper.testCommand("", null, false, true, true, "java",
                "com.runtimeverification.rvmonitor.java.rvj.Main", "-d",
                outputDir + File.separator + "rvm" + File.separator, this.testPath);

        //compile java code
        helper.testCommand("", null, false, true, true, "javac",
                this.testFolder + (this.outputDir + File.separator + "rvm" +
                        File.separator + "LogReader.java"));

        //run the log reader
        helper.testCommand("", this.testName, false, true, false,
                "java", "rvm.LogReader", this.testFolder + File.separator + "violation.log");

        //delete the output files
        helper.deleteFiles(true, this.outputDir);
        helper.deleteFiles(true, testName + ".actual.out");
        helper.deleteFiles(true, testName + ".actual.err");
    }
}
