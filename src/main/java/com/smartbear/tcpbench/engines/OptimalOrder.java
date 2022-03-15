package com.smartbear.tcpbench.engines;

import com.smartbear.tcpbench.Verdict;

import java.util.List;
import java.util.stream.Collectors;

public class OptimalOrder extends AbstractEngine {
    @Override
    public List<String> getOrdering(String testCycleId) {
        return getVerdicts(testCycleId).stream()
                .sorted((v1, v2) -> Boolean.compare(v2.isFailure(), v1.isFailure()))
                .map(Verdict::getTestId)
                .collect(Collectors.toList());
    }
}
