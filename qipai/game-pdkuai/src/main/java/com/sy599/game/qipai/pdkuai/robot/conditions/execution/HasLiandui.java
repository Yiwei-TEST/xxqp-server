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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sy599.game.qipai.pdkuai.tool.CardTypeTool;
import com.sy599.game.util.LogUtil;

/** ExecutionCondition class created from MMPM condition HasLiandui. */
public class HasLiandui extends
		jbt.execution.task.leaf.condition.ExecutionCondition {

	/**
	 * Constructor. Constructs an instance of HasLiandui that is able to run a
	 * com.sy599.game.qipai.pdkuai.robot.conditions.HasLiandui.
	 */
	public HasLiandui(
			com.sy599.game.qipai.pdkuai.robot.conditions.HasLiandui modelTask,
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
		LogUtil.printDebug("是否有连对");
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
		List<List<Integer>> shunzi = allPaixing.get(CardTypeTool.px_liandui);
		if(shunzi.isEmpty()){
			LogUtil.printDebug("手牌中没有连对");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		List<Integer> myLiandui = new ArrayList<>();
		for (List<Integer> sz:shunzi) {
			if(sz.isEmpty()){
				continue;
			}
			Collections.sort(sz);
			if(myLiandui.isEmpty()){
				myLiandui.addAll(sz);
				continue;
			}else if(sz.get(sz.size()-1) > myLiandui.get(myLiandui.size()-1) 
					|| (sz.get(sz.size()-1) == myLiandui.get(myLiandui.size()-1) && sz.size() > myLiandui.size())){
				myLiandui.clear();
				myLiandui.addAll(sz);
				continue;
			}
			
		}
		Collections.sort(myLiandui);
		getContext().setVariable("hasPaixingType", CardTypeTool.px_liandui);
		getContext().setVariable("hasPaixing", myLiandui);
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