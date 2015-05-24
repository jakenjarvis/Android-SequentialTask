package com.tojc.task.android.definition;

import java.util.UUID;

/**
 * Created by Jaken on 2015/05/24.
 */
public interface TaskWorker {
    public UUID getUuid();

    public String getTaskTypeName();

    public TaskStatus getStatus();

    public void execute();

    public void completed();
}
