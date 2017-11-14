package com.impinjCtrl;

import com.impinj.octane.ImpinjReader;
import com.impinj.octane.Tag;
import com.impinj.octane.TagReport;
import com.impinj.octane.TagReportListener;
import lib.PropertyUtils;
import java.util.List;

public class ReportFormat implements TagReportListener {
    public void onTagReported(ImpinjReader reader, TagReport report) {
        List<Tag> tags = report.getTags();
        RfidEntryManager rfidEntryManager = RfidEntryManager.getInstance();
        for (Tag t : tags) {
            String epc = t.getEpc().toString().replace(" ", "").toLowerCase();
            Long timestamp = PropertyUtils.getTimestamp();
            Short ant = t.getAntennaPortNumber();
            Double signal = t.getPeakRssiInDbm();
            rfidEntryManager.addEntry(epc, timestamp, ant, signal);
        }
    }
}
