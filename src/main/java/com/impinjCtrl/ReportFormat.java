package com.impinjCtrl;

import com.impinj.octane.ImpinjReader;
import com.impinj.octane.Tag;
import com.impinj.octane.TagReport;
import com.impinj.octane.TagReportListener;
import java.io.FileWriter;
import lib.HttpClient;
import lib.PropertyUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.List;

public class ReportFormat implements TagReportListener {
    private boolean mIsDebugMode = PropertyUtils.isDebugMode();
    private HttpClient mHttpClient = HttpClient.getInstance();

    private void writeJSONToFile() {
        try {
            FileWriter file = new FileWriter(ReaderController.mLogFileName);
            JSONObject wrapper = new JSONObject();
            //wrapper.put("raw", ReaderController.mReadResultRaw);
            wrapper.put("recordsHashTable", ReaderController.mRecordsHashTable);
            wrapper.put("slaveEpcStat", ReaderController.mSlaveEpcStat);
            file.write(wrapper.toString());
            file.flush();
        } catch (IOException e) {
            System.out.println("writeJSONToFile IO error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("writeJSONToFile assembling JSON error: " + e.getMessage());
        }
    }
    private void sendResult() {
        JSONObject txData = new JSONObject();
        JSONObject payload = new JSONObject();
        try {
            if (ReaderController.mRaceId != null) {
                txData.put("type", "rxdata");
                txData.put("race", ReaderController.mRaceId);
            } else {
                txData.put("type", "rxdatatest");
                txData.put("event", ReaderController.mEventId);
            }
            payload.put("recordsHashTable", ReaderController.mRecordsHashTable);
            payload.put("slaveEpcStat", ReaderController.mSlaveEpcStat);
            txData.put("payload", payload);
        }  catch (Exception e) {
            System.out.println("sendResult assembling JSON error: " + e.getMessage());
        }
        Request req = new Request.Builder()
                .url(PropertyUtils.getAPiHost() + "/api/socket/impinj?sid=" + ReaderController.mSocketId)
                .post(RequestBody.create(HttpClient.MEDIA_TYPE_JSON, txData.toJSONString()))
                .build();

        mHttpClient.request(req, new Callback() {
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                System.out.println("send tag data fail. Error Message: " + e.getMessage());
            }
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody body = response.body();
                body.close();
            }
        });
    }
    public void onTagReported(ImpinjReader reader, TagReport report) {
        List<Tag> tags = report.getTags();
        for (Tag t : tags) {
            String readingEpc = t.getEpc().toString().replace(" ", "").toLowerCase();
            String epc = readingEpc;
            Boolean isSlaveEpc = false;
            Long ts = PropertyUtils.getTimestamp();
            // Check if slave epc
            if (ReaderController.mSlaveEpcMap != null && ReaderController.mSlaveEpcMap.get(readingEpc) != null) {
                epc = ReaderController.mSlaveEpcMap.get(readingEpc).toString();
                isSlaveEpc = true;
            }
            if (mIsDebugMode) {
                JSONObject result = new JSONObject();
                try {
                    result.put("epc", epc);
                    result.put("timestamp", ts);
                    result.put("isSlave", isSlaveEpc);
                    result.put("antenna", t.getAntennaPortNumber());
                    result.put("doppler", t.getRfDopplerFrequency());
                    result.put("peakRssi", t.getPeakRssiInDbm());
                    result.put("phase angel", t.getPhaseAngleInRadians());
                }  catch (Exception e) {
                    System.out.println("Assembling debug result error: " + e.getMessage());
                }
                System.out.println(result.toString());
            } else {
                JSONArray hashtableArray = new JSONArray();
                JSONObject slaveStat = new JSONObject();
                Integer lastValueIndex = -1;
                // Check if entries exist
                if (ReaderController.mRecordsHashTable.get(epc) != null) {
                    hashtableArray = (JSONArray) ReaderController.mRecordsHashTable.get(epc);
                    lastValueIndex = hashtableArray.size() - 1;
                }
                // Check if interval valid
                if (lastValueIndex < 0 || (ts - (Long) hashtableArray.get(lastValueIndex) >= ReaderController.mValidIntervalMs)) {
                    String print = "Valid epc: " + readingEpc + ". timestamp: " + ts;
                    try {
                        hashtableArray.add(ts);
                        ReaderController.mRecordsHashTable.put(epc, hashtableArray);
                        // Add slaveEpcStat record
                        if (isSlaveEpc) {
                            if (ReaderController.mSlaveEpcStat.get(epc) != null) {
                                slaveStat = (JSONObject) ReaderController.mSlaveEpcStat.get(epc);
                            }
                            lastValueIndex = hashtableArray.size() - 1;
                            String key = lastValueIndex.toString();
                            slaveStat.put(key, 1);
                            ReaderController.mSlaveEpcStat.put(epc, slaveStat);
                            print += ". isSlaveOf: " + epc;
                        }
                    } catch (Exception e) {
                        System.out.println("Assembling result error: " + e.getMessage());
                    }
                    System.out.println(print);
                    writeJSONToFile(); // Write result to log file
                    sendResult(); // Post result to API
                }
            }
        }
    }
}
