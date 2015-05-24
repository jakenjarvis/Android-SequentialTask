package com.tojc.task.android.implement;

import android.content.Context;
import android.os.Handler;

import com.tojc.task.android.definition.TaskStatus;

/**
 * Created by Jaken on 2015/05/24.
 */
public class PostDelayedWorker extends WorkerBase
{
    private Handler handler = null;
    private long delayMillis;

    public PostDelayedWorker(Context context)
    {
        super(context, "PostDelayedWorker");
        this.handler = new Handler(this.context.getMainLooper());
    }

    public PostDelayedWorker(Context context, long delayMillis)
    {
        this(context);
        this.setDelayMillis(delayMillis);
    }

    public long getDelayMillis()
    {
        return this.delayMillis;
    }

    public void setDelayMillis(long delayMillis)
    {
        this.delayMillis = delayMillis;
    }

    @Override
    protected void onExecute()
    {
        this.handler.postDelayed(this, this.delayMillis);
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
