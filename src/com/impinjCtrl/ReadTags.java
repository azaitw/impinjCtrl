package com.impinjCtrl;

import com.impinj.octane.*;
import java.util.Scanner;

public class ReadTags {
    public static void main(String[] args) {

        try {
            String hostname = System.getProperty(Properties.hostname);
            String sensitivityDbm = System.getProperty(Properties.sensitivityDbm);
            String powerDbm = System.getProperty(Properties.powerDbm);
            String debugMode = System.getProperty(Properties.debugMode);

            if (hostname == null) {
                throw new Exception("Must specify the '"
                        + Properties.hostname + "' property");
            }

            ImpinjReader reader = new ImpinjReader();

            System.out.println("Connecting");
            reader.connect(hostname);

            Settings settings = reader.queryDefaultSettings();
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
            }
            //report.setIncludeFastId(true);
            //report.setIncludeGpsCoordinates(true);
            //report.setIncludeLastSeenTime(true);
            //report.setIncludePcBits(true);
            //report.setIncludeSeenCount(true);


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
            reader.setTagReportListener(new ReportFormat());

            System.out.println("Applying Settings");
            reader.applySettings(settings);

            System.out.println("Starting");
            reader.start();

            System.out.println("Type STOP and Enter to exit.");
            Scanner s = new Scanner(System.in);
            //s.nextLine();

            while (s.hasNextLine()) {
                String line = s.nextLine();
                System.out.println(line);
                if (line.equals("STOP")) {
                    break;
                }
            }
            System.out.println("Disconnecting.");
            reader.stop();
            reader.disconnect();
        } catch (OctaneSdkException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }
}
