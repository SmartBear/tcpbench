package com.smartbear.tcpbench;

import java.io.File;
import java.util.List;

/**
 * An abstraction of historical test results (verdicts) and changesets
 */
public interface TestCycleHistory {
    /**
     * @return all failing test cycles, chronologically ordered
     */
    List<String> getOrderedFailingTestCycleIds();
}
