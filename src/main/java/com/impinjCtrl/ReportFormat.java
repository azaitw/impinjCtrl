package com.impinjCtrl;

import com.impinj.octane.ImpinjReader;
import com.impinj.octane.Tag;
import com.impinj.octane.TagReport;
import com.impinj.octane.TagReportListener;
import lib.Logging;
import lib.PropertyUtils;
import model.Record;
import java.util.List;

public class ReportFormat implements TagReportListener {
    public void onTagReported(ImpinjReader reader, TagReport report) {
        List<Tag> tags = report.getTags();
        for (Tag t : tags) {
            Record record = new Record();
            record.setEpc(t.getEpc().toString().replace(" ", "").toLowerCase());
            record.setTimestamp(PropertyUtils.getTimestamp());
            record.setAnt(t.getAntennaPortNumber());
            record.setSignal(t.getPeakRssiInDbm());
            record.setAngle(t.getPhaseAngleInRadians());
            Logging.getInstance().addEntry(record);
        }
    }
}
