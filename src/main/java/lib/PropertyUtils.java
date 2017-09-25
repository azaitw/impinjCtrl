package lib;

import com.impinjCtrl.Properties;
import com.impinjCtrl.ReaderController;

public class PropertyUtils {

    public static boolean isDebugMode() {
        String debugMode = System.getProperty(Properties.debugMode, "0");
        return debugMode.equals("1");
    }

    public static String getLogFileName() {
        String logDir = "./logs/";
        String fileName;
        if (ReaderController.mRaceId != null) {
            fileName = "race-" + ReaderController.mRaceId;
        } else {
            fileName = "event-" + ReaderController.mEventId;
        }
        System.out.println("LogFileName: " + fileName);
        return logDir + fileName + ".json";
    }

    public static String getAPiHost() {
        return System.getProperty(Properties.apiHost);
    }

    public static Long getTimestamp() {
        return System.currentTimeMillis();
    }
}
