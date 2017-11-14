package com.impinjCtrl;

import com.impinj.octane.ImpinjReader;
import com.impinj.octane.OctaneSdkException;
import lib.Api;
import lib.Logging;
import lib.PropertyUtils;
import model.ReaderInfoResult;

import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class ReaderController {
    private static ReaderController instance;

    private ImpinjReader mReader;
    private Api mApi;
    private Logging mLogging;

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
        mApi = Api.getInstance();
        mApi.buildHttpClient();

        // new Logging instance
        mLogging = Logging.getInstance();
        mLogging.initLogPath();

        mReader = new ImpinjReader();
        try {
            mReader.connect(PropertyUtils.getReaderHost());
            controlReader("STOP", "init", "init"); // Reader continues singulating event when JAVA app stops. resets reader at init.
            System.out.println("Connected to reader");
        } catch (OctaneSdkException e) {
            System.out.println("mReader.connect OctaneSdkException: " + e.getMessage());
        }
        initTerminalInterface(); // Command-line reader control interface
        mApi.initSocketIOInterface(); // Socket.io reader control interface
    }
    // control readers' start, stop, debug etc, and returns JSON result
    // Output: { type: "readerstatus", message: STR, payload: OBJ, error: BOOL, debugMode: BOOL, logFile: STR }
    public ReaderInfoResult controlReader(String command, final String eventId, final String raceId) {
        ReaderInfoResult rs = new ReaderInfoResult(eventId, raceId);
        String message = "";
        Boolean isDebugMode = false;
        Boolean hasError = false;

        try {
            Boolean isSingulating = mReader.queryStatus().getIsSingulating();
            if (command.equals("STOP")) {
                if (!isSingulating) { // Already stopped, return error
                    message = "Stopped , Ignoring stop command";
                    hasError = true;
                } else {
                    resetTimer();

                    message = "Reader stopped";
                    mReader.removeTagReportListener();
                    mReader.deleteAllOpSequences();
                    mReader.stop();
                    mLogging.resetLogging();
                }
            } else if (command.equals("DEBUG") || command.equals("START")) {
                if (isSingulating) { // Already started, return error
                    message = "Started, ignoring start command";
                    hasError = true;
                } else {
                    if (command.equals("DEBUG")) {
                        message = "Starting reader (debug mode)";
                        rs.setDebugMode(true);
                        isDebugMode = true;

                        resetTimer();
                        mTimer = new java.util.Timer();
                        mTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                controlReader("STOP", eventId, raceId);
                            }
                        }, 5000);

                    } else {
                        message = "Starting reader";
                    }

                    mReader.setTagReportListener(new ReportFormat());
                    mReader.applySettings(ReaderSettings.getSettings(mReader, isDebugMode));
                    mReader.start();
                }
            } else if (command.equals("STATUS")) {
                message = "Reader status";
            } else {
                message = "Unknown command: " + command;
            }
            rs.setMessage(message);
            rs.setError(hasError);
            rs.setType("readerstatus");
            rs.setPayload(ReaderSettings.getReaderInfo(mReader, ReaderSettings.getSettings(mReader, isDebugMode)));
        } catch (OctaneSdkException e) {
            System.out.println("controlReader STATUS OctaneSdkException: " + e.getMessage());
        }
        System.out.println(message);
        return rs;
    }

    // Commandline controls
    private void initTerminalInterface() {
        System.out.println("Commands: START || DEBUG || STOP || STATUS");
        Scanner s = new Scanner(System.in);
        while (s.hasNextLine()) { controlReader(s.nextLine(), "readLine", "readLine"); }
    }

    private void resetTimer () {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
    }
}