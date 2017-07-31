package lib;

import com.impinjCtrl.Properties;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PropertyUtils {

    public static boolean isDebugMode() {
        String debugMode = System.getProperty(Properties.debugMode, "0");
        return debugMode.equals("1");
    }

    public static String getLogFileName() {
        String logDir = "./logs/";
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        String fileName = System.getProperty(Properties.logFileName);
        return TextUtils.isEmpty(fileName) ? logDir + "beardude" + "-" + timeStamp + ".log"
                : logDir + fileName + "-" + timeStamp + ".log";
    }

    public static String getAPiHost() {
        return System.getProperty(Properties.apiHost, "http://localhost:1337");
    }

    public static Integer getEventId() { return Integer.parseInt(System.getProperty(Properties.eventId)); }
}
