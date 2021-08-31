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

/** ExecutionAction class created from MMPM action Jiesanzhang. */
public class Jiesanzhang extends jbt.execution.task.leaf.action.ExecutionAction {

	/**
	 * Constructor. Constructs an instance of Jiesanzhang that is able to run a
	 * com.sy599.game.qipai.pdkuai.robot.actions.Jiesanzhang.
	 */
	public Jiesanzhang(
			com.sy599.game.qipai.pdkuai.robot.actions.Jiesanzhang modelTask,
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
		LogUtil.printDebug("接三张");
	}

	protected jbt.execution.core.ExecutionTask.Status internalTick() {
		List<Integer> disCards = getContext().getVariable("disCards") == null ? null: (List<Integer>)this.getContext().getVariable("disCards");
		List<Integer> handCards = getContext().getVariable("handCards") == null ? new ArrayList<>(): (List<Integer>)this.getContext().getVariable("handCards");
		if(disCards == null || disCards.size() <= 0){//对手出牌
			LogUtil.printDebug("没找到对手出的牌");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		if(!CardTypeTool.isDisSantiao(disCards)){
			LogUtil.printDebug("对手出牌不是三张");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		if(handCards.size() < disCards.size()){//手上牌不够了，接不了
			LogUtil.printDebug("检测到手牌数量不足，不能接牌");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		List<Integer> rivalAllSantiao = handCardHaveSantiao(disCards);
		if(rivalAllSantiao == null || rivalAllSantiao.size() <= 0){
			LogUtil.printDebug("找不出来对手出的是什么三条");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		int rivalSt = rivalAllSantiao.get(0);
		
		
		Map<Integer, List<List<Integer>>> allPaixing = CardTypeTool.getAllPaiXing(handCards,false);
		if(allPaixing.isEmpty()){
			LogUtil.printDebug("没有获取到牌型");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		List<List<Integer>> santiaos = allPaixing.get(CardTypeTool.px_santiao);
		if(!santiaos.isEmpty()  && !santiaos.get(0).isEmpty()){//拆3条
			List<Integer> allSt = santiaos.get(0);
			Collections.sort(allSt);
			for (Integer dz : allSt) {
				if(dz > rivalSt){
					List<Integer> pais = CardTypeTool.getPaisByPaiXing(CardTypeTool.px_santiao, Arrays.asList(dz), handCards);
					if(!pais.isEmpty()){
						getContext().setVariable("chupai", pais);
						return jbt.execution.core.ExecutionTask.Status.SUCCESS;
					}
				}
			}
		}
		LogUtil.printDebug("接三条失败");
		return jbt.execution.core.ExecutionTask.Status.FAILURE;
	}

	/**
	 * 手牌里面是不是有三条
	 * @param cards
	 * @return
	 */
	private List<Integer> handCardHaveSantiao(List<Integer> cards){
		if(cards.isEmpty()){//10
			return Collections.emptyList();
		}
		Map<Integer, Integer> map = CardTool.loadCards(cards);
		List<Integer> threes = new ArrayList<>();
		for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
			if(kv.getValue().intValue() == 3){
				threes.add(kv.getKey());
			}
		}
		if(threes.isEmpty()){
			return Collections.emptyList();
		}
		Collections.sort(threes);
		return threes;
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