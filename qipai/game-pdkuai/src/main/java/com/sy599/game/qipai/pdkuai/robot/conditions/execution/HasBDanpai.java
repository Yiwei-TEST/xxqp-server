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

import com.sy599.game.qipai.pdkuai.tool.CardTool;
import com.sy599.game.qipai.pdkuai.tool.CardTypeTool;
import com.sy599.game.util.LogUtil;

/** ExecutionCondition class created from MMPM condition HasBDanpai. */
public class HasBDanpai extends
		jbt.execution.task.leaf.condition.ExecutionCondition {

	/**
	 * Constructor. Constructs an instance of HasBDanpai that is able to run a
	 * com.sy599.game.qipai.pdkuai.robot.conditions.HasBDanpai.
	 */
	public HasBDanpai(
			com.sy599.game.qipai.pdkuai.robot.conditions.HasBDanpai modelTask,
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
		LogUtil.printDebug("判断手牌中是否有最大单牌");
	}

	protected jbt.execution.core.ExecutionTask.Status internalTick() {
		List<Integer> handCards = getContext().getVariable("handCards") == null ? new ArrayList<>(): (ArrayList<Integer>)this.getContext().getVariable("handCards");
		if(handCards.isEmpty()){
			LogUtil.printDebug("没有获取到手牌");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		//获取所有没出现的牌
		List<Integer> residueCards = getContext().getVariable("residueCards") == null ? new ArrayList<>(): (ArrayList<Integer>)this.getContext().getVariable("residueCards");
		if(residueCards.isEmpty()){
			LogUtil.printDebug("没有未出现的牌");
			return jbt.execution.core.ExecutionTask.Status.SUCCESS;
		}
		List<Integer> myCardVal = CardTool.getCardsVals(handCards);
		Collections.sort(myCardVal);
		int myMaxDp = myCardVal.get(myCardVal.size()-1);
		
		List<Integer> resCardVal = CardTool.getCardsVals(residueCards);
		Collections.sort(resCardVal);
		int resMaxDp = resCardVal.get(resCardVal.size()-1);
		
		if(myMaxDp < resMaxDp){
			LogUtil.printDebug("自己的最大牌不是最大单牌");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		
		getContext().setVariable("hasPaixingType", CardTypeTool.px_danpai);
		getContext().setVariable("hasPaixing", new ArrayList<>(Arrays.asList(myMaxDp)));
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