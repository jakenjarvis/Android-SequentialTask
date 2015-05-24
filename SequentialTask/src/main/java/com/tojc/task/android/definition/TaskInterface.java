package com.tojc.task.android.definition;

import android.os.Bundle;

import java.util.List;
import java.util.UUID;

/**
 * Created by Jaken on 2015/05/24.
 */
public interface TaskInterface
        extends
        TaskContainer,
        TaskWorker,
        TaskCompletedListener {
    public TaskCompletedListener getParentListener();

    public void setParentListener(TaskCompletedListener parent);

    public List<TaskInterface> getChilds();

    public TaskInterface addChild(TaskInterface childtask);

    public boolean isChildsCompleted();

    public TaskInterface findTaskFromUuid(UUID uuid);

    public TaskInterface findTaskFromParameters(String key, Object value);

    public Runnable getWorker();

    public void setWorker(Runnable worker);

    public UUID getUuid();

    public String getTaskTypeName();

    public Bundle getParameters();

    public void setParameters(Bundle parameters);

    public TaskStatus getStatus();

    public void setStatus(TaskStatus status);

    public void execute();

    public void completed();

    public int count();

    public void onDestroy();
}
