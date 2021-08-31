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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sy599.game.qipai.pdkuai.tool.CardTypeTool;
import com.sy599.game.util.LogUtil;

/** ExecutionAction class created from MMPM action Jiefeiji. */
public class Jiefeiji extends jbt.execution.task.leaf.action.ExecutionAction {

	/**
	 * Constructor. Constructs an instance of Jiefeiji that is able to run a
	 * com.sy599.game.qipai.pdkuai.robot.actions.Jiefeiji.
	 */
	public Jiefeiji(
			com.sy599.game.qipai.pdkuai.robot.actions.Jiefeiji modelTask,
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
		LogUtil.printDebug("接飞机");
	}

	protected jbt.execution.core.ExecutionTask.Status internalTick() {
		/*
		 * TODO: this method's implementation must be completed. This function
		 * should only return Status.SUCCESS, Status.FAILURE or Status.RUNNING.
		 * No other values are allowed.
		 */
		List<Integer> disCards = getContext().getVariable("disCards") == null ? null: (List<Integer>)this.getContext().getVariable("disCards");
		List<Integer> handCards = getContext().getVariable("handCards") == null ? new ArrayList<>(): (List<Integer>)this.getContext().getVariable("handCards");
		if(disCards == null || disCards.size() <= 0){//对手出牌
			LogUtil.printDebug("没找到对手出的牌");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		if(!CardTypeTool.isDisFeiji(disCards)){
			LogUtil.printDebug("对手出牌不是飞机");
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
		List<List<Integer>> feijis = allPaixing.get(CardTypeTool.px_feiji);
		if(feijis.isEmpty() ){
			LogUtil.printDebug("自己手上没有飞机");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		//获取对手的飞机
		Map<Integer, List<List<Integer>>> rivalAallPaixing = CardTypeTool.getAllPaiXing(disCards,false);
		if(rivalAallPaixing.isEmpty()){
			LogUtil.printDebug("对手没有获取到牌型");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		List<List<Integer>> rivalFeijis = rivalAallPaixing.get(CardTypeTool.px_feiji);
		if(rivalFeijis.isEmpty() || rivalFeijis.get(0).isEmpty()){
			LogUtil.printDebug("没有找到对手的飞机");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		List<Integer> rivalFeiji = rivalFeijis.get(0);
		Collections.sort(rivalFeiji);
		List<Integer> myFeiji = new ArrayList<>();
		for (List<Integer> feiji : feijis) {
			myFeiji.clear();
			if(feiji.isEmpty()){
				continue;
			}
			if(feiji.size() >= rivalFeiji.size()&&feiji.get(0) > rivalFeiji.get(0)){
				myFeiji.addAll(feiji.subList(feiji.size()-rivalFeiji.size(), feiji.size()));
			}
		}
		if(myFeiji.isEmpty()){
			LogUtil.printDebug("没有找到可以接对手飞机的牌");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		
		List<Integer> pais = CardTypeTool.getPaisByPaiXing(CardTypeTool.px_feiji, myFeiji, handCards);
		if(pais.isEmpty()){
			LogUtil.printDebug("没有找到可以接对手飞机的牌1");
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