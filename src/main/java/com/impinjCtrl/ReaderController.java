package com.impinjCtrl;

import com.impinj.octane.ImpinjReader;
import com.impinj.octane.OctaneSdkException;
import lib.Api;
import lib.Logging;
import lib.PropertyUtils;
import org.json.simple.JSONObject;
import java.util.Scanner;

public class ReaderController {
    private static ImpinjReader mReader;
    // Init command when executing this app
    public void initialize() {
        mReader = new ImpinjReader();
        try {
            mReader.connect(PropertyUtils.getReaderHost());
            controlReader("STOP", "", ""); // Reader continutes singulating even when JAVA app stops. resets reader at init.
            System.out.println("Connected to reader");
        } catch (OctaneSdkException e) {
            System.out.println("mReader.connect OctaneSdkException: " + e.getMessage());
        }
        initTerminalInterface(); // Command-line reader control interface
        Api.initSocketIOInterface(); // Socket.io reader control interface
    }
    // control readers' start, stop, debug etc, and returns JSON result
    // Output: { type: "readerstatus", message: STR, payload: OBJ, error: BOOL, debugMode: BOOL, logFile: STR }
    public static JSONObject controlReader(String command, String eventId, String raceId) {
        JSONObject result = new JSONObject();
        String message = "";
        Boolean isDebugMode = false;
        Boolean hasError = false;
        try {
            Boolean isSingulating = mReader.queryStatus().getIsSingulating();
            if (command.equals("STOP")) {
                if (!isSingulating) { // Already stopped, return error
                    message = "Not singulating, Ignoring stop command";
                    hasError = true;
                } else {
                    message = "Reader stopped";
                    mReader.removeTagReportListener();
                    mReader.deleteAllOpSequences();
                    mReader.stop();
                    Logging.resetLogging();
                }
            } else if (command.equals("DEBUG") || command.equals("START")) {
                if (isSingulating) { // Already stopped, return error
                    message = "Already singulating, ignoring start command";
                    hasError = true;
                } else {
                    if (command.equals("DEBUG")) {
                        message = "Starting reader (debug mode)";
                        result.put("debugMode", true);
                        isDebugMode = true;

                        new java.util.Timer().schedule(
                                new java.util.TimerTask() {
                                    @Override
                                    public void run() {
                                        ReaderController.controlReader("STOP", "", "");
                                    }
                                },
                                5000
                        );
                    } else {
                        message = "Starting reader";
                    }
                    result.put("logFile", Logging.initLogging(eventId, raceId));
                    mReader.setTagReportListener(new ReportFormat());
                    mReader.applySettings(ReaderSettings.getSettings(mReader, isDebugMode));
                    mReader.start();
                }
            } else if (command.equals("STATUS")) {
                message = "Reader status";
            } else {
                message = "Unknown command: " + command;
            }
            result.put("message", message);
            result.put("error", hasError);
            result.put("type", "readerstatus");
            result.put("payload", ReaderSettings.getReaderInfo(mReader, ReaderSettings.getSettings(mReader, isDebugMode)));
        } catch (OctaneSdkException e) {
            System.out.println("controlReader STATUS OctaneSdkException: " + e.getMessage());
        }
        System.out.println(message);
        return result;
    }
    // Commandline controls
    private void initTerminalInterface() {
        System.out.println("Commands: START || DEBUG || STOP || STATUS");
        Scanner s = new Scanner(System.in);
        while (s.hasNextLine()) { controlReader(s.nextLine(), "", ""); }
    }
}