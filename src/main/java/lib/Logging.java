package lib;

import com.google.gson.Gson;
import model.Record;
import model.TxData;
import model.LogReadCountInfo;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Logging {
    private static Logging instance;

    private String mLogFileName;
    private FileWriter mFileWriter;
    private BufferedWriter mBufferedWriter;
    private PrintWriter mPrintWriter;
    private Gson mGson;
    private Api mApi;
    private Integer mDebugCounter;

    public static synchronized Logging getInstance() {
        if (instance == null) {
            instance = new Logging();
        }
        return instance;
    }

    // Create log folder if not available
    public void createLogFolder() {
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
    public String start() {
        mLogFileName = PropertyUtils.getLogPath() + PropertyUtils.getLogFileName();
        mGson = new Gson();
        mApi = Api.getInstance();
        mDebugCounter = 0;
        System.out.println("mLogFileName: " + mLogFileName);
        try {
            mFileWriter = new FileWriter(mLogFileName, true);
            mBufferedWriter = new BufferedWriter(mFileWriter);
            mPrintWriter = new PrintWriter(mBufferedWriter);
            mBufferedWriter.write("[ ");
        } catch (Exception e) {
            System.out.println("Logging start Exception: " + e.getMessage());
        }
        return mLogFileName;
    }
    // Create log file and empty json array for a session
    public void stop() {
        try {
            LogReadCountInfo readCount = new LogReadCountInfo(mDebugCounter);
            String output = mGson.toJson(readCount);
            System.out.println(output);
            try {
                mBufferedWriter.write(output + " ]");
            } catch (Exception e) {
                System.out.println("Logging stop write file Exception: " + e.getMessage());
            }
            mBufferedWriter.close();
            mPrintWriter.close();
        } catch (Exception e) {
            System.out.println("Logging stop Exception: " + e.getMessage());
        }
    }
    public void addEntry(Record record) {
        TxData txData = new TxData();
        txData.addRecord(record);
        String output = mGson.toJson(record);
        mDebugCounter += 1;
        mPrintWriter.println(output);
        try {
            mBufferedWriter.write(output + ", ");
        } catch (Exception e) {
            System.out.println("addEntry write file Exception: " + e.getMessage());
        }
        mApi.sendResult(output);
    }
}
