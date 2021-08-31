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

import com.sy599.game.qipai.pdkuai.tool.CardTypeTool;
import com.sy599.game.util.LogUtil;

/** ExecutionAction class created from MMPM action ChuSduizi. */
public class ChuSduizi extends jbt.execution.task.leaf.action.ExecutionAction {

	/**
	 * Constructor. Constructs an instance of ChuSduizi that is able to run a
	 * com.sy599.game.qipai.pdkuai.robot.actions.ChuSduizi.
	 */
	public ChuSduizi(
			com.sy599.game.qipai.pdkuai.robot.actions.ChuSduizi modelTask,
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
		LogUtil.printDebug("出小对子");
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
		List<List<Integer>> duizis = allPaixing.get(CardTypeTool.px_duizi);
		if(duizis.isEmpty()){
			LogUtil.printDebug("手牌中没有对子");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		List<Integer> allDuizi = duizis.get(0);
		if(allDuizi.isEmpty()){
			LogUtil.printDebug("手牌中没有对子");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		Collections.sort(allDuizi);
		List<List<Integer>> santiaos = allPaixing.get(CardTypeTool.px_santiao);
		int myduizi = allDuizi.get(0);
		if(!santiaos.isEmpty() && !santiaos.get(0).isEmpty()){
			List<Integer> allSantiao = santiaos.get(0);
			for (Integer duizi : allDuizi) {
				if(!allSantiao.contains(duizi)){
					myduizi = duizi;
				}
			}
		}
		
		List<Integer> pais = CardTypeTool.getPaisByPaiXing(CardTypeTool.px_duizi,Arrays.asList(myduizi), handCards);
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