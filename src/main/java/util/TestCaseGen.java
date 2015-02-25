package util;

/**
 * Created by xiaohe on 1/27/15.
 */
public class TestCaseGen {
    public static void genTestCase(String nameOfGeneratedDataFile, int numOfLogEntries) {
        StringBuilder sb = new StringBuilder(10000);

        for (int i = 0; i < numOfLogEntries; i++) {
            sb.append("@" + i);
            sb.append(" approve(" + i + ") ");
            sb.append("publish (" + i + ")\n");
        }

        Utils.writeToFile(sb.toString(), "A:\\DATA\\Gen\\" + nameOfGeneratedDataFile);
    }

    public static void main(String[] args) {
        int sizeOfDataFile = 2000000;

        genTestCase("Pub_fake.log", sizeOfDataFile);

    }
}
