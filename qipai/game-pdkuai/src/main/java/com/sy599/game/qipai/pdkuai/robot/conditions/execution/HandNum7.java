// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 07/07/2020 15:21:51
// ******************************************************* 
package com.sy599.game.qipai.pdkuai.robot.conditions.execution;

import java.util.ArrayList;
import java.util.List;

import com.sy599.game.util.LogUtil;

/** ExecutionCondition class created from MMPM condition HandNum7. */
public class HandNum7 extends
		jbt.execution.task.leaf.condition.ExecutionCondition {

	/**
	 * Constructor. Constructs an instance of HandNum7 that is able to run a
	 * com.sy599.game.qipai.pdkuai.robot.conditions.HandNum7.
	 */
	public HandNum7(
			com.sy599.game.qipai.pdkuai.robot.conditions.HandNum7 modelTask,
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
//		LogUtil.printDebug(this.getClass().getCanonicalName() + " spawned");
		LogUtil.printDebug("检测手牌是不是等于7张");
	}

	protected jbt.execution.core.ExecutionTask.Status internalTick() {
		List<Integer> handCards = getContext().getVariable("handCards") == null ? null: (ArrayList<Integer>)this.getContext().getVariable("handCards");
		if(handCards != null && handCards.size() == 7){
			LogUtil.printDebug("手牌数等于7张");
			return jbt.execution.core.ExecutionTask.Status.SUCCESS;
		}
		return jbt.execution.core.ExecutionTask.Status.FAILURE;
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