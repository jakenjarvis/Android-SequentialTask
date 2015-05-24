package com.tojc.task.android.implement;

import android.content.Context;
import android.os.AsyncTask;

import com.tojc.task.android.definition.TaskInterface;
import com.tojc.task.android.definition.TaskStatus;

import java.util.concurrent.CountDownLatch;

/**
 * Created by Jaken on 2015/05/24.
 */
public class ParallelContainer extends TaskBase
{
    private AsyncTask<Void, Void, Void> waiter = null;
    private CountDownLatch latch = null;

    public ParallelContainer(Context context)
    {
        super(context, "ParallelContainer");
        this.waiter = null;
        this.latch = null;
    }

    @Override
    public void execute()
    {
        switch(this.status)
        {
            case Initialize:
                this.setStatus(TaskStatus.Waiting);
                this.processChildAllSetStatus(TaskStatus.Initialize, TaskStatus.Waiting);

                executeParallel();
                break;

            case Waiting:
                executeParallel();
                break;

            case Running:
                break;

            case Completed:
                break;
        }
    }

    private void executeParallel()
    {
        int latchcount = this.childs.size();

        if((this.latch == null) && (this.waiter == null))
        {
            if(latchcount >= 1)
            {
                this.setStatus(TaskStatus.Running);

                this.latch = new CountDownLatch(latchcount);
                this.waiter = new AsyncTask<Void, Void, Void>()
                {
                    @Override
                    protected void onPreExecute()
                    {
                        for(TaskInterface childtask : ParallelContainer.this.childs)
                        {
                            switch(childtask.getStatus())
                            {
                                case Initialize:
                                    childtask.execute();
                                    break;
                                case Waiting:
                                    childtask.execute();
                                    break;
                                case Running:
                                    break;

                                case Completed:
                                    executeCountDown();
                                    break;
                            }
                        }
                    }

                    @Override
                    protected Void doInBackground(Void... params)
                    {
                        try
                        {
                            ParallelContainer.this.latch.await();
                        }
                        catch(InterruptedException e)
                        {
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result)
                    {
                        ParallelContainer.this.setStatus(TaskStatus.Completed);
                        ParallelContainer.this.processTaskCompleted();
                    }
                }.execute();
            }
            else
            {
                this.setStatus(TaskStatus.Completed);
                this.processTaskCompleted();
            }
        }
    }

    @Override
    protected void onChildTaskCompleted(TaskInterface childtask)
    {
        switch(this.status)
        {
            case Initialize:
                break;
            case Waiting:
                break;
            case Running:
                if(this.childs.contains(childtask))
                {
                    executeCountDown();
                }
                break;
            case Completed:
                break;
        }
    }

    private void executeCountDown()
    {
        this.latch.countDown();
        //Logger.debug().print("task countDown : (uuid=%s, count=%d)", this.getUuid().toString(), this.latch.getCount());
    }

    @Override
    public void completed()
    {
        switch(this.status)
        {
            case Initialize:
                break;
            case Waiting:
                this.setStatus(TaskStatus.Completed);
                this.processTaskCompleted();
                break;
            case Running:
                this.setStatus(TaskStatus.Completed);
                this.processTaskCompleted();
                break;
            case Completed:
                break;
        }
        onDestroyBefore();
    }

    @Override
    protected void onDestroyBefore()
    {
        if(this.waiter != null)
        {
            this.waiter.cancel(false);

            long nowlatchcount = this.latch.getCount();
            for(int count = 0; count < nowlatchcount; count++)
            {
                executeCountDown();
            }
        }
        this.waiter = null;
        this.latch = null;
    }
}
