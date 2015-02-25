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
    public static boolean IsMonitoringLivenessProperty;
    public static Path genLogReaderPath;
    private static ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    private static String OutPutFilePath = "./CustomizedLogReader/rvm/LogReader.java";

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
        genLogReaderPath = initOutputFile();

        if (args.length > 1 && args[1].equals("-liveness")) {
            IsMonitoringLivenessProperty = true;
        }
        Path path2SigFile = Paths.get(args[0]);
        String tmpFolder = "./CodeModel_tmp";
        InvokerGenerator invokerGenerator = new InvokerGenerator(tmpFolder);
        SignatureFormulaExtractor.EventsInfo eventsInfo = SignatureFormulaExtractor.SigExtractor.
                extractEventsInfoFromSigFile(path2SigFile);

        String runtimeMonitorName = "rvm." + path2SigFile.toFile().getName().replaceAll(".rvm", "") + "RuntimeMonitor";

        invokerGenerator.generateCustomizedInvoker(runtimeMonitorName, eventsInfo);

        String imports = getContentFromResource("import.code");
        String mainBody = (IsMonitoringLivenessProperty) ? getContentFromResource("main-outputGenInRVM.code")
                : getContentFromResource("main.code");

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
