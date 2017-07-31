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

public class ReaderSettings {
    public static Settings getSettings (ImpinjReader reader) throws OctaneSdkException {
        Settings settings = reader.queryDefaultSettings();
        String sensitivityDbm = System.getProperty(Properties.sensitivityDbm);
        String powerDbm = System.getProperty(Properties.powerDbm);
        String debugMode = System.getProperty(Properties.debugMode);

        ReportConfig report = settings.getReport();
        report.setIncludeFirstSeenTime(true);
        report.setMode(ReportMode.Individual);
        if (debugMode != null && debugMode.equals("1")) {
            report.setIncludeAntennaPortNumber(true);
            report.setIncludeChannel(true);
            report.setIncludeCrc(true);
            report.setIncludeDopplerFrequency(true);
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

        // set some special settings for antenna 1
        AntennaConfigGroup antennas = settings.getAntennas();
        antennas.disableAll();
        antennas.enableById(new short[]{1});

        //Search mode determines how reader change tags' state, or how frequent a tag is reported when in sensor field
        //https://support.impinj.com/hc/en-us/articles/202756158-Understanding-EPC-Gen2-Search-Modes-and-Sessions
        //TagFocus uses Singletarget session 1 with fewer reports when in sensor field
        settings.setSearchMode(SearchMode.TagFocus);
        settings.setSession(1);

        // Define reader range
        if (sensitivityDbm == null) {
            antennas.getAntenna((short) 1).setIsMaxRxSensitivity(true);
        } else {
            antennas.getAntenna((short) 1).setRxSensitivityinDbm(Float.parseFloat(sensitivityDbm)); // -70
        }
        if (powerDbm == null) {
            antennas.getAntenna((short) 1).setIsMaxTxPower(true);
        } else {
            antennas.getAntenna((short) 1).setTxPowerinDbm(Float.parseFloat(powerDbm)); //20.0
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

        if (ac.get(0).getIsMaxRxSensitivity()) {
            result.put("RxSensitivityAntenna1", "Max");
        } else {
            result.put("RxSensitivityAntenna1", Double.toString(ac.get(0).getRxSensitivityinDbm()) + " dbm");
        }

        if (ac.get(0).getIsMaxTxPower()) {
            result.put("txPowerAntenna1", "Max");
        } else {
            result.put("txPowerAntenna1", Double.toString(ac.get(0).getTxPowerinDbm()) + " dbm");
        }
        //System.out.println(result.toJSONString());
        return result;
    }
}
