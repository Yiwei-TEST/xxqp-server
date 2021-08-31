// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 08/06/2020 10:08:31
// ******************************************************* 
package com.sy599.game.qipai.csmj.robot.actions.execution;

import com.sy599.game.qipai.csmj.rule.CsMj;
import com.sy599.game.util.LogUtil;

import java.util.List;

/** ExecutionAction class created from MMPM action YichuShunKan. */
public class YichuShunKan extends
		jbt.execution.task.leaf.action.ExecutionAction {

	/**
	 * Constructor. Constructs an instance of YichuShunKan that is able to run a
	 * com.sy599.game.qipai.csmj.robot.actions.YichuShunKan.
	 */
	public YichuShunKan(
			com.sy599.game.qipai.csmj.robot.actions.YichuShunKan modelTask,
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
		List<CsMj> sydui = (List<CsMj>) getContext().getVariable("YichuShunKan");
		if(null!=sydui && sydui.size()>0){
			LogUtil.printDebug(this.getClass().getCanonicalName() +" YichuShunKan SUCCESS ");
			return Status.SUCCESS;
		}else{
			LogUtil.printDebug(this.getClass().getCanonicalName() +" YichuShunKan FAILURE ");
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