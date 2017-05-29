package com.impinjCtrl;

import com.impinj.octane.ImpinjReader;
import com.impinj.octane.Tag;
import com.impinj.octane.TagReport;
import com.impinj.octane.TagReportListener;
import lib.HttpClient;
import lib.PropertyUtils;
import lib.StringUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ReportFormat implements TagReportListener {
    private boolean mIsDebugMode;
    private String mLogFileName;
    private Logger mLogger;
    private HttpClient mHttpClient;

    ReportFormat() {
        mLogFileName = PropertyUtils.getLogFileName();
        mIsDebugMode = PropertyUtils.isDebugMode();

        mHttpClient = HttpClient.getInstance();

        if (!StringUtils.isEmpty(mLogFileName)) {
            mLogger = Logger.getLogger("bearDudeRace");

            try {
                FileHandler fileHandler = new FileHandler(mLogFileName, true);
                mLogger.addHandler(fileHandler);
                SimpleFormatter formatter = new SimpleFormatter();
                fileHandler.setFormatter(formatter);

            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onTagReported(ImpinjReader reader, TagReport report) {
        List<Tag> tags = report.getTags();
        JSONArray aggregateResult = new JSONArray();

        for (Tag t : tags) {
            JSONObject result = new JSONObject();
            result.put("epc", t.getEpc().toString().replace(" ", "").toLowerCase());
            result.put("timestamp", t.getFirstSeenTime().ToString());

            if (mIsDebugMode) {
                result.put("antenna", t.getAntennaPortNumber());
                result.put("doppler", t.getRfDopplerFrequency());
                result.put("peakRssi", t.getRfDopplerFrequency());
                result.put("channelMhz", t.getChannelInMhz());
                System.out.println(result.toJSONString());
            }

            aggregateResult.add(result);

            // log file
            if(!StringUtils.isEmpty(mLogFileName)) {
                mLogger.info(result.toJSONString());
            }
        }

        // send tag data to api
        JSONObject txData = new JSONObject();
        txData.put("type", ReaderController.EVENT_TRANSFER_DATA);
        txData.put("payload", aggregateResult);

        Request req = new Request.Builder()
                .url(PropertyUtils.getAPiHost())
                .post(RequestBody.create(HttpClient.MEDIA_TYPE_JSON, txData.toJSONString()))
                .build();

        mHttpClient.request(req, new Callback() {
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                System.out.println("update tag data fail");
            }

            public void onResponse(Call call, Response response) throws IOException {
                if (mIsDebugMode) {
                    HttpClient.parseRespose(response);
                }
            }
        });
    }
}
