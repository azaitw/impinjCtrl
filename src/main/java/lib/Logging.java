package lib;

import com.impinjCtrl.ReaderController;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logging {
    private static String mLogFileName;
    private static JSONArray mReadResult;
    private static String mEventId;
    private static String mRaceId;

    // Create log folder if not available
    public static void initLogPath() {
        String logPathString = PropertyUtils.getLogPath();
        File logPath = new File(logPathString);
        if (logPath.exists()) {
            System.out.println("Log path: " + logPathString);
        } else {
            System.out.println("Creating Log path : " + logPathString);
            try {
                logPath.mkdir();
            } catch(SecurityException se) {
                System.out.println("Log path creation error");
            }
        }
    }
    // Create log file and empty json array for a session
    public static String initLogging(String eventId, String raceId) {
        String fileName = PropertyUtils.getLogFileName();
        mLogFileName = PropertyUtils.getLogPath() + fileName;
        mReadResult = new JSONArray();
        try {
            File file = new File (mLogFileName);
            FileWriter fw = new FileWriter(file);
            fw.write("");
            fw.flush();
        } catch (Exception e) {
            System.out.println("startReader Exception: " + e.getMessage());
        }
        return fileName;
    }
    public static void resetLogging() {
        mLogFileName = null;
        mReadResult = null;
        mEventId = null;
        mRaceId = null;
    }
    public static void addEntry(JSONObject entry) {
        mReadResult.add(entry);
        System.out.println(entry.toJSONString());
        Logging.writeJSONToFile(); // Write result to log file
        Api.sendResult(entry, mEventId, mRaceId);
    }
    // Write to log file
    private static void writeJSONToFile() {
        try {
            FileWriter file = new FileWriter(mLogFileName);
            file.write(mReadResult.toString());
            file.flush();
        } catch (IOException e) {
            System.out.println("writeJSONToFile IOException: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("writeJSONToFile Exception: " + e.getMessage());
        }
    }
}
