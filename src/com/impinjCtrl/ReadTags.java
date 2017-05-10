package com.impinjCtrl;

import com.impinj.octane.ImpinjReader;
import com.impinj.octane.Settings;
import com.impinj.octane.OctaneSdkException;
import java.util.Scanner;
import org.json.simple.JSONObject;

public class ReadTags {
    public static void main(String[] args) {
        try {
            String hostname = System.getProperty(Properties.hostname);
            JSONObject msg = new JSONObject();

            if (hostname == null) {
                msg.put("message", "Must specify hostname");
                throw new Exception(msg.toJSONString());
            }

            ImpinjReader reader = new ImpinjReader();
            msg.put("message", "Connecting");
            System.out.println(msg.toJSONString());
            reader.connect(hostname);

            Settings settings = ReaderSettings.getSettings(reader);

            reader.setTagReportListener(new ReportFormat());
            reader.applySettings(settings);

            reader.start();
            msg.put("message", "Starting reader");
            System.out.println(msg.toJSONString());

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
            msg.put("message", "Disconnecting");
            System.out.println(msg);
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
