package com.smartbear.tcpbench;

import me.tongfei.progressbar.ProgressBar;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.smartbear.tcpbench.Apfd.computeApfd;

public class Benchmark {
    private static final int MINIMUM_TEST_CYCLE_VERDICT_COUNT = 6;
    private final TcpEngine tcpEngine;
    private final History history;

    public Benchmark(TcpEngine tcpEngine, History history) {
        this.tcpEngine = tcpEngine;
        this.history = history;
    }

    public void run(String projectName, Stream<String> testCycleIds, int trainingCount, int maxPredictionCount, Consumer<Double> apfdConsumer) throws Exception {
        tcpEngine.createProject(projectName);

        AtomicInteger testCycleCounter = new AtomicInteger();
        AtomicInteger predictionCounter = new AtomicInteger();

        testCycleIds.forEach((testCycleId) -> {
            int testCycleIndex = testCycleCounter.incrementAndGet();

            List<Verdict> verdicts = history.getVerdicts(testCycleId);
            tcpEngine.train(testCycleId, verdicts, history);

            if(testCycleIndex >= trainingCount) {
                // We're done with the initial training
                if (verdicts.size() >= MINIMUM_TEST_CYCLE_VERDICT_COUNT) {
                    // There are enough verdicts in this test cycle that we might be interested in a prediction
                    int predictionIndex = predictionCounter.incrementAndGet();
                    if(predictionIndex < maxPredictionCount) {
                        List<String> ordering = tcpEngine.getOrdering(testCycleId);
                        if (ordering != null) {
                            List<String> orderingWithoutDuplicates = new ArrayList<>(new LinkedHashSet<>(ordering));
                            double apfd = computeApfd(verdicts, orderingWithoutDuplicates, testCycleId);
                            apfdConsumer.accept(apfd);
                        }
                    }
                }
            }
        });
    }
}
