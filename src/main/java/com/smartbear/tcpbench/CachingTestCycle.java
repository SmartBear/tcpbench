package com.smartbear.tcpbench;

import java.util.List;

public class CachingTestCycle implements TestCycle {
    private final TestCycle delegate;
    private List<Verdict> verdicts;
    private List<String> orderedShas;

    public CachingTestCycle(TestCycle delegate) {
        this.delegate = delegate;
    }

    /**
     * @return the verdicts of the test cycle
     */
    public List<Verdict> getVerdicts() {
        if (verdicts == null) {
            verdicts = delegate.getVerdicts();
        }
        return verdicts;
    }

    public String getId() {
        return delegate.getId();
    }

    public List<String> getOrderedShas() {
        if (orderedShas == null) {
            orderedShas = delegate.getOrderedShas();
        }
        return orderedShas;
    }
}
