package com.impinjCtrl;

import com.impinj.octane.AntennaConfigGroup;
import com.impinj.octane.AntennaConfig;
import com.impinj.octane.FeatureSet;
import com.impinj.octane.ImpinjReader;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.ReaderMode;
import com.impinj.octane.ReportConfig;
import com.impinj.octane.ReportMode;
import com.impinj.octane.SearchMode;
import com.impinj.octane.Settings;
import com.impinj.octane.Status;
import java.util.ArrayList;

import model.ReaderInfo;

/*
    Search mode determines how reader change tags' state, or how frequent a tag is reported when in sensor field
    https://support.impinj.com/hc/en-us/articles/202756158-Understanding-EPC-Gen2-Search-Modes-and-Sessions
    https://support.impinj.com/hc/en-us/articles/202756368-Optimizing-Tag-Throughput-Using-ReaderMode
    TagFocus uses Singletarget session 1 with fewer reports when in sensor field (Auto de-dup)
    Race timing recommendation: session 1
    http://racetiming.wimsey.co/2015/05/rfid-inventory-search-modes.html
    settings.setSearchMode(SearchMode.SingleTarget);
*/
public class ReaderSettings {
    public static Settings getSettings (ImpinjReader reader, Boolean isDebugMode) throws OctaneSdkException {
        Settings settings = reader.queryDefaultSettings();
        ReportConfig report = settings.getReport();
        AntennaConfigGroup antennas = settings.getAntennas();

        settings.setReaderMode(ReaderMode.AutoSetDenseReader);
        settings.setSession(1);
        if (isDebugMode) {
            settings.setSearchMode(SearchMode.DualTarget);
        } else {
            settings.setSearchMode(SearchMode.TagFocus);
        }
        report.setMode(ReportMode.Individual);
        report.setIncludeAntennaPortNumber(true);
        report.setIncludeChannel(true);
        report.setIncludePeakRssi(true);
        report.setIncludePhaseAngle(true);
        antennas.disableAll();
        antennas.enableAll();
        return settings;
    }
    public static ReaderInfo getReaderInfo (ImpinjReader reader, Settings settings) throws OctaneSdkException {
        ReaderInfo rf = new ReaderInfo();
        FeatureSet features = reader.queryFeatureSet();
        Status status = reader.queryStatus();

        rf.setModelName(features.getModelName());
        rf.setModelNumber(features.getModelNumber());
        rf.setAntennaCount(features.getAntennaCount());
        rf.setSingulating(status.getIsSingulating());
        rf.setTemperature(status.getTemperatureCelsius());
        rf.setReaderMode(settings.getReaderMode().toString());
        rf.setSearchMode(settings.getSearchMode().toString());
        rf.setSession(settings.getSession());

        ArrayList<AntennaConfig> ac = settings.getAntennas().getAntennaConfigs();
        String rxSensitivity = String.valueOf(ac.get(0).getRxSensitivityinDbm()) + "Dbm";
        String txPower = String.valueOf(ac.get(0).getTxPowerinDbm()) + "Dbm";
        if (ac.get(0).getIsMaxRxSensitivity()) { rxSensitivity += " (max)"; }
        if (ac.get(0).getIsMaxTxPower()) { txPower += " (max)"; }

        rf.setRxSensitivity(rxSensitivity);
        rf.setTxPower(txPower);
        return rf;
    }
}
