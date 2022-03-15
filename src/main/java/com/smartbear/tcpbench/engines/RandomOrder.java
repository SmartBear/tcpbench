package com.smartbear.tcpbench.engines;

import com.smartbear.tcpbench.Verdict;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class RandomOrder extends DefaultEngine {
    private final Random random = new Random(98765);

    @Override
    public List<String> getOrdering(String testCycleId) {
        List<String> testNames = getVerdicts(testCycleId).stream().map(Verdict::getTestId).collect(Collectors.toList());
        Collections.shuffle(testNames, random);
        return testNames;
    }
}
