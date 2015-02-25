package gen;

import formula.FormulaExtractor;
import util.Utils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;

import static sig.SignatureFormulaExtractor.SigExtractor;

/**
 * Created by xiaohe on 12/3/14.
 * Generate a monitor lib according to the template below and instantiation parameters.
 * <p/>
 * package rvm;
 * <p/>
 * Pub(Integer report) {
 * Integer report;
 * long time;
 * <p/>
 * event publish (Integer report, long time) {
 * this.report=report;
 * this.time=time;
 * }
 * <p/>
 * event approve (Integer report, long time) {
 * this.report=report;
 * this.time=time;
 * }
 * <p/>
 * ltl: [](publish => <*> approve)
 *
 * @violation { System.out.println("should not publish financial report "+this.report+" without pre-approval");}
 * <p/>
 * }
 */
public class MonitorGenerator {

    private Path sigFile;
    private FormulaExtractor formulaExtractor;

    private StringBuilder sb = new StringBuilder();

    public MonitorGenerator(Path sigFilePath, Path formulaFilePath) {
        this.sigFile = sigFilePath;
        this.formulaExtractor = new FormulaExtractor(formulaFilePath);

        this.genMonitorLib();
    }

    private void genMonitorLib() {
        //to be implemented.
        genImports();

        genPackage();

//        genMonitorMain();

        genPhysicalFile();
    }

    private void genPhysicalFile() {
        //fake path
        String monitorLibPath = "./test/rvm/PubTest.rvm";

        Utils.writeToFile(this.sb.toString(), monitorLibPath);
    }

    private void genPackage() {
        sb.append("\npackage rvm;\n");
    }

    private void genImports() {
        sb.append("\nimport java.io.IOException;\n" +
                "import java.nio.file.Files;\n" +
                "import java.nio.file.Path;\n" +
                "import java.nio.file.Paths;\n" +
                "import java.nio.file.StandardOpenOption;\n");
    }

    public HashMap<String, int[]> getMethoArgsMappingFromSigFile() throws IOException {
        return SigExtractor.extractEventsInfoFromSigFile(sigFile).getTableCol();
    }

}
