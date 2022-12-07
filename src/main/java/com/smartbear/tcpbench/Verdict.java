package com.smartbear.tcpbench;

public class Verdict {
    private final String testId;
    private final boolean failure;

    public Verdict(String testId, boolean failure) {
        this.testId = testId;
        this.failure = failure;
    }

    public String getTestId() {
        return testId;
    }

    public boolean isFailure() {
        return failure;
    }
}
