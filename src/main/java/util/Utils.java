package util;

import fsl.uiuc.Main;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static java.nio.file.Files.newBufferedWriter;

/**
 * Created by xiaohe on 9/17/14.
 */
public class Utils {
    public static final String lineSeparator = System.getProperty("line.separator");
    public static Utils MyUtils = new Utils();
    //    private Charset charset = StandardCharsets.ISO_8859_1;
    private Charset charset = StandardCharsets.US_ASCII;
    private BufferedWriter bufferedWriter;

    private Utils() {
        this.bufferedWriter = init();
    }


    public static void writeToFile(String contents, String fileName) {
        Path p = Paths.get(fileName);
        byte[] bytes = contents.getBytes();
        try {
            Files.write(p, bytes, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            try {
                p.getParent().toFile().mkdirs();

                Files.write(p, bytes, StandardOpenOption.CREATE_NEW);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void writeToOutputFileUsingBW(String contents) throws IOException {
        this.bufferedWriter.write(contents);
    }

    public void flushOutput() throws IOException {
        this.bufferedWriter.flush();
    }

    private BufferedWriter init() {
        try {
            return newBufferedWriter(Main.genLogReaderPath, charset, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }
        return null;
    }
}