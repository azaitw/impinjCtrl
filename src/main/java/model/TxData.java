package model;

import java.util.ArrayList;

public class TxData {
    private ArrayList<Record> records = new ArrayList();
    private String recordType;
    private String type;
    private String mode;

    public void addRecords (Record record) {
        this.records.add(record);
    }

    public ArrayList<Record> getRecords() {
        return records;
    }

    public void setRecords(ArrayList<Record> records) {
        this.records = records;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
