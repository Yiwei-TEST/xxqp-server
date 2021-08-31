// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 07/10/2020 09:18:45
// ******************************************************* 
package com.sy599.game.qipai.csmj.robot.actions.execution;

import com.sy599.game.util.LogUtil;

/** ExecutionAction class created from MMPM action bu. */
public class bu extends jbt.execution.task.leaf.action.ExecutionAction {

	/**
	 * Constructor. Constructs an instance of bu that is able to run a
	 * com.sy599.game.qipai.csmj.robot.actions.bu.
	 */
	public bu(com.sy599.game.qipai.csmj.robot.actions.bu modelTask,
			  jbt.execution.core.BTExecutor executor,
			  jbt.execution.core.ExecutionTask parent) {
		super(modelTask, executor, parent);

	}

	protected void internalSpawn() {
		/*
		 * Do not remove this first line- unless you know what it does and you
		 * need not do it.
		 */
		this.getExecutor().requestInsertionIntoList(
				jbt.execution.core.BTExecutor.BTExecutorList.TICKABLE, this);
		 
		LogUtil.printDebug(this.getClass().getCanonicalName() + " spawned");
	}

	protected Status internalTick() {
		/*
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
		 
	}

	protected void restoreState(jbt.execution.core.ITaskState state) {
		 
	}

	protected jbt.execution.core.ITaskState storeState() {
		 
		return null;
	}

	protected jbt.execution.core.ITaskState storeTerminationState() {
		 
		return null;
	}
}