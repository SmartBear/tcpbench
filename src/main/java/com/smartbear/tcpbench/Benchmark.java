package com.smartbear.tcpbench;

import me.tongfei.progressbar.ProgressBar;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.smartbear.tcpbench.Apfd.computeApfd;

public class Benchmark {
    private static final int INITIAL_TRAINING_COUNT = 6;
    private final TcpEngine tcpEngine;
    private final Query query;

    public Benchmark(TcpEngine tcpEngine, Query query) {
        this.tcpEngine = tcpEngine;
        this.query = query;
    }

    public void run(String projectName, int trainingCount, int predictionCount, Consumer<Double> apfdConsumer) throws Exception {
        tcpEngine.createProject(projectName);
        List<String> testCycleIds = query.getOrderedFailingTestCycleIds();
        int cycleCount = Math.min(trainingCount + predictionCount, testCycleIds.size());

        Stream<Integer> testCycleIndexes = ProgressBar.wrap(IntStream.range(0, cycleCount), "Test Cycle");
        testCycleIndexes.forEach((testCycleIndex) -> {
            String testCycleId = testCycleIds.get(testCycleIndex);
            List<Verdict> verdicts = query.getVerdicts(testCycleId);
            tcpEngine.train(testCycleId, verdicts, query);
            if (verdicts.size() >= INITIAL_TRAINING_COUNT && testCycleIndex >= trainingCount) {
                List<String> ordering = tcpEngine.getOrdering(testCycleId);
                if (ordering != null) {
                    List<String> orderingWithoutDuplicates = new ArrayList<>(new LinkedHashSet<>(ordering));
                    double apfd = computeApfd(verdicts, orderingWithoutDuplicates, testCycleId);
                    apfdConsumer.accept(apfd);
                }
            }
        });
    }
}
