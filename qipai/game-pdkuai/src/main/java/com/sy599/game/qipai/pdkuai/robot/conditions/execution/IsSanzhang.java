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
import java.util.Map;

import com.sy599.game.qipai.pdkuai.tool.CardTool;
import com.sy599.game.qipai.pdkuai.tool.CardTypeTool;
import com.sy599.game.util.LogUtil;

/** ExecutionCondition class created from MMPM condition IsSanzhang. */
public class IsSanzhang extends
		jbt.execution.task.leaf.condition.ExecutionCondition {

	/**
	 * Constructor. Constructs an instance of IsSanzhang that is able to run a
	 * com.sy599.game.qipai.pdkuai.robot.conditions.IsSanzhang.
	 */
	public IsSanzhang(
			com.sy599.game.qipai.pdkuai.robot.conditions.IsSanzhang modelTask,
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
		LogUtil.printDebug("检测对手出的是不是三带二");
	}

	protected jbt.execution.core.ExecutionTask.Status internalTick() {
		List<Integer> disCards = getContext().getVariable("disCards") == null ? null: (List<Integer>)this.getContext().getVariable("disCards");
		if(disCards == null || disCards.size() <= 0){//对手出牌
			LogUtil.printDebug("没找到对手出的牌");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		if(!CardTypeTool.isDisSantiao(disCards)){
			LogUtil.printDebug("对手出牌不是三带二");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
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