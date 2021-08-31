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

import com.sy599.game.qipai.pdkuai.tool.CardTypeTool;
import com.sy599.game.util.LogUtil;

/** ExecutionAction class created from MMPM action YichuPaixing. */
public class YichuPaixing extends
		jbt.execution.task.leaf.action.ExecutionAction {

	/**
	 * Constructor. Constructs an instance of YichuPaixing that is able to run a
	 * com.sy599.game.qipai.pdkuai.robot.actions.YichuPaixing.
	 */
	public YichuPaixing(
			com.sy599.game.qipai.pdkuai.robot.actions.YichuPaixing modelTask,
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
		LogUtil.printDebug("移出牌型");
	}

	protected jbt.execution.core.ExecutionTask.Status internalTick() {
		
		List<Integer> paiXingCards = getContext().getVariable("hasPaixing") == null ? new ArrayList<>(): (ArrayList<Integer>)this.getContext().getVariable("hasPaixing");
		int type = getContext().getVariable("hasPaixingType") == null ? -1:(int) getContext().getVariable("hasPaixingType");
		if(paiXingCards.isEmpty() || type < 0){
			LogUtil.printDebug("未获取到要移出的牌型");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		List<Integer> handCards = getContext().getVariable("handCards") == null ? new ArrayList<>(): (ArrayList<Integer>)this.getContext().getVariable("handCards");
		if(handCards.isEmpty()){
			LogUtil.printDebug("没找到手牌");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
//		List<Integer> reHandCards = CardTypeTool.removePaisByPaiXing(type, paiXingCards, handCards);
		List<Integer> reHandCards = CardTypeTool.getPaisByPaiXing(type, paiXingCards, handCards,true);
		handCards.removeAll(reHandCards);
		LogUtil.printDebug("移除牌型，更新手牌为:"+handCards);
		getContext().setVariable("handCards", new ArrayList<>(handCards));
		List<Integer> yichuHandCards = getContext().getVariable("yichuHandCards") == null ? new ArrayList<>(): (ArrayList<Integer>)this.getContext().getVariable("yichuHandCards");
		yichuHandCards.addAll(reHandCards);
		getContext().setVariable("yichuHandCards", new ArrayList<>(yichuHandCards));
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