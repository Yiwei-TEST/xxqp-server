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
import java.util.Map;
import java.util.Map.Entry;

import com.sy599.game.qipai.pdkuai.tool.CardTypeTool;
import com.sy599.game.util.LogUtil;

/** ExecutionAction class created from MMPM action GetMaxPaixing. */
public class GetMaxPaixing extends
		jbt.execution.task.leaf.action.ExecutionAction {

	/**
	 * Constructor. Constructs an instance of GetMaxPaixing that is able to run
	 * a com.sy599.game.qipai.pdkuai.robot.actions.GetMaxPaixing.
	 */
	public GetMaxPaixing(
			com.sy599.game.qipai.pdkuai.robot.actions.GetMaxPaixing modelTask,
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
		LogUtil.printDebug("获取牌数最多的牌型");
	}

	protected jbt.execution.core.ExecutionTask.Status internalTick() {
		List<Integer> handCards = getContext().getVariable("handCards") == null ? new ArrayList<>(): (ArrayList<Integer>)this.getContext().getVariable("handCards");
		if(handCards.isEmpty()){
			LogUtil.printDebug("没有获取到手牌");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		Map<Integer, List<List<Integer>>> allPaixing = CardTypeTool.getAllPaiXing(handCards,false);
		if(allPaixing.isEmpty()){
			LogUtil.printDebug("没有获取到牌型");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		int maxPaixingType = 0;//牌数最多的牌型类型
		int maxPaixingNum = 0;//牌数最多的牌型牌数
		List<Integer> maxCards = new ArrayList<>();//牌数最多的牌型牌数
		for (Entry<Integer, List<List<Integer>>> entry : allPaixing.entrySet()) {
			List<List<Integer>> paixings = entry.getValue();
			if(paixings != null && paixings.size() > 0 && entry.getKey() != CardTypeTool.px_zhadan ){
				for (List<Integer> list : paixings) {
					int paiNum = CardTypeTool.getPaiNumByPaiXing(entry.getKey(), list);
					if(paiNum > maxPaixingNum){
						maxPaixingType = entry.getKey();
						maxPaixingNum = paiNum;
						maxCards = new ArrayList<>(list);
					}else if(paiNum == maxPaixingNum && entry.getKey() > maxPaixingType){
						maxPaixingType = entry.getKey();
						maxPaixingNum = paiNum;
						maxCards = new ArrayList<>(list);
					}
				}
			}
		}
		List<Integer> maxPai = CardTypeTool.getPaisByPaiXing(maxPaixingType, maxCards, handCards);
		if(maxPai.isEmpty()){
			LogUtil.printDebug("没有拿到牌型");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		LogUtil.printDebug("最大牌型："+maxPai);
		getContext().setVariable("paiXing", maxPai);
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