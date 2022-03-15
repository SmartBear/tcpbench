package com.smartbear.tcpbench;

import java.io.File;
import java.util.List;
import java.util.Set;

public interface Query {
    List<String> getOrderedTestCycleIds();

    List<Verdict> getVerdicts(String testCycleId);

    List<String> getShas(String testCycleId);

    Set<String> getModifiedFiles(String sha);

    File getRepository();
}
