package util;

import java.lang.Math;

public class LamportClock {
    public enum Event {
        CONCURRENT,
        BEFORE,
        AFTER
    }

    private int timestamp;


    public LamportClock() {
        this.timestamp = 0;
    }

    /**
     *
     */
    public synchronized void tick() {
        this.timestamp++;
    }

    public synchronized void updateTime(int curr_timestamp) {
        this.timestamp = Math.max(this.timestamp, curr_timestamp) + 1;
    }


    public synchronized int getTimestamp() {
        return this.timestamp;
    }

    public synchronized Event processTimestamp(int external_timestamp) {
        int curr_timestamp = getTimestamp();

        if (external_timestamp <= curr_timestamp) {
            tick();
            return Event.CONCURRENT;
        } else {
            updateTime(external_timestamp);
            return Event.AFTER;
        }
    }

}
