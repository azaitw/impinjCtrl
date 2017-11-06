package lib;

import com.impinjCtrl.Properties;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PropertyUtils {
    public static String getLogFileName() { return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".json"; }
    public static String getAPiHost() { return System.getProperty(Properties.apiHost); }
    public static String getReaderHost() { return System.getProperty(Properties.readerHost); }
    public static String getLogPath() { return System.getProperty(Properties.logPath, "./log/"); }
    public static Long getTimestamp() {
        return System.currentTimeMillis();
    }
}
