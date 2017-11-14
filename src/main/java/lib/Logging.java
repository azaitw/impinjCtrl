package lib;

import com.impinjCtrl.ReaderController;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

public class Logging {
    public static Logging instance;
    private FileWriter fileWriter;
    private BufferedWriter bufferedWriter;
    private PrintWriter printerWriter;
    private Integer entryCounter = 0;

    public static Logging getInstance() {
        if (instance == null) { instance = new Logging(); }
        return instance;
    }
    private Integer getEntryCounter () { return entryCounter; }
    private void setEntryCounter(Integer entryCounter) { this.entryCounter = entryCounter; }

    // Create log folder if not available
    public static void createLogFolderIfUnavailable() {
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
    public String initialize() {
        String fileNameWithPath = PropertyUtils.getLogPath() + PropertyUtils.getLogFileName();
        try {
            fileWriter = new FileWriter(fileNameWithPath, true);
            bufferedWriter = new BufferedWriter(fileWriter);
            printerWriter = new PrintWriter(bufferedWriter);
            setEntryCounter(0);
        } catch (Exception e) {
            System.out.println("startReader Exception: " + e.getMessage());
        }
        return fileNameWithPath;
    }
    public void addEntry (String entryInJsonString) {
        String content = "";
        Integer counter = getEntryCounter();
        if (counter > 0) {
            content += ",";
        }
        content += entryInJsonString;
        counter += 1;
        setEntryCounter(counter);
        printerWriter.print(entryInJsonString);
        try {
            bufferedWriter.write(content);
        } catch (IOException e) {
            System.out.println("writeJSONToFile IOException: " + e.getMessage());
        }
    }
    public void finish () {
        try {
            bufferedWriter.flush();
            bufferedWriter.close();
            printerWriter.flush();
            printerWriter.close();
        } catch (Exception e) {
            System.out.println("finish: " + e.getMessage());
        }
        printerWriter = null;
        bufferedWriter = null;
        fileWriter = null;
    }
}