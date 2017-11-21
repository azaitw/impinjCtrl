package lib;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PropertyUtils {
    private static final String READHER_HOST = "readerHost";
    private static final String API_HOSTER = "apiHost";
    private static final String LOG_PATH = "logPath";

    public static String getLogFileName() { return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".json"; }
    public static String getAPiHost() { return System.getProperty(API_HOSTER); }
    public static String getReaderHost() { return System.getProperty(READHER_HOST); }
    public static String getLogPath() { return System.getProperty(LOG_PATH, "./log/"); }
    public static Long getTimestamp() { return System.currentTimeMillis(); }
}
