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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sy599.game.qipai.pdkuai.tool.CardTool;
import com.sy599.game.qipai.pdkuai.tool.CardTypeTool;
import com.sy599.game.util.LogUtil;

/** ExecutionAction class created from MMPM action ChuSdanpai. */
public class ChuSdanpai extends jbt.execution.task.leaf.action.ExecutionAction {

	/**
	 * Constructor. Constructs an instance of ChuSdanpai that is able to run a
	 * com.sy599.game.qipai.pdkuai.robot.actions.ChuSdanpai.
	 */
	public ChuSdanpai(
			com.sy599.game.qipai.pdkuai.robot.actions.ChuSdanpai modelTask,
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
		LogUtil.printDebug("打出手牌中最小的单牌");
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
		List<List<Integer>> danpai = allPaixing.get(CardTypeTool.px_danpai);
		if(danpai.isEmpty()  || danpai.get(0).isEmpty()){
			LogUtil.printDebug("手牌中没有单牌，出最小的那张");
			List<Integer> myCardVal = CardTool.getCardsVals(handCards);
			Collections.sort(myCardVal);
			int myMinDp = myCardVal.get(0);
			List<Integer> pais = CardTypeTool.getPaisByPaiXing(CardTypeTool.px_danpai,Arrays.asList(myMinDp), handCards);
			if(pais.isEmpty()){
				LogUtil.printDebug("没有拿到牌");
				return jbt.execution.core.ExecutionTask.Status.FAILURE;
			}
			getContext().setVariable("chupai", pais);
			return jbt.execution.core.ExecutionTask.Status.SUCCESS;
		}
		List<Integer> allDanpai = danpai.get(0);
//		if(allDanpai.isEmpty()){
//			LogUtil.printDebug("手牌中没有单牌，出最小的那张");
//			List<Integer> myCardVal = CardTool.getCardsVals(handCards);
//			Collections.sort(myCardVal);
//			int myMinDp = myCardVal.get(0);
//			List<Integer> pais = CardTypeTool.getPaisByPaiXing(CardTypeTool.px_danpai, new ArrayList<>(myMinDp), handCards);
//			if(pais.isEmpty()){
//				LogUtil.printDebug("没有拿到牌");
//				return jbt.execution.core.ExecutionTask.Status.FAILURE;
//			}
//			getContext().setVariable("chupai", pais);
//			return jbt.execution.core.ExecutionTask.Status.SUCCESS;
//		}
		Collections.sort(allDanpai);
		
		List<Integer> pais = CardTypeTool.getPaisByPaiXing(CardTypeTool.px_danpai, new ArrayList<>(Arrays.asList(allDanpai.get(0))), handCards);
		if(pais.isEmpty()){
			LogUtil.printDebug("没有拿到牌");
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