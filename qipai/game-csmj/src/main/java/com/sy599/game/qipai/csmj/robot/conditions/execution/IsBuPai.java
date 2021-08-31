// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 07/10/2020 09:18:45
// ******************************************************* 
package com.sy599.game.qipai.csmj.robot.conditions.execution;

import com.sy599.game.util.LogUtil;

/** ExecutionCondition class created from MMPM condition IsBuPai. */
public class IsBuPai extends
		jbt.execution.task.leaf.condition.ExecutionCondition {

	/**
	 * Constructor. Constructs an instance of IsBuPai that is able to run a
	 * com.sy599.game.qipai.csmj.robot.conditions.IsBuPai.
	 */
	public IsBuPai(
			com.sy599.game.qipai.csmj.robot.conditions.IsBuPai modelTask,
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		super(modelTask, executor, parent);

	}

	protected void internalSpawn() {
		/*
		 * Do not remove this first line unless you know what it does and you
		 * need not do it.
		 */
		this.getExecutor().requestInsertionIntoList(
				jbt.execution.core.BTExecutor.BTExecutorList.TICKABLE, this);
		/* TODO: this method's implementation must be completed. */
		LogUtil.printDebug(this.getClass().getCanonicalName() + " spawned");
	}

	protected Status internalTick() {
		/*
		 * TODO: this method's implementation must be completed. This function
		 * should only return Status.SUCCESS, Status.FAILURE or Status.RUNNING.
		 * No other values are allowed.
		 */
		boolean canAction = (boolean) getContext().getVariable("mingbu");
		boolean canbuzhanganAction = (boolean) getContext().getVariable("anbu");

		if(canAction){
			getContext().setVariable("action","mingbu");
			LogUtil.printDebug(this.getClass().getCanonicalName() +" "+getContext().getVariable("userName")+" action:mingbu="+canAction);
			return Status.SUCCESS;
		}else if(canbuzhanganAction){
			LogUtil.printDebug(this.getClass().getCanonicalName() +" "+getContext().getVariable("userName")+" action:anbu="+canbuzhanganAction);
			getContext().setVariable("action","anbu");
			return Status.SUCCESS;
		}
		else{
			return Status.FAILURE;
		}
	}

	protected void internalTerminate() {
		/* TODO: this method's implementation must be completed. */
	}

	protected void restoreState(jbt.execution.core.ITaskState state) {
		/* TODO: this method's implementation must be completed. */
	}

	protected jbt.execution.core.ITaskState storeState() {
		/* TODO: this method's implementation must be completed. */
		return null;
	}

	protected jbt.execution.core.ITaskState storeTerminationState() {
		/* TODO: this method's implementation must be completed. */
		return null;
	}
}