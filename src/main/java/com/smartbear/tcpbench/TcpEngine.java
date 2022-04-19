package com.smartbear.tcpbench;

import java.util.List;

public interface TcpEngine {
    void prepare(String projectName) throws Exception;

    /**
     * Define a test cycle
     *
     * @param testCycleId the test cycle ID
     * @param verdicts    the actual test results
     * @param query       an object to extract additional information, if necessary
     */
    void defineTestCycle(String testCycleId, List<Verdict> verdicts, Query query);

    /**
     * Perform a tcp opertation
     *
     * @param testCycleId the test cycle ID
     * @return the ordered test IDs for the test cycle, or null if no ordering could be determined
     */
    List<String> getOrdering(String testCycleId);

    /**
     * Train the engine
     *
     * @param testCycleId the test cycle ID
     * @param verdicts    the actual test results
     */
    void train(String testCycleId, List<Verdict> verdicts);
}
