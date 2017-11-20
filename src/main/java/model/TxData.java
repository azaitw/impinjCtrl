package model;

import java.util.ArrayList;

public class TxData {
    private String type;
    private TxDataPayload payload;

    public TxData(String eventId, String raceId) {
        this.payload = new TxDataPayload();
        if (raceId == "") {
            this.type = "txdata";
            this.payload.setRaceId(raceId);
        } else {
            this.type = "txdata_test";
            this.payload.setEventId(eventId);
        }
    }
    public void addRecords (Record record) { this.payload.addRecords(record); }
    public void setRecords(ArrayList<Record> records) { this.payload.setRecords(records); }

    private class TxDataPayload {
        private String eventId;
        private String raceId;
        private ArrayList<Record> records = new ArrayList();
        public void setRecords(ArrayList<Record> records) {
            this.records = records;
        }
        public void addRecords (Record record) { this.records.add(record); }

        public void setEventId (String eventId) { this.eventId = eventId; }
        public void setRaceId (String raceId) { this.raceId = raceId; }
    }
}
