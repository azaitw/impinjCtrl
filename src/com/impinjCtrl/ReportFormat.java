package com.impinjCtrl;

import com.impinj.octane.ImpinjReader;
import com.impinj.octane.Tag;
import com.impinj.octane.TagReport;
import com.impinj.octane.TagReportListener;
import org.json.simple.JSONObject;

import java.util.List;

public class ReportFormat implements TagReportListener {
    @Override
    public void onTagReported(ImpinjReader reader, TagReport report) {
        List<Tag> tags = report.getTags();
        String debugMode = System.getProperty(Properties.debugMode);
        JSONObject result = new JSONObject();

        for (Tag t : tags) {
            result.put("epc", t.getEpc().toString().replace(" ", "").toLowerCase());
            result.put("timestamp", t.getFirstSeenTime().ToString());
            if (debugMode != null && debugMode.equals("1")) {
                result.put("antenna", t.getAntennaPortNumber());
                result.put("doppler", t.getRfDopplerFrequency());
                result.put("peakRssi", t.getRfDopplerFrequency());
                result.put("channelMhz", t.getChannelInMhz());
            }
            System.out.println(result.toJSONString());
        }
    }
}
