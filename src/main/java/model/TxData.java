package model;

import java.util.ArrayList;

public class TxData {
    private String type;
    private TxDataPayload payload;

    public TxData() {
        this.payload = new TxDataPayload();
        this.type = "txdata";
    }
    public String getType() { return this.type; }
    public TxDataPayload getPayload() { return this.payload; }

    public void addRecord (Record record) { this.payload.addRecord(record); }
    public void setRecords(ArrayList<Record> records) { this.payload.setRecords(records); }

    private static class TxDataPayload {
        private ArrayList<Record> records = new ArrayList();
        public void setRecords(ArrayList<Record> records) {
            this.records = records;
        }
        public void addRecord (Record record) { this.records.add(record); }
    }
}
