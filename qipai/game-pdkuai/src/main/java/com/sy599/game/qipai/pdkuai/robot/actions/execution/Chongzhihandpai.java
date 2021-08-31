// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 07/07/2020 15:21:50
// ******************************************************* 
package com.sy599.game.qipai.pdkuai.robot.actions.execution;

import java.util.ArrayList;
import java.util.List;

import com.sy599.game.util.LogUtil;

/** ExecutionAction class created from MMPM action Chongzhihandpai. */
public class Chongzhihandpai extends
		jbt.execution.task.leaf.action.ExecutionAction {

	/**
	 * Constructor. Constructs an instance of Chongzhihandpai that is able to
	 * run a com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai.
	 */
	public Chongzhihandpai(
			com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai modelTask,
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
		LogUtil.printDebug("重置手牌");
	}

	protected jbt.execution.core.ExecutionTask.Status internalTick() {
		List<Integer> initHandCards = getContext().getVariable("initHandCards") == null ? new ArrayList<>(): (ArrayList<Integer>)this.getContext().getVariable("initHandCards");
		LogUtil.printDebug("获取到初始手牌:"+initHandCards);
		if(initHandCards.isEmpty()){
			LogUtil.printDebug("没有获取到初始手牌");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		getContext().setVariable("handCards", initHandCards);
		LogUtil.printDebug("重置初始手牌成功,更新后手牌为："+getContext().getVariable("handCards"));
		return jbt.execution.core.ExecutionTask.Status.SUCCESS;
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