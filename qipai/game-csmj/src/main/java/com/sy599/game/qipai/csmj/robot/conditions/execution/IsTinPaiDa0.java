// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 08/18/2020 10:39:50
// ******************************************************* 
package com.sy599.game.qipai.csmj.robot.conditions.execution;

import com.sy599.game.util.LogUtil;

/** ExecutionCondition class created from MMPM condition IsTinPaiDa0. */
public class IsTinPaiDa0 extends
		jbt.execution.task.leaf.condition.ExecutionCondition {

	/**
	 * Constructor. Constructs an instance of IsTinPaiDa0 that is able to run a
	 * com.sy599.game.qipai.csmj.robot.conditions.IsTinPaiDa0.
	 */
	public IsTinPaiDa0(
			com.sy599.game.qipai.csmj.robot.conditions.IsTinPaiDa0 modelTask,
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
		boolean tingpaida0 = (boolean) getContext().getVariable("IsTinPai");
		if(tingpaida0){
			 LogUtil.printDebug("听牌了：===设置过动作");
			getContext().setVariable("guo",true);
			return Status.SUCCESS;
		}else{
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