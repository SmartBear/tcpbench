package com.smartbear.tcpbench.engines;

import com.smartbear.tcpbench.History;
import com.smartbear.tcpbench.TestCycle;
import com.smartbear.tcpbench.Verdict;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FailureFrequencyOrder extends AbstractEngine {
    private final Map<String, Integer> failureCountByTestId = new HashMap<>();
    private final Map<String, Integer> executionCountByTestId = new HashMap<>();

    @Override
    public List<String> getOrdering(TestCycle testCycle) {
        return testCycle.getVerdicts().stream()
                .sorted((v1, v2) -> {
                    double t1Freq = freq(v1.getTestId());
                    double t2Freq = freq(v2.getTestId());
                    return Double.compare(t2Freq, t1Freq);
                })
                .map(Verdict::getTestId)
                .collect(Collectors.toList());
    }

    @Override
    public void train(TestCycle testCycle, List<Verdict> verdicts, History history) {
        for (Verdict verdict : verdicts) {
            if (verdict.isFailure()) {
                failureCountByTestId.put(verdict.getTestId(), failureCountByTestId.computeIfAbsent(verdict.getTestId(), n -> 0) + 1);
            }
            executionCountByTestId.put(verdict.getTestId(), executionCountByTestId.computeIfAbsent(verdict.getTestId(), n -> 0) + 1);
        }
    }

    private double freq(String testId) {
        if (!executionCountByTestId.containsKey(testId)) {
            // We have no verdicts - assume a higher score than 1.0
            return 2.0;
        }
        double failures = failureCountByTestId.computeIfAbsent(testId, n -> 0);
        double executions = executionCountByTestId.get(testId);
        return failures / executions;
    }
}
