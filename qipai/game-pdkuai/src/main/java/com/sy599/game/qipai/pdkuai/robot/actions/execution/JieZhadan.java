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

import com.sy599.game.qipai.pdkuai.tool.CardTool;
import com.sy599.game.qipai.pdkuai.tool.CardTypeTool;
import com.sy599.game.util.LogUtil;

/** ExecutionAction class created from MMPM action JieZhadan. */
public class JieZhadan extends jbt.execution.task.leaf.action.ExecutionAction {

	/**
	 * Constructor. Constructs an instance of JieZhadan that is able to run a
	 * com.sy599.game.qipai.pdkuai.robot.actions.JieZhadan.
	 */
	public JieZhadan(
			com.sy599.game.qipai.pdkuai.robot.actions.JieZhadan modelTask,
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
		LogUtil.printDebug("接炸弹");
	}

	protected jbt.execution.core.ExecutionTask.Status internalTick() {
		List<Integer> disCards = getContext().getVariable("disCards") == null ? null: (List<Integer>)this.getContext().getVariable("disCards");
		List<Integer> handCards = getContext().getVariable("handCards") == null ? new ArrayList<>(): (List<Integer>)this.getContext().getVariable("handCards");
		if(disCards == null || disCards.size() <= 0){//对手出牌
			LogUtil.printDebug("没找到对手出的牌");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		if(!CardTypeTool.isDisZhadan(disCards)){
			LogUtil.printDebug("对手出牌不是炸弹");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		int rivalZhadan =CardTool.loadCardValue(disCards.get(0));
		
		
		Map<Integer, List<List<Integer>>> allPaixing = CardTypeTool.getAllPaiXing(handCards,false);
		if(allPaixing.isEmpty()){
			LogUtil.printDebug("没有获取到牌型");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		List<List<Integer>> zhadans = allPaixing.get(CardTypeTool.px_zhadan);
		if(zhadans.isEmpty()  || zhadans.get(0).isEmpty()){
			LogUtil.printDebug("没有炸弹可接");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}

		List<Integer> allZd = zhadans.get(0);
		Collections.sort(allZd);
		for (int i = allZd.size()-1; i >=0; i--) {
			int zd = allZd.get(i);
			if(zd > rivalZhadan){
				List<Integer> pais = CardTypeTool.getPaisByPaiXing(CardTypeTool.px_zhadan, Arrays.asList(zd), handCards);
				if(!pais.isEmpty()){
					getContext().setVariable("chupai", pais);
					return jbt.execution.core.ExecutionTask.Status.SUCCESS;
				}
			}
		}
		LogUtil.printDebug("接炸弹失败");
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