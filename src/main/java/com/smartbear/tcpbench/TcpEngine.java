package com.smartbear.tcpbench;

import java.util.List;

public interface TcpEngine {
    /**
     * Create a new project. A typical implementation would perform the following operations:
     *
     * <ul>
     * <li>Delete the old project if it exists</li>
     * <li>(Re)create the project</li>
     * </ul>
     *
     * @param projectName the name of the project
     * @throws Exception if something went wrong
     */
    void createProject(String projectName) throws Exception;

    /**
     * Train the engine. A typical implementation would
     *
     * @param testCycleId the test cycle ID
     * @param verdicts    the actual test results
     * @param query       an object that provides access to additional information if the TCP algorithm needs it
     */
    void train(String testCycleId, List<Verdict> verdicts, Query query);

    /**
     * Requests a TCP ordering
     *
     * @param testCycleId the test cycle ID
     * @return the ordered test IDs for the test cycle, or null if no ordering could be determined
     */
    List<String> getOrdering(String testCycleId);

}
