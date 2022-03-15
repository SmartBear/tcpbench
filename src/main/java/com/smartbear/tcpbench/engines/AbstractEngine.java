package com.smartbear.tcpbench.engines;

import com.smartbear.tcpbench.Query;
import com.smartbear.tcpbench.TcpEngine;
import com.smartbear.tcpbench.Verdict;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractEngine implements TcpEngine {
    private final Map<String, List<Verdict>> verdictsByTestCycleId = new HashMap<>();

    @Override
    public void prepare(String projectName) {
    }

    @Override
    public void defineTestCycle(String testCycleId, List<Verdict> verdicts, Query query) {
        verdictsByTestCycleId.put(testCycleId, verdicts);
    }

    @Override
    public void train(String testCycleId, List<Verdict> verdicts) {

    }

    protected List<Verdict> getVerdicts(String testCycleId) {
        return verdictsByTestCycleId.get(testCycleId);
    }
}
