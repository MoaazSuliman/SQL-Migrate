package com.moaaz.migration.model;

import java.time.Instant;

public class Migration {

    private String fileName;
    private String filePath;// contains name.
    private String script;
    private String checkSum;

    private Instant executedAt;

    private boolean executed;
    private boolean success;

    public Migration() {

    }

    public Migration(String fileName, String filePath, String script, String checkSum, Instant executedAt, boolean executed, boolean success) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.script = script;
        this.checkSum = checkSum;
        this.executedAt = executedAt;
        this.executed = executed;
        this.success = success;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(String checkSum) {
        this.checkSum = checkSum;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(Instant executedAt) {
        this.executedAt = executedAt;
    }

    public boolean isExecuted() {
        return executed;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }


}
