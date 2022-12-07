package com.smartbear.tcpbench;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.smartbear.tcpbench.rtptorrent.RtpTorrentHistory;
import me.tongfei.progressbar.ProgressBar;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

public class Main {
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

        List<String> testCycleIds = history.getOrderedFailingTestCycleIds();
        Stream<String> progressBarTestCycleIds = ProgressBar.wrap(testCycleIds.stream(), "Test Cycle");

        benchmark.run(projectName, progressBarTestCycleIds, trainingCount, predictionCount, (Double apfd) -> {
            System.out.println(apfd);
        });

    }
}
