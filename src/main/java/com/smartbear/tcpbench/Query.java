package com.smartbear.tcpbench;

import java.io.File;
import java.util.List;
import java.util.Set;

public interface Query {
    /**
     * @return all test cycles, chronologically ordered
     */
    List<String> getOrderedFailingTestCycleIds();

    /**
     * @param testCycleId the test cycle id
     * @return the verdicts of the test cycle
     */
    List<Verdict> getVerdicts(String testCycleId);

    /**
     * Get the git shas of a test cycle. Intuitively there should be one and only
     * one git sha per test cycle. However, the RTPTorrent data set sometimes
     * contains zero, two or more shas for the same test cycle.
     *
     * @param testCycleId the test cycle id
     * @return the git shas of the test cycle
     */
    List<String> getShas(String testCycleId);

    /**
     * @param sha a git sha
     * @param regexp a filter for file names to return
     * @return the paths of the files modifies in the commit, after the regexp filter is applied
     */
    Set<String> getModifiedFiles(String sha, String regexp);

    /**
     * @return the path to the Git repository
     */
    File getRepository();
}
