package com.smartbear.tcpbench;

import java.io.File;
import java.util.List;

/**
 * An abstraction of historical test results (verdicts) and changesets
 */
public interface History {
    /**
     * @return all failing test cycles, chronologically ordered
     */
    List<TestCycle> getOrderedFailingTestCycles();

    /**
     * @return the path to the Git repository
     */
    File getRepository();
}
