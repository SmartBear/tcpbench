package com.smartbear.tcpbench;

import com.smartbear.tcpbench.rtptorrent.RtpTorrentQuery;
import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static com.smartbear.tcpbench.Apfd.computeApfd;

public class Main {
    private static final int FIRST_PREDICTION_NUMBER = 10;
    private static final int MAX_CYCLE_COUNT = 40;
    private static final int MIN_TEST_CASE_COUNT_FOR_PRIORITIZATION = 6;

    public static void main(String[] args) throws Exception {
        Option rtpTorrentProjectOpt = new Option("r", "rtptorrent", true, "RTPTorrent project directory");
        rtpTorrentProjectOpt.setRequired(true);
        Option engineOpt = new Option("e", "engine", true, "TCP Engine name");
        engineOpt.setRequired(true);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(new Options()
                        .addOption(rtpTorrentProjectOpt)
                        .addOption(engineOpt),
                args);
        File rtpTorrentProjectDir = new File(cmd.getOptionValue(rtpTorrentProjectOpt));
        String projectName = rtpTorrentProjectDir.getName();
        String engineClass = cmd.getOptionValue(engineOpt);
        TcpEngine tcpEngine = (TcpEngine) Main.class.getClassLoader().loadClass(engineClass).getConstructor().newInstance();

        Query query = RtpTorrentQuery.create(rtpTorrentProjectDir);

        tcpEngine.prepare(projectName);

        int testCycleNumber = 1;
        List<String> testCycleIds = query.getOrderedTestCycleIds();

        try (ProgressBar pb = new ProgressBar("Test Cycle", MAX_CYCLE_COUNT)) {
            for (String testCycleId : testCycleIds) {
                List<Verdict> verdicts = query.getVerdicts(testCycleId);
                Optional<Verdict> firstFailure = verdicts.stream().filter(Verdict::isFailure).findFirst();
                if (!firstFailure.isPresent()) {
                    // Ignore verdicts without failures
                    continue;
                }

                tcpEngine.defineTestCycle(testCycleId, verdicts, query);

                if (verdicts.size() >= MIN_TEST_CASE_COUNT_FOR_PRIORITIZATION && testCycleNumber >= FIRST_PREDICTION_NUMBER) {
                    List<String> ordering = tcpEngine.getOrdering(testCycleId);
                    if (ordering != null) {
                        List<String> orderingWithoutDuplicates = new ArrayList<>(new LinkedHashSet<>(ordering));
                        double apfd = computeApfd(verdicts, orderingWithoutDuplicates, testCycleId);
                        System.out.println(apfd);
                    }
                }

                tcpEngine.train(testCycleId, verdicts);
                testCycleNumber++;
                pb.step();
                if (testCycleNumber > MAX_CYCLE_COUNT) {
                    break;
                }
            }
        }
    }
}
