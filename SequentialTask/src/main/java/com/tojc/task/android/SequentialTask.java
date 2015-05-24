/*
 * Copyright 2013, Jaken Jarvis (jaken.jarvis@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tojc.task.android;

import android.content.Context;

import com.tojc.task.android.definition.TaskContainer;
import com.tojc.task.android.definition.TaskInterface;
import com.tojc.task.android.definition.TaskStatus;
import com.tojc.task.android.definition.TaskWorker;
import com.tojc.task.android.implement.BackgroundWorker;
import com.tojc.task.android.implement.NameTag;
import com.tojc.task.android.implement.ParallelContainer;
import com.tojc.task.android.implement.PostDelayedWorker;
import com.tojc.task.android.implement.PostWorker;
import com.tojc.task.android.implement.SerialContainer;
import com.tojc.task.android.implement.Worker;

import java.util.LinkedList;
import java.util.List;

public class SequentialTask {
    private Context context = null;
    protected List<TaskInterface> generatedList = null;

    public SequentialTask(Context context) {
        this.context = context;
        this.generatedList = new LinkedList<TaskInterface>();
    }

    public TaskContainer createSerialContainer() {
        TaskInterface result = new SerialContainer(this.context);
        this.generatedList.add(result);
        return result;
    }

    public TaskContainer createParallelContainer() {
        TaskInterface result = new ParallelContainer(this.context);
        this.generatedList.add(result);
        return result;
    }

    public TaskWorker createWorker(Runnable worker) {
        TaskInterface result = new Worker(this.context);
        result.setWorker(worker);
        this.generatedList.add(result);
        return result;
    }

    public TaskWorker createPostWorker(Runnable worker) {
        TaskInterface result = new PostWorker(this.context);
        result.setWorker(worker);
        this.generatedList.add(result);
        return result;
    }

    public TaskWorker createPostDelayedWorker(Runnable worker, long delayMillis) {
        TaskInterface result = new PostDelayedWorker(this.context, delayMillis);
        result.setWorker(worker);
        this.generatedList.add(result);
        return result;
    }

    public TaskWorker createBackgroundWorker(Runnable worker) {
        TaskInterface result = new BackgroundWorker(this.context);
        result.setWorker(worker);
        this.generatedList.add(result);
        return result;
    }

    public TaskWorker createNameTag(String nameTag) {
        TaskInterface result = new NameTag(this.context, nameTag);
        this.generatedList.add(result);
        return result;
    }

    public void completedNameTag(String nameTag) {
        TaskInterface target = null;
        for (TaskInterface task : this.generatedList) {
            target = task.findTaskFromParameters(NameTag.NAMETAG, nameTag);
            if (target != null) {
                target.completed();
                break;
            }
        }
    }

    public boolean isCompleted() {
        boolean result = true;
        for (TaskInterface task : this.generatedList) {
            if (task.getStatus() != TaskStatus.Completed) {
                result = false;
            }
        }
        return result;
    }


    public void onDestroy() {
        if (this.generatedList != null) {
            for (TaskInterface task : this.generatedList) {
                task.onDestroy();
            }
            this.generatedList.clear();
            this.generatedList = null;
        }
        this.context = null;
    }

}
