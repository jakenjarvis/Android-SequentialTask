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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

public class SequentialTask
{
	public enum TaskStatus
	{
		Initialize,
		Waiting,
		Running,
		Completed
	}

	private Context context = null;
	protected List<TaskInterface> generatedList = null;

	public SequentialTask(Context context)
	{
		this.context = context;
		this.generatedList = new LinkedList<TaskInterface>();
	}

	public TaskContainer createSerialContainer()
	{
		TaskInterface result = new SerialContainer(this.context);
		this.generatedList.add(result);
		return result;
	}

	public TaskContainer createParallelContainer()
	{
		TaskInterface result = new ParallelContainer(this.context);
		this.generatedList.add(result);
		return result;
	}

	public TaskWorker createWorker(Runnable worker)
	{
		TaskInterface result = new Worker(this.context);
		result.setWorker(worker);
		this.generatedList.add(result);
		return result;
	}

	public TaskWorker createPostWorker(Runnable worker)
	{
		TaskInterface result = new PostWorker(this.context);
		result.setWorker(worker);
		this.generatedList.add(result);
		return result;
	}

	public TaskWorker createPostDelayedWorker(Runnable worker, long delayMillis)
	{
		TaskInterface result = new PostDelayedWorker(this.context, delayMillis);
		result.setWorker(worker);
		this.generatedList.add(result);
		return result;
	}

	public TaskWorker createBackgroundWorker(Runnable worker)
	{
		TaskInterface result = new BackgroundWorker(this.context);
		result.setWorker(worker);
		this.generatedList.add(result);
		return result;
	}

	public TaskWorker createNameTag(String nameTag)
	{
		TaskInterface result = new NameTag(this.context, nameTag);
		this.generatedList.add(result);
		return result;
	}

	public void completedNameTag(String nameTag)
	{
		TaskInterface target = null;
		for(TaskInterface task : this.generatedList)
		{
			target = task.findTaskFromParameters(NameTag.NAMETAG, nameTag);
			if(target != null)
			{
				target.completed();
				break;
			}
		}
	}

	public boolean isCompleted()
	{
		boolean result = true;
		for(TaskInterface task : this.generatedList)
		{
			if(task.getStatus() != TaskStatus.Completed)
			{
				result = false;
			}
		}
		return result;
	}


	public void onDestroy()
	{
		if(this.generatedList != null)
		{
			for(TaskInterface task : this.generatedList)
			{
				task.onDestroy();
			}
			this.generatedList.clear();
			this.generatedList = null;
		}
		this.context = null;
	}

	public interface TaskCompletedListener
	{
		public void onTaskCompleted(TaskInterface task);
	}

	public interface TaskContainer
	{
		public UUID getUuid();
		public String getTaskTypeName();
		public TaskStatus getStatus();

		public TaskContainer addChild(TaskInterface childtask);
		public TaskContainer addChild(TaskWorker childtask);
		public TaskContainer addChild(TaskContainer childtask);

		public boolean isChildsCompleted();

		public void execute();
		public void completed();

		public int count();
	}

	public interface TaskWorker
	{
		public UUID getUuid();
		public String getTaskTypeName();
		public TaskStatus getStatus();

		public void execute();
		public void completed();
	}

	public interface TaskInterface
		extends
			TaskContainer,
			TaskWorker,
			TaskCompletedListener
	{
		public TaskCompletedListener getParentListener();
		public void setParentListener(TaskCompletedListener parent);

		public List<TaskInterface> getChilds();
		public TaskInterface addChild(TaskInterface childtask);

		public boolean isChildsCompleted();

		public TaskInterface findTaskFromUuid(UUID uuid);
		public TaskInterface findTaskFromParameters(String key, Object value);

		public Runnable getWorker();
		public void setWorker(Runnable worker);

		public UUID getUuid();
		public String getTaskTypeName();
		public Bundle getParameters();
		public void setParameters(Bundle parameters);
		public TaskStatus getStatus();
		public void setStatus(TaskStatus status);

		public void execute();
		public void completed();

		public int count();

		public void onDestroy();
	}

	public static abstract class TaskBase implements TaskInterface, Runnable
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
			for(TaskInterface childtask : this.childs)
			{
				if(childtask.getStatus() != TaskStatus.Completed)
				{
					result = false;
					break;
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
			for(TaskInterface child : this.childs)
			{
				if(child.getStatus() == ifstatus)
				{
					child.setStatus(setstatus);
				}
			}
		}

		protected void processTaskCompleted()
		{
			// this task completed
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

	public static class SerialContainer extends TaskBase
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

	public static class ParallelContainer extends TaskBase
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

	public static abstract class WorkerBase extends TaskBase
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

	public static class Worker extends WorkerBase
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
	
	public static class PostWorker extends WorkerBase
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

	public static class PostDelayedWorker extends WorkerBase
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

	public static class BackgroundWorker extends WorkerBase
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

	public static class NameTag extends TaskBase
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
}
