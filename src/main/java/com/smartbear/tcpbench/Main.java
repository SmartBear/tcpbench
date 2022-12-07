package com.smartbear.tcpbench;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.smartbear.tcpbench.rtptorrent.RtpTorrentHistory;
import me.tongfei.progressbar.ProgressBar;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    private static final int MINIMUM_TEST_CYCLE_VERDICT_COUNT = 6;

    @Parameter(names = {"-t", "--training"}, description = "Number of test cycles for initial training", required = true)
    int trainingCount;

    @Parameter(names = {"-p", "--prediction"}, description = "Number of test cycles for prediction", required = true)
    int predictionCount;

    @Parameter(names = {"-e", "--engine"}, description = "TCP Engine name", required = true)
    String engineClass;

    @Parameter(names = {"-r", "--rtptorrent"}, description = "RTPTorrent project directory", required = true)
    File rtpTorrentProjectDir;

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
        History history = RtpTorrentHistory.create(rtpTorrentProjectDir);
        Benchmark benchmark = new Benchmark(tcpEngine, history);

        List<TestCycle> testCycles = history.getOrderedFailingTestCycles();
        List<TestCycle> testCyclesWithEnoughVerdicts = testCycles.stream().filter(tc -> tc.getVerdicts().size() >= MINIMUM_TEST_CYCLE_VERDICT_COUNT).collect(Collectors.toList());

        if (trainingCount + predictionCount > testCyclesWithEnoughVerdicts.size()) {
            double trainingRatio = (double) trainingCount / (double) (trainingCount + predictionCount);
            trainingCount = (int) (trainingRatio * testCyclesWithEnoughVerdicts.size());
            predictionCount = testCyclesWithEnoughVerdicts.size() - trainingCount;
        }

        List<TestCycle> trainingTestCycles = testCyclesWithEnoughVerdicts.subList(0, trainingCount);
        List<TestCycle> predictionTestCycles = testCyclesWithEnoughVerdicts.subList(trainingCount, trainingCount + predictionCount);

        benchmark.run(
                projectName,
                ProgressBar.wrap(trainingTestCycles.stream(), "Training  "),
                ProgressBar.wrap(predictionTestCycles.stream(), "Prediction"),
                (Double apfd) -> {
                    System.out.println(apfd);
                }
        );

    }
}
