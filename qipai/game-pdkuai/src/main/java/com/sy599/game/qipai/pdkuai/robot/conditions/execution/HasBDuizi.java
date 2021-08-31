// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 07/07/2020 15:21:50
// ******************************************************* 
package com.sy599.game.qipai.pdkuai.robot.conditions.execution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sy599.game.qipai.pdkuai.tool.CardTypeTool;
import com.sy599.game.util.LogUtil;

/** ExecutionCondition class created from MMPM condition HasBDuizi. */
public class HasBDuizi extends
		jbt.execution.task.leaf.condition.ExecutionCondition {

	/**
	 * Constructor. Constructs an instance of HasBDuizi that is able to run a
	 * com.sy599.game.qipai.pdkuai.robot.conditions.HasBDuizi.
	 */
	public HasBDuizi(
			com.sy599.game.qipai.pdkuai.robot.conditions.HasBDuizi modelTask,
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
		LogUtil.printDebug("检测手牌有没有最大对子");
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
		//获取所有没出现的牌
		List<Integer> residueCards = getContext().getVariable("residueCards") == null ? new ArrayList<>(): (ArrayList<Integer>)this.getContext().getVariable("residueCards");
		if(residueCards.isEmpty()){
			LogUtil.printDebug("没有未出现的牌，是最大对子");
			return jbt.execution.core.ExecutionTask.Status.SUCCESS;
		}
		Map<Integer, List<List<Integer>>> residuePaixing = CardTypeTool.getAllPaiXing(residueCards,true);
		if(residuePaixing.isEmpty()){
			LogUtil.printDebug("没有获取到剩余牌的牌型");
			return jbt.execution.core.ExecutionTask.Status.SUCCESS;
		}
		List<List<Integer>> residueDuizi = residuePaixing.get(CardTypeTool.px_duizi);
		if(residueDuizi.isEmpty()){
			LogUtil.printDebug("剩余牌中没有对子");
			return jbt.execution.core.ExecutionTask.Status.SUCCESS;
		}
		List<Integer> residueAddDuizi = residueDuizi.get(0);
		if(residueAddDuizi.isEmpty()){
			LogUtil.printDebug("剩余牌中没有对子");
			return jbt.execution.core.ExecutionTask.Status.SUCCESS;
		}
		Collections.sort(allDuizi);
		int myMaxDuizi = allDuizi.get(allDuizi.size()-1);
		Collections.sort(residueAddDuizi);
		int resMaxDuizi = residueAddDuizi.get(residueAddDuizi.size()-1);
		if(resMaxDuizi > myMaxDuizi){
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		getContext().setVariable("hasPaixingType", CardTypeTool.px_duizi);
		getContext().setVariable("hasPaixing", new ArrayList<>(Arrays.asList(myMaxDuizi)));
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