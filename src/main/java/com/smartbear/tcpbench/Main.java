package com.smartbear.tcpbench;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.smartbear.tcpbench.rtptorrent.RtpTorrentQuery;
import me.tongfei.progressbar.ProgressBar;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.smartbear.tcpbench.Apfd.computeApfd;

public class Main {
    private static final int MIN_TEST_CASE_COUNT_FOR_PRIORITIZATION = 6;

    @Parameter(names = {"-t", "--training"}, description = "Number of test cycles for initial training", required = true)
    int trainingCount;

    @Parameter(names = {"-p", "--prediction"}, description = "Number of test cycles for prediction", required = true)
    int predictionCount;

    @Parameter(names = {"-r", "--rtptorrent"}, description = "RTPTorrent project directory", required = true)
    File rtpTorrentProjectDir;

    @Parameter(names = {"-e", "--engine"}, description = "TCP Engine name", required = true)
    String engineClass;

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        JCommander.newBuilder()
                .addObject(main)
                .build()
                .parse(args);
        main.run();
    }

    public void run() throws Exception {
        String projectName = rtpTorrentProjectDir.getName();
        TcpEngine tcpEngine = (TcpEngine) Main.class.getClassLoader().loadClass(engineClass).getConstructor().newInstance();
        Query query = RtpTorrentQuery.create(rtpTorrentProjectDir);

        tcpEngine.prepare(projectName);

        List<String> testCycleIds = query.getOrderedFailingTestCycleIds();
        int cycleCount = Math.min(trainingCount + predictionCount, testCycleIds.size());

        Stream<Integer> testCycleIndexes = ProgressBar.wrap(IntStream.range(0, cycleCount), "Test Cycle");
        testCycleIndexes.forEach((testCycleIndex) -> {
            String testCycleId = testCycleIds.get(testCycleIndex);
            List<Verdict> verdicts = query.getVerdicts(testCycleId);
            tcpEngine.defineTestCycle(testCycleId, verdicts, query);

//            List<String> shas = query.getOrderedShas(testCycleId);
//            if(!shas.isEmpty()) {
//                query.getChanges(shas, ".*");
//            }

            if (verdicts.size() >= MIN_TEST_CASE_COUNT_FOR_PRIORITIZATION && testCycleIndex >= trainingCount) {
                List<String> ordering = tcpEngine.getOrdering(testCycleId);
                if (ordering != null) {
                    List<String> orderingWithoutDuplicates = new ArrayList<>(new LinkedHashSet<>(ordering));
                    double apfd = computeApfd(verdicts, orderingWithoutDuplicates, testCycleId);
                    System.out.println(apfd);
                }
            }

            tcpEngine.train(testCycleId, verdicts);
        });
    }
}
