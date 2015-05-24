package com.tojc.task.android.implement;

import android.content.Context;
import android.os.Handler;

import com.tojc.task.android.definition.TaskStatus;

/**
 * Created by Jaken on 2015/05/24.
 */
public class PostWorker extends WorkerBase
{
    private Handler handler = null;

    public PostWorker(Context context)
    {
        super(context, "PostWorker");
        this.handler = new Handler(this.context.getMainLooper());
    }

    @Override
    protected void onExecute()
    {
        this.handler.post(this);
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
        if(this.handler != null)
        {
            this.handler.removeCallbacks(this);
            this.handler = null;
        }
    }
}
