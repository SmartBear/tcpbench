package com.smartbear.tcpbench.rtptorrent;

import com.smartbear.tcpbench.Changes;
import com.smartbear.tcpbench.Query;
import com.smartbear.tcpbench.Verdict;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static tech.tablesaw.api.ColumnType.*;

public class RtpTorrentQuery implements Query {

    private final Table testsTable;
    private final Table commitsTable;
    private final Table patchesTable;
    private final Git git;
    private final File repository;

    private RtpTorrentQuery(Table testsTable, Table commitsTable, Table patchesTable, Git git) {
        this.testsTable = testsTable;
        this.commitsTable = commitsTable;
        this.patchesTable = patchesTable;
        this.git = git;
        this.repository = git.getRepository().getDirectory();
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
        Git git = Git.wrap(new FileRepository(repository));
        return new RtpTorrentQuery(testsTable, commitsTable, patchesTable, git);
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
    public List<String> getShas(String testCycleId) {
        Table commits = commitsTable.where(commitsTable.stringColumn("tr_job_id").isEqualTo(testCycleId));
        // RTPTorrent sometimes links more than one commit to the same job (sounds like a bug in the dataset)
        // Sometimes there are no commits linked to a job
        return commits.stringColumn("git_commit_id").asList();
    }

    @Override
    public Set<String> getModifiedFiles(String sha, String regexp) {
        StringColumn shaColumn = patchesTable.stringColumn("sha");
        StringColumn nameColumn = patchesTable.stringColumn("name");
        Table patches = patchesTable.where(shaColumn.isEqualTo(sha).and(nameColumn.matchesRegex(regexp)));
        return patches.stringColumn("name").asSet();
    }

    @Override
    public Changes getChanges(String oldSha, String newSha, String regexp) {
        ObjectId oldCommitId = ObjectId.fromString(oldSha);
        ObjectId newCommitId = ObjectId.fromString(newSha);
        DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
        diffFormatter.setRepository(git.getRepository());
        diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);
        int linesAdded = 0;
        int linesDeleted = 0;
        int timeDiff = 0;
        int changedFiles = 0;
        try (RevWalk revWalk = new RevWalk(git.getRepository())) {
            RevCommit oldCommit = revWalk.parseCommit(git.getRepository().resolve(oldSha));
            RevCommit newCommit = revWalk.parseCommit(git.getRepository().resolve(newSha));
            timeDiff = newCommit.getCommitTime() - oldCommit.getCommitTime();
            List<DiffEntry> diffEntries;
            diffEntries = diffFormatter.scan(oldCommitId, newCommitId).stream().filter(diffEntry -> diffEntry.getNewPath().matches(regexp) || diffEntry.getOldPath().matches(regexp)).collect(toList());
            changedFiles = diffEntries.size();
            for (DiffEntry diffEntry : diffEntries) {
                for (Edit edit : diffFormatter.toFileHeader(diffEntry).toEditList()) {
                    if (edit.getType() != Edit.Type.REPLACE) {
                        linesDeleted += edit.getEndA() - edit.getBeginA();
                        linesAdded += edit.getEndB() - edit.getBeginB();
                    }
                }
            }
            return new Changes(changedFiles, linesAdded, linesDeleted, timeDiff);
        } catch (AmbiguousObjectException e) {
            // unable to find commit
            return new Changes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public File getRepository() {
        return repository;
    }
}
