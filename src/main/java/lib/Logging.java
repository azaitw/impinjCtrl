package lib;

import model.RaceResult;
import model.Record;
import model.TxData;
import java.io.File;
import java.io.FileWriter;

public class Logging {
    private static Logging instance;

    private String mLogFileName;
    private static String mEventId;
    private static String mRaceId;
    private static String mMode;

    private Api mApi;

    public static synchronized Logging getInstance() {
        if (instance == null) {
            instance = new Logging();
        }
        return instance;
    }

    // Create log folder if not available
    public void initLogPath() {
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

        // get Api instance
        mApi = Api.getInstance();
    }
    // Create log file and empty json array for a session
    public String initLogging(String eventId, String raceId) {
        mEventId = eventId;
        mRaceId = raceId;

        String fileName = PropertyUtils.getLogFileName();
        mLogFileName = PropertyUtils.getLogPath() + fileName;

        if (mRaceId == "") {
            mMode = "test";
        } else {
            mMode = "race";
        }
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

    public void resetLogging() {
        mLogFileName = null;
        mEventId = null;
        mRaceId = null;
        mMode = null;
    }

    public void addEntry(Record record) {
        RaceResult raceResult = new RaceResult(mEventId, mRaceId);
        TxData payload = new TxData();

        payload.setMode(mMode);
        payload.addRecords(record);
        payload.setType("rxdata");
        payload.setRecordType("partial");

        raceResult.setPayload(payload);

        writeJSONToFile(); // Write result to log file
        mApi.sendResult(raceResult);
    }
    // Write to log file
    private void writeJSONToFile() {
        // TODO: refine this
        /*
        try {
            FileWriter file = new FileWriter(mLogFileName);
            file.write(mReadResult.toString());
            file.flush();
        } catch (IOException e) {
            System.out.println("writeJSONToFile IOException: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("writeJSONToFile Exception: " + e.getMessage());
        }
        */
    }
}
