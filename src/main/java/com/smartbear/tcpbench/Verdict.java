package com.smartbear.tcpbench;

import java.time.Duration;

public class Verdict {
    private final String testId;
    private final boolean failure;
    private final int count;
    private final Duration duration;

    public Verdict(String testId, boolean failure, int count, Duration duration) {
        this.testId = testId;
        this.failure = failure;
        this.count = count;
        this.duration = duration;
    }

    public String getTestId() {
        return testId;
    }

    public boolean isFailure() {
        return failure;
    }

    public int getCount() {
        return count;
    }

    public Duration getDuration() {
        return duration;
    }
}
