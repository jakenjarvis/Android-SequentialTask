package com.tojc.task.android.implement;

import android.content.Context;
import android.os.Bundle;

import com.tojc.task.android.definition.TaskCompletedListener;
import com.tojc.task.android.definition.TaskContainer;
import com.tojc.task.android.definition.TaskInterface;
import com.tojc.task.android.definition.TaskStatus;
import com.tojc.task.android.definition.TaskWorker;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Jaken on 2015/05/24.
 */
public abstract class TaskBase implements TaskInterface, Runnable
{
    protected Context context = null;

    protected TaskCompletedListener parentListener;
    protected List<TaskInterface> childs = null;

    protected UUID uuid;
    protected String taskTypeName;
    protected Bundle parameters;
    protected TaskStatus status;
    protected Runnable worker;

    public TaskBase(Context context, String taskTypeName)
    {
        this.context = context;

        this.parentListener = null;
        this.childs = new LinkedList<TaskInterface>();

        this.uuid = UUID.randomUUID();
        this.taskTypeName = taskTypeName;
        this.parameters = null;
        this.status = TaskStatus.Initialize;
        this.worker = null;

        //Logger.verbose().print("worker:[uuid=%s] %s create", this.uuid.toString(), this.getTaskTypeName());
    }

    @Override
    public TaskCompletedListener getParentListener()
    {
        return this.parentListener;
    }

    @Override
    public void setParentListener(TaskCompletedListener parentListener)
    {
        this.parentListener = parentListener;
    }

    @Override
    public List<TaskInterface> getChilds()
    {
        if(this.childs == null)
        {
            throw new IllegalStateException("Invalid call after this destroyed.");
        }
        return this.childs;
    }

    @Override
    public TaskContainer addChild(TaskWorker childtask)
    {
        return (TaskContainer)this.addChild((TaskInterface)childtask);
    }
    @Override
    public TaskContainer addChild(TaskContainer childtask)
    {
        return (TaskContainer)this.addChild((TaskInterface)childtask);
    }
    @Override
    public TaskInterface addChild(TaskInterface childtask)
    {
        if(this.status != TaskStatus.Initialize)
        {
            throw new IllegalStateException("Can only register during initialization.");
        }
        if(childtask.getParentListener() != null)
        {
            throw new IllegalStateException("Parent has already been registered.");
        }
        if(this.getUuid().equals(childtask.getUuid()))
        {
            throw new IllegalStateException("Themselves can not be registered.");
        }
        if(this.childs == null)
        {
            throw new IllegalStateException("Invalid call after this destroyed.");
        }
        for(TaskInterface child : this.childs)
        {
            if(child.getUuid().equals(childtask.getUuid()))
            {
                throw new IllegalStateException("Child has already been registered.");
            }
        }
        if(onChildTaskBeforeAdd(childtask))
        {
            childtask.setParentListener(this);
            this.childs.add(childtask);
            onChildTaskAdded(childtask);
        }

        return this;
    }

    protected boolean onChildTaskBeforeAdd(TaskInterface childtask)
    {
        return true;
    }

    protected void onChildTaskAdded(TaskInterface childtask)
    {
    }

    @Override
    public UUID getUuid()
    {
        return this.uuid;
    }

    @Override
    public String getTaskTypeName()
    {
        return this.taskTypeName;
    }

    @Override
    public Bundle getParameters()
    {
        return this.parameters;
    }

    @Override
    public void setParameters(Bundle parameters)
    {
        this.parameters = parameters;
    }

    @Override
    public TaskStatus getStatus()
    {
        return this.status;
    }

    @Override
    public void setStatus(TaskStatus status)
    {
        //Logger.verbose().print("worker:[uuid=%s] status:[%s => %s]", this.uuid.toString(), this.status.name(), status.name());
        this.status = status;
    }

    @Override
    public Runnable getWorker()
    {
        return this.worker;
    }

    @Override
    public void setWorker(Runnable worker)
    {
        this.worker = worker;
    }

    @Override
    public boolean isChildsCompleted()
    {
        boolean result = true;
        if(this.childs != null)
        {
            for(TaskInterface childtask : this.childs)
            {
                if(childtask.getStatus() != TaskStatus.Completed)
                {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public final void run()
    {
        if(this.worker != null)
        {
            //Logger.verbose().print("worker:[uuid=%s] run() start", this.uuid.toString());
            onExecuteWorkerBefore();
            this.worker.run();
            onExecuteWorkerAfter();
            //Logger.verbose().print("worker:[uuid=%s] run() end", this.uuid.toString());
        }
    }

    protected void onExecuteWorkerBefore()
    {
    }

    protected void onExecuteWorkerAfter()
    {
    }

    protected void processChildAllSetStatus(TaskStatus ifstatus, TaskStatus setstatus)
    {
        if(this.childs != null)
        {
            for(TaskInterface child : this.childs)
            {
                if(child.getStatus() == ifstatus)
                {
                    child.setStatus(setstatus);
                }
            }
        }
    }

    protected void processTaskCompleted()
    {
        // this task completed
        if(this.childs != null)
        {
            if((this.status == TaskStatus.Completed) && (this.isChildsCompleted()))
            {
                if(this.parentListener != null)
                {
                    this.parentListener.onTaskCompleted(this);
                }
            }
            else
            {
                throw new IllegalStateException("Must complete all together parent and childs.");
            }
        }
    }

    @Override
    public void onTaskCompleted(TaskInterface task)
    {
        // child task completed
        onChildTaskCompleted(task);

        if(this.isChildsCompleted())
        {
            onChildAllTaskCompleted();
        }
    }

    protected void onChildTaskCompleted(TaskInterface childtask)
    {
    }

    protected void onChildAllTaskCompleted()
    {
    }

    @Override
    public TaskInterface findTaskFromUuid(UUID uuid)
    {
        TaskInterface result = null;
        if(this.getUuid().equals(uuid))
        {
            result = this;
        }
        else
        {
            for(TaskInterface child : this.childs)
            {
                result = child.findTaskFromUuid(uuid);
                if(result != null)
                {
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public TaskInterface findTaskFromParameters(String key, Object value)
    {
        TaskInterface result = null;

        if(this.getParameters() != null)
        {
            if(this.getParameters().containsKey(key))
            {
                if(this.getParameters().get(key).equals(value))
                {
                    result = this;
                }
            }
        }

        if(result == null)
        {
            for(TaskInterface child : this.childs)
            {
                result = child.findTaskFromParameters(key, value);
                if(result != null)
                {
                    break;
                }
            }
        }
        return result;
    }

    protected void onDestroyBefore()
    {
    }

    protected void onDestroyAfter()
    {
    }

    @Override
    public int count()
    {
        if(this.childs == null)
        {
            throw new IllegalStateException("Invalid call after this destroyed.");
        }
        return this.childs.size();
    }

    @Override
    public void onDestroy()
    {
        onDestroyBefore();

        if(this.childs != null)
        {
            for(TaskInterface childtask : this.childs)
            {
                childtask.onDestroy();
            }
            this.childs.clear();
            this.childs = null;
        }

        this.context = null;
        this.parentListener = null;
        this.uuid = null;
        this.parameters = null;
        this.status = null;
        this.worker = null;

        onDestroyAfter();
    }
}
