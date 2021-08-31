// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 08/06/2020 10:08:32
// ******************************************************* 
package com.sy599.game.qipai.csmj.robot.conditions.execution;

import com.sy599.game.qipai.csmj.robot.jbtAI.MajiangAITool;
import com.sy599.game.qipai.csmj.rule.CsMj;
import com.sy599.game.util.LogUtil;

import java.util.List;

/** ExecutionCondition class created from MMPM condition IsDuoJiangDui. */
public class IsDuoJiangDui extends
		jbt.execution.task.leaf.condition.ExecutionCondition {

	/**
	 * Constructor. Constructs an instance of IsDuoJiangDui that is able to run
	 * a com.sy599.game.qipai.csmj.robot.conditions.IsDuoJiangDui.
	 */
	public IsDuoJiangDui(
			com.sy599.game.qipai.csmj.robot.conditions.IsDuoJiangDui modelTask,
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
		LogUtil.printDebug(this.getClass().getCanonicalName() + " spawned");
	}

	protected Status internalTick() {
		/*
		 * TODO: this method's implementation must be completed. This function
		 * should only return Status.SUCCESS, Status.FAILURE or Status.RUNNING.
		 * No other values are allowed.
		 */

		List<CsMj> isDuoJiangDui = (List<CsMj>) getContext().getVariable("isDuoJiangDui");
		CsMj deskpai = (CsMj) getContext().getVariable("deskpai");
		if(null!=isDuoJiangDui && isDuoJiangDui.size()>=4){
			LogUtil.printDebug(this.getClass().getCanonicalName() +" isDuoJiangDui SUCCESS ");
			if(MajiangAITool.CanPeng(isDuoJiangDui,deskpai)){
				LogUtil.printDebug("出牌为将且能碰====================================");
				getContext().setVariable("peng",true);
			}
			return Status.SUCCESS;
		}else{
			LogUtil.printDebug(this.getClass().getCanonicalName() +" isDuoJiangDui FAILURE ");
			return Status.FAILURE;
		}
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