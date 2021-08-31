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

/** ExecutionAction class created from MMPM action hu. */
public class hu extends jbt.execution.task.leaf.action.ExecutionAction {

	/**
	 * Constructor. Constructs an instance of hu that is able to run a
	 * com.sy599.game.qipai.csmj.robot.actions.hu.
	 */
	public hu(com.sy599.game.qipai.csmj.robot.actions.hu modelTask,
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
		boolean canZiMoAction = (boolean) getContext().getVariable("zimo");
		boolean canXiaoHuAction = (boolean) getContext().getVariable("xiaohu");
		if(canZiMoAction){
			getContext().setVariable("action","zimo");
			LogUtil.printDebug(this.getClass().getCanonicalName() +" "+getContext().getVariable("userName")+" action:zimo="+canZiMoAction);
			return Status.SUCCESS;
		}
		if(canAction){
			getContext().setVariable("action","hu");
			LogUtil.printDebug(this.getClass().getCanonicalName() +" "+getContext().getVariable("userName")+" action:hu="+canAction);
			return Status.SUCCESS;
		}

		 if(canXiaoHuAction){
			getContext().setVariable("action","xiaohu");
			LogUtil.printDebug(this.getClass().getCanonicalName() +" "+getContext().getVariable("userName")+" action:canXiaoHuAction="+canXiaoHuAction);
			return Status.SUCCESS;
		}else {
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