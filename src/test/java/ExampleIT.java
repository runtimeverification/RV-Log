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

    public static final String valid = "valid";
    public static final String violation = "violation";

    private final int numOfValid;
    private final int numOfViolation;

    public ExampleIT(String folder, String testName, int goodNum, int badNum) {
        this.numOfValid = goodNum;
        this.numOfViolation = badNum;

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
        //the examples: the last two args are the number of
        //valid examples and violation examples respectively.
        data.add(new Object[]{"SimpleFormula", "Insert2", 0, 1});

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

        checkValidExamples();

        checkViolationExamples();


        //delete the output files
        helper.deleteFiles(true, this.outputDir);

    }

    private void checkViolationExamples() throws Exception {
        //run the log reader
        for (int i = 1; i <= this.numOfViolation; i++) {
            String relativePathI = violation + File.separator + i;

            String testPrefix = relativePathI + File.separator + violation;

            String log = this.testFolder + testPrefix + ".log";
            String actualOut = testPrefix + ".actual.out";
            String actualErr = testPrefix + ".actual.err";

            helper.testCommand(relativePathI, violation, false, true, false,
                    "java", "rvm.LogReader", log);

            helper.deleteFiles(true, actualOut);
            helper.deleteFiles(true, actualErr);
        }
    }

    private void checkValidExamples() {
        //TODO
    }
}
