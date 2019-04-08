package com.dbs;

import java.util.Objects;

public class TaskLogKey {
    public String fileId;
    public int chunkNo;
    public TaskType msgType;

    public TaskLogKey(String fileId, int chunkNo, TaskType msgType) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.msgType = msgType;
    }

    @Override
    public String toString() {
        return "TaskLogKey{" +
                "fileId='" + fileId + '\'' +
                ", chunkNo=" + chunkNo +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskLogKey that = (TaskLogKey) o;
        return chunkNo == that.chunkNo &&
                Objects.equals(fileId, that.fileId) &&
                msgType == that.msgType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileId, chunkNo, msgType);
    }


}
