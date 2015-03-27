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


    public static boolean IsMonitoringLivenessProperty;
    public static Path genLogReaderPath;
    private static ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    private static String OutPutFilePath = "./CustomizedLogReader/rvm/LogReader.java";
    private static String FORMAT = CSV;

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
            if (args[i].equals("-liveness"))
                IsMonitoringLivenessProperty = true;

            else if (args[i].equals("--format=monpoly"))
                FORMAT = MONPOLY;

            else
                path2SigFile = Paths.get(args[i]);
        }


        String tmpFolder = "./CodeModel_tmp";
        InvokerGenerator invokerGenerator = new InvokerGenerator(tmpFolder);
        SignatureFormulaExtractor.EventsInfo eventsInfo = SignatureFormulaExtractor.SigExtractor.
                extractEventsInfoFromSigFile(path2SigFile);

        String runtimeMonitorName = "rvm." + path2SigFile.toFile().getName().replaceAll(".rvm", "") + "RuntimeMonitor";

        invokerGenerator.generateCustomizedInvoker(runtimeMonitorName, eventsInfo);

        String imports = getContentFromResource("import.code");
        String mainBody;
        switch (FORMAT) {
            case CSV :
                if (IsMonitoringLivenessProperty)
                    throw new IOException("Does not support liveness property in CSV format");

                mainBody = getContentFromResource("main-csv.code");
                break;

            case MONPOLY :
                mainBody = (IsMonitoringLivenessProperty) ? getContentFromResource
                        ("main-outputGenInRVM.code")
                        : getContentFromResource("main-monpoly.code");
                break;

            default :
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
}
