package com.tojc.task.android.implement;

import android.content.Context;

import com.tojc.task.android.definition.TaskInterface;
import com.tojc.task.android.definition.TaskStatus;

/**
 * Created by Jaken on 2015/05/24.
 */
public abstract class WorkerBase extends TaskBase
{
    public WorkerBase(Context context, String taskTypeName)
    {
        super(context, taskTypeName);
    }

    @Override
    public TaskInterface addChild(TaskInterface childtask)
    {
        throw new UnsupportedOperationException("Worker, the child can not be registered.");
    }

    protected void onExecutePreCheck()
    {
        if(this.worker == null)
        {
            throw new IllegalStateException("Worker task is not registered.");
        }
    }

    protected abstract void onExecute();

    @Override
    public void execute()
    {
        onExecutePreCheck();

        switch(this.status)
        {
            case Initialize:
                onExecute();
                break;

            case Waiting:
                onExecute();
                break;

            case Running:
                break;

            case Completed:
                break;
        }
    }

    @Override
    protected void onExecuteWorkerBefore()
    {
        this.setStatus(TaskStatus.Running);
    }

    @Override
    protected void onExecuteWorkerAfter()
    {
        this.setStatus(TaskStatus.Completed);
        this.processTaskCompleted();
    }

    protected abstract void onCompleted();

    @Override
    public void completed()
    {
        switch(this.status)
        {
            case Initialize:
                break;
            case Waiting:
                onCompleted();
                break;
            case Running:
                onCompleted();
                break;
            case Completed:
                break;
        }
        onDestroyWorkerObject();
    }

    protected abstract void onDestroyWorkerObject();

    @Override
    protected void onDestroyBefore()
    {
        onDestroyWorkerObject();
    }
}
