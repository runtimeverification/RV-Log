package fsl.uiuc;

import gen.InvokerGenerator;
import org.apache.commons.io.FileUtils;
import sig.SignatureFormulaExtractor;
import util.Utils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static final String CSV = "CSV";
    public static final String MONPOLY = "MON";

    public static Path genLogReaderPath;
    private static ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    private static String OutPutFilePath = "./CustomizedLogReader/rvm/LogReader.java";
    private static String FORMAT = CSV;
    private static boolean strictParsing;
    private static String insertPoint4EventNameChecks = "        if (LogReader.isMonitoredEvent(EventName)) {";
    public static boolean TimeProp = false;

    //if the 'rawEvent' option is on, then the event names will not be read from the log file;
    //instead, the default event name 'data' will be used throughout the log file.
    private static boolean rawEvent = false;
    private static String eventNameInitCode = "private String EventName;";
    private static String newEventNameInitCode = "private String EventName = \"data\";";
    private static String eventNameUpdateCode = "EventName = this.getString();";

    /***************************************************************************************************************/
    //by default, the events that are not in spec will be ignored safely.
    //however, in certain cases, it might be desirable to notice the occurrence of these events.
    //To make it work, user needs to define an event called "other" in the rvm spec.
    public static boolean noticeOtherEvents = false;

    private static String skipEventCode = "this.skipLine();//skip the current event";

    /***************************************************************************************************************/

    public static String getContentFromResource(String resourceName) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(classLoader.getResourceAsStream(resourceName)));
        String line = null;
        StringBuilder sb = new StringBuilder(128);
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();
        return sb.toString();
    }

    /**
     * Given the path to signature file, formula file and log file, checks whether the properties stated in
     * the formula file are violated by the log file.
     *
     * @param args Three arguments need to be provided in the order of: sig file, formula file, log file.
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static void main(String[] args) throws IOException {
        Path path2SigFile = null;

        genLogReaderPath = initOutputFile();


        for (int i = 0; i < args.length; i++) {
            if ("-t".equals(args[i]))
                TimeProp = true;

            else if (args[i].equals("--format=monpoly"))
                FORMAT = MONPOLY;

            else if (args[i].equals("--strict"))
                strictParsing = true;

            else if (args[i].equals("--raw"))
                rawEvent = true;

            else if (args[i].equals("--other"))
                noticeOtherEvents = true;

            else
                path2SigFile = Paths.get(args[i]);
        }
        if (path2SigFile == null)
            throw new IOException("Please provide one .rvm specification file.");


        String tmpFolder = "./CodeModel_tmp";
        InvokerGenerator invokerGenerator = new InvokerGenerator(tmpFolder);
        SignatureFormulaExtractor.EventsInfo eventsInfo = SignatureFormulaExtractor.SigExtractor.
                extractEventsInfoFromSigFile(path2SigFile);

        String runtimeMonitorName = "rvm." + path2SigFile.toFile().getName().replaceAll(".rvm", "") + "RuntimeMonitor";

        invokerGenerator.generateCustomizedInvoker(runtimeMonitorName, eventsInfo);

        String imports = getContentFromResource("import.code");
        String mainBody;
        switch (FORMAT) {
            case CSV:
                mainBody = getContentFromResource("main-csv.code");
                if (strictParsing) {
                    int insertPoint = mainBody.indexOf(insertPoint4EventNameChecks);
                    mainBody = mainBody.substring(0, insertPoint)
                            + "\r\n" + getContentFromResource("eventNameChecks.code") +
                            mainBody.substring(insertPoint);
                } else if (noticeOtherEvents) {
                    mainBody = mainBody.replace(skipEventCode, skipEventCode +
                            "\nLogReader.MonitorMethodsInvoker.invokeOther();");
                }

                if (rawEvent) {
                    mainBody = mainBody.replace(eventNameInitCode, newEventNameInitCode);
                    mainBody = mainBody.replace(eventNameUpdateCode, "");
                }


                break;

            case MONPOLY:
                mainBody = getContentFromResource("main-monpoly.code");
                break;

            default:
                throw new IOException("Not support this format!");
        }

        Path tmpFolderPath = Paths.get(tmpFolder + "/LogReader.java");
        String logReader = new String(Files.readAllBytes(tmpFolderPath));

        FileUtils.deleteDirectory(tmpFolderPath.getParent().toFile());

        Utils.MyUtils.writeToOutputFileUsingBW(imports);
        Utils.MyUtils.writeToOutputFileUsingBW(logReader);
        Utils.MyUtils.writeToOutputFileUsingBW(mainBody);
        Utils.MyUtils.flushOutput();
    }

    private static Path initOutputFile() {
        Path path = Paths.get(OutPutFilePath);
        File file = path.toFile();
        try {
            if (file.exists()) {
                new PrintWriter(file).close();
            } else {
                if (path.getParent().toFile().exists()) {
                    file.createNewFile();
                } else {
                    path.getParent().toFile().mkdirs();
                    file.createNewFile();
                }
            }
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
            System.exit(1);
        }
        return path;
    }

    public static boolean isMonpolyLog() {
        return FORMAT.equals(MONPOLY);
    }
}
