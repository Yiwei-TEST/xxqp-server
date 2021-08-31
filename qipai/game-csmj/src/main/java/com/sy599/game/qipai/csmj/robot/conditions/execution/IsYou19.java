// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 07/15/2020 09:08:18
// ******************************************************* 
package com.sy599.game.qipai.csmj.robot.conditions.execution;

import com.sy599.game.qipai.csmj.robot.jbtAI.MajiangAITool;
import com.sy599.game.qipai.csmj.rule.CsMj;
import com.sy599.game.util.LogUtil;

import java.util.List;

/** ExecutionCondition class created from MMPM condition IsYou19. */
public class IsYou19 extends
		jbt.execution.task.leaf.condition.ExecutionCondition {

	/**
	 * Constructor. Constructs an instance of IsYou19 that is able to run a
	 * com.sy599.game.qipai.csmj.robot.conditions.IsYou19.
	 */
	public IsYou19(
			com.sy599.game.qipai.csmj.robot.conditions.IsYou19 modelTask,
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

	protected jbt.execution.core.ExecutionTask.Status internalTick() {
		/*
		 * TODO: this method's implementation must be completed. This function
		 * should only return Status.SUCCESS, Status.FAILURE or Status.RUNNING.
		 * No other values are allowed.
		 */
		boolean result  = (boolean) getContext().getVariable("YichuPaixing");
		LogUtil.printDebug(this.getClass().getCanonicalName()+" YichuPaixing:"+result);
		if(result){
			List<CsMj> list = (List<CsMj>) getContext().getVariable("YichuPaixingCard");
			LogUtil.printDebug("list==="+list);
			List<CsMj> res = MajiangAITool.IsYou19(list);
			LogUtil.printDebug("======res:"+res);
			if(res.size()>0){
				getContext().setVariable("action","da");
				getContext().setVariable("chup",res.get(0));
				LogUtil.printDebug(this.getClass().getCanonicalName()+"====出牌:"+res.get(0));
				return jbt.execution.core.ExecutionTask.Status.SUCCESS;
			}else{
				return Status.FAILURE;
			}
		}else{
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