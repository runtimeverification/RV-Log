package sig;

import reg.RegHelper;

import java.io.File;
import java.util.HashMap;

/**
 * Created by xiaohe on 12/3/14.
 */
public class SigExtractor {
    public static final String SELECT = "select";
    public static final String INSERT = "insert";
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";
    public static final String SCRIPT_START = "script_start";
    public static final String SCRIPT_END = "script_end";
    public static final String SCRIPT_SVN = "script_svn";
    public static final String SCRIPT_MD5 = "script_md5";
    public static final String COMMIT = "commit";

    public static final String PUBLISH = "publish";
    public static final String APPROVE = "approve";

    public static final int maxNumOfParams = 5;

    public static final byte[] insertByte = INSERT.getBytes();

    public static HashMap<String, int[]> TableCol = initTableCol();

    private static HashMap<String, int[]> initTableCol() {
        HashMap<String, int[]> tmp = new HashMap<>();
        //the arg types can be inferred from the signature file
        int[] argTy4Insert = new int[]{RegHelper.STRING_TYPE, RegHelper.STRING_TYPE,
                RegHelper.STRING_TYPE, RegHelper.STRING_TYPE};
        int[] argTy4Script = new int[]{RegHelper.STRING_TYPE};
        int[] argTy4ScriptSVN = new int[]{RegHelper.STRING_TYPE, RegHelper.STRING_TYPE,
                RegHelper.STRING_TYPE, RegHelper.INT_TYPE, RegHelper.INT_TYPE};
        int[] argTy4ScriptMD5 = new int[]{RegHelper.STRING_TYPE, RegHelper.STRING_TYPE};
        int[] argTy4Commit = new int[]{RegHelper.STRING_TYPE, RegHelper.INT_TYPE};

        int[] argTy4Pub = new int[]{RegHelper.INT_TYPE};

        tmp.put(SELECT, argTy4Insert);
        tmp.put(INSERT, argTy4Insert);
        tmp.put(UPDATE, argTy4Insert);
        tmp.put(DELETE, argTy4Insert);
        tmp.put(SCRIPT_START, argTy4Script);
        tmp.put(SCRIPT_END, argTy4Script);
        tmp.put(SCRIPT_SVN, argTy4ScriptSVN);
        tmp.put(SCRIPT_MD5, argTy4ScriptMD5);
        tmp.put(COMMIT, argTy4Commit);

        tmp.put(APPROVE, argTy4Pub);
        tmp.put(PUBLISH, argTy4Pub);
        return tmp;
    }


    public static HashMap<String, int[]> extractMethodArgsMappingFromSigFile(File f) {
        //fake method at the moment, needs to be implemented.
        return TableCol;
    }
}
