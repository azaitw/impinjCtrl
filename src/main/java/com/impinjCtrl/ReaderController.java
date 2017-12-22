package com.impinjCtrl;

import com.google.gson.Gson;
import com.impinj.octane.*;
import lib.Api;
import lib.Logging;
import lib.PropertyUtils;
import model.ReaderStatus;
import model.SocketInput;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class ReaderController {
    private static ReaderController instance;
    private ImpinjReader mReader;
    private Timer mTimer;

    public static synchronized ReaderController getInstance() {
        if (instance == null) {
            instance = new ReaderController();
        }
        return instance;
    }
    // Init command when executing this app
    public void initialize() {
        // new Api instance
        Api api = Api.getInstance();
        api.buildHttpClient();

        // Execute create log folder command
        Logging.getInstance().createLogFolder();

        mReader = new ImpinjReader();
        try {
            System.out.println("Connecting...");
            mReader.connect(PropertyUtils.getReaderHost());
            instance.controlReader("STOP"); // Reader continues singulating event when JAVA app stops. resets reader at init.
            System.out.println("Connected to reader");
        } catch (OctaneSdkException e) {
            System.out.println("mReader.connect OctaneSdkException: " + e.getMessage());
        }
        api.initSocketIOInterface(); // Socket.io reader control interface
        instance.initTerminalInterface(); // Command-line reader control interface
    }
    public String controlReaderFromSocketIo (String input) {
        Gson gson = new Gson();
        SocketInput inputJson = gson.fromJson(input, SocketInput.class);
        String command = inputJson.getCommand();
        return gson.toJson(instance.controlReader(command));
    }
    // control readers' start, stop
    public ReaderStatus controlReader(String command) {
        ReaderStatus rs = new ReaderStatus();
        String message = "";
        Boolean hasError = false;
        try {
            Boolean isSingulating = mReader.queryStatus().getIsSingulating();
            if (command.equals("STOP")) {
                if (!isSingulating) { // Already stopped, return error
                    message = "Already stopped. Ignoring stop command";
                    hasError = true;
                } else {
                    message = "Reader stopped";
                    Logging.getInstance().stop();
                    resetTimer();
                    mReader.removeTagReportListener();
                    mReader.deleteAllOpSequences();
                    mReader.stop();
                }
            } else if (command.equals("DEBUG") || command.equals("START")) {
                if (isSingulating) { // Already started, return error
                    message = "Already started. Ignoring start command";
                    hasError = true;
                } else {
                    rs.setLogFile(Logging.getInstance().start());
                    if (command.equals("DEBUG")) {
                        message = "Starting reader (debug mode)";
                        resetTimer();
                        mTimer = new java.util.Timer();
                        mTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                instance.controlReader("STOP");
                                System.out.println("Stopped");
                            }
                        }, 5000);
                    } else {
                        message = "Starting reader";
                    }
                    mReader.setTagReportListener(new ReportFormat());
                    mReader.applySettings(getSettings(mReader));
                    mReader.start();
                }
            } else if (command.equals("STATUS")) {
                message = "Reader status";
            } else {
                message = "Unknown command: " + command;
            }
            rs.setMessage(message);
            rs.setError(hasError);
            rs.setIsSingulating(mReader.queryStatus().getIsSingulating());
        } catch (OctaneSdkException e) {
            System.out.println("controlReader STATUS OctaneSdkException: " + e.getMessage());
        }
        return rs;
    }
    // Commandline controls
    private void initTerminalInterface() {
        System.out.println("Commands: START || DEBUG || STOP || STATUS");
        Scanner s = new Scanner(System.in);
        while (s.hasNextLine()) {
            ReaderStatus rs = instance.controlReader(s.nextLine());
            Gson gson = new Gson();
            System.out.println(gson.toJson(rs));
        }
    }
    private void resetTimer () {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
    }
    /*
    Search mode determines how reader change tags' state, or how frequent a tag is reported when in sensor field
    https://support.impinj.com/hc/en-us/articles/202756158-Understanding-EPC-Gen2-Search-Modes-and-Sessions
    https://support.impinj.com/hc/en-us/articles/202756368-Optimizing-Tag-Throughput-Using-ReaderMode
    TagFocus uses Singletarget session 1 with fewer reports when in sensor field (Auto de-dup)
    Race timing recommendation: session 1
    http://racetiming.wimsey.co/2015/05/rfid-inventory-search-modes.html
    settings.setSearchMode(SearchMode.SingleTarget);
    */
    private Settings getSettings (ImpinjReader reader) throws OctaneSdkException {
        Settings settings = reader.queryDefaultSettings();
        ReportConfig report = settings.getReport();
        AntennaConfigGroup antennas = settings.getAntennas();

        settings.setReaderMode(ReaderMode.AutoSetDenseReader);
        settings.setSession(1);
        settings.setSearchMode(SearchMode.DualTarget);

        report.setMode(ReportMode.Individual);
        report.setIncludeAntennaPortNumber(true);
        report.setIncludePeakRssi(true);
        antennas.disableAll();
        antennas.enableAll();
        return settings;
    }
}