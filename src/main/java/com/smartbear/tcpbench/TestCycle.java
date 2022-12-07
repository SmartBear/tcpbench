package com.smartbear.tcpbench;

import java.util.List;

public interface TestCycle {
    /**
     * @return the verdicts of the test cycle
     */
    List<Verdict> getVerdicts();

    String getId();

    /**
     * Get the git ordered SHAs of a test cycle. This list may be empty.
     * If the list is not empty, it must have 2 or more SHAs. The first one is
     * the *parent commit* of the *first* SHA in the test cycle. The last one
     * is the *last* SHA in the test cycle.
     * <p>
     * The first and last SHA can be used to compute the diff that went into this
     * test cycle.
     *
     * @return the git shas of the test cycle
     */
    List<String> getOrderedShas();
}
