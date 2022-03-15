package com.smartbear.tcpbench;

import java.util.List;
import java.util.Optional;

public class Apfd {
    public static double computeApfd(List<Verdict> verdicts, List<String> orderedTestCases, String testCycleId) {
        int N = orderedTestCases.size();
        long M = verdicts.stream().filter(Verdict::isFailure).count();

        if (N == 0) throw new RuntimeException("Cannot compute APFD for 0 test cases");
        if (M == 0) throw new RuntimeException("Cannot compute APFD for 0 failing test cases");

        int sum = sumRanks(verdicts, orderedTestCases);
        double apfd = 1 - ((double) sum / (N * M)) + (1d / (2 * N));
        if (apfd > 1) {
            System.err.printf("*** Unexpected APFD=%f outside [0:1] range. testCycleId=%s\n", apfd, testCycleId);
            return 1;
        }
        if (apfd < 0) {
            System.err.printf("*** Unexpected APFD=%f outside [0:1] range. testCycleId=%s\n", apfd, testCycleId);
            return 0;
        }
        return apfd;
    }

    private static int sumRanks(List<Verdict> verdicts, List<String> orderedTestCases) {
        int sumRanks = 0;
        int i = 1;
        for (String testCase : orderedTestCases) {
            Optional<Verdict> failure = verdicts.stream().filter(verdict -> verdict.getTestId().equals(testCase) && verdict.isFailure()).findFirst();
            if (failure.isPresent()) {
                sumRanks += i;
            }
            i++;
        }
        if (sumRanks == 0) {
            throw new RuntimeException("Did not expect sum to be 0");
        }
        return sumRanks;
    }
}
