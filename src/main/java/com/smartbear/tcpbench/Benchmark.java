package com.smartbear.tcpbench;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.smartbear.tcpbench.Apfd.computeApfd;

public class Benchmark {
    private final TcpEngine tcpEngine;
    private final History history;

    public Benchmark(TcpEngine tcpEngine, History history) {
        this.tcpEngine = tcpEngine;
        this.history = history;
    }

    public void run(String projectName, Stream<TestCycle> trainingCycles, Stream<TestCycle> predictionCycles, Consumer<Double> apfdConsumer) throws Exception {
        tcpEngine.createProject(projectName);

        trainingCycles.forEach((testCycle) -> {
            List<Verdict> verdicts = testCycle.getVerdicts();
            tcpEngine.train(testCycle, verdicts, history);
        });

        predictionCycles.forEach((testCycle) -> {
            List<String> ordering = tcpEngine.getOrdering(testCycle);
            List<Verdict> verdicts = testCycle.getVerdicts();
            if (ordering != null) {
                List<String> orderingWithoutDuplicates = new ArrayList<>(new LinkedHashSet<>(ordering));
                double apfd = computeApfd(verdicts, orderingWithoutDuplicates, testCycle.getId());
                apfdConsumer.accept(apfd);
            }
            tcpEngine.train(testCycle, verdicts, history);
        });
    }
}
