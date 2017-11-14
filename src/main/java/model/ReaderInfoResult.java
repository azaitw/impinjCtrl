package model;

public class ReaderInfoResult extends BasicResult {
    private ReaderInfo payload;

    public ReaderInfoResult() {
    }

    public ReaderInfoResult(String eventId, String raceId) {
        super(eventId, raceId);
    }

    public ReaderInfo getPayload() {
        return payload;
    }

    public void setPayload(ReaderInfo payload) {
        this.payload = payload;
    }
}
