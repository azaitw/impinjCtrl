package com.impinjCtrl;

import com.google.gson.Gson;
import com.impinj.octane.ImpinjReader;
import com.impinj.octane.Settings;
import com.impinj.octane.OctaneSdkException;
import java.util.Scanner;

public class ReadTags {
    public static void main(String[] args) {
        try {
            String hostname = System.getProperty(Properties.hostname);

            if (hostname == null) {
                throw new Exception("{\"message\" : \"Must specify hostname\"}");
            }

            ImpinjReader reader = new ImpinjReader();

            System.out.println("{\"message\" : \"Connecting\"}");
            reader.connect(hostname);

            Settings settings = ReaderSettings.getSettings(reader);

            reader.setTagReportListener(new ReportFormat());
            reader.applySettings(settings);

            reader.start();

            System.out.println("{\"message\" : \"Starting reader\"}");

            Scanner s = new Scanner(System.in);
            //s.nextLine();

            while (s.hasNextLine()) {
                String line = s.nextLine();
                System.out.println(line);
                if (line.equals("STOP")) {
                    break;
                } else if (line.equals("STATUS")) {
                    ReaderSettings.getReaderInfo(reader, settings);
                }
            }
            System.out.println("{\"message\" : \"Disconnecting\"}");
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
