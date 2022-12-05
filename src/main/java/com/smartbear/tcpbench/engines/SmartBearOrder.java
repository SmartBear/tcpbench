package com.smartbear.tcpbench.engines;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.smartbear.tcpbench.Query;
import com.smartbear.tcpbench.Verdict;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.smartbear.tcpbench.Env.getEnv;
import static java.util.Arrays.asList;

/**
 * This uses SmartBear's closed source TCP implementation
 */
public class SmartBearOrder extends AbstractEngine {
    public static final int TIMEOUT = 600;
    private final String tcpDir = new File(getEnv("SMARTBEAR_TCP_DIR")).getPath();
    private final String tcpMetaChangeset = new File(tcpDir, "dist/tcp-meta-changeset_darwin_amd64/tcp-meta-changeset").getPath();
    private final String tcpChangeset = new File(tcpDir, "dist/tcp-changeset_darwin_amd64/tcp-changeset").getPath();
    private final String tcp = new File(tcpDir, "dist/tcp_darwin_amd64/tcp").getPath();
    private final String tcpTrain = new File(tcpDir, "dist/tcp-train_darwin_amd64/tcp-train").getPath();

    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final Map<String, File> changesetFileByTestCycleId = new HashMap<>();
    private final Map<String, String> shaByTestCycleId = new HashMap<>();

    private File dataDir;
    private File dbFile;
    private File modelFile;

    @Override
    public void createProject(String projectName) {
        this.dataDir = new File(String.format("target/smartbear-tcp/%s", projectName));
        this.dbFile = new File(dataDir, "db.json");
        this.modelFile = new File(dataDir, "model.json");
    }

    @Override
    public List<String> getOrdering(String testCycleId) {
        List<String> testNames = getVerdicts(testCycleId).stream().map(Verdict::getTestId).collect(Collectors.toList());
        File changesetFile = changesetFileByTestCycleId.get(testCycleId);
        try {
            return tcp(changesetFile, testNames);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void train(String testCycleId, List<Verdict> verdicts, Query query) {
        super.train(testCycleId, verdicts, query);
        List<String> shas = query.getOrderedShas(testCycleId);
        if (shas.isEmpty()) {
            System.err.printf("No git shas for testCycleId %s. TCP will only use priors\n", testCycleId);
            return;
        }

        String oldSha = shas.get(0);
        String newSha = shas.get(shas.size() - 1);
        try {
            File changesetFile = buildChangeset(oldSha, newSha, query.getRepository());
            changesetFileByTestCycleId.put(testCycleId, changesetFile);
            shaByTestCycleId.put(testCycleId, newSha);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            File testReportFile = writeTestReport(testCycleId, verdicts);
            List<String> args = new ArrayList<>(asList(
                    tcpTrain,
                    "--model", modelFile.getPath(),
                    "--test-report", testReportFile.getPath()));
            File changesetFile = changesetFileByTestCycleId.get(testCycleId);
            if (changesetFile != null) {
                args.add("--changeset");
                args.add(changesetFile.getPath());
            }

            ProcessBuilder builder = new ProcessBuilder(args).redirectError(ProcessBuilder.Redirect.INHERIT);
            String cmd = String.join(" ", builder.command());
            Process process = builder.start();
            waitFor(process, cmd);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void waitFor(Process process, String cmd) throws InterruptedException {
        if (process.waitFor(TIMEOUT, TimeUnit.SECONDS)) {
            if (process.exitValue() != 0) {
                throw new RuntimeException(String.format("%s exited with %d", process, process.exitValue()));
            }
        } else {
            throw new RuntimeException(String.format("Timeout: %s", cmd));
        }
    }

    private List<String> tcp(File changesetFile, List<String> testNames) throws IOException, InterruptedException {
        List<String> args = new ArrayList<>(asList(
                tcp,
                "--model", modelFile.getPath()));
        if (changesetFile != null) {
            args.add("--changeset");
            args.add(changesetFile.getPath());
        }
        ProcessBuilder builder = new ProcessBuilder(args).redirectError(ProcessBuilder.Redirect.INHERIT);
        Process process = builder.start();
        Writer stdin = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8);
        for (String testName : testNames) {
            stdin.write(testName + "\n");
        }
        stdin.flush();
        stdin.close();
        String cmd = String.join(" ", builder.command());
        waitFor(process, cmd);
        BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        return stdout.lines().collect(Collectors.toList());
    }

    private File buildChangeset(String oldSha, String newSha, File repository) throws IOException, InterruptedException {
        File changeset = new File(dataDir, String.format("%s/changeset.json", newSha));
        changeset.getParentFile().mkdirs();
        List<ProcessBuilder> builders = asList(
                new ProcessBuilder(
                        tcpMetaChangeset,
                        "--repository", repository.getAbsolutePath(),
                        "--old-sha", oldSha,
                        "--sha", newSha
                ).redirectError(ProcessBuilder.Redirect.INHERIT),
                new ProcessBuilder(
                        tcpChangeset,
                        "--db", dbFile.getPath()
                ).redirectError(ProcessBuilder.Redirect.INHERIT).redirectOutput(changeset));
        List<Process> processes = ProcessBuilder.startPipeline(builders);

        for (int i = 0; i < processes.size(); i++) {
            Process process = processes.get(i);
            String cmd = String.join(" ", builders.get(i).command());
            waitFor(process, cmd);
        }
        return changeset;
    }

    private File writeTestReport(String testCycleId, List<Verdict> verdicts) throws IOException {
        List<NaiveBayesTestVerdict> testVerdicts = verdicts.stream()
                .map(verdict -> new NaiveBayesTestVerdict(
                        verdict.getTestId(),
                        verdict.isFailure() ? "failed" : "passed"))
                .collect(Collectors.toList());
        TestReport testReport = new TestReport(testVerdicts);

        String sha = shaByTestCycleId.get(testCycleId);
        File testReportFile = new File(dataDir, String.format("%s/testReport.json", sha));
        testReportFile.getParentFile().mkdirs();
        mapper.writeValue(testReportFile, testReport);
        return testReportFile;
    }

    public static class TestReport {
        public List<NaiveBayesTestVerdict> testVerdicts;

        public TestReport(List<NaiveBayesTestVerdict> naiveBayesTestVerdicts) {
            this.testVerdicts = naiveBayesTestVerdicts;
        }
    }

    public static class NaiveBayesTestVerdict {
        public String name;
        public String status;

        public NaiveBayesTestVerdict(String name, String status) {
            this.name = name;
            this.status = status;
        }
    }
}
