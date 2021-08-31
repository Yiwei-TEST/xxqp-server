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

/** ExecutionAction class created from MMPM action JieShunzi. */
public class JieShunzi extends jbt.execution.task.leaf.action.ExecutionAction {

	/**
	 * Constructor. Constructs an instance of JieShunzi that is able to run a
	 * com.sy599.game.qipai.pdkuai.robot.actions.JieShunzi.
	 */
	public JieShunzi(
			com.sy599.game.qipai.pdkuai.robot.actions.JieShunzi modelTask,
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
		LogUtil.printDebug("接顺子");
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
		if(!CardTypeTool.isDisShunzi(disCards)){
			LogUtil.printDebug("对手出牌不是顺子");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		if(handCards.size() < disCards.size()){//手上牌不够了，接不了
			LogUtil.printDebug("检测到手牌数量不足，不能接牌");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		List<Integer> rivalShunzi = CardTool.getCardsVals(disCards);
		if(rivalShunzi == null || rivalShunzi.size() <= 0){
			LogUtil.printDebug("找不出来对手出的是什么顺子");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		Collections.sort(rivalShunzi);
		
		
		//判断我的顺子是不是比对手大
		List<List<Integer>> myAllShunzi = handCardHaveShunziByNum(handCards, rivalShunzi.size());
		if(myAllShunzi.isEmpty()){
			LogUtil.printDebug("自己手上没有顺子可接");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		
		Collections.sort(rivalShunzi);//对手打出的顺子，从小到大
		List<Integer> myShunzi = new ArrayList<>();
		for (List<Integer> shunzi : myAllShunzi) {
			if(shunzi.get(0) > rivalShunzi.get(0)){
				myShunzi = shunzi;
				break;
			}
		}
		
		if(myShunzi.isEmpty()){
			LogUtil.printDebug("找不出来拿什么顺子去接对手的顺子");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		List<Integer> pais = CardTypeTool.getPaisByPaiXing(CardTypeTool.px_shunzi, myShunzi, handCards);
		if(pais.isEmpty()){
			LogUtil.printDebug("找不出来拿什么顺子去接对手的顺子1");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		getContext().setVariable("chupai", pais);
		return jbt.execution.core.ExecutionTask.Status.SUCCESS;
	}

	/**
	 * 根据牌个数找顺子
	 * @param cards
	 * @return
	 */
	private static List<List<Integer>> handCardHaveShunziByNum(List<Integer> cards,int num){
		if(cards.isEmpty() || cards.size() < 5){
			return Collections.emptyList();
		}
		Map<Integer, Integer> map = CardTool.loadCards(cards);
		List<List<Integer>> allShunzi = new ArrayList<>();
		
		for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
			int cardVal = kv.getKey().intValue();
			
			int curVal = cardVal;
			List<Integer> shunzi = new ArrayList<>();
			shunzi.add(cardVal);
			for (Map.Entry<Integer, Integer> kv2 : map.entrySet()) {
				if(cardVal >= kv2.getKey().intValue()){
					continue;
				}
				if(curVal+1 !=kv2.getKey().intValue()){
					break;
				}
				curVal = kv2.getKey().intValue();
				shunzi.add(kv2.getKey().intValue());
				if(shunzi.size() == num){
					allShunzi.add(new ArrayList<>(shunzi));
					break;
				}
			}
		}
			
		return allShunzi;
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