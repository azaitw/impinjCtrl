package com.impinjCtrl;


import com.google.gson.Gson;
import com.impinj.octane.*;

import java.util.ArrayList;

public class ReaderSettings {
    public String modelName;
    public Integer modelNumber;
    public String firmwareVersion;
    public Long antennaCount;
    public Boolean isConnected;
    public Boolean isSingulating;
    public Short temperature;
    public com.impinj.octane.ReaderMode readerMode;
    public com.impinj.octane.SearchMode searchMode;
    public Integer session;
    public String RxSensitivityAntenna1;
    public String txPowerAntenna1;

    public static void setSettings (ImpinjReader reader, Settings settings) throws OctaneSdkException {
        String sensitivityDbm = System.getProperty(Properties.sensitivityDbm);
        String powerDbm = System.getProperty(Properties.powerDbm);
        String debugMode = System.getProperty(Properties.debugMode);

        ReportConfig report = settings.getReport();
        report.setIncludeFirstSeenTime(true);
        report.setMode(ReportMode.Individual);
        if (debugMode.equals("1")) {
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
        reader.applySettings(settings);
    }
    public static void getSettings (ImpinjReader reader, Settings settings) throws OctaneSdkException {

        ReaderSettings result = new ReaderSettings();
        Gson gson = new Gson();

        FeatureSet features = reader.queryFeatureSet();
        Status status = reader.queryStatus();

        result.modelName = features.getModelName();
        result.modelNumber = features.getModelNumber();
        result.firmwareVersion = features.getFirmwareVersion();
        result.antennaCount = features.getAntennaCount();
        result.isConnected = status.getIsConnected();
        result.isSingulating = status.getIsSingulating();
        result.temperature = status.getTemperatureCelsius();
        result.readerMode = settings.getReaderMode();
        result.searchMode = settings.getSearchMode();
        result.session = settings.getSession();
        ArrayList<AntennaConfig> ac = settings.getAntennas().getAntennaConfigs();

        if (ac.get(0).getIsMaxRxSensitivity()) {
            result.RxSensitivityAntenna1 = "Max";
        } else {
            result.RxSensitivityAntenna1 = Double.toString(ac.get(0).getRxSensitivityinDbm()) + " dbm";
        }

        if (ac.get(0).getIsMaxTxPower()) {
            result.txPowerAntenna1 = "Max";
        } else {
            result.txPowerAntenna1 = Double.toString(ac.get(0).getTxPowerinDbm()) + " dbm";
        }
        String json = gson.toJson(result);
        System.out.println(json);
    }
}
