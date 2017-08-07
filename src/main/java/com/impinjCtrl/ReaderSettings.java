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
import org.json.simple.JSONObject;
import lib.PropertyUtils;

public class ReaderSettings {
    public static Settings getSettings (ImpinjReader reader) throws OctaneSdkException {
        Settings settings = reader.queryDefaultSettings();
        String sensitivityDbm = System.getProperty(Properties.sensitivityDbm);
        String powerDbm = System.getProperty(Properties.powerDbm);
        Boolean debugMode = PropertyUtils.isDebugMode();

        ReportConfig report = settings.getReport();
        report.setIncludeFirstSeenTime(true);
        report.setMode(ReportMode.Individual);
        if (debugMode) {
            report.setIncludeAntennaPortNumber(true);
            report.setIncludeChannel(true);
            report.setIncludeCrc(true);
            //report.setIncludeDopplerFrequency(true);
            report.setIncludePeakRssi(true);
            report.setIncludePhaseAngle(true);

            //report.setIncludeFastId(true);
            //report.setIncludeGpsCoordinates(true);
            //report.setIncludeLastSeenTime(true);
            //report.setIncludePcBits(true);
            //report.setIncludeSeenCount(true);
        }
        // The reader can be set into various modes in which reader
        // dynamics are optimized for specific regions and environments.
        // The following mode, AutoSetDenseReader, monitors RF noise and interference and then automatically
        // and continuously optimizes the reader’s configuration
        settings.setReaderMode(ReaderMode.AutoSetDenseReader);

        //Search mode determines how reader change tags' state, or how frequent a tag is reported when in sensor field
        //https://support.impinj.com/hc/en-us/articles/202756158-Understanding-EPC-Gen2-Search-Modes-and-Sessions
        //https://support.impinj.com/hc/en-us/articles/202756368-Optimizing-Tag-Throughput-Using-ReaderMode
        //TagFocus uses Singletarget session 1 with fewer reports when in sensor field
        // Race timing recommendation: session 1
        // http://racetiming.wimsey.co/2015/05/rfid-inventory-search-modes.html
        if (debugMode) {
            //DualTarget
            settings.setSearchMode(SearchMode.DualTarget);
        } else {
            //settings.setSearchMode(SearchMode.SingleTarget);
            settings.setSearchMode(SearchMode.TagFocus);
            settings.setSession(1);
        }

        // set some special settings for antennas
        AntennaConfigGroup antennas = settings.getAntennas();
        antennas.disableAll();
        antennas.enableAll();
        for (short i = 1; i <= 4; i++) {
            //antennas.enableById(new short[]{i});
            // Define reader range
            if (sensitivityDbm == null) {
                antennas.getAntenna(i).setIsMaxRxSensitivity(true);
            } else {
                antennas.getAntenna(i).setRxSensitivityinDbm(Float.parseFloat(sensitivityDbm)); // -70
            }
            if (powerDbm == null) {
                antennas.getAntenna(i).setIsMaxTxPower(true);
            } else {
                antennas.getAntenna(i).setTxPowerinDbm(Float.parseFloat(powerDbm)); //20.0
            }
        }
        return settings;
    }
    public static JSONObject getReaderInfo (ImpinjReader reader, Settings settings) throws OctaneSdkException {

        JSONObject result = new JSONObject();

        FeatureSet features = reader.queryFeatureSet();
        Status status = reader.queryStatus();

        result.put("modelName", features.getModelName());
        result.put("modelNumber", features.getModelNumber());
        //result.put("firmwareVersion", features.getFirmwareVersion());
        result.put("antennaCount", features.getAntennaCount());
        //result.put("isConnected", status.getIsConnected());
        result.put("isSingulating", status.getIsSingulating());
        result.put("temperature", status.getTemperatureCelsius());
        result.put("readerMode", settings.getReaderMode().toString());
        result.put("searchMode", settings.getSearchMode().toString());
        result.put("session", settings.getSession());

        ArrayList<AntennaConfig> ac = settings.getAntennas().getAntennaConfigs();

        for (short i = 0; i < ac.size(); i ++) {
            result.put("getRxSensitivityinDbm_" + (i + 1), Double.toString(ac.get(i).getRxSensitivityinDbm()) + " dbm");
            result.put("getTxPowerinDbm_" + (i + 1), ac.get(i).getTxPowerinDbm() );
        }
        System.out.println(result.toJSONString());
        return result;
    }
}
