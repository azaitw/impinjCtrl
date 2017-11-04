package com.impinjCtrl;

import com.impinj.octane.ImpinjReader;
import com.impinj.octane.Tag;
import com.impinj.octane.TagReport;
import com.impinj.octane.TagReportListener;
import lib.Logging;
import lib.PropertyUtils;
import org.json.simple.JSONObject;
import java.util.List;

public class ReportFormat implements TagReportListener {
    public void onTagReported(ImpinjReader reader, TagReport report) {
        List<Tag> tags = report.getTags();
        for (Tag t : tags) {
            JSONObject result = new JSONObject();
            try {
                result.put("epc", t.getEpc().toString().replace(" ", "").toLowerCase());
                result.put("timestamp", PropertyUtils.getTimestamp());
                result.put("ant", t.getAntennaPortNumber());
                result.put("signal", t.getPeakRssiInDbm());
                result.put("angel", t.getPhaseAngleInRadians());
                Logging.addEntry(result);
            } catch (Exception e) {
                System.out.println("onTagReported Exception: " + e.getMessage());
            }
        }
    }
}
