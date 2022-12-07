package com.smartbear.tcpbench.engines;

import com.smartbear.tcpbench.History;
import com.smartbear.tcpbench.TcpEngine;
import com.smartbear.tcpbench.TestCycle;
import com.smartbear.tcpbench.Verdict;

import java.util.List;

public abstract class AbstractEngine implements TcpEngine {
    @Override
    public void createProject(String projectName) throws Exception {

    }

    @Override
    public void train(TestCycle testCycle, List<Verdict> verdicts, History history) {

    }
}
