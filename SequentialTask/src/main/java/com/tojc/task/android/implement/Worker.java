package com.tojc.task.android.implement;

import android.content.Context;

import com.tojc.task.android.definition.TaskStatus;

/**
 * Created by Jaken on 2015/05/24.
 */
public class Worker extends WorkerBase
{
    public Worker(Context context)
    {
        super(context, "Worker");
    }

    @Override
    protected void onExecute()
    {
        this.run();
    }

    @Override
    protected void onCompleted()
    {
        this.setStatus(TaskStatus.Completed);
        this.processTaskCompleted();
    }

    @Override
    protected void onDestroyWorkerObject()
    {
    }
}
