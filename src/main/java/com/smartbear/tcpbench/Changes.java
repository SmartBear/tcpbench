package com.smartbear.tcpbench;

public class Changes {
    private final int changedFiles;
    private final int linesAdded;
    private final int linesDeleted;
    private final int timeDiff;

    public Changes() {
        this.changedFiles = 0;
        this.linesAdded = 0;
        this.linesDeleted = 0;
        this.timeDiff = 0;
    }

    public Changes(int changedFiles, int linesAdded, int linesDeleted, int timeDiff) {
        this.changedFiles = changedFiles;
        this.linesAdded = linesAdded;
        this.linesDeleted = linesDeleted;
        this.timeDiff = timeDiff;
    }

    public int getChangedFiles() {
        return changedFiles;
    }

    public int getLinesAdded() {
        return linesAdded;
    }

    public int getLinesDeleted() {
        return linesDeleted;
    }

    public int getTimeDiff() {
        return timeDiff;
    }
}