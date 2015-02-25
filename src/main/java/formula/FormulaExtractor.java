package formula;

import java.nio.file.Path;
import java.util.HashMap;

import static sig.SignatureFormulaExtractor.SigExtractor;

/**
 * Created by hx312 on 03/12/2014.
 */
public class FormulaExtractor {

    public static HashMap<String, boolean[]> skippedFieldsMap = init2();

    public FormulaExtractor(Path formulaPath) {
        //analyze the formula file and set the fields accordingly.
    }

    private static HashMap<String, boolean[]> init2() {
        HashMap<String, boolean[]> tmp = new HashMap<>();
        boolean[] skipList = new boolean[SigExtractor.maxNumOfParams];
        skipList[1] = true;
        tmp.put("insert", skipList);
//        tmp.put(SigExtractor.APPROVE, skipList);
//        tmp.put(SigExtractor.PUBLISH, skipList);
        return tmp;
    }
}
