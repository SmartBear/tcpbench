package com.smartbear.tcpbench.rtptorrent;

import com.smartbear.tcpbench.Query;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.File;

import static tech.tablesaw.api.ColumnType.DOUBLE;
import static tech.tablesaw.api.ColumnType.INTEGER;
import static tech.tablesaw.api.ColumnType.STRING;

public class RtpTorrent {
    public static Query makeQuery(File rtpTorrentProjectDir) throws Exception {
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
}
