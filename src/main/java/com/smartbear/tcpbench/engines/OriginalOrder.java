package com.smartbear.tcpbench.engines;

import com.smartbear.tcpbench.TestCycle;
import com.smartbear.tcpbench.Verdict;

import java.util.List;
import java.util.stream.Collectors;

public class OriginalOrder extends AbstractEngine {
    @Override
    public List<String> getOrdering(TestCycle testCycle) {
        return testCycle.getVerdicts().stream().map(Verdict::getTestId).collect(Collectors.toList());
    }
}
