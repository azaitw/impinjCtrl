package lib;

import com.google.gson.Gson;
import model.Record;
import model.TxData;
import model.LogReadCountInfo;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class Logging {
    private static Logging instance;

    private final long DELAY = 0;
    private final long INTERVAL_PERIOD_MS = 1000;

    private BufferedWriter mBufferedWriter;
    private Gson mGson;
    private Api mApi;
    private Integer mDebugCounter;

    private ArrayList<Record> aggregateRecords = new ArrayList<Record>();
    private TimerTask intervalTask;
    private Timer interval;

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
        // initial & check clean a interval
        if (interval != null || intervalTask != null) {
            cleanInterval();
        }

        intervalTask = new TimerTask() {
            @Override
            public void run() {

                if (aggregateRecords == null || aggregateRecords.size() == 0) {
                    return;
                }

                TxData txData = new TxData();
                for (Record record : aggregateRecords) {
                    if (record!= null) {
                        txData.addRecord(record);
                    }
                }

                String output = mGson.toJson(txData);

                aggregateRecords.clear();
                mDebugCounter += 1;
                System.out.println(output);

                try {
                    mBufferedWriter.write(output + ", ");
                } catch (Exception e) {
                    System.out.println("addEntry write file Exception: " + e.getMessage());
                }
                mApi.sendResult(output);
            }
        };

        interval = new Timer();

        // schedules the task to be run in an interval
        interval.scheduleAtFixedRate(intervalTask, DELAY,
                INTERVAL_PERIOD_MS);

        String logFileName = PropertyUtils.getLogPath() + PropertyUtils.getLogFileName();
        mGson = new Gson();
        mApi = Api.getInstance();
        mDebugCounter = 0;
        System.out.println("mLogFileName: " + logFileName);
        try {
            FileWriter fileWriter = new FileWriter(logFileName, true);
            mBufferedWriter = new BufferedWriter(fileWriter);
            mBufferedWriter.write("[ ");
        } catch (Exception e) {
            System.out.println("Logging start Exception: " + e.getMessage());
        }
        return logFileName;
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
            cleanInterval();
        } catch (Exception e) {
            System.out.println("Logging stop Exception: " + e.getMessage());
        }

    }
    public void addEntry(Record record) {
        /* // keep for a while
        TxData txData = new TxData();
        txData.addRecord(record);
        String output = mGson.toJson(txData);
        mDebugCounter += 1;
        System.out.println(output);
        try {
            mBufferedWriter.write(output + ", ");
        } catch (Exception e) {
            System.out.println("addEntry write file Exception: " + e.getMessage());
        }
        mApi.sendResult(output);
        */
        aggregateRecords.add(record);
    }

    private void cleanInterval() {
        interval = null;
        intervalTask.cancel();
        intervalTask = null;
    }
}
