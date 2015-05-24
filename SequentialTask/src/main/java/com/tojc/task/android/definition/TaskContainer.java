package com.tojc.task.android.definition;

import java.util.UUID;

/**
 * Created by Jaken on 2015/05/24.
 */
public interface TaskContainer {
    public UUID getUuid();

    public String getTaskTypeName();

    public TaskStatus getStatus();

    public TaskContainer addChild(TaskInterface childtask);

    public TaskContainer addChild(TaskWorker childtask);

    public TaskContainer addChild(TaskContainer childtask);

    public boolean isChildsCompleted();

    public void execute();

    public void completed();

    public int count();
}
