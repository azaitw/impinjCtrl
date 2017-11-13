package lib;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PropertyUtils {
    public static String getLogFileName() { return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".json"; }
    public static String getAPiHost() { return System.getProperty("apiHost"); }
    public static String getReaderHost() { return System.getProperty("readerHost"); }
    public static String getLogPath() { return System.getProperty("logPath", "./log/"); }
    public static Long getTimestamp() {
        return System.currentTimeMillis();
    }
}
