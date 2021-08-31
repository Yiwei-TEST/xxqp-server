// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 08/08/2020 10:52:54
// ******************************************************* 
package com.sy599.game.qipai.csmj.robot.actions.execution;

import com.sy599.game.qipai.csmj.rule.CsMj;
import com.sy599.game.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/** ExecutionAction class created from MMPM action ChongzhiShouPai. */
public class ChongzhiShouPai extends
		jbt.execution.task.leaf.action.ExecutionAction {

	/**
	 * Constructor. Constructs an instance of ChongzhiShouPai that is able to
	 * run a com.sy599.game.qipai.csmj.robot.actions.ChongzhiShouPai.
	 */
	public ChongzhiShouPai(
			com.sy599.game.qipai.csmj.robot.actions.ChongzhiShouPai modelTask,
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
		List<CsMj> mj= (List<CsMj>) getContext().getVariable("resetMj" );
		getContext().setVariable("canPengMj",new ArrayList<>(mj));
		getContext().setVariable("canChiMj",new ArrayList<>(mj));
		return Status.SUCCESS;
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