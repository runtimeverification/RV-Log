//package log;
//
//import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
//import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
//import reg.RegHelper;
//import sig.SigExtractor;
//import util.UnZipFile;
//
//import java.io.IOException;
//import java.nio.file.Path;
//import java.util.HashMap;
//
///**
// * Serves as lexer and parser for log file.
// */
//public class LogEntryExtractor_FromArchive implements log.LogReader.LogExtractor {
//
//    //some tokens
//    static final byte newLine = (byte) '\n';
//    static final byte space = (byte) ' ';
//    static final byte tab = (byte) '\t';
//    static final byte nl2 = (byte) '\r';
//    static final byte space0B = 0x0B;
//    static final byte spaceF = (byte) '\f';
//    static final byte at = (byte) '@';
//    static final byte lpa = (byte) '(';
//    static final byte rpa = (byte) ')';
//    static final byte comma = (byte) ',';
//    static final byte hash = (byte) '#';
//    static final byte doubleQuote = (byte) '\"';
//    static final byte underscore = (byte) '_';
//    static final byte rightBracket = (byte) ']';
//    static final byte exclamation = (byte) '!';
//    static final byte dot = (byte) '.';
//    private String TimeStamp; //we can add the @ symbol when it is ready to be printed
//    private String EventName;
//    private HashMap<String, Object[]> TableData;
//    /**
//     * Given a table name, return the list of types that represent the types for each column (table schema).
//     */
//    private HashMap<String, int[]> TableCol;
//
//    private String logFile_compressed;
//    //    indirect optimal 8kb
////    private static final int DirectBufSizeOptimal4MyHP = 64 * 1024;
//    private int BufSize;
//    private byte[] byteArr;
//    private int posInArr;
//    private TarArchiveInputStream tarIn;
//
//    private int[] seqOfCompressMethod;//for future extension
//
////    public LogEntryExtractor(HashMap<String, Integer[]> tableCol) {
////        this.TableCol = tableCol;
////        InputStreamReader isReader = new InputStreamReader(System.in);
////        scan = new Scanner(new BufferedReader(isReader));
////    }
//
//    /**
//     * Create an obj for log entry extractor.
//     *
//     * @param tableCol
//     * @param logFile
//     * @param multipleOf1K Multiple of 1024.
//     * @throws java.io.IOException
//     */
//    public LogEntryExtractor_FromArchive(HashMap<String, int[]> tableCol, Path logFile, int multipleOf1K)
//            throws IOException {
//        this.TableCol = tableCol;
//        this.logFile_compressed = logFile.toString();
//
//        this.BufSize = multipleOf1K * 1024;
//        this.byteArr = new byte[this.BufSize];
//        init();
//    }
//
//    public LogEntryExtractor_FromArchive(HashMap<String, int[]> tableCol, Path logFile) throws IOException {
//        this(tableCol, logFile, 4);
//    }
//
//    private void init() throws IOException {
//        this.TableData = new HashMap<>();
//        for (String eventName : this.TableCol.keySet()) {
//            Object[] fields = new Object[this.TableCol.get(eventName).length];
//            this.TableData.put(eventName, fields);
//        }
//    }
//
//
//    private boolean isWhiteSpace(byte b) {
//        return b == newLine || b == space || b == tab || b == nl2 || b == space0B || b == spaceF;
//    }
//
//    private boolean isStringChar(byte b) {
//        if (b > 44 && b < 59)
//            return true;
//        if (b > 64 && b < 92)
//            return true;
//
//        if (b > 96 && b < 123)
//            return true;
//
//        if (b == underscore || b == rightBracket || b == exclamation)
//            return true;
//        else return false;
//    }
//
//
//    private String getEventName() throws IOException {
//        StringBuffer sb = new StringBuffer();
//        while (true) {
//            while (this.posInArr < this.BufSize) {
//                byte b = byteArr[this.posInArr++];
//                if (isStringChar(b))
//                    sb.append((char) b);
//
//                else {
//                    if (b == lpa) {
//                        this.posInArr--;
//                    }
//                    return sb.toString();
//                }
//            }
//
//            int count = -1;
//            this.posInArr = 0;
//            if ((BufSize = tarIn.read(this.byteArr, 0, BufSize)) == -1) {
//                throw new IOException("Unexpected end of file while parsing an int");
//            }
//        }
//
//    }
//
//
//    private String getStringFromBuf(byte delim) throws IOException {
//        StringBuffer sb = new StringBuffer();
//        String returnedStr = null;
//        while (true) {
//            while (this.posInArr < this.BufSize) {
//                byte b = byteArr[this.posInArr++];
//                if (isStringChar(b)) {
//                    try {
//                        sb.append((char) b);
//                    } catch (Exception e) {
//                        throw new IOException("A string field should not contain white spaces inside");
//                    }
//                } else {
//                    if (!isWhiteSpace(b) && b != delim) {
//                        throw new IOException("UnExpected char\'" + (char) b + "\'");
//                    }
//
//                    if (sb != null) {
//                        returnedStr = sb.toString();
//                        sb = null;
//                    }
//                    if (b != delim) {
//                        continue;
//                    }
//                    return returnedStr;
//                }
//            }
//
//            this.posInArr = 0;
//            if ((BufSize = tarIn.read(this.byteArr, 0, BufSize)) == -1) {
//                throw new IOException("Unexpected end of file while parsing an int");
//            }
//        }
//    }
//
//    private void rmWhiteSpace() {
//        while (this.posInArr < this.BufSize) {
//            byte b = byteArr[this.posInArr++];
//            if (isWhiteSpace(b))
//                continue;
//            else {
//                this.posInArr--;
//                break;
//            }
//        }
//    }
//
//    private long getTSFromBuf() throws IOException {
//        StringBuffer sb = new StringBuffer();
//
//        while (true) {
//            while (this.posInArr < this.BufSize) {
//                byte b = byteArr[this.posInArr++];
//                if (Character.isDigit(b) || b == dot) {
//
//                    sb.append((char) b);
//
//                } else {
//                    return Math.round(Double.parseDouble(sb.toString()));
//                }
//            }
//
//            this.posInArr = 0;
//            if ((BufSize = tarIn.read(this.byteArr, 0, BufSize)) == -1) {
//                throw new IOException("Unexpected end of file while parsing an int");
//            }
//        }
//    }
//
//    private double getFloatingNumFromBuf(byte delim) throws IOException {
//        StringBuffer sb = new StringBuffer();
//        String returnedStr = null;
//        while (true) {
//            while (this.posInArr < this.BufSize) {
//                byte b = byteArr[this.posInArr++];
//                if (Character.isDigit(b) || b == dot) {
//                    try {
//                        sb.append((char) b);
//                    } catch (Exception e) {
//                        throw new IOException("A floating number field should not contain white spaces inside");
//                    }
//                } else {
//                    if (!isWhiteSpace(b) && b != delim) {
//                        throw new IOException("UnExpected char\'" + (char) b + "\'");
//                    }
//
//                    if (sb != null) {
//                        returnedStr = sb.toString();
//                        sb = null;
//                    }
//                    if (b != delim) {
//                        continue;
//                    }
//                    return Double.parseDouble(returnedStr);
//                }
//            }
//
//            this.posInArr = 0;
//            if ((BufSize = tarIn.read(this.byteArr, 0, BufSize)) == -1) {
//                throw new IOException("Unexpected end of file while parsing an int");
//            }
//        }
//    }
//
//    /**
//     * Get an int number from byte byteArr (not check thoroughly, assume white spaces).
//     *
//     * @return
//     * @throws java.io.IOException
//     */
//    private int getIntFromBuf(byte delim) throws IOException {
//        StringBuffer sb = new StringBuffer();
//        String returnedStr = null;
//        while (true) {
//            while (this.posInArr < this.BufSize) {
//                byte b = byteArr[this.posInArr++];
//                if (Character.isDigit(b)) {
//                    try {
//                        sb.append((char) b);
//                    } catch (Exception e) {
//                        throw new IOException("An int number field should not contain white spaces inside");
//                    }
//                } else {
//                    if (!isWhiteSpace(b) && b != delim) {
//                        throw new IOException("UnExpected char\'" + (char) b + "\'");
//                    }
//
//                    if (sb != null) {
//                        returnedStr = sb.toString();
//                        sb = null;
//                    }
//                    if (b != delim) {
//                        continue;
//                    }
//                    return Integer.parseInt(returnedStr);
//                }
//            }
//
//            this.posInArr = 0;
//            if ((BufSize = tarIn.read(this.byteArr, 0, BufSize)) == -1) {
//                throw new IOException("Unexpected end of file while parsing an int");
//            }
//        }
//    }
//
//    public void startReadingEventsByteByByte() throws IOException {
//        long numOfLogEntries = 0;
//
//        this.tarIn = UnZipFile.getTarInputStream(this.logFile_compressed);
//
//        TarArchiveEntry entry = null;
//
//        /** Read the tar entries using the getNextEntry method **/
//        while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
//
////            System.out.println("Encountering: " + entry.getName());
//            /** If the entry is a directory, create the directory. **/
//
//            if (entry.isDirectory()) {
//                System.out.println("Find the directory " + entry.getName());
//            }
//            /**
//             * If the entry is a file, analyze its content as log file.
//             **/
//            else {
////                System.out.println("Find file " + entry.getName());
//                int count;
//                while ((count = tarIn.read(this.byteArr, 0, BufSize)) != -1) {
//                    this.posInArr = 0;
//                    while (this.posInArr < this.BufSize) {
//                        byte b = this.byteArr[this.posInArr++];
//                        if (isWhiteSpace(b))
//                            continue;
//
//                        //change the order of different branches, cmp whether we can gain perf benefits by
//                        //considering the probabilities.
//                        if (b == lpa) {
//                            this.rmWhiteSpace();
//                            this.triggerEvent();
//                        } else if (isStringChar(b)) {
//                            EventName = (char) b + this.getEventName();
//                        } else if (b == at) {
//                            this.rmWhiteSpace();
//                            TimeStamp = String.valueOf(this.getTSFromBuf());
//                            numOfLogEntries++;
//                        } else if (b == 0) {
//                            break;
//                        } else {
//                            System.out.println("No. " + numOfLogEntries + " entry, event name is " + EventName);
//                            System.err.println("Unexpected char'" + (char) b + "'");
//                            break;
//                        }
//                    }
//                }
//
//            }
//        }
//
//        /** Close the input stream **/
//
//        tarIn.close();
//
//        System.out.println("There are " +
//                numOfLogEntries + " log entries in the .tar.gz log file!!!");
//
//    }
//
//
//    private void triggerEvent() throws IOException {
//        int[] typesInTuple = TableCol.get(EventName);
//        Object[] argsInTuple = TableData.get(EventName);
//        for (int i = 0; i < typesInTuple.length - 1; i++) {
//
//            switch (typesInTuple[i]) {
//                case RegHelper.INT_TYPE:
//                    argsInTuple[i] = this.getIntFromBuf(comma);
//                    break;
//
//                case RegHelper.FLOAT_TYPE:
//                    argsInTuple[i] = this.getFloatingNumFromBuf(comma);
//                    break;
//
//                case RegHelper.STRING_TYPE:
//                    argsInTuple[i] = this.getStringFromBuf(comma);
//                    break;
//            }
//            this.rmWhiteSpace();
//        }
//
//        if (typesInTuple.length > 0) {
//            //the last field should be followed by a right parenthesis
//            int lastIndex = typesInTuple.length - 1;
//            switch (typesInTuple[lastIndex]) {
//                case RegHelper.INT_TYPE:
//                    argsInTuple[lastIndex] = this.getIntFromBuf(rpa);
//                    break;
//
//                case RegHelper.FLOAT_TYPE:
//                    argsInTuple[lastIndex] = this.getFloatingNumFromBuf(rpa);
//                    break;
//
//                case RegHelper.STRING_TYPE:
//                    argsInTuple[lastIndex] = this.getStringFromBuf(rpa);
//                    break;
//            }
//        }
////        this.printEvent();
//        if (EventName.equals(SigExtractor.INSERT)) {
//            if (argsInTuple[1].equals("MYDB") && !argsInTuple[0].equals("notARealUserInTheDB"))
//                this.printEvent();
//        }
//    }
//
//    private void printEvent() {
//        System.out.print("\n@" + this.TimeStamp + " " + this.EventName + "(");
//        Object[] data = this.TableData.get(this.EventName);
//        for (int i = 0; i < data.length - 1; i++) {
//            System.out.print(data[i] + ",");
//        }
//
//        if (data.length > 0) {
//            System.out.print(data[data.length - 1]);
//        }
//
//        System.out.print(")\n");
//    }
//
//}
