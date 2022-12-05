package com.smartbear.tcpbench;

import java.io.File;
import java.util.List;

public interface Query {
    /**
     * @return all failing test cycles, chronologically ordered
     */
    List<String> getOrderedFailingTestCycleIds();

    /**
     * @param testCycleId the test cycle id
     * @return the verdicts of the test cycle
     */
    List<Verdict> getVerdicts(String testCycleId);

    /**
     * Get the git ordered SHAs of a test cycle. This list may be empty.
     * If the list is not empty, it has 2 or more SHAs. The first one is
     * the *parent commit* of the *first* SHA in the test cycle. The last one
     * is the *last* SHA in the test cycle.
     *
     * The first and last SHA can be used to compute the diff that went into this
     * test cycle.
     *
     * @param testCycleId the test cycle id
     * @return the git shas of the test cycle
     */
    List<String> getOrderedShas(String testCycleId);

    /**
     * @param orderedShas a set of shas
     * @param regexp a filter for file
     * @return the diff between two commits
     */
    Changes getChanges(List<String> orderedShas, String regexp);

    /**
     * @return the path to the Git repository
     */
    File getRepository();
}
