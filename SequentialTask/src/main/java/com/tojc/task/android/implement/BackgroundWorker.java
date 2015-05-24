package com.tojc.task.android.implement;

import android.content.Context;
import android.os.AsyncTask;

import com.tojc.task.android.definition.TaskStatus;

/**
 * Created by Jaken on 2015/05/24.
 */
public class BackgroundWorker extends WorkerBase
{
    private AsyncTask<Void, Void, Void> background = null;

    public BackgroundWorker(Context context)
    {
        super(context, "BackgroundWorker");
    }

    @Override
    protected void onExecuteWorkerBefore()
    {
        // not super
    }

    @Override
    protected void onExecuteWorkerAfter()
    {
        // not super
    }

    @Override
    protected void onExecute()
    {
        this.background = new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected void onPreExecute()
            {
                BackgroundWorker.this.setStatus(TaskStatus.Running);
            }

            @Override
            protected Void doInBackground(Void... params)
            {
                BackgroundWorker.this.run();
                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                BackgroundWorker.this.setStatus(TaskStatus.Completed);
                BackgroundWorker.this.processTaskCompleted();
            }
        }.execute();
    }

    @Override
    protected void onCompleted()
    {
        if(this.background != null)
        {
            this.background.cancel(true);
            this.background = null;

            this.setStatus(TaskStatus.Completed);
            this.processTaskCompleted();
        }
    }

    @Override
    protected void onDestroyWorkerObject()
    {
        if(this.background != null)
        {
            this.background.cancel(true);
            this.background = null;
        }
    }
}
