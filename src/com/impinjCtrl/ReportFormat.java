package com.impinjCtrl;

import com.impinj.octane.ImpinjReader;
import com.impinj.octane.Tag;
import com.impinj.octane.TagReport;
import com.impinj.octane.TagReportListener;

import java.util.List;

public class ReportFormat implements TagReportListener {
    @Override
    public void onTagReported(ImpinjReader reader, TagReport report) {
        List<Tag> tags = report.getTags();
        String debugMode = System.getProperty(Properties.debugMode);

        for (Tag t : tags) {
            System.out.print("{\"EPC\": \"" + t.getEpc().toString().replace(" ", "").toLowerCase() + "\"");

            if (debugMode.equals("1")) {
                System.out.print(", \"antenna\": " + t.getAntennaPortNumber());
                System.out.print(", \"doppler\": " + t.getRfDopplerFrequency());
                System.out.print(", \"peak_rssi\": " + t.getPeakRssiInDbm());
                System.out.print(", \"channnelMHz: " + t.getChannelInMhz());
            }
            System.out.print(", \"timestamp\": " + t.getFirstSeenTime().ToString() + "}");
            System.out.println("\n");
        }
    }
}
