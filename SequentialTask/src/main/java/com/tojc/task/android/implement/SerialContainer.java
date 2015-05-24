package com.tojc.task.android.implement;

import android.content.Context;

import com.tojc.task.android.definition.TaskInterface;
import com.tojc.task.android.definition.TaskStatus;

import java.util.Iterator;

/**
 * Created by Jaken on 2015/05/24.
 */
public class SerialContainer extends TaskBase
{
    private Iterator<TaskInterface> iteratorTask = null;

    public SerialContainer(Context context)
    {
        super(context, "SerialContainer");
    }

    @Override
    public void execute()
    {
        switch(this.status)
        {
            case Initialize:
                this.setStatus(TaskStatus.Waiting);
                this.processChildAllSetStatus(TaskStatus.Initialize, TaskStatus.Waiting);

                this.setStatus(TaskStatus.Running);
                this.iteratorTask = this.getChilds().iterator();
                executeSerial();
                break;

            case Waiting:
                this.setStatus(TaskStatus.Running);
                this.iteratorTask = this.getChilds().iterator();
                executeSerial();
                break;

            case Running:
                break;

            case Completed:
                break;
        }
    }

    private void executeSerial()
    {
        if(this.iteratorTask.hasNext())
        {
            TaskInterface targetTask = this.iteratorTask.next();
            targetTask.execute();
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
                executeSerial();
                break;
            case Completed:
                break;
        }
    }

    @Override
    protected void onChildAllTaskCompleted()
    {
        switch(this.status)
        {
            case Initialize:
                break;
            case Waiting:
                break;
            case Running:
                this.setStatus(TaskStatus.Completed);
                this.processTaskCompleted();
                break;
            case Completed:
                break;
        }
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
    }
}
