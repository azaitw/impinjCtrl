package model;

public class RaceResult extends BasicResult {
    private TxData payload;

    public RaceResult () {}

    public RaceResult(String eventId, String raceId) {
        super(eventId, raceId);
    }

    public TxData getPayload() {
        return payload;
    }

    public void setPayload(TxData payload) {
        this.payload = payload;
    }
}
