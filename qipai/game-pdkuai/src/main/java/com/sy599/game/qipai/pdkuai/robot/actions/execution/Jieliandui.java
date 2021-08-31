// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 07/15/2020 10:17:59
// ******************************************************* 
package com.sy599.game.qipai.pdkuai.robot.actions.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sy599.game.qipai.pdkuai.tool.CardTool;
import com.sy599.game.qipai.pdkuai.tool.CardTypeTool;
import com.sy599.game.util.LogUtil;

/** ExecutionAction class created from MMPM action Jieliandui. */
public class Jieliandui extends jbt.execution.task.leaf.action.ExecutionAction {

	/**
	 * Constructor. Constructs an instance of Jieliandui that is able to run a
	 * com.sy599.game.qipai.pdkuai.robot.actions.Jieliandui.
	 */
	public Jieliandui(
			com.sy599.game.qipai.pdkuai.robot.actions.Jieliandui modelTask,
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
		LogUtil.printDebug("接连对");
	}

	protected jbt.execution.core.ExecutionTask.Status internalTick() {
		List<Integer> disCards = getContext().getVariable("disCards") == null ? null: (List<Integer>)this.getContext().getVariable("disCards");
		List<Integer> handCards = getContext().getVariable("handCards") == null ? new ArrayList<>(): (List<Integer>)this.getContext().getVariable("handCards");
		if(disCards == null || disCards.size() <= 0){//对手出牌
			LogUtil.printDebug("没找到对手出的牌");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		if(!CardTypeTool.isDisLiandui(disCards)){
			LogUtil.printDebug("对手出牌不是连对");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		if(handCards.size() < disCards.size()){//手上牌不够了，接不了
			LogUtil.printDebug("检测到手牌数量不足，不能接牌");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		Map<Integer, List<List<Integer>>> allPaixing = CardTypeTool.getAllPaiXing(handCards,false);
		if(allPaixing.isEmpty()){
			LogUtil.printDebug("没有获取到牌型");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		List<List<Integer>> lianduis = allPaixing.get(CardTypeTool.px_liandui);
		if(lianduis.isEmpty()){
			LogUtil.printDebug("自己手上没有连对");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		
		List<Integer> rivalLiandui = new ArrayList<>();
		for (Integer card : disCards) {
			if(!rivalLiandui.contains(CardTool.loadCardValue(card))){
				rivalLiandui.add(CardTool.loadCardValue(card));
			}
		}
		Collections.sort(rivalLiandui);
		List<Integer> myLiandui = new ArrayList<>();
		for (List<Integer> liandui : lianduis) {
			myLiandui.clear();
			if(liandui.isEmpty()){
				continue;
			}
			if(liandui.size() >= rivalLiandui.size()&&liandui.get(liandui.size()-1) > rivalLiandui.get(rivalLiandui.size()-1)){
				myLiandui.addAll(liandui.subList(liandui.size()-rivalLiandui.size(), liandui.size()));
			}
		}
		if(myLiandui.isEmpty()){
			LogUtil.printDebug("没有找到可以接对手连对的牌");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		
		List<Integer> pais = CardTypeTool.getPaisByPaiXing(CardTypeTool.px_liandui, myLiandui, handCards);
		if(pais.isEmpty()){
			LogUtil.printDebug("没有找到可以接对手连对的牌1");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		getContext().setVariable("chupai", pais);
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