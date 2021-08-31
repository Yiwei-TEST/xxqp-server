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

/** ExecutionCondition class created from MMPM condition IsHuPai. */
public class IsHuPai extends
		jbt.execution.task.leaf.condition.ExecutionCondition {

	/**
	 * Constructor. Constructs an instance of IsHuPai that is able to run a
	 * com.sy599.game.qipai.csmj.robot.conditions.IsHuPai.
	 */
	public IsHuPai(
			com.sy599.game.qipai.csmj.robot.conditions.IsHuPai modelTask,
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
		boolean canAction = (boolean) getContext().getVariable("hu");
		boolean xiaohu = (boolean) getContext().getVariable("xiaohu");
		if(xiaohu){
			 LogUtil.printDebug("有小胡 返回胡了");
			 getContext().setVariable("action","xiaohu");
			 return Status.SUCCESS;
		}
		boolean canZiMoAction = (boolean) getContext().getVariable("zimo");
		if(canAction){
			getContext().setVariable("action","hu");
			LogUtil.printDebug(this.getClass().getCanonicalName() +" "+getContext().getVariable("userName")+" action:hu="+canAction);
			return Status.SUCCESS;
		}else if(canZiMoAction){
			getContext().setVariable("action","zimo");
			LogUtil.printDebug(this.getClass().getCanonicalName() +" "+getContext().getVariable("userName")+" action:zimo="+canZiMoAction);
			return Status.SUCCESS;
		} else{
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