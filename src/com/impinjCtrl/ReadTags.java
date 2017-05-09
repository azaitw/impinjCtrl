package com.impinjCtrl;

import com.impinj.octane.ImpinjReader;
import com.impinj.octane.Settings;
import com.impinj.octane.OctaneSdkException;
import java.util.Scanner;

public class ReadTags {
    public static void main(String[] args) {
        try {
            String hostname = System.getProperty(Properties.hostname);

            if (hostname == null) {
                throw new Exception("Must specify the '"
                        + Properties.hostname + "' property");
            }

            ImpinjReader reader = new ImpinjReader();

            System.out.println("Connecting...");
            reader.connect(hostname);

            Settings settings = reader.queryDefaultSettings();
            ReaderSettings.setSettings(reader, settings);
            reader.setTagReportListener(new ReportFormat());
            reader.start();

            System.out.println("Starting reader");
            System.out.println("Type STOP and return to exit");
            System.out.println("Type STATUS and return for reader info");

            Scanner s = new Scanner(System.in);
            //s.nextLine();

            while (s.hasNextLine()) {
                String line = s.nextLine();
                System.out.println(line);
                if (line.equals("STOP")) {
                    break;
                } else if (line.equals("STATUS")) {
                    ReaderSettings.getSettings(reader, settings);
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
