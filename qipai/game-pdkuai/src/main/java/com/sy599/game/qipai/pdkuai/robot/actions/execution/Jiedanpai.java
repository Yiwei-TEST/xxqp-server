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

/** ExecutionAction class created from MMPM action Jiedanpai. */
public class Jiedanpai extends jbt.execution.task.leaf.action.ExecutionAction {

	/**
	 * Constructor. Constructs an instance of Jiedanpai that is able to run a
	 * com.sy599.game.qipai.pdkuai.robot.actions.Jiedanpai.
	 */
	public Jiedanpai(
			com.sy599.game.qipai.pdkuai.robot.actions.Jiedanpai modelTask,
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
		LogUtil.printDebug("接单牌");
	}

	protected jbt.execution.core.ExecutionTask.Status internalTick() {
		List<Integer> disCards = getContext().getVariable("disCards") == null ? null: (List<Integer>)this.getContext().getVariable("disCards");
		if(disCards == null || disCards.size() <= 0){//对手出牌
			LogUtil.printDebug("没找到对手出的牌");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		if(!CardTypeTool.isDisDanpai(disCards)){
			LogUtil.printDebug("对手出牌不是单牌");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		int rivalDanpai=CardTool.loadCardValue(disCards.get(0));
		List<Integer> handCards = getContext().getVariable("handCards") == null ? new ArrayList<>(): (List<Integer>)this.getContext().getVariable("handCards");
		Map<Integer, List<List<Integer>>> allPaixing = CardTypeTool.getAllPaiXing(handCards,false);
		if(allPaixing.isEmpty()){
			LogUtil.printDebug("没有获取到牌型");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		
		List<List<Integer>> danpai = allPaixing.get(CardTypeTool.px_danpai);
		if(!danpai.isEmpty()  && !danpai.get(0).isEmpty()){//有单接单
			for (Integer dp : danpai.get(0)) {
				if(dp > rivalDanpai){
					List<Integer> pais = CardTypeTool.getPaisByPaiXing(CardTypeTool.px_danpai, Arrays.asList(dp), handCards);
					if(pais.isEmpty()){
						continue;
					}
					getContext().setVariable("chupai", pais);
					return jbt.execution.core.ExecutionTask.Status.SUCCESS;
					
				}
			}
		}
		List<List<Integer>> duizis = allPaixing.get(CardTypeTool.px_duizi);
		List<List<Integer>> santiaos = allPaixing.get(CardTypeTool.px_santiao);
		if(!duizis.isEmpty()  && !duizis.get(0).isEmpty()){//没单拆对
			LogUtil.printDebug("拆对");
			for (Integer dz : duizis.get(0)) {
				if(!santiaos.isEmpty()  && !santiaos.get(0).isEmpty() && santiaos.get(0).contains(dz)){//不拆3条
					continue;
				}
				if(dz > rivalDanpai){
					List<Integer> pais = CardTypeTool.getPaisByPaiXing(CardTypeTool.px_danpai, Arrays.asList(dz), handCards);
					if(pais.isEmpty()){
						continue;
					}
					getContext().setVariable("chupai", pais);
					return jbt.execution.core.ExecutionTask.Status.SUCCESS;
				}
			}
		}
		List<List<Integer>> shunzis = allPaixing.get(CardTypeTool.px_shunzi);
		if(!shunzis.isEmpty()){//没单没对拆顺
			LogUtil.printDebug("拆顺子");
			for (List<Integer> sz : shunzis) {
				if(sz.isEmpty()){
					continue;
				}
				int chaisz = sz.get(sz.size()-1);
				if(sz.size() > 5 && chaisz > rivalDanpai){
					if(chaisz > rivalDanpai){
						List<Integer> pais = CardTypeTool.getPaisByPaiXing(CardTypeTool.px_danpai, Arrays.asList(chaisz), handCards);
						if(pais.isEmpty()){
							continue;
						}
						getContext().setVariable("chupai", pais);
						return jbt.execution.core.ExecutionTask.Status.SUCCESS;
					}
				}
			}
			
		}
		if(!santiaos.isEmpty()  && !santiaos.get(0).isEmpty()){//拆3条
			LogUtil.printDebug("拆3条");
			for (Integer dz : santiaos.get(0)) {
				if(dz > rivalDanpai){
					List<Integer> pais = CardTypeTool.getPaisByPaiXing(CardTypeTool.px_danpai, Arrays.asList(dz), handCards);
					if(pais.isEmpty()){
						continue;
					}
					getContext().setVariable("chupai", pais);
					return jbt.execution.core.ExecutionTask.Status.SUCCESS;
				}
			}
		}
		List<List<Integer>> zhadans = allPaixing.get(CardTypeTool.px_zhadan);
		List<Integer> zhadanList = zhadans.isEmpty()?new ArrayList<>():zhadans.get(0);
		List<Integer> cardVals = CardTool.getCardsVals(handCards);
		for (Integer card :cardVals) {
			LogUtil.printDebug("遍历所有牌");
			if(card > rivalDanpai && (zhadanList.isEmpty() || !zhadanList.contains(card))){
				List<Integer> pais = CardTypeTool.getPaisByPaiXing(CardTypeTool.px_danpai, Arrays.asList(card), handCards);
				if(pais.isEmpty()){
					continue;
				}
				getContext().setVariable("chupai", pais);
				return jbt.execution.core.ExecutionTask.Status.SUCCESS;
			}
		}
		LogUtil.printDebug("接单牌失败");
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