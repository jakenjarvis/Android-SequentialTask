package com.tojc.task.android.implement;

import android.content.Context;
import android.os.Bundle;

import com.tojc.task.android.definition.TaskInterface;
import com.tojc.task.android.definition.TaskStatus;

/**
 * Created by Jaken on 2015/05/24.
 */
public class NameTag extends TaskBase
{
    public static final String NAMETAG = "NameTag";

    private String nameTag = "";

    public NameTag(Context context)
    {
        super(context, "NameTag");
        this.nameTag = "";
    }

    public NameTag(Context context, String nameTag)
    {
        super(context, "NameTag(" + nameTag + ")");
        this.setNameTag(nameTag);
    }

    public String getNameTag()
    {
        return this.nameTag;
    }

    public void setNameTag(String nameTag)
    {
        this.nameTag = nameTag;

        if(this.parameters == null)
        {
            this.parameters = new Bundle();
        }
        this.parameters.putString(NAMETAG, this.nameTag);
    }

    @Override
    public TaskInterface addChild(TaskInterface childtask)
    {
        throw new UnsupportedOperationException("NameTag, the child can not be registered.");
    }

    @Override
    public void execute()
    {
        if((this.nameTag == null) || (this.nameTag.length() <= 0))
        {
            throw new IllegalStateException("NameTag is undefined.");
        }

        switch(this.status)
        {
            case Initialize:
                this.setStatus(TaskStatus.Running);
                break;

            case Waiting:
                this.setStatus(TaskStatus.Running);
                break;

            case Running:
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
                this.setStatus(TaskStatus.Completed);
                this.processTaskCompleted();
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
