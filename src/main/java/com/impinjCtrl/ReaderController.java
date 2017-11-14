package com.impinjCtrl;

import com.impinj.octane.*;
import lib.RequestManager;
import lib.Logging;
import lib.PropertyUtils;
import java.util.Scanner;

public class ReaderController {
    private static ImpinjReader mReader;
    private static RfidEntryManager rfidEntryManager;
    private static ReaderStatus readerStatus;
    private static ReaderController instance;
    public static ReaderController getInstance () {
        if (instance == null) { instance = new ReaderController(); }
        return instance;
    }
    public void initialize() {
        mReader = new ImpinjReader();
        rfidEntryManager = new RfidEntryManager();
        readerStatus = new ReaderStatus();
        RequestManager requestManager = RequestManager.getInstance();
        try {
            mReader.connect(PropertyUtils.getReaderHost());
            controlReader("STOP", "", ""); // Reader continutes singulating even when JAVA app stops. resets reader at init.
            System.out.println("Connected to reader");
        } catch (OctaneSdkException e) {
            System.out.println("mReader.connect OctaneSdkException: " + e.getMessage());
        }
        initTerminalInterface(); // Command-line reader control interface
        requestManager.initSocketIOInterface(); // Socket.io reader control interface
    }
    // control readers' start, stop, debug etc, and returns JSON result
    // Output: { type: "readerstatus", message: STR, payload: OBJ, error: BOOL, debugMode: BOOL, logFile: STR }
    public String controlReader(String command, String eventId, String raceId) {
        Boolean isSingulating;
        Boolean hasError = false;
        String message = "";
        Logging logger = Logging.getInstance();
        try {
            isSingulating = mReader.queryStatus().getIsSingulating();
            readerStatus.setIsSingulating(isSingulating);
            if (command.equals("STOP")) {
                if (!isSingulating) { // Already stopped, return error
                    message = "Not singulating, Ignoring stop command";
                    hasError = true;
                } else {
                    message = "Reader stopped";
                    mReader.removeTagReportListener();
                    mReader.deleteAllOpSequences();
                    mReader.stop();
                    rfidEntryManager.setEventId("");
                    rfidEntryManager.setRaceId("");
                    readerStatus.setLogFile("");
                    logger.finish();
                }
            } else if (command.equals("DEBUG") || command.equals("START")) {
                if (isSingulating) { // Already stopped, return error
                    message = "Already singulating, ignoring start command";
                    hasError = true;
                } else {
                    if (command.equals("DEBUG")) {
                        message = "Starting reader (debug mode)";
                        mReader.applySettings(getSettings(SearchMode.DualTarget));
                        // Terminates debug run in 5 secs
                        new java.util.Timer().schedule(
                                new java.util.TimerTask() {
                                    @Override
                                    public void run() { controlReader("STOP", "", ""); }
                                },
                                5000
                        );
                    } else {
                        message = "Starting reader";
                        mReader.applySettings(getSettings(SearchMode.TagFocus));
                    }
                    rfidEntryManager.setEventId(eventId);
                    rfidEntryManager.setRaceId(raceId);
                    readerStatus.setLogFile(logger.initialize());
                    mReader.setTagReportListener(new ReportFormat());
                    mReader.start();
                }
            } else if (command.equals("STATUS")) {
                message = "Reader status";
            } else {
                message = "Unknown command: " + command;
            }
        } catch (OctaneSdkException e) {
            System.out.println("controlReader STATUS OctaneSdkException: " + e.getMessage());
        }
        readerStatus.setMessage(message);
        readerStatus.setHasError(hasError);
        return readerStatus.getReaderStatusInJSONString();
    }
    private static Settings getSettings (SearchMode searchMode) throws OctaneSdkException {
        Settings settings = mReader.queryDefaultSettings();
        ReportConfig report = settings.getReport();
        AntennaConfigGroup antennas = settings.getAntennas();

        settings.setReaderMode(ReaderMode.AutoSetDenseReader);
        settings.setSession(1);
        settings.setSearchMode(searchMode);
        report.setMode(ReportMode.Individual);
        report.setIncludeAntennaPortNumber(true);
        report.setIncludeChannel(true);
        report.setIncludePeakRssi(true);
        report.setIncludePhaseAngle(true);
        antennas.disableAll();
        antennas.enableAll();
        return settings;
    }
    // Commandline controls
    private void initTerminalInterface() {
        System.out.println("Commands: START || DEBUG || STOP || STATUS");
        Scanner s = new Scanner(System.in);
        while (s.hasNextLine()) { controlReader(s.nextLine(), "", ""); }
    }
}