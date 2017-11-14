package model;

public class ReaderInfo {
    private String modelName;
    private int modelNumber;
    private long antennaCount;
    private boolean isSingulating;
    private short temperature;
    private String readerMode;
    private String searchMode;
    private int session;
    private String rxSensitivity;
    private String txPower;

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public int getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(int modelNumber) {
        this.modelNumber = modelNumber;
    }

    public long getAntennaCount() {
        return antennaCount;
    }

    public void setAntennaCount(long antennaCount) {
        this.antennaCount = antennaCount;
    }

    public boolean isSingulating() {
        return isSingulating;
    }

    public void setSingulating(boolean singulating) {
        isSingulating = singulating;
    }

    public short getTemperature() {
        return temperature;
    }

    public void setTemperature(short temperature) {
        this.temperature = temperature;
    }

    public String getReaderMode() {
        return readerMode;
    }

    public void setReaderMode(String readerMode) {
        this.readerMode = readerMode;
    }

    public String getSearchMode() {
        return searchMode;
    }

    public void setSearchMode(String searchMode) {
        this.searchMode = searchMode;
    }

    public int getSession() {
        return session;
    }

    public void setSession(int session) {
        this.session = session;
    }

    public String getRxSensitivity() {
        return rxSensitivity;
    }

    public void setRxSensitivity(String rxSensitivity) {
        this.rxSensitivity = rxSensitivity;
    }

    public String getTxPower() {
        return txPower;
    }

    public void setTxPower(String txPower) {
        this.txPower = txPower;
    }
}
