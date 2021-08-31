// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 07/10/2020 15:32:16
// ******************************************************* 
package com.sy599.game.qipai.pdkuai.robot.actions.execution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sy599.game.qipai.pdkuai.tool.CardTool;
import com.sy599.game.qipai.pdkuai.tool.CardTypeTool;
import com.sy599.game.util.LogUtil;

/** ExecutionAction class created from MMPM action ChuBdanzhang. */
public class ChuBdanzhang extends
		jbt.execution.task.leaf.action.ExecutionAction {

	/**
	 * Constructor. Constructs an instance of ChuBdanzhang that is able to run a
	 * com.sy599.game.qipai.pdkuai.robot.actions.ChuBdanzhang.
	 */
	public ChuBdanzhang(
			com.sy599.game.qipai.pdkuai.robot.actions.ChuBdanzhang modelTask,
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
		LogUtil.printDebug("出最大单张");
	}

	protected jbt.execution.core.ExecutionTask.Status internalTick() {
		List<Integer> handCards = getContext().getVariable("handCards") == null ? new ArrayList<>(): (ArrayList<Integer>)this.getContext().getVariable("handCards");
		if(handCards.isEmpty()){
			LogUtil.printDebug("没有获取到手牌");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		List<Integer> myCardVal = CardTool.getCardsVals(handCards);
		Collections.sort(myCardVal);
		int  myMaxDp= myCardVal.get(myCardVal.size()-1);

		List<Integer> pais = CardTypeTool.getPaisByPaiXing(CardTypeTool.px_danpai, Arrays.asList(myMaxDp), handCards);
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