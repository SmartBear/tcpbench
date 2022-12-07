package com.smartbear.tcpbench;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.smartbear.tcpbench.APTF.aptf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

class APTFTest {
    @Test
    void example1() {
        List<Verdict> verdicts = asList(
                new Verdict("t1", true),
                new Verdict("t2", true),
                new Verdict("t3", true)
        );
        double apfd = aptf(verdicts);
        assertEquals(0.5, apfd);
    }

    @Test
    void example2() {
        List<Verdict> verdicts = asList(
                new Verdict("t4", true),
                new Verdict("t1", true),
                new Verdict("t3", false),
                new Verdict("t2", false)
        );
        double aptf = aptf(verdicts);
        assertEquals(0.75, aptf);
    }

    @Test
    void example2_unorderd() {
        List<Verdict> verdicts = asList(
                new Verdict("t4", true),
                new Verdict("t3", false),
                new Verdict("t2", false),
                new Verdict("t1", true)
        );
        List<String> ordering = asList("t4", "t1", "t3", "t2");
        double aptf = aptf(ordering, verdicts);
        assertEquals(0.75, aptf);
    }
}
