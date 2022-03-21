package com.smartbear.tcpbench.rtptorrent;

import com.smartbear.tcpbench.Changes;
import com.smartbear.tcpbench.Query;
import com.smartbear.tcpbench.Verdict;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static tech.tablesaw.api.ColumnType.DOUBLE;
import static tech.tablesaw.api.ColumnType.INTEGER;
import static tech.tablesaw.api.ColumnType.STRING;

public class RtpTorrentQuery implements Query {

    private final Table testsTable;
    private final Table commitsTable;
    private final Repository repository;

    private RtpTorrentQuery(Table testsTable, Table commitsTable, Repository repository) {
        this.testsTable = testsTable;
        this.commitsTable = commitsTable;
        this.repository = repository;
    }

    public static Query create(File rtpTorrentProjectDir) throws Exception {
        String projectName = rtpTorrentProjectDir.getName();

        Table testsTable = Table.read().usingOptions(CsvReadOptions.builder(new File(rtpTorrentProjectDir, projectName + ".csv")).columnTypes(new ColumnType[]{
                STRING, STRING, INTEGER, DOUBLE, INTEGER, INTEGER, INTEGER, INTEGER
        }));
        Table commitsTable = Table.read().usingOptions(CsvReadOptions.builder(new File(rtpTorrentProjectDir.getParentFile(), "tr_all_built_commits.csv")).columnTypes(new ColumnType[]{
                STRING, STRING
        }));
        File repository = new File(new File(rtpTorrentProjectDir.getParentFile(), "repo"), projectName);
        Git git = Git.wrap(new FileRepository(repository));
        return new RtpTorrentQuery(testsTable, commitsTable, git.getRepository());
    }

    @Override
    public List<String> getOrderedFailingTestCycleIds() {
        return testsTable.stringColumn("travisJobId")
                .where(testsTable.intColumn("failures").isGreaterThan(0))
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
                ).collect(toList());
    }

    @Override
    public List<String> getOrderedShas(String testCycleId) {
        Table commits = commitsTable.where(commitsTable.stringColumn("tr_job_id").isEqualTo(testCycleId));
        // RTPTorrent sometimes links more than one commit to the same job. This happens if multiple commits went into
        // a "push" that triggered the build. We need to order those shas so we know which one is the oldest and the newest.
        // The returned list also contains the parent of the oldest SHA from the dataset, which can be used to create a diff.
        Set<String> shas = commits.stringColumn("git_commit_id").asSet();

        try (RevWalk revWalk = new RevWalk(repository)) {
            Set<RevCommit> revCommits = shas.stream().map(sha -> {
                try {
                    return revWalk.parseCommit(repository.resolve(sha));
                } catch (IOException e) {
                    throw new RuntimeException();
                }
            }).collect(Collectors.toSet());
            revWalk.markStart(revCommits);
            revWalk.sort(RevSort.TOPO_KEEP_BRANCH_TOGETHER);
            List<RevCommit> orderedCommits = new ArrayList<>();
            for (RevCommit revCommit : revWalk) {
                orderedCommits.add(revCommit);
                if (!revCommits.contains(revCommit)) {
                    break;
                }
            }
            return orderedCommits.stream().map(AnyObjectId::getName).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Changes getChanges(List<String> orderedShas, String regexp) {
        DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
        diffFormatter.setRepository(repository);
        diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);

        RevCommit oldCommit = parseCommit(orderedShas.get(0));
        RevCommit newCommit = parseCommit(orderedShas.get(orderedShas.size() - 1));

        int timeDiff = newCommit.getCommitTime() - oldCommit.getCommitTime();
        try {
            List<DiffEntry> diffEntries = diffFormatter.scan(oldCommit, newCommit).stream()
                    .filter(diffEntry -> diffEntry.getNewPath().matches(regexp) || diffEntry.getOldPath().matches(regexp)).collect(toList());
            int changedFiles = diffEntries.size();
            int linesDeleted = 0;
            int linesAdded = 0;
            for (DiffEntry diffEntry : diffEntries) {
                for (Edit edit : diffFormatter.toFileHeader(diffEntry).toEditList()) {
                    linesDeleted += edit.getEndA() - edit.getBeginA();
                    linesAdded += edit.getEndB() - edit.getBeginB();
                }
            }
            return new Changes(changedFiles, linesAdded, linesDeleted, timeDiff);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private RevCommit parseCommit(String sha) {
        try {
            return repository.parseCommit(repository.resolve(sha));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public File getRepository() {
        return repository.getDirectory();
    }
}
