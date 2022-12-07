package com.smartbear.tcpbench;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class APTF {
    public static double aptf(List<String> ordering, List<Verdict> unorderedVerdicts) {
        List<String> orderingWithoutDuplicates = new ArrayList<>(new LinkedHashSet<>(ordering));
        List<Verdict> orderedVerdicts = new ArrayList<>(unorderedVerdicts);
        orderedVerdicts.sort((v1, v2) -> {
            int i1 = orderingWithoutDuplicates.indexOf(v1.getTestId());
            if (i1 == -1) throw new Error("Verdict not found: " + v1.getTestId());
            int i2 = orderingWithoutDuplicates.indexOf(v2.getTestId());
            if (i2 == -1) throw new Error("Verdict not found: " + v2.getTestId());
            return i1 - i2;
        });
        return aptf(orderedVerdicts);
    }

    public static double aptf(List<Verdict> orderedVerdicts) {
        int n = orderedVerdicts.size();
        long m = orderedVerdicts.stream().filter(Verdict::isFailure).count();
        double sum = 0d;
        for (int i = 0; i < orderedVerdicts.size(); i++) {
            if (orderedVerdicts.get(i).isFailure()) {
                sum += i + 1;
            }
        }
        double aptf = 1 - (sum / (n * m)) + (1d / (2 * n));
        if (aptf > 1.0 || aptf < 0.0) {
            throw new RuntimeException("Unexpected APTF: " + aptf);
        }
        return aptf;
    }
}
