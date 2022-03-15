package com.smartbear.tcpbench.rtptorrent;

import com.smartbear.tcpbench.Query;
import com.smartbear.tcpbench.Verdict;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.File;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static tech.tablesaw.api.ColumnType.*;

public class RtpTorrentQuery implements Query {
    private final Table testsTable;
    private final Table commitsTable;
    private final Table patchesTable;
    private final File repository;

    private RtpTorrentQuery(Table testsTable, Table commitsTable, Table patchesTable, File repository) {
        this.testsTable = testsTable;
        this.commitsTable = commitsTable;
        this.patchesTable = patchesTable;
        this.repository = repository;
    }

    public static Query create(File rtpTorrentProjectDir) throws Exception {
        String projectName = rtpTorrentProjectDir.getName();

        Table testsTable = Table.read().usingOptions(CsvReadOptions.builder(new File(rtpTorrentProjectDir, projectName + ".csv")).columnTypes(new ColumnType[]{
                STRING, STRING, INTEGER, DOUBLE, INTEGER, INTEGER, INTEGER, INTEGER
        }));
        Table patchesTable = Table.read().usingOptions(CsvReadOptions.builder(new File(rtpTorrentProjectDir, projectName + "-patches.csv")).columnTypes(new ColumnType[]{
                STRING, STRING
        }));
        Table commitsTable = Table.read().usingOptions(CsvReadOptions.builder(new File(rtpTorrentProjectDir.getParentFile(), "tr_all_built_commits.csv")).columnTypes(new ColumnType[]{
                STRING, STRING
        }));
        File repository = new File(new File(rtpTorrentProjectDir.getParentFile(), "repo"), projectName);

        return new RtpTorrentQuery(testsTable, commitsTable, patchesTable, repository);
    }

    @Override
    public List<String> getOrderedTestCycleIds() {
        return testsTable.stringColumn("travisJobId")
                .unique()
                .sorted(Comparator.comparingInt(Integer::parseInt))
                .asList();
    }

    @Override
    public List<Verdict> getVerdicts(String testCycleId) {
        return testsTable
                .where(testsTable.stringColumn("travisJobId").isEqualTo(testCycleId))
                .stream()
                .map(testCase -> new Verdict(
                        testCase.getString("testName"),
                        testCase.getInt("failures") > 0,
                        testCase.getInt("count"),
                        Duration.ofMillis((long) (testCase.getDouble("duration") * 1000)))
                ).collect(Collectors.toList());
    }

    @Override
    public List<String> getShas(String testCycleId) {
        Table commits = commitsTable.where(commitsTable.stringColumn("tr_job_id").isEqualTo(testCycleId));
        // RTPTorrent sometimes links more than one commit to the same job (sounds like a bug in the dataset)
        // Sometimes there are no commits linked to a job
        return commits.stringColumn("git_commit_id").asList();
    }

    @Override
    public Set<String> getModifiedFiles(String sha) {
        Table patches = patchesTable.where(patchesTable.stringColumn("sha").isEqualTo(sha).and(patchesTable.stringColumn("name").endsWith(".java")));
        return patches.stringColumn("name").asSet();
    }

    @Override
    public File getRepository() {
        return repository;
    }
}
