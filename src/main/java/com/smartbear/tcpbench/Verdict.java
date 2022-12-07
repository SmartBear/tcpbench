package com.smartbear.tcpbench;

import java.time.Duration;

public class Verdict {
    private final String testId;
    private final boolean failure;
    private final Duration duration;

    public Verdict(String testId, boolean failure, Duration duration) {
        this.testId = testId;
        this.failure = failure;
        this.duration = duration;
    }

    public String getTestId() {
        return testId;
    }

    public boolean isFailure() {
        return failure;
    }

    public Duration getDuration() {
        return duration;
    }
}
