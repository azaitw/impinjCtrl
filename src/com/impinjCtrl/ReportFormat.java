package com.impinjCtrl;

import com.google.gson.Gson;
import com.impinj.octane.ImpinjReader;
import com.impinj.octane.Tag;
import com.impinj.octane.TagReport;
import com.impinj.octane.TagReportListener;

import java.util.List;

public class ReportFormat implements TagReportListener {
    public class ReportObj {
        public String epc;
        public String timestamp;
        public Short antenna;
        public Double doppler;
        public Double peakRssi;
        public Double channelMhz;
    }

    @Override
    public void onTagReported(ImpinjReader reader, TagReport report) {
        List<Tag> tags = report.getTags();
        String debugMode = System.getProperty(Properties.debugMode);
        ReportObj result = new ReportObj();
        Gson gson = new Gson();

        for (Tag t : tags) {
            result.epc =  t.getEpc().toString().replace(" ", "").toLowerCase();
            result.timestamp = t.getFirstSeenTime().ToString();
            if (debugMode != null && debugMode.equals("1")) {
                result.antenna = t.getAntennaPortNumber();
                result.doppler = t.getRfDopplerFrequency();
                result.peakRssi = t.getRfDopplerFrequency();
                result.channelMhz = t.getChannelInMhz();

            }
            System.out.println(gson.toJson(result));
        }
    }
}
